package com.rhdzmota.bitso.consumer.model.implicits

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder
import com.rhdzmota.bitso.consumer.model.trades._
import com.rhdzmota.bitso.consumer.model.orders.Order._

object Decoders {
  implicit val decodeSingleTrade: Decoder[SingleTrade]  = deriveDecoder[SingleTrade]
  implicit val decodeTrades: Decoder[Trades]            = deriveDecoder[Trades]

  // SingleOrder ADT
  implicit val decodeOpen: Decoder[SingleOrder.Open] = deriveDecoder[SingleOrder.Open]
  implicit val decodeChangeStatus: Decoder[SingleOrder.ChangeStatus] = deriveDecoder[SingleOrder.ChangeStatus]
  implicit val decodeSingleOrder: Decoder[SingleOrder] =
    Decoder[SingleOrder.Open].map[SingleOrder](identity).or(Decoder[SingleOrder.ChangeStatus].map[SingleOrder](identity))

  // DiffOrders
  implicit val decodeDiffOrders: Decoder[DiffOrders] = deriveDecoder[DiffOrders]
}
