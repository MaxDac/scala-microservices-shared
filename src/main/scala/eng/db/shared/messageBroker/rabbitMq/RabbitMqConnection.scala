package eng.db.shared.messageBroker.rabbitMq

import com.rabbitmq.client._
import eng.db.shared.base.Disposable
import eng.db.shared.log.LogProvider

object RabbitMqConnection {
    // Factory
    def apply(uri: String, virtualHost: String = ""): RabbitMqConnection = new RabbitMqConnection(uri, virtualHost)
}
class RabbitMqConnection(uri: String, virtualHost: String = "") extends Disposable {

    val connection: Connection = this.getConnection(uri, virtualHost)
    val channel: Channel = this.connection.createChannel()

    private def getConnection(uri: String, virtualHost: String)(implicit logProvider: LogProvider): Connection = {
        logProvider.writeLine("Establish connection")

        val connectionFactory: ConnectionFactory = new ConnectionFactory()
        connectionFactory.setUri(uri)
        if (!virtualHost.isEmpty) connectionFactory.setVirtualHost(virtualHost)
        connectionFactory.newConnection()
    }

    override def dispose(): Unit = {
        this.channel.close()
        this.connection.close()
    }
}
