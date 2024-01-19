package gfbot

import canoe.api._
import canoe.syntax._
import cats.Parallel
import cats.effect.unsafe.implicits.global
import cats.effect.{Async, IO}
import fs2.Stream
import gfbot.models.{MainMenu, User}
import gfbot.repositories.AppRepositories
import gfbot.resources.AppResources
import gfbot.services.{AppServices, ScenarioService}

class GachaBot(token: String, logger: Logger[IO]) {

  private def matchText(str1: String, str2: String): Boolean =
    str1.toLowerCase.trim == str2.toLowerCase.trim

  private def printCanoeUser(f: canoe.models.User) = {
    s"Name: ${f.firstName} ${f.lastName.getOrElse("???")} - @${f.username.getOrElse("???")} ID: ${f.id}"
  }

  private def canoeUserToUser(f: canoe.models.User) = {
    User(null, f.id.toString, "unknown", f.username.getOrElse(f.firstName + "_" + f.id.toString))
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
      val user = canoeUserToUser(msg.from.get)
      logger.use(s"Received message: ${msg.text} from user ${printCanoeUser(msg.from.get)}").unsafeRunAndForget()
      msg.text match {
        case language if matchText(language, MainMenu.cnBtn.text) || matchText(language, MainMenu.ruBtn.text) || matchText(language, MainMenu.enBtn.text) =>
          scenario.instructions(msg.chat, language)
        case magicLink if magicLink.startsWith("https://gf2-gacha-record.sunborngame.com") && magicLink.contains("::") =>
          scenario.download(msg.chat, user, magicLink)
        case command if command.startsWith("/me") =>
          scenario.me(msg.chat, user)
        case command if command.startsWith("/global") =>
          scenario.globalInfo(msg.chat)
        case command if command.startsWith("/leaderboard") =>
          scenario.leaderboard(msg.chat, user)
        case command if command.startsWith("/") =>
          Scenario.done[F]
        case _ =>
          scenario.help(msg.chat)
      }
    }
}
