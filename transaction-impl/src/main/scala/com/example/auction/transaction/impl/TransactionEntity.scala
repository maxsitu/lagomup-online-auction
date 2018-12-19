package com.example.auction.transaction.impl

import com.example.auction.utils.JsonFormats._
import com.example.auction.transaction.domain.TransactionDomain
import akka.{Done, NotUsed}
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import com.lightbend.lagom.scaladsl.persistence.{AggregateEvent, AggregateEventTag, PersistentEntity}
import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}
import com.lightbend.lagom.scaladsl.pubsub.{PubSubRegistry, TopicId}
import play.api.libs.json.{Format, Json}
import java.time.Instant
import akka.stream.scaladsl.Source
import java.util.UUID

class TransactionEntity(val transactionEventStream: TransactionEventStream) extends PersistentEntity
  with TransactionDomain {

  override type State = TransactionState
  override type Command = TransactionCommand
  override type Event = TransactionEvent

  override def behavior: Behavior = {
    Actions()
      .onCommand[StartTransaction, Done] {
  case (command: StartTransaction, ctx, state) =>
    onStartTransaction(command, state, ctx)
}

      .onReadOnlyCommand[GetTransaction.type, Option[TransactionAggregate]] {
  case (query: GetTransaction.type, ctx, state) =>
    onGetTransaction(query, state, ctx)
}

      .onEvent {
  case (event: TransactionStarted, state) =>
    onTransactionStarted(event, state)
}

  }

}

case class TransactionAggregate(itemId: UUID, creator: UUID, winner: UUID, itemData: ItemData, itemPrice: Int, deliveryData: DeliveryData, deliveryPrice: Option[Int], payment: Option[Payment]) 

object TransactionAggregate {
  implicit val format: Format[TransactionAggregate] = Json.format
}



object TransactionAggregateStatus extends Enumeration {
  val NotStarted, NegotiatingDelivery, PaymentPending, PaymentSubmitted, PaymentConfirmed, ItemDispatched, ItemReceived, Cancelled, Refunding, Refunded = Value
  type Status = Value
  implicit val format: Format[Status] = enumFormat(TransactionAggregateStatus)
}

case class TransactionState(aggregate: Option[TransactionAggregate], status: TransactionAggregateStatus.Status)

object TransactionState {
  implicit val format: Format[TransactionState] = Json.format
}

sealed trait TransactionCommand

case class StartTransaction(transaction: TransactionAggregate) extends TransactionCommand with ReplyType[Done]

object StartTransaction {
  implicit val format: Format[StartTransaction] = Json.format
}

case object GetTransaction extends TransactionCommand with ReplyType[Option[TransactionAggregate]] {
  implicit val format: Format[GetTransaction.type] = JsonSerializer.emptySingletonFormat(GetTransaction)
}

sealed trait TransactionEvent extends AggregateEvent[TransactionEvent] {
  def aggregateTag = TransactionEvent.Tag
}

object TransactionEvent {
  val Tag = AggregateEventTag[TransactionEvent]
}

case class TransactionStarted(itemId: UUID, transaction: TransactionAggregate) extends TransactionEvent

object TransactionStarted {
  implicit val format: Format[TransactionStarted] = Json.format
}

case class ItemData(title: String, description: String, currencyId: String, increment: Int, reservePrice: Int, auctionDuration: Int, categoryId: Option[UUID]) 

object ItemData {
  implicit val format: Format[ItemData] = Json.format
}

case class DeliveryData(addressLine1: String, addressLine2: String, city: String, state: String, postalCode: Int, country: String) 

object DeliveryData {
  implicit val format: Format[DeliveryData] = Json.format
}

case class Payment(comment: String) 

object Payment {
  implicit val format: Format[Payment] = Json.format
}

trait TransactionEventStream {

  def publish(qualifier: String, event: TransactionEvent): Unit

  def subscriber(qualifier: String): Source[TransactionEvent, NotUsed]

}

class TransactionEventStreamImpl(pubSubRegistry: PubSubRegistry) extends TransactionEventStream {

  def publish(qualifier: String, event: TransactionEvent): Unit = {
    pubSubRegistry.refFor(TopicId[TransactionEvent](qualifier)).publish(event)
  }

  def subscriber(qualifier: String): Source[TransactionEvent, NotUsed] = {
    pubSubRegistry.refFor(TopicId[TransactionEvent](qualifier)).subscriber
  }

}

object TransactionSerializerRegistry extends JsonSerializerRegistry {
  override def serializers = List(
    JsonSerializer[TransactionState],
JsonSerializer[TransactionAggregate],
JsonSerializer[StartTransaction],
JsonSerializer[GetTransaction.type],
JsonSerializer[TransactionStarted],
JsonSerializer[ItemData],
JsonSerializer[DeliveryData],
JsonSerializer[Payment]
  )
}

