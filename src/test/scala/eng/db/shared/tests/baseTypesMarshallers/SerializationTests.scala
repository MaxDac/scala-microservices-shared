package eng.db.shared.tests.baseTypesMarshallers

import eng.db.shared.base.BackEndError._
import eng.db.shared.base.{BackEndError, BaseTypesMarshallers}
import org.scalatest.FlatSpec
import spray.json._

class SerializationTests extends FlatSpec with BaseTypesMarshallers {
    "The class BackEndError" should "serialize properly using internal methods" in {
        val errorCode = "1"
        val errorDescription = "Error code"

        val error = BackEndError.error(errorCode, errorDescription)
        val serialized = error.serialized()
        val deserialized = serialized.parseJson.convertTo[BackEndError]

        assert(deserialized.code === errorCode)
        assert(deserialized.description === errorDescription)
    }
}
