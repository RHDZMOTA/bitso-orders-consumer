package com.rhdzmota.bitso.consumer.service.websocket

import java.util.concurrent.atomic.AtomicInteger

import akka.Done
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.ws.{Message, WebSocketRequest, WebSocketUpgradeResponse}
import akka.http.scaladsl.settings.ClientConnectionSettings
import akka.stream.scaladsl.{Flow, Keep}
import akka.util.ByteString
import com.rhdzmota.bitso.consumer.conf.{Context, Settings}
import com.rhdzmota.bitso.consumer.service.bitso.Bitso
import com.rhdzmota.bitso.consumer.service.database.Database

import scala.concurrent.{Future, Promise}

case object Websocket extends Context {

  val webSocketSettings: ClientConnectionSettings = {
    val defaultSettings = ClientConnectionSettings(actorSystem)
    val pingCounter = new AtomicInteger()
    val customWebsocketSettings = defaultSettings.websocketSettings.withPeriodicKeepAliveData(
      () => ByteString(s"${pingCounter.incrementAndGet()}"))
    defaultSettings.withWebsocketSettings(customWebsocketSettings)
  }

  def print(bitso: Bitso.type): Future[WebSocketUpgradeResponse] = {
    val webSocketFlow: Flow[Message, Message, (Future[Done], Promise[Option[Message]])] =
      Flow.fromSinkAndSourceMat(bitso.printSink, bitso.ordersSource)(Keep.both)
    val (upgradeResponse: Future[WebSocketUpgradeResponse], (sinkClose: Future[Done], sourceClose: Promise[Option[Message]]))=
      Http().singleWebSocketRequest(
        WebSocketRequest(Settings.Bitso.ws),
        webSocketFlow,
        Http().defaultClientHttpsContext,
        None,
        webSocketSettings,
        actorSystem.log
      )
    upgradeResponse
  }

  def runWith(database: Database, bitso: Bitso.type): Future[WebSocketUpgradeResponse] = {

    // Define the webSocket flow
    val webSocketFlow: Flow[Message, Message, (Future[Done], Promise[Option[Message]])] =
      Flow.fromSinkAndSourceMat(bitso.ordersSink(database.sink), bitso.ordersSource)(Keep.both)

    // Create request and run
    val (upgradeResponse: Future[WebSocketUpgradeResponse], (sinkClose: Future[Done], sourceClose: Promise[Option[Message]]))=
      Http().singleWebSocketRequest(
        WebSocketRequest(Settings.Bitso.ws),
        webSocketFlow,
        Http().defaultClientHttpsContext,
        None,
        webSocketSettings,
        actorSystem.log
      )

    // Connection Failure - recursive solution
    val recursiveUpgradeResponse: Future[WebSocketUpgradeResponse] = upgradeResponse.flatMap {upgrade =>
      if (upgrade.response.status == StatusCodes.SwitchingProtocols) Future.successful(upgrade)
      else {
        println(s"Connection failed: ${upgrade.response.status}")
        runWith(database, bitso)
      }
    }
    recursiveUpgradeResponse
  }
}
