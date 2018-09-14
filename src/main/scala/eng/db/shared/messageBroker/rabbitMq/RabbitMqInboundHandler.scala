package eng.db.shared.messageBroker.rabbitMq

import eng.db.shared.base.RoutedService
import eng.db.shared.log.LogProvider
import eng.db.shared.messageBroker.rabbitMq.RabbitMqCustomConsumers.InboundHandlerConsumer
import eng.db.shared.messageBroker.Dispatcher

import scala.concurrent.ExecutionContext

class RabbitMqInboundHandler(internalDispatcher: Dispatcher, val exchangeName: String, val hostname: String, val virtualHost: String = "")
                            (implicit val logProvider: LogProvider, implicit val ec: ExecutionContext)
                            extends RoutedService {

    private val connection = RabbitMqConnection(hostname, virtualHost)
    private val consumer =
        new InboundHandlerConsumer(connection.channel, this.exchangeName, internalDispatcher) //(logProvider)


    def start(): Unit = {
        logProvider.writeLine(s"Rabbit construction begun for $exchangeName")
        orchestrateExchanges()
        this.connection.channel.basicQos(0, 1, false)
        this.connection.channel.basicConsume(s"$exchangeName.${RabbitMqConstants.requestRoutingKey}", false, this.consumer)
        logProvider.writeLineAndFlush("Rabbit construction ended")
    }

    def orchestrateExchanges(): Unit = {
        // Declaring the exchange and queues names
        val requestExchangeName = s"$exchangeName.${RabbitMqConstants.requestRoutingKey}"
        val processingExchangeName = s"$exchangeName.${RabbitMqConstants.processingRoutingKey}"
        val okExchangeName = s"$exchangeName.${RabbitMqConstants.okRoutingKey}"
        val errorExchangeName = s"$exchangeName.${RabbitMqConstants.errorRoutingKey}"

        // Declaring the exchanges
        this.connection.channel.exchangeDeclare(this.exchangeName, "topic", true, false, null)
        this.connection.channel.exchangeDeclare(requestExchangeName, "fanout", true, false, null)
        this.connection.channel.exchangeDeclare(processingExchangeName, "fanout", true, false, null)
        this.connection.channel.exchangeDeclare(okExchangeName, "fanout", true, false, null)
        this.connection.channel.exchangeDeclare(errorExchangeName, "fanout", true, false, null)

        // Binding the main queue to the childs queue with the topic string
        this.connection.channel.exchangeBind(requestExchangeName, this.exchangeName, RabbitMqConstants.requestRoutingKey, null)
        this.connection.channel.exchangeBind(processingExchangeName, this.exchangeName, RabbitMqConstants.processingRoutingKey, null)
        this.connection.channel.exchangeBind(okExchangeName, this.exchangeName, RabbitMqConstants.okRoutingKey, null)
        this.connection.channel.exchangeBind(errorExchangeName, this.exchangeName, RabbitMqConstants.errorRoutingKey, null)

        // Binding the consumer queue to the request exchange. This will be the only exchange binded here
        this.connection.channel.queueDeclare(requestExchangeName, true, false, false, null)
        this.connection.channel.queueBind(requestExchangeName, requestExchangeName, "", null)
    }

    def dispose(): Unit = {
        this.logProvider.writeLine("Disposing... ")
        this.connection.dispose()
        this.logProvider.writeLineAndFlush("Disposed!")
    }
}
