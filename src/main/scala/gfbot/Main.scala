package gfbot

import cats.effect.unsafe.implicits.global
import cats.effect.{ExitCode, IO, IOApp}
import gfbot.repositories.AppRepositories
import gfbot.resources.AppResources
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.slf4j.Slf4jFactory

object Main extends IOApp {
  Config.loadConfigs()

  implicit val logging: LoggerFactory[IO] = Slf4jFactory.create[IO]
  val logger = new Logger[IO]

  override def run(args: List[String]): IO[ExitCode] = {
    logger.use("Starting bot...").unsafeRunAndForget()
    AppResources.make[IO].use { resources =>
      AppRepositories.make[IO](resources).flatMap { repositories =>
        new GachaBot(Config.getApiKeyBot, logger).app[IO](repositories, resources).map(_ => ExitCode.Success)
      }
    }
  }
}