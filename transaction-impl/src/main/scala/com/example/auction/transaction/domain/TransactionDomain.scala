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
      case _ =>
        ???
    }
  }

  def onSetDeliveryPrice(command: SetDeliveryPrice, state: TransactionState, ctx: CommandContext[Done]): Persist = {
    state.status match {
      case TransactionAggregateStatus.NegotiatingDelivery =>
        if (command.userId == state.aggregate.get.creator) {
          ctx.thenPersist(DeliveryPriceUpdated(UUID.fromString(entityId), command.deliveryPrice))(_ => ctx.reply(Done))
        } else {
          throw Forbidden("Only the item creator can set the delivery price")
        }
      case _ =>
        ???
    }
  }

  def onApproveDeliveryDetails(command: ApproveDeliveryDetails, state: TransactionState, ctx: CommandContext[Done]): Persist = {
    state.status match {
      case TransactionAggregateStatus.NegotiatingDelivery =>
        if(command.userId == state.aggregate.get.creator) {
          if(state.aggregate.get.deliveryData.isDefined && state.aggregate.get.deliveryPrice.isDefined) {
            ctx.thenPersist(DeliveryDetailsApproved(UUID.fromString(entityId)))(_ => ctx.reply(Done))
          } else {
            throw Forbidden("Can't approve empty delivery detail")
          }
        } else {
          throw Forbidden("Only the item creator can approve the delivery details")
        }
      case _ =>
        ???
    }
  }

  def onSubmitPaymentDetails(command: SubmitPaymentDetails, state: TransactionState, ctx: CommandContext[Done]): Persist = {
    state.status match {
      case TransactionAggregateStatus.PaymentPending =>
        if (command.userId == state.aggregate.get.winner) {
          ctx.thenPersist(PaymentDetailsSubmitted(UUID.fromString(entityId), command.payment))(_ => ctx.reply(Done))
        } else {
          throw Forbidden("Only the auction winner can submit payment details")
        }
      case _ =>
        ???
    }
  }

  def onSubmitPaymentStatus(command: SubmitPaymentStatus, state: TransactionState, ctx: CommandContext[Done]): Persist = {
    state.status match {
      case TransactionAggregateStatus.PaymentSubmitted =>
        if (command.userId ==  state.aggregate.get.creator) {
          command.paymentStatus match {
            case "Approved" =>
              ctx.thenPersist(PaymentApproved(UUID.fromString(entityId)))(_ => ctx.reply(Done))
            case "Rejected" =>
              ctx.thenPersist(PaymentRejected(UUID.fromString(entityId)))(_ => ctx.reply(Done))
            case _ =>
              throw new IllegalArgumentException("Illegal payment status")
          }
        } else {
          throw Forbidden("Only the item creator can approve or reject payment")
        }
      case _ =>
        ???
    }
  }

  def onGetTransaction(query: GetTransaction, state: TransactionState, ctx: ReadOnlyCommandContext[Option[TransactionAggregate]]): Unit = {
    if (query.userId == state.aggregate.get.creator || query.userId == state.aggregate.get.winner){
      ctx.reply(state.aggregate)
    } else {
      throw Forbidden("Only the item owner and the auction winner can see transaction details")
    }
  }

  def onTransactionStarted(event: TransactionStarted, state: TransactionState): TransactionState = {
    TransactionState(Some(event.transaction), TransactionAggregateStatus.NegotiatingDelivery)
  }

  def onDeliveryDetailsSubmitted(event: DeliveryDetailsSubmitted, state: TransactionState): TransactionState = {
    val updatedAggregate = state.aggregate.get.copy(deliveryData = Some(event.deliveryData))
    state.copy(aggregate = Some(updatedAggregate))
  }

  def onDeliveryPriceUpdated(event: DeliveryPriceUpdated, state: TransactionState): TransactionState = {
    val updatedAggregate = state.aggregate.get.copy(deliveryPrice = Some(event.deliveryPrice))
    state.copy(aggregate = Some(updatedAggregate))
  }

  def onDeliveryDetailsApproved(event: DeliveryDetailsApproved, state: TransactionState): TransactionState = {
    state.copy(status = TransactionAggregateStatus.PaymentPending)
  }

  def onPaymentDetailsSubmitted(event: PaymentDetailsSubmitted, state: TransactionState): TransactionState = {
    val updatedAggregate = state.aggregate.get.copy(payment = Some(event.payment))
    state.copy(
      aggregate = Some(updatedAggregate),
      status = TransactionAggregateStatus.PaymentSubmitted
    )
  }

  def onPaymentApproved(event: PaymentApproved, state: TransactionState): TransactionState = {
    state.copy(status = TransactionAggregateStatus.PaymentConfirmed)
  }

  def onPaymentRejected(event: PaymentRejected, state: TransactionState): TransactionState = {
    state.copy(status = TransactionAggregateStatus.PaymentPending)
  }

}

