package gfbot.resources

import cats.effect.kernel.{Async, Resource}
import cats.implicits.catsSyntaxTuple2Parallel
import mongo4cats.database.MongoDatabase
import org.http4s.blaze.client.BlazeClientBuilder
import org.http4s.client.Client

import scala.concurrent.ExecutionContext.Implicits.global

sealed abstract class AppResources[F[_]](val httpClient: Client[F], val mongo: MongoDatabase[F])

object AppResources {
  def make[F[_]: Async]: Resource[F, AppResources[F]] = {
    (
      BlazeClientBuilder[F].withExecutionContext(global).resource,
      MongoDbClient.make[F],
    ).parMapN(new AppResources[F](_, _) {})
  }
}