package eng.db.shared.tests.webServer

import akka.actor.ActorSystem
import akka.http.scaladsl.server._
import Directives._
import akka.stream.ActorMaterializer
import eng.db.shared.log.LogProvider
import eng.db.shared.messageBroker.DispatchProperties
import eng.db.shared.messageBroker.DispatchPropertiesJsonProtocol._
import eng.db.shared.webservices.{AkkaInternalDispatcher, RouteProvider}
import org.scalatest.AsyncFlatSpec

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

class RouteProviderAdditionTests extends AsyncFlatSpec {
    implicit val logProvider: LogProvider = LogProvider.logProvider
    implicit val system: ActorSystem = ActorSystem("test-system")
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val ec: ExecutionContextExecutor = system.dispatcher

    val firstMessage = "First-Message"
    val secondMessage = "Second-message"
    val thirdMessage = "Third-message"

    val firstRouteProvider = new RouteProvider {
        override def getWhitelistedRoutes()(implicit logProvider: LogProvider, system: ActorSystem, materializer: ActorMaterializer, ec: ExecutionContext): Option[Route] = Some {
            path("First") {
                get {
                    akka.http.scaladsl.server.Directives.complete(firstMessage)
                }
            }
        }

        override def getRoutes()(implicit logProvider: LogProvider, system: ActorSystem, materializer: ActorMaterializer, ec: ExecutionContext): Option[Route] = None
    }

    val secondRouteProvider = new RouteProvider {
        override def getWhitelistedRoutes()(implicit logProvider: LogProvider, system: ActorSystem, materializer: ActorMaterializer, ec: ExecutionContext): Option[Route] = Some {
            path("Second") {
                get {
                    akka.http.scaladsl.server.Directives.complete(secondMessage)
                }
            }
        }
        override def getRoutes()(implicit logProvider: LogProvider, system: ActorSystem, materializer: ActorMaterializer, ec: ExecutionContext): Option[Route] = None
    }

    val thirdRouteProvider = new RouteProvider {
        override def getRoutes()(implicit logProvider: LogProvider, system: ActorSystem, materializer: ActorMaterializer, ec: ExecutionContext): Option[Route] = Some {
            path("Third") {
                get {
                    akka.http.scaladsl.server.Directives.complete(thirdMessage)
                }
            }
        }
    }

    "The route providers" should "add themselves" in {
        import LogProvider._
        import eng.db.shared.webservices.DefaultExceptionHandlers._

        val routeProvider = firstRouteProvider ~ secondRouteProvider ~ thirdRouteProvider
        val dispatcher = AkkaInternalDispatcher(routeProvider)

        try {
            for {
                firstResult <- dispatcher.ask(DispatchProperties(
                    None,
                    Some(""),
                    "GET".encode(),
                    "/First",
                    None))
                secondResult <- dispatcher.ask(DispatchProperties(
                    None,
                    Some(""),
                    "GET".encode(),
                    "/Second",
                    None))
                thirdResult <- dispatcher.ask(DispatchProperties(
                    Some("<Contesto></Contesto>"),
                    Some(""),
                    "GET".encode(),
                    "/Third",
                    None))
            } yield {
                val obtainedFirstMessage = firstResult.map(_.toChar).mkString
                val obtainedSecondMessage = secondResult.map(_.toChar).mkString
                val obtainedThirdMessage = thirdResult.map(_.toChar).mkString

                assert(obtainedFirstMessage === firstMessage)
                assert(obtainedSecondMessage === secondMessage)
                assert(obtainedThirdMessage === thirdMessage)
            }
        }
        catch {
            case ex: Exception => assert(false, ex.toString)
        }
    }

}
