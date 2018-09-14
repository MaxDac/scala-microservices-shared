package eng.db.shared.tests.webServer

import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.stream.ActorMaterializer
import eng.db.shared.log.LogProvider
import eng.db.shared.webservices.RouteProvider
import spray.json._

import scala.concurrent.ExecutionContext

case class TestItem(id: String, password: Option[String])

object SessionValidatorTestRouteGenerator extends SprayJsonSupport with DefaultJsonProtocol {
    implicit val testItemConverter: RootJsonFormat[TestItem] = jsonFormat2(TestItem)
}
class SessionValidatorTestRouteGenerator extends RouteProvider {
    import SessionValidatorTestRouteGenerator._
    private val messageOk: String = "<h1>Test route ok</h1>"

    /**
      * Gets the routes subjected to session control.
      * The method has to be implemented for simplicity, because these are the most used routes.
      *
      * @param logProvider  The log provider.
      * @param system       The actor system.
      * @param materializer The actor materializer.
      * @param ec           The execution context.
      * @return The session-controlled routes.
      */
    //noinspection ScalaUnusedSymbol
    override def getRoutes()(implicit logProvider: LogProvider, system: ActorSystem, materializer: ActorMaterializer, ec: ExecutionContext): Option[Route] = Some(
        path("") {
            post {
                extractRequestContext { ctx =>
                    entity(as[TestItem]) { entity =>
                        complete(s"${this.messageOk}: ${entity.id}")
                    }
                }
            } ~ get {
                complete(this.messageOk)
            }
        }
    )
}
