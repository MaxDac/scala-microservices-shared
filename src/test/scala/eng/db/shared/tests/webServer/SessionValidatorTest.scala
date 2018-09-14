package eng.db.shared.tests.webServer

import akka.actor.ActorSystem
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.model.headers
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.stream.ActorMaterializer
import eng.db.shared.base.JsonConvert
import eng.db.shared.log.LogProvider
import eng.db.shared.messageBroker.DispatchProperties
import eng.db.shared.webservices.{AkkaInternalDispatcher, BaseRoutes}
import org.scalatest.AsyncFlatSpec

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._

//noinspection NameBooleanParameters
class SessionValidatorTest extends AsyncFlatSpec {
    private val messageOk: String = "<h1>Test route ok</h1>"
    private val sessionToken: String = "test-session-token"
    private val unauthenticatedErrorMessage: String = "Authentication is possible but has failed or not yet been provided."
    
    import eng.db.shared.messageBroker.DispatchPropertiesJsonProtocol._

    implicit val logProvider: LogProvider = LogProvider.logProvider
    implicit val system: ActorSystem = ActorSystem("test-system")
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val ec: ExecutionContextExecutor = system.dispatcher

    private def getTestRoute = new SessionValidatorTestRouteGenerator().getRoutes()

    "The route" should "validate the serialized context" in {

        import LogProvider._
        import eng.db.shared.webservices.DefaultExceptionHandlers._

        val dispatcher = new AkkaInternalDispatcher(this.getTestRoute, None)

        try {
            val result = dispatcher.ask(DispatchProperties(
                Some("<Contesto></Contesto>"),
                Some(""),
                "GET".encode(),
                "/",
                Some("")))
            result.map(a => {
                val message = a.map(_.toChar).mkString
//                println(s"The response is: $message")
                assert(message === this.messageOk)
            })
        }
        catch {
            case ex: Exception => assert(false, ex.toString)
        }

    }

    "The route" should "validate the serialized context for post requests" in {

//        implicit val system: ActorSystem = ActorSystem("test-system")
//        implicit val materializer: ActorMaterializer = ActorMaterializer()
//        implicit val executionContext: ExecutionContextExecutor = system.dispatcher

        import LogProvider._
        import eng.db.shared.tests.webServer.SessionValidatorTestRouteGenerator._
        import eng.db.shared.webservices.DefaultExceptionHandlers._

        val dispatcher = new AkkaInternalDispatcher(this.getTestRoute, None)

        try {
            val body = TestItem("Id", Some("secret"))
            val serializedBody = JsonConvert(body).asJson()
            val result = dispatcher.ask(DispatchProperties(
                Some("<Contesto></Contesto>"),
                Some(""),
                "POST".encode(),
                "/",
                Some(serializedBody)))
            result.map(a => {
                val message = a.map(_.toChar).mkString
                //                println(s"The response is: $message")
                assert(message === s"${this.messageOk}: Id")
            })
        }
        catch {
            case ex: Exception => assert(false, ex.toString)
        }

    }

    "The route" should "validate the session token" in {

//        implicit val system: ActorSystem = ActorSystem("test-system")
//        implicit val materializer: ActorMaterializer = ActorMaterializer()
//        implicit val executionContext: ExecutionContextExecutor = system.dispatcher

        val dispatcher = Route.asyncHandler(BaseRoutes.sessionValidator() ~ this.getTestRoute.get)

        try {
            val request = RequestBuilding
                .Get("/")
                .withHeaders(headers.RawHeader("session-token", this.sessionToken))
            val result = dispatcher(request)
            result
                .flatMap(a => a.entity.toStrict(1000.millis).map(_.data).map(_.toArray))
                .map(a => {
                    val message = a.map(_.toChar).mkString
                    println(s"The response is: $message")
                    assert(message === this.messageOk)
                })
        }
        catch {
            case ex: Exception => assert(false, ex.toString)
        }

    }

    "The route" should "not validate the request" in {

//        implicit val system: ActorSystem = ActorSystem("test-system")
//        implicit val materializer: ActorMaterializer = ActorMaterializer()
//        implicit val executionContext: ExecutionContextExecutor = system.dispatcher

        val dispatcher = Route.asyncHandler(BaseRoutes.sessionValidator() ~ this.getTestRoute.get)

        try {
            val request = RequestBuilding
                .Get("/")
//                .withHeaders(headers.RawHeader("session-token", this.sessionToken))
            val result = dispatcher(request)
            result
                .flatMap(a => a.entity.toStrict(1000.millis).map(_.data).map(_.toArray))
                .map(a => {
                    val message = a.map(_.toChar).mkString
                    println(s"The response is: $message")
                    assert(message === this.unauthenticatedErrorMessage)
                })
        }
        catch {
            case ex: Exception => assert(false, ex.toString)
        }

    }

    "The route" should "let the whitelisted call pass" in {

//        implicit val system: ActorSystem = ActorSystem("test-system")
//        implicit val materializer: ActorMaterializer = ActorMaterializer()
//        implicit val executionContext: ExecutionContextExecutor = system.dispatcher

        import LogProvider._
        import eng.db.shared.webservices.DefaultExceptionHandlers._

        val dispatcher = new AkkaInternalDispatcher(None, this.getTestRoute)

        try {
            val result = dispatcher.ask(DispatchProperties(
                None,
                Some(""),
                "GET".encode(),
                "/",
                Some("")))
            result.map(a => {
                val message = a.map(_.toChar).mkString
                //                println(s"The response is: $message")
                assert(message === this.messageOk)
            })
        }
        catch {
            case ex: Exception => assert(false, ex.toString)
        }

    }

}
