package gfbot.resources

import cats.effect.kernel.Async
import gfbot.Config
import mongo4cats.client.MongoClient

object MongoDbClient {
  private val login = Config.getMongoLogin
  private val pass = Config.getMongoPass
  private val authDb = Config.getMongoDbName
  private val connectionString = s"mongodb://$login:$pass@localhost:27017/$authDb"

  def make[F[_] : Async] =
    MongoClient.fromConnectionString[F](connectionString).evalMap(_.getDatabase(authDb))
}
