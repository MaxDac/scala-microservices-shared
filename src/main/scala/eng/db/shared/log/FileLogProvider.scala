package eng.db.shared.log

import eng.db.shared.base.AppConfiguration

import scala.tools.nsc.io._

class FileLogProvider(val path: String) extends LogProvider {

    private val temp: StringBuilder = new StringBuilder
    val logFile: File = this.checkFile()

    override def writeLine(log: String): Unit = {
        temp.append(s"\r\n$log")
    }

    override def flush(): Unit = {
//        println(temp.toString())
        this.logFile.appendAll(temp.toString)
        temp.clear()
    }
    
    private def checkFile(): File = {
        val path = Path(this.path)
        if (!path.exists) {
            path.createDirectory(force = true, failIfExists = false)
        }
        
        Path(s"${this.path}/${AppConfiguration.name}.log").createFile(false)
    }
}
