package eng.db.shared.messageBroker.rabbitMq

object RabbitMqConstants {
    val messageContentType = "application/json"
    val errorContentType = "text/plain"

    val requestRoutingKey = "Request"
    val processingRoutingKey = "Processing"
    val okRoutingKey = "Ok"
    val errorRoutingKey = "Error"
}