package eng.db.shared.log

import eng.db.shared.base.AppConfiguration

object LogProvider {
    
//    private val defaultConfig = ConfigFactory.parseResources("defaults.conf").resolve()
    
    implicit val logProvider: LogProvider = {
//        println(s"active: ${AppConfiguration.logDefined.toString}")
//        println(s"type: ${AppConfiguration.logType.getOrElse("")}")
//        println(s"path: ${AppConfiguration.logPath.getOrElse("")}")
        (AppConfiguration.logType, AppConfiguration.logPath) match {
            case (Some(logType), Some(logPath)) if logType == "file-system" && !logPath.isEmpty => new FileLogProvider(logPath)
            case _ => new ConsoleLogProvider()
        }
    }
}
trait LogProvider {
    def writeLine(log: String): Unit
    def flush(): Unit
    def writeLineAndFlush(log: String): Unit = {
        this.writeLine(log)
        this.flush()
    }
}
