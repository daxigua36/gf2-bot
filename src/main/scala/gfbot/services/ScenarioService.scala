package gfbot.services

import canoe.api._
import canoe.api.models.Keyboard
import canoe.models
import canoe.models.messages.TextMessage
import canoe.models.outgoing.TextContent
import canoe.models.{Chat, ParseMode}
import cats.effect.{Async, IO}
import cats.syntax.all._
import gfbot.Logger
import gfbot.models.{GachaRecord, MainMenu, User}
import gfbot.repositories.{GachaRepository, Item, ItemRepository, RecordRepository, UserRepository}
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.slf4j.Slf4jFactory

import java.text.SimpleDateFormat

trait ScenarioService[F[_]] {
  def start(chat: Chat): Scenario[F, Unit]

  def myInfo(chat: Chat, from: Option[canoe.models.User]): Scenario[F, Unit]

  def help(chat: Chat): Scenario[F, Unit]

  def instructions(chat: Chat, language: String): Scenario[F, Unit]

  def download(chat: Chat, from: Option[canoe.models.User], magicLink: String): Scenario[F, Unit]
}

object ScenarioService {
  def make[F[_] : Async : TelegramClient](
                                           recordRepository: RecordRepository[F],
                                           userRepository: UserRepository[F],
                                           gachaRepository: GachaRepository[F]
                                         ): ScenarioService[F] = new ScenarioService[F] {
    implicit val logging: LoggerFactory[IO] = Slf4jFactory.create[IO]
    val logger = new Logger[IO]

    override def start(chat: Chat): Scenario[F, Unit] =
      for {
        _ <- Scenario.eval(chat.send(
          TextContent(
            "请选择语言\nChoose your language\nВыберите язык",
            Some(ParseMode.Markdown)
          ),
          keyboard = MainMenu.keyboard
        )
        )
      } yield ()


    override def myInfo(chat: Chat, from: Option[canoe.models.User]): Scenario[F, Unit] = {
      val result: F[TextMessage] = userRepository.getUser(from.get.id) flatMap { userOpt =>
        val user = userOpt.getOrElse(User(null, from.get.id.toString, "unknown", from.get.username.get))
        recordRepository.getRecordsByUserId(user.gfId).flatMap { records =>

          val msg = records match {
            case Nil =>
              s"@${user.username}, you haven't imported any data.\nPlease import you first gacha records using /start command."
            case _ =>
              val itemsMap: Map[Int, Item] = ItemRepository.weapons ++ ItemRepository.heroes
              val fiveStarsCount = records.count(r => itemsMap(r.itemId.toInt).rarity == 5)
              val fourStarsCount = records.count(r => itemsMap(r.itemId.toInt).rarity == 4)
              val groupByBanner = records.groupBy(_.bannerId).map { case (k, v) =>
                (ItemRepository.banners.getOrElse(k.toInt, k), s"*${v.length.toString}* records")
              }.mkString("\n")

              s"""@${user.username}, here is your report:
                 |Total summons: *${records.length}*
                 |Crystals spent: *${records.length * 150}*
                 |5⭐ summons: *$fiveStarsCount*
                 |4⭐ summons: *$fourStarsCount*
                 |
                 |Distributed by banner:
                 |$groupByBanner""".stripMargin
          }

          chat.send(TextContent(msg, Some(ParseMode.Markdown)))
        } recoverWith { case ex: Exception =>
          import cats.effect.unsafe.implicits.global
          logger.error(ex).unsafeRunAndForget()
          chat.send(TextContent("Oops! Something failed unexpectedly...", Some(ParseMode.Markdown)))
        }
      }
      for {
        _ <- Scenario.eval(result)
      } yield ()
    }

    override def help(chat: Chat): Scenario[F, Unit] = for {
      _ <- Scenario.eval(chat.send(
        TextContent("请发送 /start 命令\nPlease send /start\nПожалуйста, отправьте /start", Some(ParseMode.Markdown)))
      )
    } yield ()

    override def instructions(chat: Chat, language: String): Scenario[F, Unit] = {
      val instructionsContent1 = language match {
        case MainMenu.cnBtn.text =>
          "你好指挥官！\n\n登录《少女前线2》PC端并打开游戏内的跃迁历史记录。\n\n打开Windows PowerShell，然后贴上并运行下述任一命令。通过Windows搜索搜寻「Windows PowerShell」即可找到该程序。"
        case MainMenu.enBtn.text =>
          "Hi, Commander!\n\nLaunch Girls Frontline 2 on PC and open your in-game Gacha Records.\n\nOpen Windows PowerShell, then paste and run the following command. You can find PowerShell by searching for \"Windows PowerShell\" within Windows Search."
        case MainMenu.ruBtn.text =>
          "Здравия желаю, главнокомандующий!\n\nЗапустите Girls Frontline 2 на ПК и откройте свою историю с крутками персонажей.\n\nОткройте Windows PowerShell, вставьте и запустите следующую команду. Вы можете найти PowerShell, если введете \"Windows PowerShell\" в меню поиска Windows."
      }

      val instructionsContent2 = "`[Net.ServicePointManager]::SecurityProtocol = [Net.ServicePointManager]::SecurityProtocol -bor [Net.SecurityProtocolType]::Tls12; $wc = New-Object System.Net.WebClient; $wc.Encoding = [System.Text.Encoding]::UTF8; Invoke-Expression $wc.DownloadString(\"https://gist.githubusercontent.com/daxigua36/2001ad1e55cf0b5df46f879f981f0fde/raw/49ad885818edb691d6f859690569832d0b6e13f3/gf2_get_gacha_url.ps1\")`"

      val instructionsContent3 = language match {
        case MainMenu.cnBtn.text =>
          "按下回车键后，扭蛋历史记录网址将被复制到剪贴板。\n\n请向我发送上一个命令的输出"
        case MainMenu.enBtn.text =>
          "Upon pressing Enter, your Gacha Records URL will be copied to your clipboard.\n\nPlease paste and send it to this bot in the next message."
        case MainMenu.ruBtn.text =>
          "После нажатия клавиши Enter URL-адрес вашей истории круток будет скопирован в буфер обмена.\n\nСледующим сообщением пришлите мне вывод этой команды."
      }

      for {
        _ <- Scenario.eval(chat.send(
          TextContent(instructionsContent1, Some(ParseMode.Markdown)),
          keyboard = Keyboard.Remove)
        )
        _ <- Scenario.eval(chat.send(
          TextContent(instructionsContent2, Some(ParseMode.Markdown)))
        )
        _ <- Scenario.eval(chat.send(
          TextContent(instructionsContent3, Some(ParseMode.Markdown)))
        )
      } yield ()
    }

    override def download(chat: Chat, from: Option[models.User], magicLink: String): Scenario[F, Unit] = {
      import cats.effect.unsafe.implicits.global
      val url = magicLink.split("::")(0)
      val token = magicLink.split("::")(1)
      val df: SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

      val result: F[TextMessage] = gachaRepository.getAllRecords(url, token) flatMap { recordsInApi =>
        recordRepository.getRecordsByUserToken(token) flatMap { recordsInDb =>

          val diffRecords: List[GachaRecord] =
            if (recordsInDb.isEmpty) {
              recordsInApi
            } else {
              val latestDbRecordTime = recordsInDb.maxBy(_.t).t
              recordsInApi.filter(_.time > latestDbRecordTime)
            }

          recordRepository.insertRecords(diffRecords, token) flatMap { _ =>
            userRepository.saveUser(from.get.id, token, from.get.username.get) flatMap { _ =>
              val count = diffRecords.length

              val tuples: List[(Item, Long, Int)] = diffRecords.sortBy(_.time).zipWithIndex map { case (r, i) =>
                val t = (ItemRepository.heroes.getOrElse(r.item, ItemRepository.weapons.getOrElse(r.item, Item(r.item.toString, "???", 0))), r.time, i + 1)
                if (t._1.rarity == 0) logger.use(s"MISSING ITEM $r").unsafeRunAndForget()
                t
              }
              logger.use(s"Imported Items $tuples").unsafeRunAndForget()

              val strings = tuples.filter(t => t._1.rarity > 3) map { case (item, time, index) =>
                val dateString = df.format(time * 1000)
                val itemText = item.rarity match {
                  case 5 => s"⭐*${item.enName}*⭐ (${item.cnName})"
                  case 4 => s"*${item.enName}* (${item.cnName})"
                  case _ => s"${item.enName}(${item.cnName})"
                }
                s"$index.".padTo(4, ' ') + itemText + " - " + dateString
              }

              val msg = s"We have imported *$count* total records.\n\nImported higher-rank records:\n${strings.mkString("\n")}"

              chat.send(TextContent(msg, Some(ParseMode.Markdown)))
            }
          }
        }
      } recoverWith { case ex: Exception =>
        logger.error(ex).unsafeRunAndForget()
        chat.send(TextContent("Oops! Something failed unexpectedly...", Some(ParseMode.Markdown)))
      }

      for {
        _ <- Scenario.eval(result)
      } yield ()

    }
  }
}
