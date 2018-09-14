package eng.db.shared.messageBroker.rabbitMq

import com.rabbitmq.client._
import eng.db.shared.base.JsonConvert
import eng.db.shared.log.LogProvider
import eng.db.shared.messageBroker.{DispatchProperties, Dispatcher}

import scala.concurrent.{ExecutionContext, Promise}
import scala.util.{Failure, Success}

object RabbitMqCustomConsumers {

    sealed class InboundHandlerConsumer(val channel: Channel, val exchangeName: String, private val internalDispatcher: Dispatcher)
                                       (implicit val logProvider: LogProvider, implicit val ec: ExecutionContext) extends DefaultConsumer(channel) {
        import eng.db.shared.messageBroker.DispatchPropertiesJsonProtocol._

        override def handleDelivery(consumerTag: String, envelope: Envelope, properties: AMQP.BasicProperties, body: Array[Byte]): Unit = {
//            val routingKey = envelope.getRoutingKey
            val deliveryTag = envelope.getDeliveryTag
            this.channel.basicAck(deliveryTag, false)

            // Publishing the processing message
            val replyProperties = this.getResponseProperties(properties)
            this.channel.basicPublish(this.exchangeName, RabbitMqConstants.processingRoutingKey, false, replyProperties, null)

            try {
                val serializedMessage = body.map(_.toChar).mkString
//                println(s"Serialized message: $serializedMessage")
                logProvider.writeLine(s"Serialized message: $serializedMessage")
                val properties = JsonConvert.fromString[DispatchProperties](serializedMessage)

                this.internalDispatcher.ask(properties) onComplete {
                    case Success(responseBody) =>
                        val responseBodyAsString = responseBody.map(_.toChar).mkString
                        this.logProvider.writeLineAndFlush(s"Message handled! Response: $responseBodyAsString")
                        this.channel.basicPublish(this.exchangeName, RabbitMqConstants.okRoutingKey, false, replyProperties, responseBody)
                    case Failure(error) =>
                        this.logProvider.writeLineAndFlush(s"An error occoured in the service: ${error.toString}")
                        this.channel.basicPublish(this.exchangeName, RabbitMqConstants.errorRoutingKey, false, replyProperties, this.exceptionAsMessage(error))
                }
//                this.channel.basicPublish(this.exchangeName, RabbitMqConstants.okRoutingKey, false, replyProperties, "Tutto ok".toCharArray.map(_.toByte))
            }
            catch {
                case ex: Exception =>
                    this.logProvider.writeLineAndFlush(s"A fatal error occoured in the service: ${ex.toString}")
                    this.channel.basicPublish(this.exchangeName, RabbitMqConstants.errorRoutingKey, false, replyProperties, this.exceptionAsMessage(ex))
            }
        }

        private def getResponseProperties(properties: AMQP.BasicProperties, contentType: String = RabbitMqConstants.messageContentType): AMQP.BasicProperties = {
            new AMQP.BasicProperties.Builder()
                .correlationId(properties.getCorrelationId)
                .headers(properties.getHeaders)
                .contentType(properties.getContentType)
                .build()
        }

        private def exceptionAsMessage(ex: Throwable): Array[Byte] = {
            ex.toString.getBytes
        }

    }

    sealed class OutboundConsumer(
                                     val outboundProperties: AMQP.BasicProperties,
                                     val channel: Channel,
                                     val resultHandler: Array[Byte] => Unit
                                 )
                                 (val logProvider: LogProvider) extends DefaultConsumer(channel) {
        override def handleDelivery(consumerTag: Predef.String, envelope: Envelope, properties: AMQP.BasicProperties, body: Array[Byte]): Unit = {
//            println(s"correlation: ${properties.getCorrelationId}")
            if (properties.getCorrelationId == outboundProperties.getCorrelationId) {
                channel.basicAck(envelope.getDeliveryTag, false)
                resultHandler(body)
            }
            else {
                this.channel.basicNack(envelope.getDeliveryTag, false, true)
            }
        }
    }

    sealed class OutboundErrorConsumer(val awaiter: Promise[Array[Byte]], val outboundProperties: AMQP.BasicProperties, val channel: Channel)(val logProvider: LogProvider) extends DefaultConsumer(channel) {

        override def handleDelivery(consumerTag: Predef.String, envelope: Envelope, properties: AMQP.BasicProperties, body: Array[Byte]): Unit = {
            if (properties.getCorrelationId == outboundProperties.getCorrelationId) {
                channel.basicAck(envelope.getDeliveryTag, false)
                awaiter success body
            }
            else {
                this.channel.basicNack(envelope.getDeliveryTag, false, true)
            }
        }

    }

}
