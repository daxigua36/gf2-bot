package gfbot

import org.typelevel.log4cats.LoggerFactory

class Logger[F[_]: LoggerFactory] {

  private val logger = LoggerFactory[F].getLogger

  def use(message: String): F[Unit] = logger.info(message)

  def error(exception: Exception): F[Unit] = logger.error(exception)("oops!")

  def error(message: String): F[Unit] = logger.error(message)
}
