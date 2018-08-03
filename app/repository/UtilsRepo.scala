package repository

import java.util.Date

import anorm.SqlParser.{get, str}
import anorm._
import anorm.{Row, SQL, SimpleSql, ~}
import javax.inject.Inject
import org.joda.time.DateTime
import play.api.db.DBApi

import scala.concurrent.Future

case class Micellaneous(id: Option[Int],
                      section: String,
                      name: String,
                      content: String,
                      deleted: Option[Boolean],
                      createdDate: Option[Date],
                      updatedDate: Option[Date])

object Micellaneous {
  implicit def toParameters: ToParameterList[Micellaneous] =
    Macro.toParameters[Micellaneous]
}

@javax.inject.Singleton
class UtilsRepo @Inject()(dbapi: DBApi)(implicit ec: DatabaseExecutionContext) {

  private val db = dbapi.database("default")

  private[repository] val rowParser = {
    get[Option[Int]]("micellaneous.id") ~ str("micellaneous.section") ~ str("micellaneous.name") ~ str("micellaneous.content")  ~ get[Boolean]("micellaneous.is_deleted") ~ get[Option[Date]]("micellaneous.created_date") ~ get[Option[Date]]("micellaneous.updated_date") map {
      case id ~ section ~ name ~ content ~ deleted ~ createdDate ~ updatedDate
      => Micellaneous(id, section, name, content, deleted, createdDate, updatedDate)
    }
  }

  def getMessengerInfo: Future[List[Micellaneous]] = Future {
    db.withConnection { implicit connection =>
      val query: SimpleSql[Row] = SQL"select * from micellaneous where section = 'messenger' and is_deleted = 0"
      query.as(rowParser *)
    }
  }(ec)
}
