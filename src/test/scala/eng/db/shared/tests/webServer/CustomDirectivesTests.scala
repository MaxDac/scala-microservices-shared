package eng.db.shared.tests.webServer

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import eng.db.shared.log.LogProvider
import eng.db.shared.messageBroker.DispatchProperties
import eng.db.shared.webservices.AkkaInternalDispatcher
import org.scalatest.AsyncFlatSpec

import scala.concurrent.ExecutionContextExecutor

class CustomDirectivesTests extends AsyncFlatSpec {

    import eng.db.shared.messageBroker.DispatchPropertiesJsonProtocol._
    import eng.db.shared.webservices.CustomDirectives._
    import eng.db.shared.webservices.DefaultExceptionHandlers._

    implicit val logProvider: LogProvider = LogProvider.logProvider
    implicit val system: ActorSystem = ActorSystem("test-system")
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val ec: ExecutionContextExecutor = system.dispatcher

    val netaContext = "{context-string}"
    val netaSessionToken = "sessionToken"

    "The directive" should "pick up the Net@ deserialized execution context" in {
        val dispatcher = new AkkaInternalDispatcher(Some(path("") {
            getSerializedNetaExecutionContext { ctx =>
                get {
                    akka.http.scaladsl.server.Directives.complete(ctx)
                }
            }
        }), None)

        try {
            val result = dispatcher.ask(DispatchProperties(
                Some(netaContext),
                None,
                "GET".encode(),
                "/",
                None
            ))
            result.map(a => {
                val message = a.map(_.toChar).mkString
                assert(message === netaContext)
            })
        }
        catch {
            case ex: Exception => assert(false, ex.toString)
        }
    }

    "The directive" should "pick up the Net@ execution context" in {
        val dispatcher = new AkkaInternalDispatcher(Some(path("") {
            getNetaExecutionContext { ctx =>
                get {
                    akka.http.scaladsl.server.Directives.complete(ctx.get.operatorId)
                }
            }
        }), None)

        try {
            val result = dispatcher.ask(DispatchProperties(
                Some(netaContext),
                None,
                "GET".encode(),
                "/",
                None
            ))
            result.map(a => {
                val message = a.map(_.toChar).mkString
                assert(message === "SUPER")
            })
        }
        catch {
            case ex: Exception => assert(false, ex.toString)
        }
    }
}
