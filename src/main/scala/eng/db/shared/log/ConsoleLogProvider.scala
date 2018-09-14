package eng.db.shared.log

class ConsoleLogProvider extends LogProvider {
    override def writeLine(log: String): Unit = println(log)

    override def flush(): Unit = {}
}
