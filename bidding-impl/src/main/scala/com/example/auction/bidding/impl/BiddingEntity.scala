package com.example.auction.bidding.impl

import com.example.auction.utils.JsonFormats._
import com.example.auction.bidding.domain.BiddingDomain
import akka.{Done, NotUsed}
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import com.lightbend.lagom.scaladsl.persistence.{AggregateEvent, AggregateEventTag, PersistentEntity}
import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}
import com.lightbend.lagom.scaladsl.pubsub.PubSubRegistry
import play.api.libs.json.{Format, Json}
import java.time.Instant

class BiddingEntity(val pubSubRegistry: PubSubRegistry) extends PersistentEntity
  with BiddingDomain {

  override type State = BiddingState
  override type Command = BiddingCommand
  override type Event = BiddingEvent

  override def behavior: Behavior = {
    Actions()
      .onCommand[StartAuction, Done] {
  case (command: StartAuction, ctx, state) =>
    onStartAuction(command, state, ctx)
}
.onCommand[CancelAuction.type, Done] {
  case (command: CancelAuction.type, ctx, state) =>
    onCancelAuction(command, state, ctx)
}
.onCommand[PlaceBid, PlaceBidResult] {
  case (command: PlaceBid, ctx, state) =>
    onPlaceBid(command, state, ctx)
}
.onCommand[FinishBidding.type, Done] {
  case (command: FinishBidding.type, ctx, state) =>
    onFinishBidding(command, state, ctx)
}

      .onReadOnlyCommand[GetAuction.type, AuctionAggregate] {
  case (query: GetAuction.type, ctx, state) =>
    onGetAuction(query, state, ctx)
}

      .onEvent {
  case (event: AuctionStarted, state) =>
    onAuctionStarted(event, state)
}
.onEvent {
  case (event: AuctionCancelled.type, state) =>
    onAuctionCancelled(event, state)
}
.onEvent {
  case (event: BidPlaced, state) =>
    onBidPlaced(event, state)
}
.onEvent {
  case (event: BiddingFinished.type, state) =>
    onBiddingFinished(event, state)
}

  }

}

case class AuctionAggregate(auction: Auction, status: String, biddingHistory: List[Bid]) 

object AuctionAggregate {
  implicit val format: Format[AuctionAggregate] = Json.format
}



object AuctionAggregateStatus extends Enumeration {
  val NotStarted, UnderAuction, Complete, Cancelled = Value
  type Status = Value
  implicit val format: Format[Status] = enumFormat(AuctionAggregateStatus)
}

case class BiddingState(aggregate: Option[AuctionAggregate], status: AuctionAggregateStatus.Status)

object BiddingState {
  implicit val format: Format[BiddingState] = Json.format
}

sealed trait BiddingCommand

case class StartAuction(auction: Auction) extends BiddingCommand with ReplyType[Done]

object StartAuction {
  implicit val format: Format[StartAuction] = Json.format
}

case object CancelAuction extends BiddingCommand with ReplyType[Done] {
  implicit val format: Format[CancelAuction.type] = JsonSerializer.emptySingletonFormat(CancelAuction)
}

case class PlaceBid(bidPrice: Int, bidder: String) extends BiddingCommand with ReplyType[PlaceBidResult]

object PlaceBid {
  implicit val format: Format[PlaceBid] = Json.format
}

case object FinishBidding extends BiddingCommand with ReplyType[Done] {
  implicit val format: Format[FinishBidding.type] = JsonSerializer.emptySingletonFormat(FinishBidding)
}

case object GetAuction extends BiddingCommand with ReplyType[AuctionAggregate] {
  implicit val format: Format[GetAuction.type] = JsonSerializer.emptySingletonFormat(GetAuction)
}

sealed trait BiddingEvent extends AggregateEvent[BiddingEvent] {
  def aggregateTag = BiddingEvent.Tag
}

object BiddingEvent {
  val Tag = AggregateEventTag[BiddingEvent]
}

case class AuctionStarted(auction: Auction) extends BiddingEvent

object AuctionStarted {
  implicit val format: Format[AuctionStarted] = Json.format
}

case object AuctionCancelled extends BiddingEvent {
  implicit val format: Format[AuctionCancelled.type] = JsonSerializer.emptySingletonFormat(AuctionCancelled)
}

case class BidPlaced(bid: Bid) extends BiddingEvent

object BidPlaced {
  implicit val format: Format[BidPlaced] = Json.format
}

case object BiddingFinished extends BiddingEvent {
  implicit val format: Format[BiddingFinished.type] = JsonSerializer.emptySingletonFormat(BiddingFinished)
}

case class PlaceBidResult(status: String, currentPrice: Int, currentBidder: Option[String]) 

object PlaceBidResult {
  implicit val format: Format[PlaceBidResult] = Json.format
}

case class Auction(itemId: String, creator: String, reservePrice: Int, increment: Int, startTime: Instant, endTime: Instant) 

object Auction {
  implicit val format: Format[Auction] = Json.format
}

case class Bid(bidder: String, bidTime: Instant, bidPrice: Int, maximumBid: Int) 

object Bid {
  implicit val format: Format[Bid] = Json.format
}

object BiddingSerializerRegistry extends JsonSerializerRegistry {
  override def serializers = List(
    JsonSerializer[BiddingState],
JsonSerializer[AuctionAggregate],
JsonSerializer[StartAuction],
JsonSerializer[CancelAuction.type],
JsonSerializer[PlaceBid],
JsonSerializer[FinishBidding.type],
JsonSerializer[GetAuction.type],
JsonSerializer[AuctionStarted],
JsonSerializer[AuctionCancelled.type],
JsonSerializer[BidPlaced],
JsonSerializer[BiddingFinished.type],
JsonSerializer[PlaceBidResult],
JsonSerializer[Auction],
JsonSerializer[Bid]
  )
}

