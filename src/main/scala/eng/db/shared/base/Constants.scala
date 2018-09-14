package eng.db.shared.base

import java.text.SimpleDateFormat

object DbConstants {
    def netaTimestampFormat = new SimpleDateFormat("yyyy-mm-dd HH:mm:ss.SSS")
    def netaDateFormat = new SimpleDateFormat("yyyy-mm-dd")
    def initialDate = netaDateFormat.parse("0001-01-01")
    def finalDate = netaDateFormat.parse("9999-12-31")
    def initialTimestamp = netaTimestampFormat.parse("0001-01-01 00:00:00.000")
    def finalTimestamp = netaTimestampFormat.parse("9999-12-31 23:59:59.999")
}
