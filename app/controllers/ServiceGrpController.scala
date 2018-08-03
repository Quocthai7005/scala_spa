package controllers

import play.api.libs.json._
import javax.inject.{Inject, Singleton}
import play.api.data.Form
import play.api.data.Forms._
import play.api.http.Writeable
import play.api.libs.json.Writes
import play.api.mvc.{AbstractController, ControllerComponents, MessagesAbstractController, MessagesControllerComponents}
import repository.{Post, ServiceDtlRepo, ServiceGrp, ServiceGrpRepo}
import play.api.libs.json.{JsNull, JsString, JsValue, Json}

import scala.concurrent.{ExecutionContext, Future, TimeoutException}



@Singleton
class ServiceGrpController @Inject()(cc: MessagesControllerComponents,
                                     serviceGrpService: ServiceGrpRepo,
                                     serviceDtlService: ServiceDtlRepo)(implicit ec: ExecutionContext) extends MessagesAbstractController(cc){

  val Home = Redirect(routes.ServiceGrpController.aServiceGrpList(0, 2, ""))

  val serviceGrpForm = Form(
    mapping(
      "id" -> ignored(None: Option[Int]),
      "name" -> nonEmptyText,
      "content" -> text(maxLength = 150000),
      "image" -> text,
      "is_deleted" -> ignored(None: Option[Boolean]),
      "created_date" -> optional(date("yyyy-MM-dd")),
      "updated_date" -> optional(date("yyyy-MM-dd")),
    )(ServiceGrp.apply)(ServiceGrp.unapply)
  )

  implicit val implicitFooWrites = new Writes[ServiceGrp] {
    def writes(s: ServiceGrp): JsValue = {
      Json.obj(
        "id" -> s.id,
        "name" -> s.name
      )
    }
  }

  def serviceGrp = Action.async { implicit request =>
    sendResult(serviceGrpService.findAll())
  }

  private def sendResult[T](result: Future[T])(implicit writeable: Writes[T]) = {
    result.map {
      items => {
        Ok(Json.toJson(items))
      }
    }.recover {
      case t:TimeoutException => InternalServerError("Api Timed out")
      case t: Throwable => InternalServerError(s"Exception in the api $t")
    }
  }

  def aServiceGrpList(page: Int, orderBy: Int, filter: String) = Action.async { implicit request =>
    serviceGrpService.list(page = page, orderBy = orderBy, filter = ("%" + filter + "%")).map { page =>
      Ok(views.html.admin.aServiceGrpList(page, orderBy, filter))
    }
  }

  def aServiceGrpAdd = Action { implicit request =>
    Ok(views.html.admin.aServiceGrpAdd(serviceGrpForm))
  }

  def aServiceGrpEdit(id: Int) = Action.async { implicit request =>

    serviceGrpService.findById(id).flatMap {
      case Some(serviceGrp) => Future.successful(Ok(views.html.admin.aServiceGrpEdit(id, serviceGrpForm.fill(serviceGrp))))
      case other => Future.successful(NotFound)
    }
  }

  def delete(id: Int) = Action.async { implicit request =>
    serviceGrpService.delete(id).map { group =>
      group match {
        case 0 => Ok("fail")
        case other => Ok("success")
      }
    }
  }

  def save = Action.async { implicit request =>
    serviceGrpForm.bindFromRequest.fold(
      formWithErrors => Future.successful(BadRequest(views.html.admin.aServiceGrpAdd(formWithErrors)))
      ,
      serviceGrp => {
        serviceGrpService.insert(serviceGrp).map { _ =>
          Home.flashing("success" -> "Service group %s has been created".format(serviceGrp.name))
        }
      }
    )
  }

  def update(id: Int) = Action.async { implicit request =>
    serviceGrpForm.bindFromRequest.fold(
      formWithErrors => Future.successful(BadRequest(views.html.admin.aServiceGrpAdd(formWithErrors)))
      ,
      serviceGrp => {
        serviceGrpService.update(id, serviceGrp).map { _ =>
          Home.flashing("success" -> "Service group %s has been created".format(serviceGrp.name))
        }
      }
    )
  }
}
