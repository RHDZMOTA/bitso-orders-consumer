package com.rhdzmota.bitso.consumer.conf

import com.typesafe.config.{Config, ConfigFactory}

object Settings {
  private val app: Config = ConfigFactory.load().getConfig("application")

  object Cassandra {
    private val cassandra: Config = app.getConfig("cassandra")
    case class TableObj(name: String) {
      private val config: Config = cassandra.getConfig(name)
      val table: String = config.getString("table")
      val parallelism: Int = config.getInt("parallelism")
    }
    val address: String = cassandra.getString("address")
    val port: Int       = cassandra.getInt("port")
    val keyspaceName: String  = cassandra.getString("keyspaceName")
    val trades: TableObj = TableObj("trades")
    val orders: TableObj = TableObj("orders")
  }

  object Bitso {
    private val bitso: Config = app.getConfig("bitso")
    val ws: String = bitso.getString("ws")

    object Subscription {
      private val subs: Config = bitso.getConfig("subscription")

      object Json {
        private val json: Config = subs.getConfig("json")
        val string: String = json.getString("string")
        object Replace {
          private val replace: Config = json.getConfig("replace")
          val book: String = replace.getString("book")
          val `type`: String = replace.getString("type")
        }
      }

      object Labels {
        private val labels: Config = subs.getConfig("labels")
        val orders: String = labels.getString("orders")
        val trades: String = labels.getString("trades")
        val diff: String = labels.getString("diff")
      }
    }

    object Coins {
      private val coins: Config = bitso.getConfig("coins")
      case class BaseCoin(name: String, units: String) {
        private val baseConfig: Config = coins.getConfig(name)
        case class UnitsCoin() {
          private val unitsConfig: Config = baseConfig.getConfig(units)
          private val label: String = unitsConfig.getString("label")
          private val jsonString: String = Subscription.Json.string
            .replace(Subscription.Json.Replace.book, label)
          val orders: String = jsonString
            .replace(Subscription.Json.Replace.`type`, Subscription.Labels.orders)
          val trades: String = jsonString
            .replace(Subscription.Json.Replace.`type`, Subscription.Labels.trades)
          val diff: String = jsonString
            .replace(Subscription.Json.Replace.`type`, Subscription.Labels.diff)
        }
        val exchange: UnitsCoin = UnitsCoin()
      }
      val btcmxn: BaseCoin = BaseCoin("btc", "mxn")
      val ethmxn: BaseCoin = BaseCoin("eth", "mxn")
      val xrpmxn: BaseCoin = BaseCoin("xrp", "mxn")
      val ltcmxn: BaseCoin = BaseCoin("ltc", "mxn")
      val bchmxn: BaseCoin = BaseCoin("bch", "mxn")
      val tusdmxn: BaseCoin = BaseCoin("tusd", "mxn")
    }

  }
}