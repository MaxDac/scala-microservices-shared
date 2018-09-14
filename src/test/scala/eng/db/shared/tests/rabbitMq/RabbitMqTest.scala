package eng.db.shared.tests.rabbitMq

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives.{get, path}
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import eng.db.shared.base.RoutedService
import eng.db.shared.log.LogProvider
import eng.db.shared.messageBroker.rabbitMq.RabbitMqDispatcher
import eng.db.shared.messageBroker.{DispatchProperties, MessageBrokerConsumerFactory, MessageBrokerException}
import org.scalatest.AsyncFlatSpec

import scala.concurrent.ExecutionContextExecutor

//noinspection NameBooleanParameters
class RabbitMqTest extends AsyncFlatSpec {
//    import scala.concurrent._
//    import ExecutionContext.Implicits.global
    
    private val messageOk: String = "<h1>Test route ok</h1>"
//    private val unauthenticatedErrorMessage: String = "Authentication is possible but has failed or not yet been provided."

    private def getTestRoute: Route = {
        path("") {
            get {
                akka.http.scaladsl.server.Directives.complete(this.messageOk)
            }
        }
    }

    import LogProvider._
    import eng.db.shared.messageBroker.DispatchPropertiesJsonProtocol._
    import eng.db.shared.webservices.DefaultExceptionHandlers._

    implicit val system: ActorSystem = ActorSystem("test-system")
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val ec: ExecutionContextExecutor = system.dispatcher

    val serviceName = "Credito"
    val hostname = "amqp://Credito:Credito@localhost:5672/Credito"
    val consumer: RoutedService = MessageBrokerConsumerFactory.getConsumer(this.getTestRoute) // new RabbitMqInboundHandler(dispatcher, serviceName, hostname) //(LogProviderFactory.logProvider)
    consumer.start()
    
    "The broker" should "dispatch the message" in {
        println("Launching probe")

        try {
            val dispatchProperties = DispatchProperties(
                Some("<contesto></contesto>"),
                Some("Credito"),
                "GET".encode(),
                "/",
                Some("{ \"asking\": \"Test message!\"}"))
            val probe = RabbitMqDispatcher(hostname, serviceName)

            val result = probe.ask(dispatchProperties)
            result.map(a => {
                val parsedMessage = a.map(_.toChar).mkString
                assert(parsedMessage === this.messageOk)
            })
//            result onComplete {
//                case Success(message) => {
//                    assert(true)
//                    println(s"Success!: ${message.map(_.toChar).mkString}")
//                }
//                case Failure(error) => {
//                    assert(false, s"Failure!: $error")
//                    println(s"Failure!: $error")
//                }
//            }
            //        val result = for (
            //            rawAnswer <- probe.ask(dispatchProperties)
            //        ) yield rawAnswer.toString

//            println(result)
        }
        catch {
            case ex: MessageBrokerException =>
                println(ex.toString)
                assert(false, s"Failure!: ${ex.toString}")
        }
    }
}
