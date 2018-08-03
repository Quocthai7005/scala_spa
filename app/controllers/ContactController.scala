package controllers

import javax.inject.Inject
import play.api.mvc.{AbstractController, ControllerComponents}

class ContactController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  def contact = Action { implicit request =>
    Ok(views.html.common.contact())
  }

}
