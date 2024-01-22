package gfbot.models

import canoe.models.{InputFile, ParseMode}
import canoe.models.outgoing.{PhotoContent, TextContent}

import java.io.File
import java.nio.file.Files

object Messages {
  def chooseLanguage: TextContent = TextContent(
    "请选择语言\nChoose your language\nВыберите язык",
    Some(ParseMode.Markdown)
  )

  def errorMessage: TextContent = TextContent(
    "Oops! Something failed unexpectedly...",
    Some(ParseMode.Markdown)
  )

  def inProgress: TextContent = TextContent(
    "Import in progress...",
    Some(ParseMode.Markdown)
  )

  def helpMessage: TextContent = TextContent(
    "请发送 /start 命令\nPlease send /start\nПожалуйста, отправьте /start",
    Some(ParseMode.Markdown)
  )

  def userNotFound(username: String): TextContent = TextContent(
    s"$username, you haven't imported any data.\nPlease import you first gacha records using /start command.",
    Some(ParseMode.Markdown)
  )

  def meReport(username: String,
               recordCount: Int,
               fiveStarsCount: Int,
               fourStarsCount: Int,
               countByBanner: String): TextContent = TextContent(
    s"""*$username*, here is your report:
       |Total Summons: *$recordCount*
       |Crystals Spent: *${recordCount * 150}*
       |5⭐ Summons: *$fiveStarsCount*
       |4⭐ Summons: *$fourStarsCount*
       |
       |Distributed by banner:
       |$countByBanner""".stripMargin,
    Some(ParseMode.Markdown)
  )

  def globalReport(userCount: Int,
                   recordCount: Int,
                   fiveStarsCount: Int,
                   fourStarsCount: Int,
                   fiveStarsChance: BigDecimal,
                   fourStarsChance: BigDecimal): TextContent = TextContent(
    s"""Total Users: *$userCount*
       |Total Summons: *$recordCount*
       |Crystals Spent: *${recordCount * 150}*
       |
       |5⭐ Summons: *$fiveStarsCount*
       |4⭐ Summons: *$fourStarsCount*
       |5⭐ Chance: *${fiveStarsChance.setScale(2, BigDecimal.RoundingMode.HALF_UP)}%*
       |4⭐ Chance: *${fourStarsChance.setScale(2, BigDecimal.RoundingMode.HALF_UP)}%*""".stripMargin,
    Some(ParseMode.Markdown)
  )

  def instruction1(language: String): PhotoContent = {
    val file = new File("images/instruction.jpg")
    val image = InputFile.fromBytes("instruction.jpg", Files.readAllBytes(file.toPath))

    val msg = language match {
      case MainMenu.cnBtn.text =>
        "你好指挥官！\n\n登录《少女前线2》PC端并打开游戏内的跃迁历史记录。\n\n打开Windows PowerShell，然后贴上并运行下述任一命令。通过Windows搜索搜寻「Windows PowerShell」即可找到该程序。"
      case MainMenu.enBtn.text =>
        "Hi, Commander!\n\nLaunch Girls Frontline 2 on PC and open your in-game Gacha Records.\n\nOpen Windows PowerShell, then paste and run the following command. You can find PowerShell by searching for \"Windows PowerShell\" within Windows Search."
      case MainMenu.ruBtn.text =>
        "Здравия желаю, командир!\n\nЗапустите Girls Frontline 2 на ПК и откройте свою историю с крутками персонажей.\n\nОткройте Windows PowerShell, вставьте и запустите следующую команду. Вы можете найти PowerShell, если введете \"Windows PowerShell\" в меню поиска Windows."
    }
    PhotoContent(image, msg, Some(ParseMode.Markdown))
  }

  def instruction2: TextContent = TextContent(
    "`[Net.ServicePointManager]::SecurityProtocol = [Net.ServicePointManager]::SecurityProtocol -bor [Net.SecurityProtocolType]::Tls12; $wc = New-Object System.Net.WebClient; $wc.Encoding = [System.Text.Encoding]::UTF8; Invoke-Expression $wc.DownloadString(\"https://gist.githubusercontent.com/daxigua36/2001ad1e55cf0b5df46f879f981f0fde/raw/49ad885818edb691d6f859690569832d0b6e13f3/gf2_get_gacha_url.ps1\")`",
    Some(ParseMode.Markdown)
  )

  def instruction3(language: String): TextContent = {
    val msg = language match {
      case MainMenu.cnBtn.text =>
        "按下回车键后，扭蛋历史记录网址将被复制到剪贴板。\n\n请向这个机器人发送上一个命令的输出"
      case MainMenu.enBtn.text =>
        "Upon pressing Enter, your Gacha Records URL will be copied to your clipboard.\n\nPlease paste and send it back to this bot"
      case MainMenu.ruBtn.text =>
        "После нажатия клавиши Enter URL-адрес вашей истории круток будет скопирован в буфер обмена.\n\nСледующим сообщением пришлите вывод этой команды обратно в чат с ботом."
    }
    TextContent(msg, Some(ParseMode.Markdown))
  }

  def emptyImport: TextContent = TextContent(
    s"""No new summons have been found.
       |
       |Explore leaderboard by sending /leaderboard
       |Explore global stats by sending /global
       |Explore personal stats by sending /me""".stripMargin,
    Some(ParseMode.Markdown)
  )

  def importResult(recordCount: Int, highRankRecords: List[String]): TextContent = TextContent(
    s"""We have imported *$recordCount* total new records.
       |
       |New higher-rank records:
       |${highRankRecords.mkString("\n")}
       |
       |Explore leaderboard by sending /leaderboard
       |Explore global stats by sending /global
       |Explore personal stats by sending /me""".stripMargin,
    Some(ParseMode.Markdown)
  )

  def leadersMessage(username: String, userInfo: (Int, String, Int), highRankPlayers: List[String]): TextContent = TextContent(
    s"""*$username*, you are currently in *${userInfo._1 + 1}* place with *${userInfo._3}* summons:
       |
       |Top 5 players:
       |${highRankPlayers.take(5).mkString("\n")}""".stripMargin,
    Some(ParseMode.Markdown)
  )
}
