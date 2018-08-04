package repository

import java.text.SimpleDateFormat
import java.util.Date

import anorm.SQL
import javax.inject.Inject
import play.api.db.DBApi

import scala.concurrent.Future

case class User(oldPassword: String,
                newPassword: String,
                newConfirmpassword: String,
                deleted: Option[Boolean])

@javax.inject.Singleton
class UserRepo @Inject()(dbapi: DBApi)(implicit ec: DatabaseExecutionContext) {

  private val db = dbapi.database("default")

  def update(user: User, username: String) = Future {
    val d = new SimpleDateFormat("yyyy-MM-dd HH:MM").format(new Date()).toString
    db.withConnection { implicit connection =>
      SQL(s"""update user set password = '${user.newPassword}' where username = '${username}' and password = '${user.oldPassword}'""").executeUpdate()
    }
  }(ec)

}
