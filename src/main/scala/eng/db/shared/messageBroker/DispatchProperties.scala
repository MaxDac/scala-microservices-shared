package eng.db.shared.messageBroker

import akka.http.scaladsl.client._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.RawHeader
import akka.util.ByteString
import eng.db.shared.base.JsonConvert
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

object DispatchPropertiesJsonProtocol extends SprayJsonSupport with DefaultJsonProtocol {

    implicit val dispatchPropertiesFormat: RootJsonFormat[DispatchProperties] = jsonFormat6(DispatchProperties.apply)
    
    implicit class IntVerbMarshaller(val verb: Int) {
        def decode(): String = this.verb match {
            case 0 => "GET"
            case 1 => "POST"
            case 2 => "PUT"
            case 3 => "DELETE"
            case _ => throw new UnsupportedOperationException(s"${this.verb} does not represent a valid HTTP method.")
        }
    }
    
    implicit class StringVerbMarshaller(val verb: String) {
        def encode(): Int = this.verb match {
            case "GET" => 0
            case "POST" => 1
            case "PUT" => 2
            case "DELETE" => 3
            case _ => throw new UnsupportedOperationException(s"${this.verb} does not represent a valid HTTP method.")
        }
    }
}

object DispatchProperties {
    import DispatchPropertiesJsonProtocol._
    def `for`(
                 context: Option[String],
                 exchangeName: Option[String],
                 Verb: String,
                 url: String,
                 body: Option[String],
                 responseQueue: Option[String] = None
             ): DispatchProperties = DispatchProperties(context, exchangeName, Verb.encode(), url, body, responseQueue)
}
final case class DispatchProperties(
                                       context: Option[String],
                                       exchangeName: Option[String],
                                       Verb: Int,
                                       url: String,
                                       body: Option[String],
                                       responseQueue: Option[String] = None
                             ) {
    import DispatchPropertiesJsonProtocol._

    def prepareToDispatch(): Array[Byte] = {
        val serialized = JsonConvert(this)(dispatchPropertiesFormat).asJson()
        serialized.toCharArray.map(_.toByte)
    }

    private def getSessionHeader: HttpHeader = {
        RawHeader("contesto", this.context.getOrElse(""))
    }
    
    def asHttpRequest(): HttpRequest = {
        val request = this.Verb.decode() match {
            case "GET" => RequestBuilding.Get(this.url)
            case "POST" => RequestBuilding.Post(this.url).withEntity(ContentTypes.`application/json`, ByteString(this.body.getOrElse("")))
            case "PUT" => RequestBuilding.Put(this.url).withEntity(ContentTypes.`application/json`, ByteString(this.body.getOrElse("")))
            case "DELETE" => RequestBuilding.Delete(this.url).withEntity(ContentTypes.`application/json`, ByteString(this.body.getOrElse("")))
            case _ => throw new UnsupportedOperationException(s"${this.Verb} does not represent a valid HTTP method.")
        }

        request.withHeaders(this.getSessionHeader)
//        HttpRequest(this.httpMethod(),
//            uri = this.url,
//            entity = ByteString(this.body),
//            headers = this.getHeaders())
    }
}
