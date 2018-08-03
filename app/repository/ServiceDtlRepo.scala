package repository

import java.text.SimpleDateFormat
import java.util.Date

import anorm.SqlParser.{get, scalar, str}
import anorm._
import javax.inject.Inject
import org.joda.time.DateTime
import play.api.db.DBApi

import scala.concurrent.Future

case class ServiceDtl(id: Option[Int],
                      serviceGrpId: Int,
                      name: String,
                      content: String,
                      image: String,
                      deleted: Option[Boolean],
                      is_shown_home: Boolean,
                      createdDate: Option[Date],
                      updatedDate: Option[Date])

object ServiceDtl {
  implicit def toParameters: ToParameterList[ServiceDtl] =
    Macro.toParameters[ServiceDtl]
}

@javax.inject.Singleton
class ServiceDtlRepo @Inject()(dbapi: DBApi, serviceGrpRepo: ServiceGrpRepo)(implicit ec: DatabaseExecutionContext) {

  private val db = dbapi.database("default")

  private[repository] val rowParser = {
    get[Option[Int]]("service.id") ~
      get[Int]("service.parent_service_id") ~
      str("service.name") ~ str("service.content") ~
      str("service.image") ~
      get[Option[Boolean]]("service.is_deleted") ~
      get[Boolean]("service.is_shown_home") ~
      get[Option[Date]]("service.created_date") ~
      get[Option[Date]]("service.updated_date") map {
      case id ~ serviceGrpId ~ name ~ content ~ image ~ deleted ~ is_shown_home ~ createdDate ~ updatedDate
      => ServiceDtl(id, serviceGrpId, name, content, image, deleted, is_shown_home, createdDate, updatedDate)
    }
  }

  private val withServiceGrp = rowParser ~ (serviceGrpRepo.rowParser.?) map {
    case computer ~ company => computer -> company
  }

  def list(page: Int = 0, pageSize: Int = 10, orderBy: Int = 1, filter: String = "%"): Future[Page[(ServiceDtl, Option[ServiceGrp])]] = Future {
    val offset = pageSize * page
    db.withConnection { implicit connection =>
      val serviceDtls = SQL"select * from service left join service_group on service.parent_service_id = service_group.id where service.is_deleted = 0 and service.name like ${filter} order by ${orderBy} limit ${pageSize} offset ${offset}".as(withServiceGrp *)
      val totalRows = SQL"select count(*) from service where service.is_deleted = 0 and service.name like ${filter}".as(scalar[Long].single)
      Page(serviceDtls, page, offset, totalRows)
    }
  }(ec)

  def findShownOnHome(): Future[List[ServiceDtl]] = Future {
    db.withConnection { implicit connection =>
      val query: SimpleSql[Row] = SQL"select * from service where is_deleted = 0"
      query.as(rowParser *)
    }
  }(ec)

  def findByGrpId(id: Int): Future[List[ServiceDtl]] = Future {
    db.withConnection { implicit connection =>
      val query: SimpleSql[Row] = SQL"select * from service where parent_service_id = ${id} and is_deleted = 0"
      query.as(rowParser *)
    }
  }

  def findById(id: Long): Future[Option[ServiceDtl]] = Future {
    db.withConnection { implicit connection =>
      SQL"select * from service where id = $id and is_deleted = 0".as(rowParser.singleOpt)
    }
  }(ec)

  def delete(id: Int) = Future {
    db.withConnection { implicit connection =>
      SQL"update service set service.is_deleted=1 where id = ${id}".executeUpdate()
    }
  }(ec)

  def insert(service: ServiceDtl): Future[Option[Long]] = Future {
    val d = new SimpleDateFormat("yyyy-MM-dd HH:MM").format(new Date()).toString
    db.withConnection { implicit connection =>
      SQL("insert into service (parent_service_id, name, content, image, is_deleted, created_date, updated_date) values " +
        "({serviceGrpId}, {name}, {content}, {image}, {deleted}, {createdDate}, {updatedDate})")
        .on("serviceGrpId" -> service.serviceGrpId, "name" -> service.name, /*"url" -> "",*/ "content" -> service.content, "image" ->service.image,
          "deleted" -> 0, "createdDate" -> d, "updatedDate" -> d).executeInsert()
    }
  }(ec)

  def update(id: Int, service: ServiceDtl) = Future {
    val d = new SimpleDateFormat("yyyy-MM-dd HH:MM").format(new Date()).toString
    db.withConnection { implicit connection =>
      SQL(s"""update service set parent_service_id = {serviceGrpId}, name = {name}, content = {content}, image = {image}, updated_date = '${d}' where id = {id}""").bind(service.copy(id = Some(id)/* ensure */)).executeUpdate()
      // case class binding using ToParameterList,
      // note using SQL(..) but not SQL.. interpolation
    }
  }(ec)
}
