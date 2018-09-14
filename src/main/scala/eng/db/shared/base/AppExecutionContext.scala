package eng.db.shared.base

import spray.json.DefaultJsonProtocol._
import spray.json._

object SerializationProtocols {
    implicit val appExecutionContextJsonProtocol: RootJsonFormat[AppExecutionContext] = jsonFormat1(AppExecutionContext)
}

case class AppExecutionContext(serializedContext: String)
