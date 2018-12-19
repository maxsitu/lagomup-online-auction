package com.example.auction.item.impl

import com.example.auction.utils.JsonFormats._
import com.example.auction.item.domain.ItemDomain
import akka.{Done, NotUsed}
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import com.lightbend.lagom.scaladsl.persistence.{AggregateEvent, AggregateEventTag, PersistentEntity}
import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}
import com.lightbend.lagom.scaladsl.pubsub.{PubSubRegistry, TopicId}
import play.api.libs.json.{Format, Json}
import java.time.Instant
import akka.stream.scaladsl.Source
import java.util.UUID

class ItemEntity(val itemEventStream: ItemEventStream) extends PersistentEntity
  with ItemDomain {

  override type State = ItemState
  override type Command = ItemCommand
  override type Event = ItemEvent

  override def behavior: Behavior = {
    Actions()
      .onCommand[CreateItem, Done] {
  case (command: CreateItem, ctx, state) =>
    onCreateItem(command, state, ctx)
}
.onCommand[StartAuction, Done] {
  case (command: StartAuction, ctx, state) =>
    onStartAuction(command, state, ctx)
}
.onCommand[UpdatePrice, Done] {
  case (command: UpdatePrice, ctx, state) =>
    onUpdatePrice(command, state, ctx)
}
.onCommand[FinishAuction, Done] {
  case (command: FinishAuction, ctx, state) =>
    onFinishAuction(command, state, ctx)
}

      .onReadOnlyCommand[GetItem.type, Option[ItemAggregate]] {
  case (query: GetItem.type, ctx, state) =>
    onGetItem(query, state, ctx)
}

      .onEvent {
  case (event: ItemCreated, state) =>
    onItemCreated(event, state)
}
.onEvent {
  case (event: AuctionStarted, state) =>
    onAuctionStarted(event, state)
}
.onEvent {
  case (event: PriceUpdated, state) =>
    onPriceUpdated(event, state)
}
.onEvent {
  case (event: AuctionFinished, state) =>
    onAuctionFinished(event, state)
}

  }

}

case class ItemAggregate(id: UUID, creator: UUID, title: String, description: String, currencyId: String, increment: Int, reservePrice: Int, price: Option[Int], status: String, auctionDuration: Int, auctionStart: Option[Instant], auctionEnd: Option[Instant], auctionWinner: Option[UUID]) 

object ItemAggregate {
  implicit val format: Format[ItemAggregate] = Json.format
}



object ItemAggregateStatus extends Enumeration {
  val NotCreated, Created, Auction, Completed, Cancelled = Value
  type Status = Value
  implicit val format: Format[Status] = enumFormat(ItemAggregateStatus)
}

case class ItemState(aggregate: Option[ItemAggregate], status: ItemAggregateStatus.Status)

object ItemState {
  implicit val format: Format[ItemState] = Json.format
}

sealed trait ItemCommand

case class CreateItem(item: ItemAggregate) extends ItemCommand with ReplyType[Done]

object CreateItem {
  implicit val format: Format[CreateItem] = Json.format
}

case class StartAuction(userId: UUID) extends ItemCommand with ReplyType[Done]

object StartAuction {
  implicit val format: Format[StartAuction] = Json.format
}

case class UpdatePrice(price: Int) extends ItemCommand with ReplyType[Done]

object UpdatePrice {
  implicit val format: Format[UpdatePrice] = Json.format
}

case class FinishAuction(winner: Option[UUID], price: Option[Int]) extends ItemCommand with ReplyType[Done]

object FinishAuction {
  implicit val format: Format[FinishAuction] = Json.format
}

case object GetItem extends ItemCommand with ReplyType[Option[ItemAggregate]] {
  implicit val format: Format[GetItem.type] = JsonSerializer.emptySingletonFormat(GetItem)
}

sealed trait ItemEvent extends AggregateEvent[ItemEvent] {
  
  def aggregateTag = ItemEvent.Tag
}

object ItemEvent {
  val Tag = AggregateEventTag[ItemEvent]
}

case class ItemCreated(item: ItemAggregate) extends ItemEvent

object ItemCreated {
  implicit val format: Format[ItemCreated] = Json.format
}

case class AuctionStarted(startTime: Instant) extends ItemEvent

object AuctionStarted {
  implicit val format: Format[AuctionStarted] = Json.format
}

case class PriceUpdated(price: Int) extends ItemEvent

object PriceUpdated {
  implicit val format: Format[PriceUpdated] = Json.format
}

case class AuctionFinished(winner: Option[UUID], price: Option[Int]) extends ItemEvent

object AuctionFinished {
  implicit val format: Format[AuctionFinished] = Json.format
}

trait ItemEventStream {

  def publish(qualifier: String, event: ItemEvent): Unit

  def subscriber(qualifier: String): Source[ItemEvent, NotUsed]

}

class ItemEventStreamImpl(pubSubRegistry: PubSubRegistry) extends ItemEventStream {

  def publish(qualifier: String, event: ItemEvent): Unit = {
    pubSubRegistry.refFor(TopicId[ItemEvent](qualifier)).publish(event)
  }

  def subscriber(qualifier: String): Source[ItemEvent, NotUsed] = {
    pubSubRegistry.refFor(TopicId[ItemEvent](qualifier)).subscriber
  }

}

object ItemSerializerRegistry extends JsonSerializerRegistry {
  override def serializers = List(
    JsonSerializer[ItemState],
JsonSerializer[ItemAggregate],
JsonSerializer[CreateItem],
JsonSerializer[StartAuction],
JsonSerializer[UpdatePrice],
JsonSerializer[FinishAuction],
JsonSerializer[GetItem.type],
JsonSerializer[ItemCreated],
JsonSerializer[AuctionStarted],
JsonSerializer[PriceUpdated],
JsonSerializer[AuctionFinished]
  )
}

