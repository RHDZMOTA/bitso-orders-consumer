package com.rhdzmota.bitso.consumer.service.database

import akka.Done
import akka.stream.scaladsl.Sink
import com.rhdzmota.bitso.consumer.model.orders.Order.OrderRow

import scala.concurrent.Future

trait Database {
  def sink: Sink[OrderRow, Future[Done]]
}
