package gfbot

import ch.qos.logback.classic.util.ContextInitializer

import java.io.FileInputStream
import java.util.Properties

object Config {

  private val p = new Properties()

  def loadConfigs(): Unit = {
    p.load(new FileInputStream("conf.d/server.properties"))
    System.setProperty(ContextInitializer.CONFIG_FILE_PROPERTY, "conf.d/logback.xml"): Unit
  }

  def getApiKeyBot: String = p.getProperty("api_key")

  def getMongoLogin: String = p.getProperty("mongo_login")

  def getMongoPass: String = p.getProperty("mongo_pass")

  def getMongoDbName: String = p.getProperty("mongo_dbname")
}
