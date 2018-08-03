package repository

import java.text.SimpleDateFormat

import anorm.SqlParser.{get, scalar, str}
import anorm._
import controllers.routes
import javax.inject.Inject
import java.util.Date

import play.api.db.DBApi

import scala.concurrent.Future


case class Post(id: Option[Int],
                      name: String,
                      content: String,
                      image: String,
                      /*url: String,*/
                      serviceId: Int,
                      deleted: Option[Boolean],
                      createdDate: Option[Date],
                      updatedDate: Option[Date])

object Post {
  implicit def toParameters: ToParameterList[Post] =
    Macro.toParameters[Post]
}

case class Page[A](items: Seq[A], page: Int, offset: Long, total: Long) {
  lazy val prev = Option(page - 1).filter(_ >= 0)
  lazy val next = Option(page + 1).filter(_ => (offset + items.size) < total)
}

@javax.inject.Singleton
class PostRepo @Inject()(dbapi: DBApi)(implicit ec: DatabaseExecutionContext) {

  private val db = dbapi.database("default")

  private[repository] val rowParser = {
    get[Option[Int]]("post.id") ~
      str("post.name") ~
      str("post.content") ~
      str("post.image") ~
      /*str("post.url") ~*/
      get[Int]("post.service_id") ~
      get[Option[Boolean]]("post.is_deleted") ~
      get[Option[Date]]("post.created_date") ~
      get[Option[Date]]("post.updated_date") map {
      case id ~ name ~ content ~ image /*~ url*/ ~ service_id ~ deleted ~ createdDate ~ updatedDate
      => Post(id, name, content, image, /*url,*/ service_id, deleted, createdDate, updatedDate)
    }
  }

  def list(page: Int = 0, pageSize: Int = 10, orderBy: Int = 1, filter: String = "%"): Future[Page[Post]] = Future {
    val offset = pageSize * page
    db.withConnection { implicit connection =>
      val posts = SQL"select * from post where post.is_deleted = 0 and post.name like ${filter} order by ${orderBy} limit ${pageSize} offset ${offset}".as(rowParser *)
      val totalRows = SQL"select count(*) from post where post.is_deleted = 0 and post.name like ${filter}".as(scalar[Long].single)
      Page(posts, page, offset, totalRows)
    }
  }(ec)

  def findLatest5(): Future[List[Post]] = Future {
    db.withConnection { implicit connection =>
      val query: SimpleSql[Row] = SQL"select * from post where is_deleted = 0 limit 5"
      query.as(rowParser *)
    }
  }

  def findById(id: Long): Future[Option[Post]] = Future {
    db.withConnection { implicit connection =>
      SQL"select * from post where id = $id".as(rowParser.singleOpt)
    }
  }(ec)

  def delete(id: Int) = Future {
    db.withConnection { implicit connection =>
      SQL"update post set post.is_deleted=1 where id = ${id}".executeUpdate()
    }
  }(ec)

  def insert(post: Post): Future[Option[Long]] = Future {
    val d = new SimpleDateFormat("yyyy-MM-dd HH:MM").format(new Date()).toString
    db.withConnection { implicit connection =>
      SQL("insert into post (name, content, image, service_id, is_deleted, created_date, updated_date) values " +
        "({name}, {content}, {image}, {serviceId}, {deleted}, {createdDate}, {updatedDate})")
        .on("name" -> post.name, /*"url" -> "",*/ "content" -> post.content, "image" ->post.image, "serviceId" -> post.serviceId,
        "deleted" -> 0, "createdDate" -> d, "updatedDate" -> d).executeInsert()
    }
  }(ec)

  def update(id: Int, post: Post) = Future {
    val d = new SimpleDateFormat("yyyy-MM-dd HH:MM").format(new Date()).toString
    db.withConnection { implicit connection =>
      SQL(s"""update post set name = {name}, content = {content}, image = {image}, service_id = {serviceId}, updated_date = '${d}' where id = {id}""").bind(post.copy(id = Some(id)/* ensure */)).executeUpdate()
      // case class binding using ToParameterList,
      // note using SQL(..) but not SQL.. interpolation
    }
  }(ec)
}
