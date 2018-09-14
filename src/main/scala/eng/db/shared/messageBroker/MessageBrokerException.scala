package eng.db.shared.messageBroker

class MessageBrokerException(val body: Array[Byte]) extends Exception {

}
