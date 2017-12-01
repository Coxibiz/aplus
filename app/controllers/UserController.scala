package controllers

import java.util.UUID
import javax.inject.{Inject, Singleton}

import actions.LoginAction
import models.User
import org.webjars.play.WebJarsUtil
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.mvc.InjectedController
import services.{NotificationService, UserService}

@Singleton
class UserController @Inject()(loginAction: LoginAction,
                               userService: UserService,
                               notificationsService: NotificationService)(implicit val webJarsUtil: WebJarsUtil) extends InjectedController with play.api.i18n.I18nSupport {

  def all = loginAction { implicit request =>
    Ok(views.html.allUsers(request.currentUser)(userService.all()))
  }

  val usersForm = Form(
    single(
      "users" -> list(mapping(
        "id" -> default(uuid, UUID.randomUUID()),
        "key" -> ignored("key"),  //TODO refactoring security
        "name" -> nonEmptyText,
        "qualite" -> nonEmptyText,
        "email" -> email.verifying(nonEmpty),
        "helper" -> boolean,
        "instructor" -> boolean,
        "admin" -> boolean,
        "areas" -> list(uuid)
      )(User.apply)(User.unapply))
    )
  )

  def edit = loginAction { implicit request =>
    val users = userService.allDBOnly()
    val form = usersForm.fill(users)
    Ok(views.html.editUsers(request.currentUser)(form, users.length, routes.UserController.editPost()))
  }

  def editPost = loginAction { implicit request =>
    usersForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(views.html.editUsers(request.currentUser)(formWithErrors, 0, routes.UserController.editPost()))
      },
      users => {
        if(users.foldRight(true)({ (user, result) => userService.update(user) && result})) {
          Redirect(routes.UserController.all()).flashing("success" -> "Modification sauvegardé")
        } else {
          val form = usersForm.fill(users).withGlobalError("Impossible de mettre à jour certains utilisateurs (Erreur interne)")
          InternalServerError(views.html.editUsers(request.currentUser)(form, users.length, routes.UserController.editPost()))
        }
      }
    )
  }

  def add = loginAction { implicit request =>
    val rows = request.getQueryString("rows").map(_.toInt).getOrElse(1)
    Ok(views.html.editUsers(request.currentUser)(usersForm, rows, routes.UserController.addPost()))
  }

  def addPost = loginAction { implicit request =>
    usersForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(views.html.editUsers(request.currentUser)(formWithErrors, 0, routes.UserController.addPost()))
      },
      users => {
          if(userService.add(users)) {
            Redirect(routes.UserController.all()).flashing("success" -> "Utilisateurs ajouté")
          } else {
            val form = usersForm.fill(users).withGlobalError("Impossible d'ajouté les utilisateurs (Erreur interne)")
            InternalServerError(views.html.editUsers(request.currentUser)(form, users.length, routes.UserController.addPost()))
          }
      }
    )
  }
}