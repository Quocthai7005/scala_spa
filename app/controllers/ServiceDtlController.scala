package controllers

import javax.inject.{Inject, Singleton}
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc.{AbstractController, ControllerComponents, MessagesAbstractController, MessagesControllerComponents}
import repository.{Post, ServiceDtl, ServiceDtlRepo, ServiceGrpRepo}

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration.Duration

@Singleton
class ServiceDtlController @Inject()(cc: MessagesControllerComponents,
                                     serviceGrpService: ServiceGrpRepo,
                                     serviceDtlService: ServiceDtlRepo)(implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {

  val Home = Redirect(routes.ServiceDtlController.aServiceDtlList(0, 2, ""))

  val serviceDtlForm = Form(
    mapping(
      "id" -> ignored(None: Option[Int]),
      "parent_service_id" -> number,
      "name" -> nonEmptyText,
      "content" -> text(maxLength = 150000),
      "image" -> text,
      "is_deleted" -> ignored(None: Option[Boolean]),
      "is_shown_home"-> boolean,
      "created_date" -> optional(date("yyyy-MM-dd")),
      "updated_date" -> optional(date("yyyy-MM-dd")),
    )(ServiceDtl.apply)(ServiceDtl.unapply)
  )

  def serviceDtlList(id: Int) = Action.async { implicit request =>
    serviceDtlService.findByGrpId(id).map { serviceDtl =>
      Ok(views.html.common.serviceDtlList(serviceDtl))
    }
  }

  def serviceDtl(id: Int) = Action.async { implicit request =>
    val listOfFutures = serviceDtlService.findById(id).map { serviceDtl =>
      serviceDtlService.findByGrpId(serviceDtl.get.serviceGrpId).map { otherService =>
        Ok(views.html.common.serviceDtl(serviceDtl, otherService))
      }
    }
    Await.result(listOfFutures, Duration.Inf)
  }

  def aServiceDtlList(page: Int, orderBy: Int, filter: String) = Action.async { implicit request =>
    serviceDtlService.list(page = page, orderBy = orderBy, filter = ("%" + filter + "%")).map { page =>
      Ok(views.html.admin.aServiceDtlList(page, orderBy, filter))
    }
  }

  def aServiceDtlAdd() = Action.async { implicit request =>
    serviceGrpService.options.map { options =>
      Ok(views.html.admin.aServiceDtlAdd(serviceDtlForm, options))
    }
  }

  def aServiceDtlEdit(id: Int) = Action.async { implicit request =>
    serviceDtlService.findById(id).flatMap {
      case Some(serviceDtl) =>
        serviceGrpService.options.map { options =>
          Ok(views.html.admin.aServiceDtlEdit(id, serviceDtlForm.fill(serviceDtl), options))
        }
      case other =>
        Future.successful(NotFound)
    }
  }

  def delete(id: Int) = Action.async {
    serviceDtlService.delete(id).map { group =>
      group match {
        case 0 => Ok("fail")
        case other => Ok("success")
      }
    }
  }

  def save = Action.async { implicit request =>
    serviceDtlForm.bindFromRequest.fold(
      formWithErrors => serviceGrpService.options.map { options =>
        BadRequest(views.html.admin.aServiceDtlAdd(formWithErrors, options))
      },
      service => {
        serviceDtlService.insert(service).map { _ =>
          Home.flashing("success" -> "service %s has been created".format(service.name))
        }
      }
    )
  }

  def update(id: Int) = Action.async { implicit request =>
    serviceDtlForm.bindFromRequest.fold(
      formWithErrors => {
        serviceGrpService.options.map { options =>
          BadRequest(views.html.admin.aServiceDtlEdit(id, formWithErrors, options))
        }
      },
      service => {
        serviceDtlService.update(id, service).map { _ =>
          Home.flashing("success" -> "Post %s has been updated".format(service.name))
        }
      }
    )
  }
}
