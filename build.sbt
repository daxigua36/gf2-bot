import Dependencies._

scalacOptions ++= ScalaOpts.all

lazy val root = (project in file("."))
  .settings(
    organization := "daxigua",
    name := "gf2-bot",
    version := "0.0.1",
    scalaVersion := "2.13.12",
    libraryDependencies ++= Cats.all ++ Other.all ++ Http4s.all ++ Log4Cats.all,
    assembly / assemblyMergeStrategy := {
      case "module-info.class"                     => MergeStrategy.discard
      case "META-INF/native-image/org.mongodb/bson/native-image.properties" => MergeStrategy.last
      case "META-INF/io.netty.versions.properties" => MergeStrategy.last
      case x                                       => (assembly / assemblyMergeStrategy).value.apply(x)
    }
  )
