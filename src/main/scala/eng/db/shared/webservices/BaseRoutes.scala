package eng.db.shared.webservices

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server._
import Directives._
import eng.db.shared.base.AppConfiguration

import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Future}

object BaseRoutes {
    import SessionValidators._

    private def executeValidators(validator: ListBuffer[SessionValidator], requestContext: RequestContext)(implicit ec: ExecutionContext): Future[SessionToken] = {
        if (validator.isEmpty) {
            Future.successful(SessionToken.invalid())
        }
        else {
            validator.head.validateSession(requestContext).flatMap {
                case s@SessionToken(_, _, valid) if valid => Future.successful(s)
                case _ =>
                    validator.remove(0)
                    executeValidators(validator, requestContext)(ec)
            }
        }
    }

    private def validateToken(requestContext: RequestContext)(implicit ec: ExecutionContext): Future[SessionToken] = {
        val validators = ListBuffer[SessionValidator](
            SerializedContextSessionValidator(),
            SessionTokenSessionValidator()
        )

        this.executeValidators(validators, requestContext)(ec)
    }

    def sessionValidator()(implicit ec: ExecutionContext): Route = { ctx =>
        this.validateToken(ctx).flatMap { s =>
            if (s.valid) ctx.reject()
            else ctx.complete(StatusCodes.Unauthorized)
        }
    }

    def healthChecker()(implicit ec: ExecutionContext): Route = path("_health") {
        get {
            complete(s"Service is working. Current service version: ${AppConfiguration.version}")
        }
    }

}
