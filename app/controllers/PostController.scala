package controllers

import javax.inject.{Inject, Singleton}
import play.api.data.Form
import play.api.data.Forms._
import play.api.data._
import play.api.mvc.{AbstractController, ControllerComponents, MessagesAbstractController, MessagesControllerComponents}
import repository.{Post, PostRepo, ServiceGrpRepo}
import views._
import play.api.i18n.Messages.Implicits._

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration.Duration

@Singleton
class PostController @Inject()(cc: MessagesControllerComponents,
                               postService: PostRepo,
                               serviceGrpService: ServiceGrpRepo)(implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {

  val Home = Redirect(routes.PostController.aPostList(0, 2, ""))

  val postForm = Form(
    mapping(
      "id" -> ignored(None: Option[Int]),
      "name" -> nonEmptyText,
      "content" -> text(maxLength = 150000),
      "image" -> text,
      /*"url" -> nonEmptyText,*/
      "service_id" -> number,
      "is_deleted" -> ignored(None: Option[Boolean]),
      "created_date" -> optional(date("yyyy-MM-dd")),
      "updated_date" -> optional(date("yyyy-MM-dd")),
    )(Post.apply)(Post.unapply)
  )

  def post(id: Int) = Action.async { implicit request =>
    val listOfFutures = postService.findById(id).map { post =>
      postService.findLatest5().map { otherPosts =>
        Ok(views.html.common.post(post, otherPosts))
      }
    }
    Await.result(listOfFutures, Duration.Inf)
  }

  def postList(page: Int, orderBy: Int, filter: String)  = Action.async { implicit request =>
    postService.list(page = page, orderBy = orderBy, filter = ("%" + filter + "%")).map { page =>
      Ok(views.html.common.postList(page, orderBy, filter))
    }
  }

  def aPostList(page: Int, orderBy: Int, filter: String)  = Action.async { implicit request =>
    postService.list(page = page, orderBy = orderBy, filter = ("%" + filter + "%")).map { page =>
      Ok(views.html.admin.aPostList(page, orderBy, filter))
    }
  }

  def aPostEdit(id: Int) = Action.async { implicit request =>
    postService.findById(id).flatMap {
      case Some(post) =>
        serviceGrpService.options.map { options =>
          Ok(views.html.admin.aPostEdit(id, postForm.fill(post), options))
        }
      case other =>
        Future.successful(NotFound)
    }
  }

  def update(id: Int) = Action.async { implicit request =>
    postForm.bindFromRequest.fold(
      formWithErrors => {
        serviceGrpService.options.map { options =>
          BadRequest(views.html.admin.aPostEdit(id, formWithErrors, options))
        }
      },
      post => {
        postService.update(id, post).map { _ =>
          Home.flashing("success" -> "Post %s has been updated".format(post.name))
        }
      }
    )
  }

  def delete(id: Int) = Action.async {
    postService.delete(id).map { post =>
      post match {
        case 0 => Ok("fail")
        case other => Ok("success")
      }
    }
  }

  def aPostAdd() = Action.async { implicit request =>
    serviceGrpService.options.map { options =>
      Ok(views.html.admin.aPostAdd(postForm, options))
    }
  }

  def save = Action.async { implicit request =>
    postForm.bindFromRequest.fold(
      formWithErrors => serviceGrpService.options.map { options =>
        BadRequest(views.html.admin.aPostAdd(formWithErrors, options))
      },
      post => {
        postService.insert(post).map { _ =>
          Home.flashing("success" -> "Post %s has been created".format(post.name))
        }
      }
    )
  }
}
