package com.rhdzmota.bitso.consumer.service.database.impl

import com.rhdzmota.bitso.consumer.model.trades.TradeRow
import com.rhdzmota.bitso.consumer.service.database.Database
import akka.stream.alpakka.cassandra.scaladsl.CassandraSink
import akka.stream.scaladsl.{Flow, Keep, Sink}
import akka.Done
import com.datastax.driver.core.{BoundStatement, Cluster, PreparedStatement, Session}
import com.rhdzmota.bitso.consumer.conf.{Context, Settings}
import com.rhdzmota.bitso.consumer.model.orders.Order.OrderRow

import scala.concurrent.Future


case object Cassandra extends Database with Context {

  implicit private val session: Session = Cluster.builder
    .addContactPoint(Settings.Cassandra.address)
    .withPort(Settings.Cassandra.port)
    .build.connect()


  private val query: String =
    s"""
       |INSERT INTO ${Settings.Cassandra.keyspaceName}.${Settings.Cassandra.orders.table}(received, arrival, book, sequence, id, milliseconds, rate, maker_action, status, amount, value)
       |VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
     """.stripMargin

  private val preparedStatement: PreparedStatement = session.prepare(query)

  private val statementBinder: (OrderRow, PreparedStatement) => BoundStatement =
    (orderRow: OrderRow, statement: PreparedStatement) => statement.bind(
      orderRow.received,
      orderRow.arrival.asInstanceOf[java.lang.Integer],
      orderRow.book,
      orderRow.sequence.asInstanceOf[java.lang.Long],
      orderRow.orderId,
      orderRow.milliseconds.asInstanceOf[java.lang.Long],
      orderRow.rate.map(_.asInstanceOf[java.lang.Double]).orNull,
      orderRow.makerAction,
      orderRow.status,
      orderRow.amount.map(_.asInstanceOf[java.lang.Double]).orNull,
      orderRow.value.map(_.asInstanceOf[java.lang.Double]).orNull
    )


  private val cassandraSink: Sink[OrderRow, Future[Done]] = CassandraSink[OrderRow](
    Settings.Cassandra.orders.parallelism,
    preparedStatement,
    statementBinder
  )

  override def sink: Sink[OrderRow, Future[Done]] = Flow[OrderRow].toMat(cassandraSink)(Keep.right)

}
