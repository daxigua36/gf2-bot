package gfbot.services

import canoe.api.TelegramClient
import cats.effect.kernel.Async
import gfbot.repositories.AppRepositories
import gfbot.resources.AppResources

sealed trait AppServices[F[_]] {
  val scenarioService: ScenarioService[F]
}

object AppServices {
  def make[F[_]: Async: TelegramClient](
                         appRepositories: AppRepositories[F],
                         appResources: AppResources[F]
                       ): AppServices[F] =
    new AppServices[F] {
      override val scenarioService: ScenarioService[F] =
        ScenarioService.make[F](
          appRepositories.recordRepository,
          appRepositories.userRepository,
          appRepositories.gachaRepository)
    }
}
