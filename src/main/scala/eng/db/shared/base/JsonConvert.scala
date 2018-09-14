package eng.db.shared.base

import spray.json._

object JsonConvert {
    def fromString[A](json: String)(implicit jsonConverterProtocol: RootJsonFormat[A]): A = json.parseJson.convertTo[A](jsonConverterProtocol)
    def fromString[A](converter: JsonConvert[A], json: String): A = json.parseJson.convertTo[A](converter.jsonConverterProtocol)

    def apply[A](item : A)(implicit jsonConverterProtocol: RootJsonFormat[A]): JsonConvert[A] = new JsonConvert(item)(jsonConverterProtocol)
}
class JsonConvert[A](val item: A)(implicit private val jsonConverterProtocol: RootJsonFormat[A]) {
    def asJson(): String = item.toJson(this.jsonConverterProtocol).prettyPrint
}
