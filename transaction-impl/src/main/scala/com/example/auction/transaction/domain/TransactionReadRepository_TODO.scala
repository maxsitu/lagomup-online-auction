package com.example.auction.transaction.domain

import com.example.auction.transaction.impl._
import akka.NotUsed
import akka.stream.scaladsl.Source
import com.datastax.driver.core.Row
import scala.concurrent.Future
import java.util.UUID

trait TransactionReadRepository_TODO {

  def selectTransactionUser(itemId: UUID): Future[Option[TransactionUser]]

def countUserTransactions(userId: UUID, status: String): Future[Option[Int]]

def selectUserTransactions(userId: UUID, status: String, limit: Int): Future[Seq[TransactionSummary]]



  def mapSelectTransactionUserResult(row: Row): TransactionUser = {
  ???
}

def mapCountUserTransactionsResult(row: Row): Int = {
  ???
}

def mapSelectUserTransactionsResult(row: Row): TransactionSummary = {
  ???
}



}

