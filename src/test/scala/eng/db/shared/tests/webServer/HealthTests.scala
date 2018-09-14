package eng.db.shared.tests.webServer

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives.{get, path}
import akka.stream.ActorMaterializer
import eng.db.shared.base.AppConfiguration
import eng.db.shared.log.LogProvider
import eng.db.shared.messageBroker.DispatchProperties
import eng.db.shared.webservices.AkkaInternalDispatcher
import org.scalatest.AsyncFlatSpec

import scala.concurrent.ExecutionContextExecutor

class HealthTests extends AsyncFlatSpec {

    import eng.db.shared.webservices.DefaultExceptionHandlers._

    implicit val logProvider: LogProvider = LogProvider.logProvider
    implicit val system: ActorSystem = ActorSystem("test-system")
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val ec: ExecutionContextExecutor = system.dispatcher

    "The directive" should "expose the health message" in {
        val dispatcher = new AkkaInternalDispatcher(Some(path("") {
            get {
                akka.http.scaladsl.server.Directives.complete("This is some message.")
            }
        }), None)

        try {
            val result = dispatcher.ask(DispatchProperties.`for`(
                None,
                None,
                "GET",
                "/_health",
                None
            ))
            result.map(a => {
                val message = a.map(_.toChar).mkString
                assert(message === s"Service is working. Current service version: ${AppConfiguration.version}")
            })
        }
        catch {
            case ex: Exception => assert(false, ex.toString)
        }
    }
}
