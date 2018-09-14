package eng.db.shared.messageBroker.rabbitMq

import java.util.UUID

import com.rabbitmq.client.AMQP.BasicProperties
import com.rabbitmq.client.DefaultConsumer
import eng.db.shared.base.Disposable
import eng.db.shared.log.LogProvider
import eng.db.shared.messageBroker.rabbitMq.RabbitMqCustomConsumers.OutboundConsumer
import eng.db.shared.messageBroker.{DispatchProperties, Dispatcher, MessageBrokerException}

import scala.concurrent.{Future, Promise}

object RabbitMqDispatcher {
    def apply(hostname: String, virtualHost: String = "")(implicit logProvider: LogProvider): RabbitMqDispatcher =
        new RabbitMqDispatcher(hostname, virtualHost)(logProvider)
}
class RabbitMqDispatcher(val hostname: String, val virtualHost: String = "")(implicit val logProvider: LogProvider) extends Dispatcher with Disposable {

    private val connection = RabbitMqConnection(hostname, virtualHost)

    override def ask(properties: DispatchProperties): Future[Array[Byte]] = {
        val messageProperties = this.getMessageProperties(properties)
        val message = properties.prepareToDispatch()
        val result = Promise[Array[Byte]]

        val okConsumer = new OutboundConsumer(messageProperties, this.connection.channel, x => result success x)(logProvider)
        val errorConsumer = new OutboundConsumer(messageProperties, this.connection.channel, x => result failure new MessageBrokerException(x))(logProvider)
        this.orchestrateQueueForAsk(properties, okConsumer, errorConsumer)
        this.connection.channel.basicPublish(
            properties.exchangeName.getOrElse(""),
            RabbitMqConstants.requestRoutingKey, messageProperties,
            message
        )
        result.future
    }

    override def dispatch(properties: DispatchProperties): Future[Any] = {
        val messageProperties = this.getMessageProperties(properties)
        val message = properties.prepareToDispatch()
        val result = Promise[Array[Byte]]

        val okConsumer = new OutboundConsumer(messageProperties, this.connection.channel, x => result success x)(logProvider)
        val errorConsumer = new OutboundConsumer(messageProperties, this.connection.channel, x => result failure new MessageBrokerException(x))(logProvider)
        this.orchestrateQueuesForDispatch(properties, okConsumer, errorConsumer)
        this.connection.channel.basicPublish(
            properties.exchangeName.getOrElse(""),
            RabbitMqConstants.requestRoutingKey,
            messageProperties,
            message
        )
        result.future
    }

    private def getMessageProperties(properties: DispatchProperties): BasicProperties = {
        val messageProperties = new BasicProperties.Builder()
            .correlationId(UUID.randomUUID().toString)

        if (properties.responseQueue.isDefined) {
            messageProperties.replyTo(properties.responseQueue.get)
        }

        messageProperties.build()
    }

    private def orchestrateQueueForAsk(properties: DispatchProperties, okConsumer: DefaultConsumer, errorConsumer: DefaultConsumer): Unit = {
        val exchangeName = properties.exchangeName.getOrElse("")
        if (properties.responseQueue.getOrElse("").isEmpty) {
            this.declareAndBindQueue(s"$exchangeName.${RabbitMqConstants.okRoutingKey}", s"$exchangeName.${RabbitMqConstants.okRoutingKey}", okConsumer)
        }
        else {
            this.declareAndBindQueue(
                properties.responseQueue.getOrElse(""),
                s"$exchangeName.${RabbitMqConstants.okRoutingKey}",
                okConsumer)
        }

        this.declareAndBindQueue(s"$exchangeName.${RabbitMqConstants.errorRoutingKey}", s"$exchangeName.${RabbitMqConstants.errorRoutingKey}", errorConsumer)
    }

    private def orchestrateQueuesForDispatch(properties: DispatchProperties, processingConsumer: DefaultConsumer, errorConsumer: DefaultConsumer): Unit = {
        val exchangeName = properties.exchangeName.getOrElse("")
        this.declareAndBindQueue(s"$exchangeName.${RabbitMqConstants.processingRoutingKey}", s"$exchangeName.${RabbitMqConstants.processingRoutingKey}", processingConsumer)
        this.declareAndBindQueue(s"$exchangeName.${RabbitMqConstants.errorRoutingKey}", s"$exchangeName.${RabbitMqConstants.errorRoutingKey}", errorConsumer)
    }

    private def declareAndBindQueue(queueName: String, exchangeName: String, consumer: DefaultConsumer) = {
        this.connection.channel.queueDeclare(queueName, false, false, false, null)
        this.connection.channel.queueBind(queueName, exchangeName, "", null)
        this.connection.channel.basicConsume(queueName, false, consumer)
    }

    override def dispose(): Unit = {
        this.connection.dispose()
    }
}
