package com.rhdzmota.bitso.consumer

import com.rhdzmota.bitso.consumer.conf.Context
import com.rhdzmota.bitso.consumer.service.bitso.Bitso
import com.rhdzmota.bitso.consumer.service.database.Database
import com.rhdzmota.bitso.consumer.service.database.impl.Cassandra
import com.rhdzmota.bitso.consumer.service.websocket.Websocket


object App extends Context {
  def main(args: Array[String]): Unit = {
    val database: Database = Cassandra
    val bitso: Bitso.type = Bitso
    Websocket.runWith(database, bitso) onComplete println
    //Websocket.print(bitso)
  }
}
