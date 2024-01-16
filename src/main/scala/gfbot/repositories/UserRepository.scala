package gfbot.repositories

import io.circe.generic.auto._
import cats.effect.IO
import cats.effect.kernel.Async
import cats.syntax.all._
import gfbot.repositories.GachaRepository.extractPlayerId
import gfbot.Logger
import gfbot.models.User
import mongo4cats.bson.ObjectId
import mongo4cats.circe._
import mongo4cats.codecs.MongoCodecProvider
import mongo4cats.collection.MongoCollection
import mongo4cats.database.MongoDatabase
import mongo4cats.operations.Filter
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.slf4j.Slf4jFactory

trait UserRepository[F[_]] {
  def saveUser(tgId: Long, userToken: String, username: String): F[Unit]

  def getAllUsers: F[List[User]]

  def getUser(tgId: Long): F[Option[User]]
}

final private class UserRepositoryImpl[F[_] : Async](private val collection: MongoCollection[F, User])
  extends UserRepository[F] {

  implicit val logging: LoggerFactory[IO] = Slf4jFactory.create[IO]
  val logger = new Logger[IO]

  override def saveUser(tgId: Long, userToken: String, username: String): F[Unit] = {
    val gfId = extractPlayerId(userToken).get
    val user = User(_id = ObjectId.gen, tgId.toString, gfId, username)
    getUser(tgId) flatMap { userOpt =>
      if (userOpt.isDefined) {
        Async[F].unit
      } else {
        collection.insertOne(user) flatMap { _ =>
          Async[F].unit
        }
      }
    }
  }

  override def getAllUsers: F[List[User]] =
    collection.find.all.map(_.toList)

  override def getUser(tgId: Long): F[Option[User]] =
    collection.find(Filter.eq("tgId", tgId.toString)).first
}

object UserRepository {
  final private val COLLECTION_NAME: String = "users"

  implicit val userCodecProvided: MongoCodecProvider[User] = deriveCirceCodecProvider

  def make[F[_] : Async](db: MongoDatabase[F]): F[UserRepository[F]] = {
    db.getCollectionWithCodec[User](COLLECTION_NAME).map(col => new UserRepositoryImpl[F](col))
  }
}
