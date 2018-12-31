package com.example.auction.transaction.impl

import com.example.auction.transaction.domain.TransactionRepository
import akka.stream.scaladsl.Source
import akka.{ Done, NotUsed }
import com.datastax.driver.core.{ BoundStatement, PreparedStatement }
import com.lightbend.lagom.scaladsl.persistence.ReadSideProcessor
import com.lightbend.lagom.scaladsl.persistence.cassandra.{ CassandraReadSide, CassandraSession }
import scala.concurrent.{ ExecutionContext, Future }
import play.api.libs.json.{ Format, Json }
import java.util.UUID
import java.time.Instant

class TransactionRepositoryImpl(db: CassandraSession, readSide: CassandraReadSide, val akkaComponents: AkkaComponents)
  extends ReadSideProcessor[TransactionEvent] with TransactionRepository {

  // Tables
  val createTransactionUsersTableStatement = """CREATE TABLE IF NOT EXISTS transactionUsers (
  itemId timeuuid PRIMARY KEY,
  creatorId UUID,
  winnerId UUID
)
"""
  val createTransactionSummaryByUserTableStatement = """CREATE TABLE IF NOT EXISTS transactionSummaryByUser (
  userId UUID,
  itemId timeuuid,
  creatorId UUID,
  winnerId UUID,
  itemTitle text,
  currencyId text,
  itemPrice int,
  status text,
  PRIMARY KEY (userId, itemId)
)
WITH CLUSTERING ORDER BY (itemId DESC)
"""
  val createTransactionSummaryByUserAndStatusTableStatement = """CREATE MATERIALIZED VIEW IF NOT EXISTS transactionSummaryByUserAndStatus AS
  SELECT * FROM transactionSummaryByUser
  WHERE status IS NOT NULL AND itemId IS NOT NULL
PRIMARY KEY (userId, status, itemId)
WITH CLUSTERING ORDER BY (status ASC, itemId DESC)
"""

  // Writes
  val insertTransactionUserStatement = """INSERT INTO transactionUsers (
  itemId, creatorId, winnerId)
VALUES (?, ?, ?)
"""
  var insertTransactionUser: PreparedStatement = _
  val insertTransactionSummaryByUserStatement = """INSERT INTO transactionSummaryByUser(
  userId,
  itemId,
  creatorId,
  winnerId,
  itemTitle,
  currencyId,
  itemPrice,
  status
) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
"""
  var insertTransactionSummaryByUser: PreparedStatement = _
  val updateTransactionSummaryStatusStatement = """UPDATE transactionSummaryByUser
SET status = ?
WHERE userId = ? AND itemId = ?
"""
  var updateTransactionSummaryStatus: PreparedStatement = _

  // Reads
  val selectTransactionUserStatement = """SELECT * FROM transactionUsers WHERE itemId = ?"""
  var selectTransactionUser: PreparedStatement = _
  val countUserTransactionsStatement = """SELECT COUNT(*)
FROM transactionSummaryByUserAndStatus
WHERE userId = ? AND status = ?
ORDER BY status ASC, itemId DESC
"""
  var countUserTransactions: PreparedStatement = _
  val selectUserTransactionsStatement = """SELECT * FROM transactionSummaryByUserAndStatus
WHERE userId = ? AND status = ?
ORDER BY status ASC, itemId DESC
LIMIT ?
"""
  var selectUserTransactions: PreparedStatement = _

  def selectTransactionUser(itemId: UUID): Future[Option[TransactionUser]] = {
    db.selectOne(bindSelectTransactionUser(itemId)).map(_.map(mapSelectTransactionUserResult))
  }

  def countUserTransactions(userId: UUID, status: String): Future[Option[Int]] = {
    db.selectOne(bindCountUserTransactions(userId, status)).map(_.map(mapCountUserTransactionsResult))
  }

  def selectUserTransactions(userId: UUID, status: String, limit: Int): Future[Seq[TransactionSummary]] = {
    db.selectAll(bindSelectUserTransactions(userId, status, limit)).map(_.map(mapSelectUserTransactionsResult))
  }

  def bindInsertTransactionUser(itemId: UUID, creatorId: UUID, winnerId: UUID): BoundStatement = {
    val boundStatement = insertTransactionUser.bind()
    boundStatement.setUUID("itemId", itemId)
    boundStatement.setUUID("creatorId", creatorId)
    boundStatement.setUUID("winnerId", winnerId)
    boundStatement
  }

  def bindInsertTransactionSummaryByUser(userId: UUID, itemId: UUID, creatorId: UUID, winnerId: UUID, itemTitle: String, currencyId: String, itemPrice: Int, status: String): BoundStatement = {
    val boundStatement = insertTransactionSummaryByUser.bind()
    boundStatement.setUUID("userId", userId)
    boundStatement.setUUID("itemId", itemId)
    boundStatement.setUUID("creatorId", creatorId)
    boundStatement.setUUID("winnerId", winnerId)
    boundStatement.setString("itemTitle", itemTitle)
    boundStatement.setString("currencyId", currencyId)
    boundStatement.setInt("itemPrice", itemPrice)
    boundStatement.setString("status", status)
    boundStatement
  }

  def bindUpdateTransactionSummaryStatus(status: String, userId: UUID, itemId: UUID): BoundStatement = {
    val boundStatement = updateTransactionSummaryStatus.bind()
    boundStatement.setString("status", status)
    boundStatement.setUUID("userId", userId)
    boundStatement.setUUID("itemId", itemId)
    boundStatement
  }

  def bindSelectTransactionUser(itemId: UUID): BoundStatement = {
    val boundStatement = selectTransactionUser.bind()
    boundStatement.setUUID("itemId", itemId)

    boundStatement
  }

  def bindCountUserTransactions(userId: UUID, status: String): BoundStatement = {
    val boundStatement = countUserTransactions.bind()
    boundStatement.setUUID("userId", userId)
    boundStatement.setString("status", status)

    boundStatement
  }

  def bindSelectUserTransactions(userId: UUID, status: String, limit: Int): BoundStatement = {
    val boundStatement = selectUserTransactions.bind()
    boundStatement.setUUID("userId", userId)
    boundStatement.setString("status", status)
    boundStatement.setInt("[LIMIT]", limit)
    boundStatement
  }

  override def buildHandler() = readSide.builder[TransactionEvent]("transaction_offset")
    .setGlobalPrepare(() =>
      for {
        _ <- db.executeCreateTable(createTransactionUsersTableStatement)
        _ <- db.executeCreateTable(createTransactionSummaryByUserTableStatement)
        _ <- db.executeCreateTable(createTransactionSummaryByUserAndStatusTableStatement)
      } yield Done)
    .setPrepare(_ =>
      for {
        _insertTransactionUser <- db.prepare(insertTransactionUserStatement)
        _insertTransactionSummaryByUser <- db.prepare(insertTransactionSummaryByUserStatement)
        _updateTransactionSummaryStatus <- db.prepare(updateTransactionSummaryStatusStatement)
        _selectTransactionUser <- db.prepare(selectTransactionUserStatement)
        _countUserTransactions <- db.prepare(countUserTransactionsStatement)
        _selectUserTransactions <- db.prepare(selectUserTransactionsStatement)
      } yield {
        insertTransactionUser = _insertTransactionUser
        insertTransactionSummaryByUser = _insertTransactionSummaryByUser
        updateTransactionSummaryStatus = _updateTransactionSummaryStatus
        selectTransactionUser = _selectTransactionUser
        countUserTransactions = _countUserTransactions
        selectUserTransactions = _selectUserTransactions
        Done
      })
    .setEventHandler[TransactionStarted](e => processTransactionStarted(e.entityId, e.event))
    .setEventHandler[DeliveryDetailsSubmitted](e => processDeliveryDetailsSubmitted(e.entityId, e.event))
    .setEventHandler[DeliveryPriceUpdated](e => processDeliveryPriceUpdated(e.entityId, e.event))
    .setEventHandler[DeliveryDetailsApproved](e => processDeliveryDetailsApproved(e.entityId, e.event))
    .setEventHandler[PaymentDetailsSubmitted](e => processPaymentDetailsSubmitted(e.entityId, e.event))
    .setEventHandler[PaymentApproved](e => processPaymentApproved(e.entityId, e.event))
    .setEventHandler[PaymentRejected](e => processPaymentRejected(e.entityId, e.event))
    .build()

  override def aggregateTags = Set(TransactionEvent.Tag)

}

case class TransactionUser(itemId: UUID, creatorId: UUID, winnerId: UUID)

object TransactionUser {
  implicit val format: Format[TransactionUser] = Json.format
}

case class TransactionSummary(itemId: UUID, creatorId: UUID, winnerId: UUID, itemTitle: String, currencyId: String, itemPrice: Int, status: String)

object TransactionSummary {
  implicit val format: Format[TransactionSummary] = Json.format
}

