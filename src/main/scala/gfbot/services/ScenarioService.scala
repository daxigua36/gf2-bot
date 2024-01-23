package gfbot.services

import canoe.api._
import canoe.api.models.Keyboard
import canoe.models.messages.TextMessage
import canoe.models.outgoing.TextContent
import canoe.models.{Chat, ParseMode}
import cats.effect.{Async, IO}
import cats.syntax.all._
import gfbot.Logger
import gfbot.models.Messages._
import gfbot.models.{GachaRecord, MainMenu, User}
import gfbot.repositories._
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.slf4j.Slf4jFactory

import java.text.SimpleDateFormat

trait ScenarioService[F[_]] {
  def start(chat: Chat): Scenario[F, Unit]

  def me(chat: Chat, from: User): Scenario[F, Unit]

  def globalInfo(chat: Chat): Scenario[F, Unit]

  def help(chat: Chat): Scenario[F, Unit]

  def instructions(chat: Chat, language: String): Scenario[F, Unit]

  def download(chat: Chat, from: User, magicLink: String): Scenario[F, Unit]

  def leaderboard(chat: Chat, from: User): Scenario[F, Unit]

  def showScript(chat: Chat): Scenario[F, Unit]
}

object ScenarioService {
  def make[F[_] : Async : TelegramClient](
                                           recordRepository: RecordRepository[F],
                                           userRepository: UserRepository[F],
                                           gachaRepository: GachaRepository[F]
                                         ): ScenarioService[F] = new ScenarioService[F] {
    implicit val logging: LoggerFactory[IO] = Slf4jFactory.create[IO]
    val logger = new Logger[IO]

    override def start(chat: Chat): Scenario[F, Unit] = Scenario.eval(
      chat.send(chooseLanguage, keyboard = MainMenu.keyboard)
    ).map(_ => ())

    override def help(chat: Chat): Scenario[F, Unit] = Scenario.eval(
      chat.send(helpMessage)
    ).map(_ => ())

    override def showScript(chat: Chat): Scenario[F, Unit] = Scenario.eval(
      chat.send(instruction2)
    ).map(_ => ())


    override def me(chat: Chat, from: User): Scenario[F, Unit] = {
      val result: F[TextMessage] = userRepository.getUser(from.tgId.toLong) flatMap { userOpt =>
        val user = userOpt.getOrElse(from)
        recordRepository.getRecordsByUserId(user.gfId).flatMap { records =>

          val msg = records match {
            case Nil =>
              userNotFound(user.username)
            case _ =>
              val itemsMap: Map[Int, Item] = ItemRepository.getAllItems
              val fiveStarsCount = records.count(r => itemsMap(r.itemId.toInt).rarity == 5)
              val fourStarsCount = records.count(r => itemsMap(r.itemId.toInt).rarity == 4)
              val groupByBanner = records.groupBy(_.bannerId).map { case (k, v) =>
                (ItemRepository.banners.getOrElse(k.toInt, k), s"*${v.length.toString}* summons")
              }.mkString("\n")

              meReport(user.username, records.length, fiveStarsCount, fourStarsCount, groupByBanner)
          }
          chat.send(msg)
        } recoverWith { case ex: Exception =>
          import cats.effect.unsafe.implicits.global
          logger.error(ex).unsafeRunAndForget()
          chat.send(errorMessage)
        }
      }

      Scenario.eval(result).map(_ => ())
    }

    override def instructions(chat: Chat, language: String): Scenario[F, Unit] = for {
      _ <- Scenario.eval(chat.send(instruction1(language), keyboard = Keyboard.Remove))
      _ <- Scenario.eval(chat.send(instruction2, keyboard = Keyboard.Remove))
      _ <- Scenario.eval(chat.send(instruction3(language), keyboard = Keyboard.Remove))
    } yield ()

    override def download(chat: Chat, from: User, magicLink: String): Scenario[F, Unit] = {
      import cats.effect.unsafe.implicits.global
      val url = magicLink.split("::")(0)
      val token = magicLink.split("::")(1)
      val df: SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

      val result: F[TextMessage] = chat.send(inProgress) flatMap { _ =>
        gachaRepository.getAllRecords(url, token) flatMap { recordsInApi =>
          recordRepository.getRecordsByUserToken(token) flatMap { recordsInDb =>

            val diffRecords: List[GachaRecord] =
              if (recordsInDb.isEmpty) {
                recordsInApi
              } else {
                val latestDbRecordTime = recordsInDb.maxBy(_.t).t
                recordsInApi.filter(_.time > latestDbRecordTime)
              }

            recordRepository.insertRecords(diffRecords, token) flatMap { _ =>
              userRepository.saveUser(from.tgId.toLong, token, from.username) flatMap { _ =>
                val newRecordCount = diffRecords.length

                val tuples: List[(Item, Long, Int)] = diffRecords.sortBy(_.time).zipWithIndex map { case (r, i) =>
                  val item = ItemRepository.getAllItems.getOrElse(r.item, Item(r.item.toString, "???", 0))
                  val tuple = (item, r.time, i + 1)
                  if (tuple._1.rarity == 0) logger.use(s"MISSING ITEM $r").unsafeRunAndForget()
                  tuple
                }
                logger.use(s"Imported $newRecordCount items $diffRecords").unsafeRunAndForget()

                val highRankRecords = tuples.filter(t => t._1.rarity > 3) map { case (item, time, index) =>
                  val dateString = df.format(time * 1000)
                  val itemText = item.rarity match {
                    case 5 => s"⭐*${item.enName}*⭐ (${item.cnName})"
                    case 4 => s"*${item.enName}* (${item.cnName})"
                    case _ => s"${item.enName}(${item.cnName})"
                  }
                  s"$index.".padTo(4, ' ') + itemText + " - " + dateString
                }

                val msg = if (newRecordCount > 0)
                  importResult(newRecordCount, highRankRecords)
                else
                  emptyImport

                chat.send(msg)
              }
            }
          }
        } recoverWith { case ex: Exception =>
          logger.error(ex).unsafeRunAndForget()
          chat.send(errorMessage)
        }
      }

      Scenario.eval(result).map(_ => ())
    }

    override def globalInfo(chat: Chat): Scenario[F, Unit] = {
      val result = recordRepository.getAllRecords flatMap { records =>
        userRepository.getAllUsers flatMap { users =>
          val recordCount = records.length
          val userCount = users.length
          val itemsMap: Map[Int, Item] = ItemRepository.getAllItems
          val fiveStarsCount = records.count(r => itemsMap(r.itemId.toInt).rarity == 5)
          val fourStarsCount = records.count(r => itemsMap(r.itemId.toInt).rarity == 4)
          val fiveStarsChance = BigDecimal(fiveStarsCount * 100) / recordCount
          val fourStarsChance = BigDecimal(fourStarsCount * 100) / recordCount

          val msg = globalReport(userCount, recordCount, fiveStarsCount, fourStarsCount, fiveStarsChance, fourStarsChance)
          chat.send(msg)
        }
      } recoverWith { case ex: Exception =>
        import cats.effect.unsafe.implicits.global
        logger.error(ex).unsafeRunAndForget()
        chat.send(errorMessage)
      }

      Scenario.eval(result).map(_ => ())
    }

    override def leaderboard(chat: Chat, from: User): Scenario[F, Unit] = {
      val result = recordRepository.getAllRecords flatMap { records =>
        userRepository.getAllUsers flatMap { users =>
          val usersByCount = records
            .groupBy(_.userId)
            .toList
            .sortBy(-_._2.length)
            .zipWithIndex
            .map { case ((userId, records), index) => (index, userId, records.length) }
          val highRankPlayers = usersByCount map { case (index, userId, records) =>
            s"${index + 1}. ${users.find(_.gfId == userId).get.username} - *$records* summons"
          }
          val currentUserOpt = users.find(_.tgId == from.tgId)
          val currentPlaceOpt = currentUserOpt.flatMap(u => usersByCount.find(_._2 == u.gfId))

          val msg = if (currentUserOpt.isEmpty)
            userNotFound(from.username)
          else
            leadersMessage(from.username, currentPlaceOpt.get, highRankPlayers)
          chat.send(msg)
        }
      } recoverWith { case ex: Exception =>
        import cats.effect.unsafe.implicits.global
        logger.error(ex).unsafeRunAndForget()
        chat.send(errorMessage)
      }

      Scenario.eval(result).map(_ => ())
    }
  }
}
