package gfbot

import canoe.api._
import canoe.models.User
import canoe.syntax._
import cats.Parallel
import cats.effect.unsafe.implicits.global
import cats.effect.{Async, IO}
import fs2.Stream
import gfbot.models.MainMenu
import gfbot.repositories.AppRepositories
import gfbot.resources.AppResources
import gfbot.services.{AppServices, ScenarioService}

class GachaBot(token: String, logger: Logger[IO]) {

  private def matchText(str1: String, str2: String): Boolean =
    str1.toLowerCase.trim == str2.toLowerCase.trim

  private def expandFrom(f: User) = {
    s"Name: ${f.firstName} ${f.lastName.getOrElse("???")} - @${f.username.getOrElse("???")} ID: ${f.id}"
  }

  def app[F[_] : Async : Parallel](repositories: AppRepositories[F], resources: AppResources[F]): F[Unit] = {
    val tgClient: Stream[F, TelegramClient[F]] = Stream.resource(TelegramClient[F](token))

    tgClient.flatMap { implicit client =>
      val services = AppServices.make[F](repositories, resources)
      Bot.polling[F].follow(start(services.scenarioService), polling(services.scenarioService))
    }.compile.drain
  }


  private def start[F[_] : TelegramClient](scenario: ScenarioService[F]): Scenario[F, Unit] =
    Scenario.expect(command("start").chat)
      .flatMap(chat => scenario.start(chat).map(_ => ()))

  private def polling[F[_] : Async : TelegramClient](scenario: ScenarioService[F]): Scenario[F, Unit] =
    Scenario.expect(textMessage).flatMap { msg =>
      logger.use(s"Received message: ${msg.text} from user ${msg.from map expandFrom}").unsafeRunAndForget()
      msg.text match {
        case language if matchText(language, MainMenu.cnBtn.text) || matchText(language, MainMenu.ruBtn.text) || matchText(language, MainMenu.enBtn.text) =>
          scenario.instructions(msg.chat, language)
        case magicLink if magicLink.startsWith("https://gf2-gacha-record.sunborngame.com") && magicLink.contains("::") =>
          scenario.download(msg.chat, msg.from, magicLink)
        case command if command.startsWith("/me") =>
          scenario.myInfo(msg.chat, msg.from)
        case command if command.startsWith("/") =>
          Scenario.done[F]
        case _ =>
          scenario.help(msg.chat)
      }
    }
}
