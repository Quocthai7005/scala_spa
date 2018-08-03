package repository

import java.text.SimpleDateFormat
import java.util.Date

import javax.inject.Inject

import scala.util.{Failure, Success}
import play.api.db.DBApi
import anorm._
import anorm.SqlParser.{get, scalar, str}

import scala.concurrent.Future

case class ServiceGrp(id: Option[Int],
                      name: String,
                      content: String,
                      image: String,
                      deleted: Option[Boolean],
                      createdDate: Option[Date],
                      updatedDate: Option[Date])



@javax.inject.Singleton
class ServiceGrpRepo @Inject()(dbapi: DBApi)(implicit ec: DatabaseExecutionContext) {

  private val db = dbapi.database("default")

  private[repository] val rowParser = {
    get[Option[Int]]("service_group.id") ~
      str("service_group.name") ~
      str("service_group.content") ~
      str("service_group.image") ~
      get[Option[Boolean]]("service_group.is_deleted") ~
      get[Option[Date]]("service_group.created_date") ~
      get[Option[Date]]("service_group.updated_date") map {
      case id ~ name ~ content ~ image ~ deleted ~ createdDate ~ updatedDate
      => ServiceGrp(id, name, content, image, deleted, createdDate, updatedDate)
    }
  }

  /**
    * Construct the Seq[(String,String)] needed to fill a select options set.
    *
    * Uses `SqlQueryResult.fold` from Anorm streaming,
    * to accumulate the rows as an options list.
    */
  def options: Future[Seq[(String,String)]] = Future(db.withConnection { implicit connection =>
    SQL"select * from service_group where service_group.is_deleted=0".
      fold(Seq.empty[(String, String)]) { (acc, row) => // Anorm streaming
        row.as(rowParser) match {
          case Failure(parseErr) => {
            println(s"Fails to parse $row: $parseErr")
            acc
          }
          case Success(ServiceGrp(Some(id), name, content, image, Some(is_deleted), Some(created_date), Some(updated_date))) =>
            (id.toString -> name) +: acc
          case Success(ServiceGrp(None, _, _, _, None, None, None)) => acc
        }
      }
  }).flatMap {
    case Left(err :: _) => Future.failed(err)
    case Left(_) => Future(Seq.empty)
    case Right(acc) => Future.successful(acc.reverse)
  }

  def findAll(): Future[List[ServiceGrp]] = Future {
    db.withConnection { implicit connection =>
      val query: SimpleSql[Row] = SQL"select * from service_group where is_deleted = 0"
      query.as(rowParser *)
    }
  }(ec)

  def list(page: Int = 0, pageSize: Int = 10, orderBy: Int = 1, filter: String = "%"): Future[Page[ServiceGrp]] = Future {
    val offset = pageSize * page
    db.withConnection { implicit connection =>
      val serviceGrps = SQL"select * from service_group where service_group.is_deleted = 0 and service_group.name like ${filter} order by ${orderBy} limit ${pageSize} offset ${offset}".as(rowParser *)
      val totalRows = SQL"select count(*) from service_group where service_group.is_deleted = 0 and service_group.name like ${filter}".as(scalar[Long].single)
      Page(serviceGrps, page, offset, totalRows)
    }
  }(ec)

  def delete(id: Int) = Future {
    db.withConnection { implicit connection =>
      SQL"update service_group set service_group.is_deleted=1 where id = ${id} and (select count(*) from service where service.parent_service_id = ${id}) = 0".executeUpdate()
    }
  }(ec)

  def findById(id: Long): Future[Option[ServiceGrp]] = Future {
    db.withConnection { implicit connection =>
      SQL"select * from service_group where id = $id".as(rowParser.singleOpt)
    }
  }(ec)

  def insert(serviceGrp: ServiceGrp): Future[Option[Long]] = Future {
    val d = new SimpleDateFormat("yyyy-MM-dd HH:MM").format(new Date()).toString
    db.withConnection { implicit connection =>
      SQL("insert into service_group (name, content, image, is_deleted, created_date, updated_date) values " +
        "({name}, {content}, {image}, {deleted}, {createdDate}, {updatedDate})")
        .on("name" -> serviceGrp.name, "content" -> serviceGrp.content, "image" ->serviceGrp.image,
          "deleted" -> 0, "createdDate" -> d, "updatedDate" -> d).executeInsert()
    }
  }(ec)

  def update(id: Int, serviceGrp: ServiceGrp) = Future {
    val d = new SimpleDateFormat("yyyy-MM-dd HH:MM").format(new Date()).toString
    db.withConnection { implicit connection =>
      SQL(s"""update service_group set name = {name}, content = {content}, image = {image}, updated_date = '${d}' where id = {id}""")/*.bind(post.copy(id = Some(id)/* ensure */))*/.executeUpdate()
      // case class binding using ToParameterList,
      // note using SQL(..) but not SQL.. interpolation
    }
  }(ec)
}
