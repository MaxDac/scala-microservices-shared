package eng.db.shared.tests.webServer

import eng.db.shared.base.NetaContext
import org.scalatest.FlatSpec

class ContextDeserializationTests extends FlatSpec {
    val context = "<Contesto><Nodo TipoID=\"P_OPEID\" Tipo=\"\" Valore=\"SUPER\" /><Nodo TipoID=\"P_LINGUAID\" Tipo=\"\" Valore=\"ITA\" /><Nodo TipoID=\"P_RUOLOID\" Tipo=\"\" Valore=\"137\" /><Nodo TipoID=\"P_MODULOID\" Tipo=\"\" Valore=\"10\" /><Nodo TipoID=\"P_PROFILOID\" Tipo=\"\" Valore=\"zjE3Oasv67LyVJAh\" /><Nodo TipoID=\"P_LIVELLOID\" Tipo=\"\" Valore=\"80\" /><Nodo TipoID=\"P_STRATEGICITAID\" Tipo=\"\" Valore=\"1\" /><Nodo TipoID=\"P_REPARTOID\" Tipo=\"\" Valore=\"1\" /><Nodo TipoID=\"P_SOCID\" Tipo=\"\" Valore=\"01\" /><Nodo TipoID=\"P_SEZIONEID\" Tipo=\"\" Valore=\"ZF01\" /><Nodo TipoID=\"PageAttivita\" Tipo=\"PageAttivita\" Valore=\"GESTIONE\" /><Nodo TipoID=\"EDW_PAG\" Tipo=\"EDW_PAG\" Valore=\"/NETASIU/SIUWEB/SIUWEB.REPORTS/REPORTCR/FORMS/LANCIOREPORTBILLING.ASPX\" /><Nodo TipoID=\"FORN_READONLY\" Tipo=\"FORN_READONLY\" Valore=\"False\" /><Nodo TipoID=\"KeyServerId\" Tipo=\"KeyServerId\" Valore=\"56\" /><Nodo TipoID=\"SchedKeyPF\" Tipo=\"SchedKeyPF\" Valore=\"/NETASIU/SIUWEB/SIUWEB.REPORTS/REPORTCR/POPUP/POPUPSTAMPAREPORT.ASPX\" /><Nodo TipoID=\"SchKeyClassAppId\" Tipo=\"SchKeyClassAppId\" Valore=\"58\" /><Nodo TipoID=\"SchedKeyAId\" Tipo=\"SchedKeyAId\" Valore=\"878\" /><Nodo TipoID=\"SchedKeyADesc\" Tipo=\"SchedKeyADesc\" Valore=\"POPUP STAMPA REPORT\" /><Nodo TipoID=\"SchedKeyGId\" Tipo=\"SchedKeyGId\" Valore=\"6\" /><Nodo TipoID=\"SchedKeyGCod\" Tipo=\"SchedKeyGCod\" Valore=\"REPORT\" /><Nodo TipoID=\"SchedKeyGDesc\" Tipo=\"SchedKeyGDesc\" Valore=\"GRUPPO SCHEDULAZIONE REPORT\" /><Nodo TipoID=\"KeyServerCode\" Tipo=\"KeyServerCode\" Valore=\"DEFAULTSERVER\" /><Nodo TipoID=\"KeyServerHost\" Tipo=\"KeyServerHost\" Valore=\"172.26.51.34\" /><Nodo TipoID=\"EDW_EPR\" Tipo=\"EDW_EPR\" Valore=\"zjE3Oasv67LyVJAh\" /></Contesto>"

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
