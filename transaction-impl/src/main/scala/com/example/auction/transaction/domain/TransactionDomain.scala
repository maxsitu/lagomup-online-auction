package com.example.auction.transaction.domain

import java.util.UUID

import akka.Done
import com.example.auction.transaction.impl._
import com.lightbend.lagom.scaladsl.api.transport.Forbidden
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity

trait TransactionDomain {
  this: TransactionEntity with PersistentEntity =>

  def transactionEventStream: TransactionEventStream

  def initialState: TransactionState = TransactionState(None, TransactionAggregateStatus.NotStarted)

  def onStartTransaction(command: StartTransaction, state: TransactionState, ctx: CommandContext[Done]): Persist = {
    state.status match {
      case TransactionAggregateStatus.NotStarted =>
        ctx.thenPersist(TransactionStarted(UUID.fromString(entityId), command.transaction))(_ => ctx.reply(Done))
      case _ =>
        ???
    }
  }

  def onSubmitDeliveryDetails(command: SubmitDeliveryDetails, state: TransactionState, ctx: CommandContext[Done]): Persist = {
    state.status match {
      case TransactionAggregateStatus.NegotiatingDelivery =>
        if (command.userId == state.aggregate.get.winner) {
          ctx.thenPersist(DeliveryDetailsSubmitted(UUID.fromString(entityId), command.deliveryData))(_ => ctx.reply(Done))
        } else {
          throw Forbidden("Only the item creator can set the delivery price")
        }
    }
  }

  def onSetDeliveryPrice(command: SetDeliveryPrice, state: TransactionState, ctx: CommandContext[Done]): Persist = {
    ???
  }

  def onApproveDeliveryDetails(command: ApproveDeliveryDetails, state: TransactionState, ctx: CommandContext[Done]): Persist = {
    ???
  }

  def onSubmitPaymentDetails(command: SubmitPaymentDetails, state: TransactionState, ctx: CommandContext[Done]): Persist = {
    ???
  }

  def onSubmitPaymentStatus(command: SubmitPaymentStatus, state: TransactionState, ctx: CommandContext[Done]): Persist = {
    ???
  }

  def onGetTransaction(query: GetTransaction.type, state: TransactionState, ctx: ReadOnlyCommandContext[Option[TransactionAggregate]]): Unit = {
    ???
  }

  def onTransactionStarted(event: TransactionStarted, state: TransactionState): TransactionState = {
    TransactionState(Some(event.transaction), TransactionAggregateStatus.NegotiatingDelivery)
  }

  def onDeliveryDetailsSubmitted(event: DeliveryDetailsSubmitted, state: TransactionState): TransactionState = {
    val updatedAggregate = state.aggregate.get.copy(deliveryData = Some(event.deliveryData))
    state.copy(aggregate = Some(updatedAggregate))
  }

  def onDeliveryPriceUpdated(event: DeliveryPriceUpdated, state: TransactionState): TransactionState = {
    ???
  }

  def onDeliveryDetailsApproved(event: DeliveryDetailsApproved, state: TransactionState): TransactionState = {
    ???
  }

  def onPaymentDetailsSubmitted(event: PaymentDetailsSubmitted, state: TransactionState): TransactionState = {
    ???
  }

  def onPaymentApproved(event: PaymentApproved, state: TransactionState): TransactionState = {
    ???
  }

  def onPaymentRejected(event: PaymentRejected, state: TransactionState): TransactionState = {
    ???
  }

}

