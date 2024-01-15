package gfbot.repositories

import cats.Parallel
import cats.effect.Async
import cats.implicits._
import gfbot.resources.AppResources

sealed abstract class AppRepositories[F[_]](
                                             val recordRepository: RecordRepository[F],
                                             val gachaRepository: GachaRepository[F],
                                             val userRepository: UserRepository[F]
                                           )

object AppRepositories {
  def make[F[_]: Async: Parallel](appResources: AppResources[F]): F[AppRepositories[F]] =
    (
      RecordRepository.make[F](appResources.mongo),
      GachaRepository.make[F](appResources.httpClient),
      UserRepository.make[F](appResources.mongo)
    ).parMapN(new AppRepositories[F](_, _, _) {})
}