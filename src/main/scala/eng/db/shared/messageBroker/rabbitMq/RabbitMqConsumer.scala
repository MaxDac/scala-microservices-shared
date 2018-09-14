//package eng.db.shared.messageBroker.rabbitMq
//
//import com.rabbitmq.client._
//import eng.db.shared.base.JsonConvert
//import eng.db.shared.log.{LogProvider, _}
//import eng.db.shared.messageBroker.{DispatchProperties, Dispatcher, MessageBrokerConsumer}
//
//import scala.concurrent.ExecutionContext
//import scala.util.{Failure, Success}
//
//sealed class CustomConsumer(val channel: Channel, val exchangeName: String, private val internalDispatcher: Dispatcher)
//                           (implicit val logProvider: LogProvider, implicit val ec: ExecutionContext) extends DefaultConsumer(channel) {
//    import eng.db.shared.messageBroker.DispatchPropertiesJsonProtocol._
//
//    override def handleDelivery(consumerTag: String, envelope: Envelope, properties: AMQP.BasicProperties, body: Array[Byte]): Unit = {
//        val routingKey = envelope.getRoutingKey
//        val deliveryTag = envelope.getDeliveryTag
//        this.channel.basicAck(deliveryTag, false)
//
//        // Publishing the processing message
//        val replyProperties = this.getResponseProperties(properties)
//        this.channel.basicPublish(this.exchangeName, RabbitMqConstants.processingRoutingKey, false, replyProperties, null)
//
//        try {
//            val serializedMessage = body.map(_.toChar).mkString
//            val properties = JsonConvert.fromString[DispatchProperties](serializedMessage)
//            this.internalDispatcher.ask(properties) onComplete {
//                case Success(responseBody) => this.channel.basicPublish(this.exchangeName, RabbitMqConstants.okRoutingKey, false, replyProperties, responseBody)
//                case Failure(error) => this.channel.basicPublish(this.exchangeName, RabbitMqConstants.errorRoutingKey, false, replyProperties, this.exceptionAsMessage(error))
//            }
//
//
//        } catch {
//            case ex: Exception => this.channel.basicPublish(this.exchangeName, RabbitMqConstants.errorRoutingKey, false, replyProperties, this.exceptionAsMessage(ex))
//        }
//    }
//
//    private def getResponseProperties(properties: AMQP.BasicProperties, contentType: String = RabbitMqConstants.messageContentType): AMQP.BasicProperties = {
//        new AMQP.BasicProperties.Builder()
//            .correlationId(properties.getCorrelationId)
//            .headers(properties.getHeaders)
//            .contentType(properties.getContentType)
//            .build()
//    }
//
//    private def exceptionAsMessage(ex: Throwable): Array[Byte] = {
//        ex.toString.getBytes
//    }
//
//}
//
//class RabbitMqConsumer(internalDispatcher: Dispatcher, val exchangeName: String, val hostname: String, val virtualHost: String = "")
//                      (implicit val logProvider: LogProvider, implicit val ec: ExecutionContext) extends MessageBrokerConsumer {
//
//    logProvider.writeLine("Rabbit construction begun")
//    private val connection = new RabbitMqConnection(hostname, virtualHost)
//
//    private val consumer = new CustomConsumer(connection.channel, this.exchangeName, internalDispatcher)(new FileLogProvider, ec)
//
//    orchestrateExchanges()
//    this.connection.channel.basicQos(0, 1, false)
//    this.connection.channel.basicConsume(s"$exchangeName.${RabbitMqConstants.requestRoutingKey}",false, this.consumer)
//
//    def orchestrateExchanges(): Unit = {
//        // Declaring the exchange and queues names
//        val requestExchangeName = s"$exchangeName.${RabbitMqConstants.requestRoutingKey}"
//        val processingExchangeName = s"$exchangeName.${RabbitMqConstants.processingRoutingKey}"
//        val okExchangeName = s"$exchangeName.${RabbitMqConstants.okRoutingKey}"
//        val errorExchangeName = s"$exchangeName.${RabbitMqConstants.errorRoutingKey}"
//
//        // Declaring the exchanges
//        this.connection.channel.exchangeDeclare(this.exchangeName, "topic", true, false, null)
//        this.connection.channel.exchangeDeclare(requestExchangeName, "fanout", true, false, null)
//        this.connection.channel.exchangeDeclare(processingExchangeName, "fanout", true, false, null)
//        this.connection.channel.exchangeDeclare(okExchangeName, "fanout", true, false, null)
//        this.connection.channel.exchangeDeclare(errorExchangeName, "fanout", true, false, null)
//
//        // Binding the main queue to the childs queue with the topic string
//        this.connection.channel.exchangeBind(requestExchangeName, this.exchangeName, RabbitMqConstants.requestRoutingKey, null)
//        this.connection.channel.exchangeBind(processingExchangeName, this.exchangeName, RabbitMqConstants.processingRoutingKey, null)
//        this.connection.channel.exchangeBind(okExchangeName, this.exchangeName, RabbitMqConstants.okRoutingKey, null)
//        this.connection.channel.exchangeBind(errorExchangeName, this.exchangeName, RabbitMqConstants.errorRoutingKey, null)
//
//        // Binding the consumer queue to the request exchange. This will be the only exchange binded here
//        this.connection.channel.queueDeclare(requestExchangeName, true, false, false, null)
//        this.connection.channel.queueBind(requestExchangeName, requestExchangeName, "", null)
//    }
//}
