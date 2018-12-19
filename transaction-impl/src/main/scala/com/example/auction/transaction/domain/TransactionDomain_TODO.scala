package com.example.auction.transaction.domain

import com.example.auction.transaction.impl._
import akka.Done
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity

trait TransactionDomain_TODO {
  this: TransactionEntity with PersistentEntity =>

  def transactionEventStream: TransactionEventStream

  def initialState: TransactionState = ???

  def onStartTransaction(command: StartTransaction, state: TransactionState, ctx: CommandContext[Done]): Persist = {
  ???
}

def onSubmitDeliveryDetails(command: SubmitDeliveryDetails, state: TransactionState, ctx: CommandContext[Done]): Persist = {
  ???
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
  ???
}

def onDeliveryDetailsSubmitted(event: DeliveryDetailsSubmitted, state: TransactionState): TransactionState = {
  ???
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

