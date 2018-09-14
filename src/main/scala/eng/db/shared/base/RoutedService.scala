package eng.db.shared.base

/**
  * This trait identifies a service that applies a routing logic.
  */
trait RoutedService extends Disposable {

    /**
      * This method starts the instance.
      */
    def start(): Unit
}
