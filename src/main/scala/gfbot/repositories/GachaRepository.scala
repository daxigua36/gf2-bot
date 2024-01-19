package gfbot.repositories

import cats.effect.Async
import cats.syntax.all._
import io.circe.generic.auto._
import gfbot.models.{GachaLogResponse, GachaRecord, PlayerId}
import io.circe.parser.decode
import org.http4s.circe._
import org.http4s.client.Client
import org.http4s.headers.`Content-Type`
import org.http4s.implicits.http4sLiteralsSyntax
import org.http4s.{EntityDecoder, Header, MediaType, Method, Request, Uri}
import org.typelevel.ci._

import java.util.Base64

trait GachaRepository[F[_]] {
  def getAllRecords(uri: String, token: String): F[List[GachaRecord]]
}

final private class GachaRepositoryImpl[F[_] : Async](private val httpClient: Client[F])
  extends GachaRepository[F] {

  implicit val gachaResponseEntityDecoder: EntityDecoder[F, GachaLogResponse] = jsonOf

  override def getAllRecords(uri: String, token: String): F[List[GachaRecord]] = {
    val gachaTypes = List("1", "2", "3", "4", "5")

    val listOfFutures: List[F[List[GachaRecord]]] = gachaTypes.map(typeId => consumePagination(uri, token, typeId))
    listOfFutures.sequence.map(_.flatten.sortBy(_.time))
  }

  private def consumePagination(uri: String, token: String, typeId: String, next: String = "", acc: List[GachaRecord] = List.empty): F[List[GachaRecord]] = {
    singleRequest(uri, token, typeId, next) flatMap { response =>
      if (response.data.next == "") {
        Async[F].pure(response.data.list ::: acc)
      } else {
        consumePagination(uri, token, typeId, response.data.next, response.data.list ::: acc)
      }
    }
  }

  private def singleRequest(uri: String, token: String, type_id: String, next: String) = {
    val url = Uri.fromString(s"$uri&next=$next").getOrElse(uri"localhost").withQueryParam("type_id", type_id)
    val postRequest = Request[F](method = Method.POST, uri = url)
      .withHeaders(
        Header.Raw.apply(ci"Authorization", token),
        Header.Raw.apply(ci"X-Unity-Version", "2019.4.40f1"),
        Header.Raw.apply(ci"User-Agent", "UnityPlayer/2019.4.40f1 (UnityWebRequest/1.0, libcurl/7.80.0-DEV)"),
      ).withContentType(`Content-Type`.apply(MediaType.application.`x-www-form-urlencoded`))

    httpClient.expect[GachaLogResponse](postRequest)
  }
}

object GachaRepository {
  def make[F[_] : Async](httpClient: Client[F]): F[GachaRepository[F]] = {
    Async[F].pure(new GachaRepositoryImpl[F](httpClient))
  }

  def extractPlayerId(token: String): Option[String] = {
    val jwtEncoded = token.split("\\.")(0)
    val json = Base64.getDecoder.decode(jwtEncoded)
    decode[PlayerId](new String(json)).toOption.map(_.openid)
  }
}

