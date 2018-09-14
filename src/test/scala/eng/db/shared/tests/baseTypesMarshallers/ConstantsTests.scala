package eng.db.shared.tests.baseTypesMarshallers

import eng.db.shared.base.DbConstants
import org.scalatest.FlatSpec

class ConstantsTests extends FlatSpec {
    "The constants" should "load correctly" in {
        try {
            val initialDate = DbConstants.initialDate
            val initialTimesta = DbConstants.initialTimestamp
            val finalDate = DbConstants.finalDate
            val finalTimestamp = DbConstants.finalTimestamp
            assert(true)
        }
        catch {
            case ex: Exception => assert(false, ex.toString)
        }
    }
}
