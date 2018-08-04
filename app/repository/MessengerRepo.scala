package repository

import java.text.SimpleDateFormat
import java.util.Date

import anorm.{Row, SQL, SimpleSql, ~, _}
import anorm.SqlParser.{get, scalar, str}
import javax.inject.Inject
import org.omg.CosNaming.NamingContextPackage.NotFound
import play.api.db.DBApi

import scala.concurrent.Future
import scala.util.Failure


case class Messenger(appId: String,
                     pageId: String,
                     autoLogAppEvents: Boolean,
                     xfbml: Boolean,
                     version: String,
                     minimized: Boolean)

object Messenger {
  implicit def toParameters: ToParameterList[Messenger] =
    Macro.toParameters[Messenger]
}

@javax.inject.Singleton
class MessengerRepo @Inject()(dbapi: DBApi)(implicit ec: DatabaseExecutionContext) {

  private val db = dbapi.database("default")

  private[repository] val rowParser = {
    get[Option[Int]]("micellaneous.id") ~
      get[String]("micellaneous.section") ~
      get[String]("micellaneous.name") ~
      get[String]("micellaneous.content") ~
      get[Option[Boolean]]("micellaneous.is_deleted") ~
      get[Option[Date]]("created_date") ~
      get[Option[Date]]("updated_date") map {
      case id ~ section ~ name ~ content ~ deleted ~ createdDate ~ updatedDate
        => Micellaneous(id, section, name, content, deleted, createdDate, updatedDate)
    }

  }

  def findConfigList: Future[List[Micellaneous]] = Future {
    db.withConnection { implicit connection =>
      val query: SimpleSql[Row] = SQL"select * from micellaneous where section = 'messenger'"
      query.as(rowParser *)
    }
  }

  def toMessenger(Configs: List[Micellaneous]): Messenger = {
    var appId = ""
    var pageId = ""
    var autoLogAppEvents = false
    var xfbml = false
    var version = ""
    var minimized = false
    Configs.map { c =>
      c.name match {
        case "appId" => appId = c.content
        case "pageId" => pageId = c.content
        case "autoLogAppEvents" => autoLogAppEvents = c.content.toBoolean
        case "xfbml" => xfbml = c.content.toBoolean
        case "version" => version = c.content
        case "minimized" => minimized = c.content.toBoolean
      }
    }
    Messenger(appId, pageId, autoLogAppEvents, xfbml, version, minimized)
  }

  def update(messenger: Messenger) = Future {
    val d = new SimpleDateFormat("yyyy-MM-dd HH:MM").format(new Date()).toString
    db.withConnection { implicit connection =>
      SQL(s"""update micellaneous set content = '${messenger.appId}', updated_date = '${d}' where name = 'appId'""").executeUpdate()
      SQL(s"""update micellaneous set content = '${messenger.pageId}', updated_date = '${d}' where name = 'pageId'""").executeUpdate()
      SQL(s"""update micellaneous set content = '${messenger.autoLogAppEvents.toString}', updated_date = '${d}' where name = 'autoLogAppEvents'""").executeUpdate()
      SQL(s"""update micellaneous set content = '${messenger.xfbml.toString}', updated_date = '${d}' where name = 'xfbml'""").executeUpdate()
      SQL(s"""update micellaneous set content = '${messenger.version}', updated_date = '${d}' where name = 'version'""").executeUpdate()
      SQL(s"""update micellaneous set content = '${messenger.minimized.toString}', updated_date = '${d}' where name = 'minimized'""").executeUpdate()
    }
  }(ec)
}
