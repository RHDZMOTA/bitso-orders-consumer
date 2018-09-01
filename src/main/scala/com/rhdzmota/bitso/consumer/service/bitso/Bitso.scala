package com.rhdzmota.bitso.consumer.service.bitso

import java.sql.Timestamp
import java.time.LocalDateTime

import akka.Done
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import com.rhdzmota.bitso.consumer.conf.{Context, Settings}
import com.rhdzmota.bitso.consumer.model.orders.Order._

import scala.concurrent.{Future, Promise}

case object Bitso extends Context {

  val ordersSource: Source[Message, Promise[Option[Message]]] = Source(List(
    TextMessage(Settings.Bitso.Coins.btcmxn.exchange.diff),
    TextMessage(Settings.Bitso.Coins.ethmxn.exchange.diff),
    TextMessage(Settings.Bitso.Coins.xrpmxn.exchange.diff),
    TextMessage(Settings.Bitso.Coins.ltcmxn.exchange.diff),
    TextMessage(Settings.Bitso.Coins.bchmxn.exchange.diff),
    TextMessage(Settings.Bitso.Coins.tusdmxn.exchange.diff),
  )).concatMat(Source.maybe[Message])(Keep.right)

  def printSink: Sink[Message, Future[Done]] =
    Sink.foreach(println)

  def ordersSink(databaseSink: Sink[OrderRow, Future[Done]]): Sink[Message, Future[Done]] =
    Flow[Message]
      .map({
        case message: TextMessage.Strict  => DiffOrders.fromString(message.text)
        case _                            => Left(DiffOrders.Errors.NotTextMessage)})
      .mapConcat({
        case Right(orders)  => orders.toOrderRow
        case Left(error)    => error match {
          case DiffOrders.Errors.NotTextMessage   =>
            val t = Timestamp.valueOf(LocalDateTime.now()).toString
            println(s"[Error][NotTextMessage] $t The object received is not a TextMessage.String")
          case DiffOrders.Errors.CirceError(_, s) =>
            val t = Timestamp.valueOf(LocalDateTime.now()).toString
            println(s"[Error][Circe] $t $s")
        }
          List[OrderRow]()
      })
      .toMat(databaseSink)(Keep.right)
}
