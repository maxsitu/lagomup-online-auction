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
      .onReadOnlyCommand[StateSnapshot.type, TransactionState] {
      case (_: StateSnapshot.type , ctx, state) =>
        ctx.reply(state)
    }
      .onCommand[StartTransaction, Done] {
  case (command: StartTransaction, ctx, state) =>
    onStartTransaction(command, state, ctx)
}
.onCommand[SubmitDeliveryDetails, Done] {
  case (command: SubmitDeliveryDetails, ctx, state) =>
    onSubmitDeliveryDetails(command, state, ctx)
}
.onCommand[SetDeliveryPrice, Done] {
  case (command: SetDeliveryPrice, ctx, state) =>
    onSetDeliveryPrice(command, state, ctx)
}
.onCommand[ApproveDeliveryDetails, Done] {
  case (command: ApproveDeliveryDetails, ctx, state) =>
    onApproveDeliveryDetails(command, state, ctx)
}
.onCommand[SubmitPaymentDetails, Done] {
  case (command: SubmitPaymentDetails, ctx, state) =>
    onSubmitPaymentDetails(command, state, ctx)
}
.onCommand[SubmitPaymentStatus, Done] {
  case (command: SubmitPaymentStatus, ctx, state) =>
    onSubmitPaymentStatus(command, state, ctx)
}

      .onReadOnlyCommand[GetTransaction, Option[TransactionAggregate]] {
  case (query: GetTransaction, ctx, state) =>
    onGetTransaction(query, state, ctx)
}

      .onEvent {
  case (event: TransactionStarted, state) =>
    onTransactionStarted(event, state)
}
.onEvent {
  case (event: DeliveryDetailsSubmitted, state) =>
    onDeliveryDetailsSubmitted(event, state)
}
.onEvent {
  case (event: DeliveryPriceUpdated, state) =>
    onDeliveryPriceUpdated(event, state)
}
.onEvent {
  case (event: DeliveryDetailsApproved, state) =>
    onDeliveryDetailsApproved(event, state)
}
.onEvent {
  case (event: PaymentDetailsSubmitted, state) =>
    onPaymentDetailsSubmitted(event, state)
}
.onEvent {
  case (event: PaymentApproved, state) =>
    onPaymentApproved(event, state)
}
.onEvent {
  case (event: PaymentRejected, state) =>
    onPaymentRejected(event, state)
}

  }

}

case class TransactionAggregate(itemId: UUID, creator: UUID, winner: UUID, itemData: ItemData, itemPrice: Int, deliveryData: Option[DeliveryData], deliveryPrice: Option[Int], payment: Option[Payment])

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

case object StateSnapshot extends TransactionCommand with ReplyType[TransactionState] {
  implicit val format: Format[StateSnapshot.type] = JsonSerializer.emptySingletonFormat(StateSnapshot)
}

case class StartTransaction(transaction: TransactionAggregate) extends TransactionCommand with ReplyType[Done]

object StartTransaction {
  implicit val format: Format[StartTransaction] = Json.format
}

case class SubmitDeliveryDetails(userId: UUID, deliveryData: DeliveryData) extends TransactionCommand with ReplyType[Done]

object SubmitDeliveryDetails {
  implicit val format: Format[SubmitDeliveryDetails] = Json.format
}

case class SetDeliveryPrice(userId: UUID, deliveryPrice: Int) extends TransactionCommand with ReplyType[Done]

object SetDeliveryPrice {
  implicit val format: Format[SetDeliveryPrice] = Json.format
}

case class ApproveDeliveryDetails(userId: UUID) extends TransactionCommand with ReplyType[Done]

object ApproveDeliveryDetails {
  implicit val format: Format[ApproveDeliveryDetails] = Json.format
}

case class SubmitPaymentDetails(userId: UUID, payment: Payment) extends TransactionCommand with ReplyType[Done]

object SubmitPaymentDetails {
  implicit val format: Format[SubmitPaymentDetails] = Json.format
}

case class SubmitPaymentStatus(userId: UUID, paymentStatus: String) extends TransactionCommand with ReplyType[Done]

object SubmitPaymentStatus {
  implicit val format: Format[SubmitPaymentStatus] = Json.format
}

case class GetTransaction(userId: UUID) extends TransactionCommand with ReplyType[Option[TransactionAggregate]]

object GetTransaction {
  implicit val format: Format[GetTransaction] = Json.format
}

sealed trait TransactionEvent extends AggregateEvent[TransactionEvent] {
  val itemId: UUID

  def aggregateTag = TransactionEvent.Tag
}

object TransactionEvent {
  val Tag = AggregateEventTag[TransactionEvent]
}

case class TransactionStarted(itemId: UUID, transaction: TransactionAggregate) extends TransactionEvent

object TransactionStarted {
  implicit val format: Format[TransactionStarted] = Json.format
}

case class DeliveryDetailsSubmitted(itemId: UUID, deliveryData: DeliveryData) extends TransactionEvent

object DeliveryDetailsSubmitted {
  implicit val format: Format[DeliveryDetailsSubmitted] = Json.format
}

case class DeliveryPriceUpdated(itemId: UUID, deliveryPrice: Int) extends TransactionEvent

object DeliveryPriceUpdated {
  implicit val format: Format[DeliveryPriceUpdated] = Json.format
}

case class DeliveryDetailsApproved(itemId: UUID) extends TransactionEvent

object DeliveryDetailsApproved {
  implicit val format: Format[DeliveryDetailsApproved] = Json.format
}

case class PaymentDetailsSubmitted(itemId: UUID, payment: Payment) extends TransactionEvent

object PaymentDetailsSubmitted {
  implicit val format: Format[PaymentDetailsSubmitted] = Json.format
}

case class PaymentApproved(itemId: UUID) extends TransactionEvent

object PaymentApproved {
  implicit val format: Format[PaymentApproved] = Json.format
}

case class PaymentRejected(itemId: UUID) extends TransactionEvent

object PaymentRejected {
  implicit val format: Format[PaymentRejected] = Json.format
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
    JsonSerializer[StateSnapshot.type],
    JsonSerializer[TransactionState],
JsonSerializer[TransactionAggregate],
JsonSerializer[StartTransaction],
JsonSerializer[SubmitDeliveryDetails],
JsonSerializer[SetDeliveryPrice],
JsonSerializer[ApproveDeliveryDetails],
JsonSerializer[SubmitPaymentDetails],
JsonSerializer[SubmitPaymentStatus],
JsonSerializer[GetTransaction],
JsonSerializer[TransactionStarted],
JsonSerializer[DeliveryDetailsSubmitted],
JsonSerializer[DeliveryPriceUpdated],
JsonSerializer[DeliveryDetailsApproved],
JsonSerializer[PaymentDetailsSubmitted],
JsonSerializer[PaymentApproved],
JsonSerializer[PaymentRejected],
JsonSerializer[ItemData],
JsonSerializer[DeliveryData],
JsonSerializer[Payment]
  )
}

