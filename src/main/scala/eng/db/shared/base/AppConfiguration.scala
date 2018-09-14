package eng.db.shared.base

import com.typesafe.config.{Config, ConfigFactory}

/**
  * This object contains all the configuration of the application
  * The desired configuration would look like this
  * conf {
  * messageBroker {
  * hostname = "this-is-the-hostname"
  * virtualHost = "this-is-the-virtualhost"
  * exchangeName = "this-is-the-exchangeName"
  * }
  * webServer {
  * hostname = "localhost"
  * port = "8082"
  * }
  * }
  */
object AppConfiguration {

    private val defaultConfig = ConfigFactory.parseResources("defaults.conf").resolve()

    trait ConfigurationExtractor[T] {
        def extract(path: String, configurations: Config): T
    }

    implicit val StringConfigurationExtractor: ConfigurationExtractor[String] =
        (path: String, configurations: Config) => configurations.getString(path)

    implicit val BooleanConfigurationExtractor: ConfigurationExtractor[Boolean] =
        (path: String, configurations: Config) => configurations.getBoolean(path)

    implicit val IntConfigurationExtractor: ConfigurationExtractor[Int] =
        (path: String, configurations: Config) => configurations.getInt(path)

    def tryGetValue[T](path: String)(implicit configurationExtractor: ConfigurationExtractor[T]): Option[T] = {
        if (!this.defaultConfig.hasPath(path) || this.defaultConfig.getIsNull(path)) {
            None
        }
        else {
            Some(configurationExtractor.extract(path, this.defaultConfig))
        }
    }

    lazy val name: String = this.defaultConfig.getString("conf.name")

    lazy val logDefined: Boolean = !this.defaultConfig.hasPath("conf.log") || !this.defaultConfig.getIsNull("conf.log")

    lazy val logType: Option[String] = if (logDefined) Some(this.defaultConfig.getString("conf.log.type")) else None
    lazy val logPath: Option[String] = if (logDefined && logType.contains("file-system")) Some(this.defaultConfig.getString("conf.log.path")) else None

    lazy val mqConsumerHostname: String = this.defaultConfig.getString("conf.messageBroker.hostname")
    lazy val mqConsumerVirtualHost: String = this.defaultConfig.getString("conf.messageBroker.virtualHost")
    lazy val mqConsumerExchangeName: String = this.defaultConfig.getString("conf.messageBroker.exchangeName")

    lazy val webServerHostname: String = this.defaultConfig.getString("conf.webServer.hostname")
    lazy val webServerPort: Int = this.defaultConfig.getInt("conf.webServer.port")

    lazy val version: String = this.defaultConfig.getString("conf.version")

    // TODO - temporary, for debug purpouse
    lazy val debugSessionToken: String = this.defaultConfig.getString("conf.webServer.debugSessionToken")

}
