package eng.db.shared.webservices

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.stream.ActorMaterializer
import eng.db.shared.base.{AppConfiguration, RoutedService}
import eng.db.shared.log.LogProvider

import scala.concurrent.{ExecutionContext, Future}

/**
  * This object acts as a factory for the creation of a new WebServer instance.
  */
object WebServer {

    /**
      * Gets an instance of a web server responding to the RoutedService interface.
      * The instance is not to consider as a singleton, hence, only one should be used and applied at startup.
      * @param logProvider The log provider.
      * @param provider The routes provider instance.
      * @param exceptionHandler The exception handler route
      * @param system The actor system instance.
      * @param materializer The actor materializer instance.
      * @param executionContext The execution context instance.
      * @return The WebServer instance as a RoutedService.
      */
    def apply(provider: RouteProvider)(
        implicit logProvider: LogProvider,
        exceptionHandler: ExceptionHandler,
        system: ActorSystem,
        materializer: ActorMaterializer,
        executionContext: ExecutionContext): RoutedService = new WebServer(
            provider.getRoutes()(logProvider, system, materializer, executionContext),
            provider.getWhitelistedRoutes()(logProvider, system, materializer, executionContext))(logProvider, exceptionHandler, system, materializer, executionContext)

    /**
      * Gets an instance of a web server responding to the RoutedService interface.
      * The instance is not to consider as a singleton, hence, only one should be used and applied at startup.
      * @param logProvider The log provider.
      * @param routes The routes for the service.
      * @param exceptionHandler The exception handler route
      * @param system The actor system instance.
      * @param materializer The actor materializer instance.
      * @param executionContext The execution context instance.
      * @return The WebServer instance as a RoutedService.
      */
    def apply(routes: Route)(
        implicit logProvider: LogProvider,
        exceptionHandler: ExceptionHandler,
        system: ActorSystem,
        materializer: ActorMaterializer,
        executionContext: ExecutionContext): RoutedService = new WebServer(Some(routes), None)(logProvider, exceptionHandler, system, materializer, executionContext)

    /**
      * Gets an instance of a web server responding to the RoutedService interface.
      * The instance is not to consider as a singleton, hence, only one should be used and applied at startup.
      * @param logProvider The log provider.
      * @param routes The routes that the web server will have to handle.
      * @param whitelistedRoutes The whitelisted route the web server will have to handle.
      * @param exceptionHandler The exception handler route
      * @param system The actor system instance.
      * @param materializer The actor materializer instance.
      * @param executionContext The execution context instance.
      * @return The WebServer instance as a RoutedService.
      */
    def apply(routes: Option[Route], whitelistedRoutes: Option[Route])(
        implicit logProvider: LogProvider,
        exceptionHandler: ExceptionHandler,
        system: ActorSystem,
        materializer: ActorMaterializer,
        executionContext: ExecutionContext): RoutedService = {
        if (routes.isEmpty && whitelistedRoutes.isEmpty) {
            throw new IllegalArgumentException("No routes provided for the service")
        }

        new WebServer(routes, whitelistedRoutes)(logProvider, exceptionHandler, system, materializer, executionContext)
    }

}
class WebServer(val routes: Option[Route], val whitelistedRoutes: Option[Route])(
    implicit val logProvider: LogProvider,
    implicit val exceptionHandler: ExceptionHandler,
    implicit val system: ActorSystem,
    implicit val materializer: ActorMaterializer,
    implicit val executionContext: ExecutionContext) extends RoutedService {

    private var binding: Option[Future[Http.ServerBinding]] = None

    def start(): Unit = {
        val hostname = AppConfiguration.webServerHostname
        val port = AppConfiguration.webServerPort
        logProvider.writeLine(s"Hosting at $hostname:$port")
        val http = Http()

        val routes: Route = (this.routes, this.whitelistedRoutes) match {
            case (Some(r), Some(w)) => handleExceptions(this.exceptionHandler) {
                w ~ BaseRoutes.sessionValidator() ~ r
            }
            case (Some(r), None) => handleExceptions(this.exceptionHandler) {
                BaseRoutes.sessionValidator() ~ r
            }
            case (None, Some(w)) => handleExceptions(this.exceptionHandler) {
                w
            }
            case _ => throw new IllegalArgumentException("No routes provided for the web server")
        }

        this.binding = Some(http.bindAndHandle(routes, hostname, port))
        this.logProvider.writeLineAndFlush(s"Service is ready at $hostname:$port")
    }

    def dispose(): Unit = {
        logProvider.writeLine("Disposing... ")
        binding match {
            case Some(b) => b.flatMap(_.unbind()).onComplete(_ => system.terminate())
            case None =>
        }
        logProvider.writeLine("Disposed!")
    }
}
