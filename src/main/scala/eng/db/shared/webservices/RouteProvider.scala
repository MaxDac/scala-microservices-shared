package eng.db.shared.webservices

import akka.actor.ActorSystem
import akka.http.scaladsl.server._
import Directives._
import akka.stream.ActorMaterializer
import eng.db.shared.log.LogProvider

import scala.concurrent.ExecutionContext

trait RouteProvider {

    /**
      * Gets the routes subjected to session control.
      * The method has to be implemented for simplicity, because these are the most used routes.
      * @param logProvider The log provider.
      * @param system The actor system.
      * @param materializer The actor materializer.
      * @param ec The execution context.
      * @return The session-controlled routes.
      */
    def getRoutes()(implicit logProvider: LogProvider, system: ActorSystem, materializer: ActorMaterializer, ec: ExecutionContext): Option[Route]

    /**
      * Gets the whitelisted routes, or the routes not subjected to session control.
      * @param logProvider The log provider.
      * @param system The actor system.
      * @param materializer The actor materializer.
      * @param ec The execution context.
      * @return The whitelisted routes.
      */
    def getWhitelistedRoutes()(implicit logProvider: LogProvider, system: ActorSystem, materializer: ActorMaterializer, ec: ExecutionContext): Option[Route] = None

    def ~(provider: RouteProvider)(implicit logProvider: LogProvider, system: ActorSystem, materializer: ActorMaterializer, ec: ExecutionContext): RouteProvider = {
        lazy val thisRoutes = this.getRoutes()
        lazy val thisWhitelistedRoutes = this.getWhitelistedRoutes()
        new RouteProvider {
            override def getRoutes()(implicit logProvider: LogProvider, system: ActorSystem, materializer: ActorMaterializer, ec: ExecutionContext): Option[Route] = {
                (thisRoutes, provider.getRoutes()) match {
                    case (Some(innerRoutes), Some(addedRoutes)) => Some(innerRoutes ~ addedRoutes)
                    case (None, Some(addedRoutes)) => Some(addedRoutes)
                    case (Some(innerRoutes), None) => Some(innerRoutes)
                    case (None, None) => None
                }
            }

            override def getWhitelistedRoutes()(implicit logProvider: LogProvider, system: ActorSystem, materializer: ActorMaterializer, ec: ExecutionContext): Option[Route] = {
                (thisWhitelistedRoutes, provider.getWhitelistedRoutes()) match {
                    case (Some(innerRoutes), Some(addedRoutes)) => Some(innerRoutes ~ addedRoutes)
                    case (None, Some(addedRoutes)) => Some(addedRoutes)
                    case (Some(innerRoutes), None) => Some(innerRoutes)
                    case (None, None) => None
                }
            }
        }
    }
}
