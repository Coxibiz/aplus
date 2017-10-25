package controllers

import javax.inject.{Inject, Singleton}

import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import models._

import org.joda.time.DateTime

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class ApplicationController @Inject()(implicit val webJarAssets: WebJarAssets) extends Controller {
  val sabineAuthor = "Sabine, Assistante Sociale de la ville d'Argenteuil"

  var applications = List(
    Application(
      "1",
      "En cours",
      DateTime.now(),
      sabineAuthor,
      "Etat dossier CAF de Mr MARTIN John",
      "Bonjour,\nMr MARTIN John a fait transférer son dossier de la CAF de Marseille à la CAF d'Argenteuil, il ne sait pas où en est sa demande et voudrait savoir à qui envoyer ces documents de suivie.\nSon numéro à la CAF de Marseille est le 98767687, il est né le 16 juin 1985.\n\nMerci de votre aide",
      "Forte"),
    Application(
      "0",
      "Terminé",
      DateTime.now(),
      sabineAuthor,
      "Demande d'APL de Mme DUPOND Martine",
      "Bonjour,\nMme DUPOND Martine né le 12 juin 1978 a déposé une demande d'APL le 1 octobre.\nPouvez-vous m'indiquez l'état de sa demande ?\n\nMerci de votre réponse",
      "Moyenne")
  )


  def create = Action { implicit request =>
    Ok(views.html.createApplication())
  }

  case class ApplicatonData(subject: String, description: String, priority: String)
  val applicationForm = Form(
    mapping(
      "subject" -> text,
      "description" -> text,
      "priority" -> text
    )(ApplicatonData.apply)(ApplicatonData.unapply)
  )


  def createPost = Action { implicit request =>
    val applicationData = applicationForm.bindFromRequest.get
    val application = Application(applications.length.toString, "En cours", DateTime.now(), sabineAuthor, applicationData.subject, applicationData.description, applicationData.priority)
    applications = application :: applications
    Redirect(routes.ApplicationController.all()).flashing("success" -> "Votre demande a bien été envoyé")
  }

  def all = Action { implicit request =>
    Ok(views.html.allApplication(applications))
  }

  def show(id: String) = Action { implicit request =>
    applications.find(_.id == id) match {
      case None =>
        NotFound("")
      case Some(application) =>
        Ok(views.html.showApplication(application))
    }
  }

  def answer = Action { implicit request =>
    Redirect(routes.ApplicationController.all()).flashing("success" -> "Votre commentaire a bien été envoyé")
  }

  def invite = Action { implicit request =>
    Redirect(routes.ApplicationController.all()).flashing("success" -> "Les agents A+ ont été invité sur la demande")
  }
}
