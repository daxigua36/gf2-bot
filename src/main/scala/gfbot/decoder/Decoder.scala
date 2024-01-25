package gfbot.decoder

import gfbot.decoder.characters.GunDataTable
import gfbot.decoder.language_string_data.LanguageStringDataTable
import gfbot.decoder.weapons.GunWeaponDataTable

import java.nio.ByteBuffer
import java.nio.ByteOrder.LITTLE_ENDIAN
import java.nio.file.{Files, Paths}

//MANUAL SCRIPT
object Decoder extends App {
  private val HEADER_COUNTER_LENGTH = 4

  private def headerLength(bytes: Array[Byte]): Int = {
    HEADER_COUNTER_LENGTH + ByteBuffer.wrap(bytes.take(HEADER_COUNTER_LENGTH)).order(LITTLE_ENDIAN).getInt
  }

  private def getProtoBytes(bytes: Array[Byte]): Array[Byte] = {
    bytes.drop(headerLength(bytes))
  }

  val dictionaryBytes = Files.readAllBytes(Paths.get("gamedata/LangPackageTableCnData.bytes"))
  val characterBytes = Files.readAllBytes(Paths.get("gamedata/GunData.bytes"))
  val weaponBytes = Files.readAllBytes(Paths.get("gamedata/GunWeaponData.bytes"))

  val dictionary = LanguageStringDataTable.parseFrom(dictionaryBytes).data
  val characterTable = GunDataTable.parseFrom(getProtoBytes(characterBytes)).data
  val weaponTable = GunWeaponDataTable.parseFrom(getProtoBytes(weaponBytes)).data

  val characterMapping = characterTable map { line =>
    val id = line.id
    val cnName = dictionary.find(d => d.id == line.name.get.id).map(_.content).getOrElse("???")
    val enName = dictionary.find(d => d.id == line.enName.get.id)
      .map(_.content)
      .getOrElse("???")
      .replace("SSR", "")
      .replace("SR", "")
    val rarity = line.rank

    s"$id -> Item(\"$enName\", \"$cnName\", $rarity),"
  } mkString "\n"

  val weaponMapping = weaponTable map { line =>
    val id = line.id
    val cnName = dictionary.find(d => d.id == line.name.get.id).map(_.content).getOrElse("???")
    val enName = line.resCode.replace("Weapon_", "").dropRight(2)
    val rarity = line.rank

    s"$id -> Item(\"$enName\", \"$cnName\", $rarity),"
  } mkString "\n"

  println("=====Characters=====")
  println(characterMapping)
  println("======Weapons=======")
  println(weaponMapping)
}
