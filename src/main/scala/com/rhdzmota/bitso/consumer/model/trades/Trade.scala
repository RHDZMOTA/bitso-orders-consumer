package com.rhdzmota.bitso.consumer.model.trades

import java.sql.Timestamp
import java.time.LocalDateTime

import com.rhdzmota.bitso.consumer.model.implicits.Decoders
import io.circe.parser.decode

import scala.util.Try

sealed trait Trade

final case class SingleTrade(i: Long, a: Double, r: Double, v: String, mo: String, to: String, t: Int) extends Trade

final case class Trades(`type`: String, book: String, payload: List[SingleTrade]) extends Trade {
  def toTradeRow: List[TradeRow] = payload.zipWithIndex.map({case (singleTrade, index) => TradeRow(
    received = Timestamp.valueOf(LocalDateTime.now()),
    arrival = index,
    book    = book,
    id      = singleTrade.i,
    amount  = singleTrade.a,
    rate    = singleTrade.r,
    value   = Try(singleTrade.v.toDouble).toOption.get,
    makerSideAction = if (singleTrade.t == 1) "sell" else "buy",
    makerOrder = singleTrade.mo,
    takerOrder = singleTrade.to
  )})
}

case object Trades {
  import Decoders._
  object Errors {
    sealed trait TradeError
    final case class CirceError(e: io.circe.Error, string: String) extends TradeError
    case object NotTextMessage extends TradeError
  }
  def fromString(string: String): Either[Errors.TradeError, Trades] = decode[Trades](string) match {
    case Left(e)      => Left(Errors.CirceError(e, string))
    case Right(value) => Right(value)
  }
}

final case class TradeRow(
                           received: Timestamp,
                           arrival: Int,
                           book: String,
                           id: Long,
                           amount: Double,
                           rate: Double,
                           value: Double,
                           makerSideAction: String,
                           makerOrder: String,
                           takerOrder: String) extends Trade
