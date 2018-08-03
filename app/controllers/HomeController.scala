package controllers

import javax.inject._
import play.api.mvc._
import repository.{ServiceDtlRepo, ServiceGrp, ServiceGrpRepo}
import scala.concurrent.duration.Duration

import scala.concurrent.{ExecutionContext, Future, Await}

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(cc: ControllerComponents,
                               serviceGrpService: ServiceGrpRepo,
                               serviceDtlService: ServiceDtlRepo)(implicit ec: ExecutionContext) extends AbstractController(cc) {

  def home = Action.async { implicit request =>
    val listOfProfileFutures = serviceGrpService.findAll().map { serviceGrps => {
      serviceDtlService.findShownOnHome().map { serviceDtls =>
        Ok(views.html.common.home(serviceGrps, serviceDtls))
        }
      }
    }
    Await.result(listOfProfileFutures, Duration.Inf)
  }

  def aHome = Action { implicit request =>
    Ok(views.html.admin.aHome())
  }
}
