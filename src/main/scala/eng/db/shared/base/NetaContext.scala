package eng.db.shared.base

object NetaContext {
    val operatorNode: String = "P_OPEID"
    val idiomNode: String = "P_LINGUAID"
    val roleNode: String = "P_RUOLOID"
    val moduleNode: String = "P_MODULOID"
    val sectionNode: String = "P_SEZIONEID"
    val profileNode: String = "P_PROFILOID"
    val levelNode: String = "P_LIVELLOID"
    val strategicNode: String = "P_STRATEGICITAID"
    val repartoNode: String = "P_REPARTOID"
    val uorNode: String = "P_OPEUOR"
    val orgNode: String = "P_OPEORGDEP"
    val societyNode: String = "P_SOCID"
    val executionIdNode: String = "P_ESESIUID"

    def deserialize(serializedContext: String): NetaContext = {
        val contextAsXml = scala.xml.XML.loadString(serializedContext)
        val contextNodes = contextAsXml \\ "Nodo"

        if (contextNodes.length == 0) {
            throw new IllegalArgumentException("No nodes in the context")
        }

        val valueNodes = contextNodes.map(n => (n.attribute("TipoID").get.head.text, n.attribute("Valore").get.head.text))
        new NetaContext(
            valueNodes.filter(n => n._1 == operatorNode).head._2,
            valueNodes.filter(n => n._1 == profileNode).head._2,
            valueNodes.filter(n => n._1 == idiomNode).head._2,
            valueNodes.filter(n => n._1 == sectionNode).head._2,
            valueNodes
        )
    }
}
class NetaContext(
                     val operatorId: String,
                     val profileId: String,
                     val idiomId: String,
                     val sectionId: String,
                     private val nodes: Seq[(String, String)]) {
    /**
      * Gets a custom value from the node series of the Net@ Context.
      * @param nodeId The node id.
      * @return The node value, or None.
      */
    def getCustomNodeValue(nodeId: String): Option[String] = {
        nodes.filter(n => n._1 == nodeId) match {
            case sq if sq.length == 1 => Some(sq.head._2)
            case _ => None
        }
    }
}
