package controllers

import javax.inject.{Inject, Singleton}
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc.{MessagesAbstractController, MessagesControllerComponents}
import repository.{UserRepo, MessengerRepo, User, UtilsRepo}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserController @Inject()(cc: MessagesControllerComponents,
                               userService: UserRepo)(implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {

  val Home = Redirect(routes.UserController.aUserEdit())

  val userForm = Form (
    mapping(
      /*"username" -> text,*/
      "oldPassword" -> nonEmptyText(minLength = 5, maxLength = 30),
      "newPassword" -> nonEmptyText(minLength = 5, maxLength = 30),
      "newConfirmPassword" -> nonEmptyText(minLength = 5, maxLength = 30),
      "deleted" -> ignored(None: Option[Boolean])
    )(User.apply)(User.unapply)
  )

  def update() = Action.async { implicit request =>

    val username = "thai"
    userForm.bindFromRequest.fold(
      formWithErrors => {
        Future.successful(BadRequest(views.html.admin.aUserEdit(formWithErrors)))
      },
      user => {
        userService.update(user, username).map { _ =>
          Home.flashing("success" -> "User %s has been updated".format(user.toString))
        }
      }
    )
  }

  def aUserEdit = Action.async { implicit request =>
    Future.successful(Ok(views.html.admin.aUserEdit(userForm)))
  }

  def login = Action.async { implicit request =>
      Future.successful(Ok(views.html.admin.login()))
  }
}
