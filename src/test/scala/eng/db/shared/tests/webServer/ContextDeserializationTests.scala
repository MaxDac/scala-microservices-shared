package eng.db.shared.tests.webServer

import eng.db.shared.base.NetaContext
import org.scalatest.FlatSpec

class ContextDeserializationTests extends FlatSpec {
    val context = "{context-string}"

    "The deserializer" should "deserialize the context correctly" in {
        try {
            val netaContext = NetaContext.deserialize(this.context)
            assert(netaContext.operatorId === "SUPER")
            assert(netaContext.profileId === "zjE3Oasv67LyVJAh")
            assert(netaContext.idiomId === "ITA")
            assert(netaContext.sectionId === "ZF01")

            netaContext.getCustomNodeValue(NetaContext.levelNode) match {
                case Some(value) => assert(value === "80")
                case _ => assert(false, "The context couldn't find the node.")
            }
        }
        catch {
            case ex: Exception => assert(false, s"Unexpected error: ${ex.toString}")
        }
    }
}
