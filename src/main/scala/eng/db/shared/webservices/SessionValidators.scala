package eng.db.shared.webservices

import akka.http.scaladsl.server.RequestContext
import eng.db.shared.base.AppConfiguration

import scala.concurrent.{ExecutionContext, Future}

object SessionValidators {

    object SessionToken {
        def invalid(): SessionToken = SessionToken()
    }

    val netaContextSessionTokenKey = "contesto"
    val netaSessionTokenKey = "session-token"

    sealed case class SessionToken(
                                      token: String = "",
                                      serializedContext: Option[String] = None,
                                      valid: Boolean = false
                                  )

    sealed trait SessionValidator {
        def validateSession(requestContext: RequestContext)(implicit ec: ExecutionContext): Future[SessionToken]
    }

    object SerializedContextSessionValidator {
        def apply(): SerializedContextSessionValidator = new SerializedContextSessionValidator()
    }
    sealed class SerializedContextSessionValidator extends SessionValidator {
        def validateSession(requestContext: RequestContext)(implicit ec: ExecutionContext): Future[SessionToken] = Future {
            val executionContextHeader = requestContext.request.headers.filter(h => h.name.toLowerCase == netaContextSessionTokenKey)
            if (!(executionContextHeader == Nil || executionContextHeader.length != 1 || executionContextHeader.head.value.isEmpty)) {
                SessionToken(
                    serializedContext = Some(executionContextHeader.head.value),
                    valid = true
                )
            }
            else {
                SessionToken(
                )
            }
        }
    }

    object SessionTokenSessionValidator {
        def apply(): SessionTokenSessionValidator = new SessionTokenSessionValidator()
    }
    sealed class SessionTokenSessionValidator extends SessionValidator {
        def validateSession(requestContext: RequestContext)(implicit ec: ExecutionContext): Future[SessionToken] = Future {
            // TODO - implement logic for session token recognition
            val configuredSessionToken = AppConfiguration.debugSessionToken
            val tokenHeader = requestContext.request.headers.filter(h => h.name == netaSessionTokenKey)

            if (configuredSessionToken.isEmpty) SessionToken()
            if (tokenHeader == Nil || tokenHeader.length != 1 || tokenHeader.head.value.isEmpty) SessionToken()
            else {
                SessionToken(
                    token = tokenHeader.head.value,
                    valid = tokenHeader.head.value == configuredSessionToken
                )
            }
        }
    }

}
