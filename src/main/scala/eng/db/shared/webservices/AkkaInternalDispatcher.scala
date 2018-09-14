package eng.db.shared.webservices

import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.stream.ActorMaterializer
import eng.db.shared.log.LogProvider
import eng.db.shared.messageBroker.{DispatchProperties, Dispatcher}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

object AkkaInternalDispatcher {
    def apply(
                 serviceRoutes: Option[Route],
                 whitelistedServiceRoutes: Option[Route])(implicit
                                                          logProvider: LogProvider,
                                                          exceptionHandler: ExceptionHandler,
                                                          system: ActorSystem,
                                                          materializer: ActorMaterializer,
                                                          executionContext: ExecutionContext): AkkaInternalDispatcher =
        new AkkaInternalDispatcher(serviceRoutes, whitelistedServiceRoutes)

    def apply(provider: RouteProvider)(implicit
                                       logProvider: LogProvider,
                                       exceptionHandler: ExceptionHandler,
                                       system: ActorSystem,
                                       materializer: ActorMaterializer,
                                       executionContext: ExecutionContext): AkkaInternalDispatcher =
        new AkkaInternalDispatcher(provider.getRoutes(), provider.getWhitelistedRoutes())
}
class AkkaInternalDispatcher(serviceRoutes: Option[Route], whitelistedServiceRoutes: Option[Route])(
    implicit val logProvider: LogProvider,
    implicit val exceptionHandler: ExceptionHandler,
    implicit val system: ActorSystem,
    implicit val materializer: ActorMaterializer,
    implicit val executionContext: ExecutionContext) extends Dispatcher {

    private def getDefaultRouteContainer(route: Route) = handleExceptions(this.exceptionHandler) {
        ignoreTrailingSlash {
            route
        }
    }

    private val internalHandler = Route.asyncHandler((serviceRoutes, whitelistedServiceRoutes) match {
        case (Some(s), Some(w)) => this.getDefaultRouteContainer(BaseRoutes.healthChecker ~ w ~ BaseRoutes.sessionValidator() ~ s)
        case (Some(s), None) => this.getDefaultRouteContainer(BaseRoutes.healthChecker ~ BaseRoutes.sessionValidator() ~ s)
        case (None, Some(w)) => this.getDefaultRouteContainer(BaseRoutes.healthChecker ~ w)
        case _ => throw new IllegalArgumentException("No routes provided for the routed service")
    })

    private def parseInternalResponse(response: HttpResponse): Future[Array[Byte]] = {
        response.entity.toStrict(300.millis).map(_.data).map(_.toArray)
    }

    override def ask(properties: DispatchProperties): Future[Array[Byte]] = {
        val request = properties.asHttpRequest()
        this.internalHandler(request).flatMap(this.parseInternalResponse)
    }

    //noinspection NotImplementedCode
    override def dispatch(properties: DispatchProperties): Future[Any] = ???
}
