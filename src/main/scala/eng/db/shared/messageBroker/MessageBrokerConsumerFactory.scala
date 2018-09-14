package eng.db.shared.messageBroker

import akka.actor.ActorSystem
import akka.http.scaladsl.server.{ExceptionHandler, Route}
import akka.stream.ActorMaterializer
import eng.db.shared.base.{AppConfiguration, Disposable, RoutedService}
import eng.db.shared.log.LogProvider
import eng.db.shared.messageBroker.rabbitMq.RabbitMqInboundHandler
import eng.db.shared.webservices.{AkkaInternalDispatcher, RouteProvider}

import scala.concurrent.ExecutionContext

object MessageBrokerConsumerFactory {

    // Information regarding the consumer creation are expected to be found in the configuration file

//    implicit val system = ActorSystem("test-system")
//    implicit val materializer = ActorMaterializer()
//    implicit val executionContext = system.dispatcher

    def getConsumer(routeProvider: RouteProvider)
                   (implicit logProvider: LogProvider,
                    exceptionHandler: ExceptionHandler,
                    system: ActorSystem,
                    materializer: ActorMaterializer,
                    executionContext: ExecutionContext): RoutedService =
        getConsumer(
            routeProvider.getRoutes()(logProvider, system, materializer, executionContext),
            routeProvider.getWhitelistedRoutes()(logProvider, system, materializer, executionContext)
        )(logProvider, exceptionHandler, system, materializer, executionContext)

    def getConsumer(routes: Route)
                   (implicit logProvider: LogProvider,
                    exceptionHandler: ExceptionHandler,
                    system: ActorSystem,
                    materializer: ActorMaterializer,
                    executionContext: ExecutionContext): RoutedService = {
        getConsumer(Some(routes), None,
            AppConfiguration.mqConsumerExchangeName,
            AppConfiguration.mqConsumerHostname,
            AppConfiguration.mqConsumerVirtualHost)(logProvider, exceptionHandler, system, materializer, executionContext)
    }

    def getConsumer(routes: Option[Route], whitelistedRoutes: Option[Route])
                   (implicit logProvider: LogProvider,
                    exceptionHandler: ExceptionHandler,
                    system: ActorSystem,
                    materializer: ActorMaterializer,
                    executionContext: ExecutionContext): RoutedService = {
        getConsumer(routes, whitelistedRoutes,
            AppConfiguration.mqConsumerExchangeName,
            AppConfiguration.mqConsumerHostname,
            AppConfiguration.mqConsumerVirtualHost)(logProvider, exceptionHandler, system, materializer, executionContext)
    }

    def getConsumer(routes: Option[Route], whitelistedRoutes: Option[Route], exchangeName: String, hostname: String, virtualHost: String)
                   (implicit logProvider: LogProvider,
                    exceptionHandler: ExceptionHandler,
                    system: ActorSystem,
                    materializer: ActorMaterializer,
                    executionContext: ExecutionContext): RoutedService = {
        new RabbitMqInboundHandler(
            new AkkaInternalDispatcher(routes, whitelistedRoutes), exchangeName, hostname, virtualHost)(logProvider, executionContext)
    }
}
