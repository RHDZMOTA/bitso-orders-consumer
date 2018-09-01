package com.rhdzmota.bitso.consumer.model.orders

import java.sql.Timestamp
import java.time.LocalDateTime

import com.rhdzmota.bitso.consumer.model.implicits.Decoders
import io.circe.parser.decode


import scala.util.Try

sealed trait Order

object Order {

  // Single Order ADT
  sealed trait SingleOrder extends Order {
    def o: String
    def d: Long
    def r: String
    def t: Int
    def s: String
  }
  object SingleOrder {
    final case class Open(o: String, d: Long, r: String, t: Int, a: String, v: String, s: String) extends SingleOrder
    final case class ChangeStatus(o: String, d: Long, r: String, t: Int, s: String) extends SingleOrder // cancelled or completed
  }

  final case class DiffOrders(`type`: String, book: String, sequence: Long, payload: List[SingleOrder]) extends Order {
    def toOrderRow: List[OrderRow] = payload.zipWithIndex.map({
      case (SingleOrder.Open(o, d, r, t, a, v, s), index: Int) => OrderRow(
        received = Timestamp.valueOf(LocalDateTime.now()),
        arrival = index,
        book = book,
        sequence = sequence,
        orderId = o,
        milliseconds = d,
        rate = Try(r.toDouble).toOption,
        makerAction = if (t == 1) "sell" else "buy",
        status = s,
        amount = Try(a.toDouble).toOption,
        value = Try(v.toDouble).toOption)
      case (SingleOrder.ChangeStatus(o, d, r, t, s), index: Int) => OrderRow(
        received = Timestamp.valueOf(LocalDateTime.now()),
        arrival = index,
        book = book,
        sequence = sequence,
        orderId = o,
        milliseconds = d,
        rate = Try(r.toDouble).toOption,
        makerAction = if (t == 1) "sell" else "buy",
        status = s,
        amount = None,
        value = None)
    })
  }
  object DiffOrders {
    import Decoders._
    object Errors {
      sealed trait DiffOrderError
      final case class CirceError(e: io.circe.Error, string: String) extends DiffOrderError
      case object NotTextMessage extends DiffOrderError
    }
    def fromString(string: String): Either[Errors.DiffOrderError, DiffOrders] = decode[DiffOrders](string) match {
      case Left(e)      => Left(Errors.CirceError(e, string))
      case Right(value) => Right(value)
    }

  }

  final case class OrderRow(
                             received: Timestamp,
                             arrival: Int,
                             book: String,
                             sequence: Long,
                             orderId: String,
                             milliseconds: Long,
                             rate: Option[Double],
                             makerAction: String,
                             status: String,
                             amount: Option[Double],
                             value: Option[Double],
                           ) extends Order

}