package eng.db.shared.webservices

import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.ExceptionHandler
import eng.db.shared.base.BackEndError

object DefaultExceptionHandlers {
    implicit val defaultExceptionHandler: ExceptionHandler = ExceptionHandler {
        case ex: Exception =>
            //            println(ex.toString)
            ex.printStackTrace()
            val errorClass = BackEndError.error("1", s"An exception occoured in the service: ${ex.toString}")
            complete(HttpResponse(entity = errorClass.serialized()))
    }
}
