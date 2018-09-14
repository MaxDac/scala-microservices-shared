package eng.db.shared.base

object BackEndError {

    object ErrorType {
        val info: Int = 0
        val error: Int = 1
        val warning: Int = 2
        val question: Int = 3
        val debug: Int = 4
    }

    implicit class Converter(val error: BackEndError) extends BaseTypesMarshallers {
        import spray.json._

        def serialized(): String = error.toJson.prettyPrint
    }

    def empty(): BackEndError = BackEndError(ErrorType.info, "", "")

    def error(code: String, description: String) = BackEndError(ErrorType.error, code, description)

    def warning(code: String, description: String) = BackEndError(ErrorType.warning, code, description)
}
case class BackEndError(
                       `type`: Int,
                       code: String,
                       description: String
                       )
