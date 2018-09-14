package eng.db.shared.webservices

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import eng.db.shared.base.NetaContext

object CustomDirectives {
    /**
      * This directive extracts the serialized context from the request.
      */
    val getNetaExecutionContext: Directive1[Option[NetaContext]] = extract(ctx => {
        ctx.request.headers.filter(h => h.name() == SessionValidators.netaContextSessionTokenKey) match {
            case contextHeader if contextHeader.length == 1 => Some(NetaContext.deserialize(contextHeader.head.value()))
            case _ => None
        }
    })

    /**
      * This directive extracts the serialized context from the request.
      */
    val getSerializedNetaExecutionContext: Directive1[Option[String]] = extract(ctx => {
        ctx.request.headers.filter(h => h.name() == SessionValidators.netaContextSessionTokenKey) match {
            case contextHeader if contextHeader.length == 1 => Some(contextHeader.head.value())
            case _ => None
        }
    })

    /**
      * This directive extracts the session token from the request.
      */
    val getNetaSessionToken: Directive1[Option[String]] = extract(ctx => {
        ctx.request.headers.filter(h => h.name() == SessionValidators.netaSessionTokenKey) match {
            case contextHeader if contextHeader.length == 1 => Some(contextHeader.head.value())
            case _ => None
        }
    })
}
