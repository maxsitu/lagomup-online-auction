package com.example.auction.transaction.domain

import com.example.auction.transaction.impl._
import akka.Done
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity

trait TransactionDomain {
  this: TransactionEntity with PersistentEntity =>

  def transactionEventStream: TransactionEventStream

  def initialState: TransactionState = ???


  def onGetTransaction(query: GetTransaction.type, state: TransactionState, ctx: ReadOnlyCommandContext[Option[TransactionAggregate]]): Unit = {
  ???
}




}

