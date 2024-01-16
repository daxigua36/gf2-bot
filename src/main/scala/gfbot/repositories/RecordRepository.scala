package gfbot.repositories

import io.circe.generic.auto._
import cats.effect.kernel.Async
import cats.syntax.all._
import gfbot.models.GachaRecord
import gfbot.repositories.GachaRepository.extractPlayerId
import mongo4cats.bson.ObjectId
import mongo4cats.circe._
import mongo4cats.codecs.MongoCodecProvider
import mongo4cats.collection.MongoCollection
import mongo4cats.database.MongoDatabase
import mongo4cats.operations.Filter

case class Record(_id: ObjectId, userId: String, itemId: String, bannerId: String, t: Long)

trait RecordRepository[F[_]] {
  def getAllRecords: F[List[Record]]

  def getRecordsByUserToken(userToken: String): F[List[Record]]

  def getRecordsByUserId(userId: String): F[List[Record]]

  def insertRecords(records: List[GachaRecord], userToken: String): F[Unit]
}

final private class RecordRepositoryImpl[F[_] : Async](private val collection: MongoCollection[F, Record])
  extends RecordRepository[F] {

  import RecordRepository._

  override def getAllRecords: F[List[Record]] =
    collection.find.all.map(_.toList)

  override def getRecordsByUserToken(userToken: String): F[List[Record]] = {
    val gfId = extractPlayerId(userToken).get

    collection.find(Filter.eq("userId", gfId)).all.map(_.toList)
  }

  override def getRecordsByUserId(userId: String): F[List[Record]] = {
    collection.find(Filter.eq("userId", userId)).all.map(_.toList)
  }

  override def insertRecords(records: List[GachaRecord], userToken: String): F[Unit] = {
    val userIdOpt = extractPlayerId(userToken)
    if (records.isEmpty) {
      Async[F].unit
    } else {
      collection.insertMany(records map gachaRec2dbRec(userIdOpt)) flatMap { _ =>
        Async[F].unit
      }
    }
  }
}

object RecordRepository {
  final private val COLLECTION_NAME: String = "records"

  implicit val recordCodecProvided: MongoCodecProvider[Record] = deriveCirceCodecProvider

  def make[F[_] : Async](db: MongoDatabase[F]): F[RecordRepository[F]] = {
    db.getCollectionWithCodec[Record](COLLECTION_NAME).map(col => new RecordRepositoryImpl[F](col))
  }

  def gachaRec2dbRec(userIdOpt: Option[String])(record: GachaRecord): Record = {
    Record(_id = ObjectId.gen, userIdOpt.getOrElse("???"), record.item.toString, record.pool_id.toString, record.time)
  }
}