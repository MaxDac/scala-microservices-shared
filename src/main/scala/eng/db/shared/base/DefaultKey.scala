package eng.db.shared.base

import java.util.Date

/**
  * The Net@ default objects key.
  * @param id The object id.
  * @param code The object code.
  * @param initialDate The creation date.
  * @param version The object version.
  */
case class DefaultKey(
                         id: String = "",
                         code: Option[String] = None,
                         initialDate: Option[Date] = None,
                         version: Option[Int] = None
                     )
