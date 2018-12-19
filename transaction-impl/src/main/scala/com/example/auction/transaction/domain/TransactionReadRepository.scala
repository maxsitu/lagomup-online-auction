package com.example.auction.transaction.domain

import java.util.UUID

import com.datastax.driver.core.Row
import com.example.auction.transaction.impl._

import scala.concurrent.Future

trait TransactionReadRepository {

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
    TransactionSummary(
      row.getUUID("itemId"),
      row.getUUID("creatorId"),
      row.getUUID("winnerId"),
      row.getString("itemTitle"),
      row.getString("currencyId"),
      row.getInt("itemPrice"),
      row.getString("status")
    )
  }

}

