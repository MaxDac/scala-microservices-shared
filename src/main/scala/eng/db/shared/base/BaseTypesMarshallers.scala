package eng.db.shared.base

import java.util.Date
import java.text._

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._

import scala.util.Try

trait BaseTypesMarshallers extends SprayJsonSupport with DefaultJsonProtocol {
    implicit val backEndErrorFormatter: RootJsonFormat[BackEndError] = jsonFormat3(BackEndError.apply)
    implicit val defaultKeyFormatter: RootJsonFormat[DefaultKey] = jsonFormat4(DefaultKey.apply)
    implicit val queryKeyFormatter: RootJsonFormat[QueryKey] = jsonFormat4(QueryKey.apply)

    implicit object DateFormat extends JsonFormat[Date] {
        def write(date: Date) = JsString(dateToIsoString(date))
        def read(json: JsValue) = json match {
            case JsString(rawDate) =>
                parseIsoDateString(rawDate)
                    .fold(deserializationError(s"Expected ISO Date format, got $rawDate"))(identity)
            case error => deserializationError(s"Expected JsString, got $error")
        }
    }

    private val localIsoDateFormatter = new ThreadLocal[SimpleDateFormat] {
        override def initialValue() = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    }

    private def dateToIsoString(date: Date) =
        localIsoDateFormatter.get().format(date)

    private def parseIsoDateString(date: String): Option[Date] =
        Try{ localIsoDateFormatter.get().parse(date) }.toOption
}
