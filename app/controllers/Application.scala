package controllers

import javax.inject.{Inject, Singleton}
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.{JsValue, Json, Writes}
import play.api.mvc.{AbstractController, ControllerComponents}
import play.api.routing.JavaScriptReverseRouter
import repository._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class Application @Inject()(cc: ControllerComponents,
                            utilsService: UtilsRepo)(implicit ec: ExecutionContext) extends AbstractController(cc) {

  implicit val implicitFooWrites = new Writes[Micellaneous] {
    def writes(s: Micellaneous): JsValue = {
      Json.obj(
        "name" -> s.name,
        "content" -> s.content
      )
    }
  }

  val fmForm = Form(
    mapping(
      "id" -> ignored(None: Option[Int]),
      "section" -> nonEmptyText,
      "name" -> nonEmptyText,
      "content" -> text(maxLength = 150000),
      "is_deleted" -> ignored(None: Option[Boolean]),
      "created_date" -> optional(date("yyyy-MM-dd")),
      "updated_date" -> optional(date("yyyy-MM-dd")),
    )(Micellaneous.apply)(Micellaneous.unapply)
  )

  def javascriptRoutes = Action { implicit request =>
    Ok(
      JavaScriptReverseRouter("jsRoutes")(
        routes.javascript.ServiceGrpController.serviceGrp,
        routes.javascript.Application.messenger,
        routes.javascript.PostController.delete,
        routes.javascript.ServiceGrpController.delete
      )
    ).as("text/javascript")
  }

  def messenger = Action.async { implicit request =>
    utilsService.getMessengerInfo.map { info =>
      Ok(Json.toJson(info))
    }
  }
}
