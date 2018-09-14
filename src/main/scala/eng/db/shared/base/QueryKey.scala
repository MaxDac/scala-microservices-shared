package eng.db.shared.base

import java.util.Date

/**
  * This object is used as a key for objects in queries.
  * @param id The object id.
  * @param code The object code.
  * @param initialDate The insert date.
  * @param version The object version.
  */
case class QueryKey(
                       var id: String = "",
                       var code: Option[String] = None,
                       var initialDate: Option[Date] = None,
                       var version: Option[Int] = None
                   )
