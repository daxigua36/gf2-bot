import sbt._

object Dependencies {

  object Cats {
    val catsEffectVersion = "3.5.2"

    val catsEffect = "org.typelevel" %% "cats-effect" % catsEffectVersion
    val all: Seq[ModuleID] = Seq(catsEffect)
  }

  object Http4s {
    val http4sVersion = "0.23.9"

    val httpDsl = "org.http4s" %% "http4s-dsl" % http4sVersion
    val blazeServer = "org.http4s" %% "http4s-blaze-server" % http4sVersion
    val blazeClient = "org.http4s" %% "http4s-blaze-client" % http4sVersion
    val http4sCirce = "org.http4s" %% "http4s-circe" % http4sVersion
    val http4sCirceGenetic = "io.circe" %% "circe-generic" % "0.14.6"
    val http4sCirceLiteral = "io.circe" %% "circe-literal" % "0.14.6"
    val all: Seq[ModuleID] = Seq(
      httpDsl,
      blazeClient,
      blazeServer,
      http4sCirce,
      http4sCirceGenetic,
      http4sCirceLiteral
    )
  }

  object Log4Cats {
    val version = "2.6.0"

    val core = "org.typelevel" %% "log4cats-core" % version
    val slf4j = "org.typelevel" %% "log4cats-slf4j" % version
    val all: Seq[ModuleID] = Seq(core, slf4j)
  }

  object Other {
    val logbackClassic = "ch.qos.logback" % "logback-classic" % "1.2.3"
    val s3aws = "com.amazonaws" % "aws-java-sdk-s3" % "1.12.122"
    val canoe = "org.augustjune" %% "canoe" % "0.6.0"
    val mongo = "io.github.kirill5k" %% "mongo4cats-core" % "0.6.17"
    val mongoCirce = "io.github.kirill5k" %% "mongo4cats-circe" % "0.6.17"

    val all: Seq[ModuleID] = Seq(logbackClassic, s3aws, canoe, mongo, mongoCirce)
  }

}