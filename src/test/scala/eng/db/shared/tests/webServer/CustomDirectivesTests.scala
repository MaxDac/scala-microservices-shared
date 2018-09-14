package eng.db.shared.tests.webServer

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import eng.db.shared.log.LogProvider
import eng.db.shared.messageBroker.DispatchProperties
import eng.db.shared.webservices.AkkaInternalDispatcher
import org.scalatest.AsyncFlatSpec

import scala.concurrent.ExecutionContextExecutor

class CustomDirectivesTests extends AsyncFlatSpec {

    import eng.db.shared.messageBroker.DispatchPropertiesJsonProtocol._
    import eng.db.shared.webservices.CustomDirectives._
    import eng.db.shared.webservices.DefaultExceptionHandlers._

    implicit val logProvider: LogProvider = LogProvider.logProvider
    implicit val system: ActorSystem = ActorSystem("test-system")
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val ec: ExecutionContextExecutor = system.dispatcher

    val netaContext = "<Contesto><Nodo TipoID=\"P_OPEID\" Tipo=\"\" Valore=\"SUPER\" /><Nodo TipoID=\"P_LINGUAID\" Tipo=\"\" Valore=\"ITA\" /><Nodo TipoID=\"P_RUOLOID\" Tipo=\"\" Valore=\"137\" /><Nodo TipoID=\"P_MODULOID\" Tipo=\"\" Valore=\"10\" /><Nodo TipoID=\"P_PROFILOID\" Tipo=\"\" Valore=\"zjE3Oasv67LyVJAh\" /><Nodo TipoID=\"P_LIVELLOID\" Tipo=\"\" Valore=\"80\" /><Nodo TipoID=\"P_STRATEGICITAID\" Tipo=\"\" Valore=\"1\" /><Nodo TipoID=\"P_REPARTOID\" Tipo=\"\" Valore=\"1\" /><Nodo TipoID=\"P_SOCID\" Tipo=\"\" Valore=\"01\" /><Nodo TipoID=\"P_SEZIONEID\" Tipo=\"\" Valore=\"ZF01\" /><Nodo TipoID=\"PageAttivita\" Tipo=\"PageAttivita\" Valore=\"GESTIONE\" /><Nodo TipoID=\"EDW_PAG\" Tipo=\"EDW_PAG\" Valore=\"/NETASIU/SIUWEB/SIUWEB.REPORTS/REPORTCR/FORMS/LANCIOREPORTBILLING.ASPX\" /><Nodo TipoID=\"FORN_READONLY\" Tipo=\"FORN_READONLY\" Valore=\"False\" /><Nodo TipoID=\"KeyServerId\" Tipo=\"KeyServerId\" Valore=\"56\" /><Nodo TipoID=\"SchedKeyPF\" Tipo=\"SchedKeyPF\" Valore=\"/NETASIU/SIUWEB/SIUWEB.REPORTS/REPORTCR/POPUP/POPUPSTAMPAREPORT.ASPX\" /><Nodo TipoID=\"SchKeyClassAppId\" Tipo=\"SchKeyClassAppId\" Valore=\"58\" /><Nodo TipoID=\"SchedKeyAId\" Tipo=\"SchedKeyAId\" Valore=\"878\" /><Nodo TipoID=\"SchedKeyADesc\" Tipo=\"SchedKeyADesc\" Valore=\"POPUP STAMPA REPORT\" /><Nodo TipoID=\"SchedKeyGId\" Tipo=\"SchedKeyGId\" Valore=\"6\" /><Nodo TipoID=\"SchedKeyGCod\" Tipo=\"SchedKeyGCod\" Valore=\"REPORT\" /><Nodo TipoID=\"SchedKeyGDesc\" Tipo=\"SchedKeyGDesc\" Valore=\"GRUPPO SCHEDULAZIONE REPORT\" /><Nodo TipoID=\"KeyServerCode\" Tipo=\"KeyServerCode\" Valore=\"DEFAULTSERVER\" /><Nodo TipoID=\"KeyServerHost\" Tipo=\"KeyServerHost\" Valore=\"172.26.51.34\" /><Nodo TipoID=\"EDW_EPR\" Tipo=\"EDW_EPR\" Valore=\"zjE3Oasv67LyVJAh\" /></Contesto>"
    val netaSessionToken = "sessionToken"

    "The directive" should "pick up the Net@ deserialized execution context" in {
        val dispatcher = new AkkaInternalDispatcher(Some(path("") {
            getSerializedNetaExecutionContext { ctx =>
                get {
                    akka.http.scaladsl.server.Directives.complete(ctx)
                }
            }
        }), None)

        try {
            val result = dispatcher.ask(DispatchProperties(
                Some(netaContext),
                None,
                "GET".encode(),
                "/",
                None
            ))
            result.map(a => {
                val message = a.map(_.toChar).mkString
                assert(message === netaContext)
            })
        }
        catch {
            case ex: Exception => assert(false, ex.toString)
        }
    }

    "The directive" should "pick up the Net@ execution context" in {
        val dispatcher = new AkkaInternalDispatcher(Some(path("") {
            getNetaExecutionContext { ctx =>
                get {
                    akka.http.scaladsl.server.Directives.complete(ctx.get.operatorId)
                }
            }
        }), None)

        try {
            val result = dispatcher.ask(DispatchProperties(
                Some(netaContext),
                None,
                "GET".encode(),
                "/",
                None
            ))
            result.map(a => {
                val message = a.map(_.toChar).mkString
                assert(message === "SUPER")
            })
        }
        catch {
            case ex: Exception => assert(false, ex.toString)
        }
    }
}
