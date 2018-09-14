package eng.db.shared.messageBroker

import scala.concurrent.Future

trait Dispatcher {
    def ask(properties: DispatchProperties): Future[Array[Byte]]
    def dispatch(properties: DispatchProperties): Future[Any]
}
