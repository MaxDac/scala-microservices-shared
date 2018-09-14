package eng.db.shared.apps

import akka.actor.ActorSystem
import akka.http.scaladsl.server.ExceptionHandler
import akka.stream.ActorMaterializer
import eng.db.shared.base.RoutedService
import eng.db.shared.log.LogProvider

import scala.concurrent.ExecutionContextExecutor

abstract class ServerApp extends App {
    import eng.db.shared.log.LogProvider._
    import eng.db.shared.webservices.DefaultExceptionHandlers._
    
    implicit val system: ActorSystem = ActorSystem("test-system")
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher
    
    protected def getHandlerInstance()(implicit logProvider: LogProvider, exceptionHandler: ExceptionHandler): RoutedService
    
    protected val handler = this.getHandlerInstance()
    
    if (this.args.length > 0) {
        LogProvider.logProvider.writeLine("Closing... ")
        
        for (arg <- args) {
            LogProvider.logProvider.writeLine(s"Arg: $arg\n")
        }
        
        LogProvider.logProvider.writeLineAndFlush("Disposing ... ")
        this.dispose()
        System.exit(0)
    }
    else {
        this.start()
    }
    
    def start(): Unit = {
        LogProvider.logProvider.writeLine("Starting... ")
        handler.start()
        LogProvider.logProvider.writeLineAndFlush("Started!")
    }
    
    def dispose(): Unit = {
        LogProvider.logProvider.writeLine("Arresting... ")
        handler.dispose()
        LogProvider.logProvider.writeLine("Arrested!")
    }
}
