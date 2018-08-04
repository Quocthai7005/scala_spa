package controllers

import javax.inject.{Inject, Singleton}
import play.api.data.Form
import play.api.data.Forms._
import play.api.data._
import play.api.mvc.{MessagesAbstractController, MessagesControllerComponents}
import repository.{Messenger, MessengerRepo, Post, UtilsRepo}
import views._
import play.api.i18n.Messages.Implicits._

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration.Duration


@Singleton
class MessengerController  @Inject()(cc: MessagesControllerComponents,
                                      utilsService: UtilsRepo,
                                     messengerService: MessengerRepo)(implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {

  val Home = Redirect(routes.MessengerController.aMessengerEdit())

  val messengerForm = Form (
    mapping(
    "appId" -> nonEmptyText,
    "pageId" -> nonEmptyText,
    "autoLogAppEvents" -> boolean,
    "xfbml" -> boolean,
    "version" -> nonEmptyText,
    "minimized" -> boolean
    )(Messenger.apply)(Messenger.unapply)
  )

  def aMessengerEdit = Action.async { implicit request =>
    messengerService.findConfigList.flatMap {
      case messenger => Future.successful(Ok(views.html.admin.aMessengerEdit(messengerForm.fill(messengerService.toMessenger(messenger)))))
      case other =>Future.successful(NotFound)
    }
  }

  def update() = Action.async { implicit request =>
    messengerForm.bindFromRequest.fold(
      formWithErrors => {
          Future.successful(BadRequest(views.html.admin.aMessengerEdit(formWithErrors)))
      },
      messenger => {
        messengerService.update(messenger).map { _ =>
          Home.flashing("success" -> "Messenger %s has been updated".format(messenger.toString))
        }
      }
    )
  }
}
