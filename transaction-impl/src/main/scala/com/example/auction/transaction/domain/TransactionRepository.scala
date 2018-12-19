package com.example.auction.transaction.domain

import java.util.UUID

import com.datastax.driver.core.BoundStatement
import com.example.auction.transaction.impl._

import scala.concurrent.{ExecutionContext, Future}

trait TransactionRepository extends TransactionReadRepository {

  val akkaComponents: AkkaComponents
  implicit val ec: ExecutionContext = akkaComponents.ec

  def bindInsertTransactionUser(itemId: UUID, creatorId: UUID, winnerId: UUID): BoundStatement

  def bindInsertTransactionSummaryByUser(userId: UUID, itemId: UUID, creatorId: UUID, winnerId: UUID, itemTitle: String, currencyId: String, itemPrice: Int, status: String): BoundStatement

  def bindUpdateTransactionSummaryStatus(status: String, userId: UUID, itemId: UUID): BoundStatement

  def processTransactionStarted(entityId: String, event: TransactionStarted): Future[List[BoundStatement]] = {
    val insertUser = bindInsertTransactionUser(event.itemId, event.transaction.creator, event.transaction.winner)
    val creatorSummary = bindInsertTransactionSummaryByUser(
      userId = event.transaction.creator,
      itemId = event.itemId,
      creatorId = event.transaction.creator,
      winnerId = event.transaction.winner,
      itemTitle = event.transaction.itemData.title,
      currencyId = event.transaction.itemData.currencyId,
      itemPrice = event.transaction.itemPrice,
      status = "NegotiatingDelivery"
    )
    val winnerSummary = bindInsertTransactionSummaryByUser(
      userId = event.transaction.winner,
      itemId = event.itemId,
      creatorId = event.transaction.creator,
      winnerId = event.transaction.winner,
      itemTitle = event.transaction.itemData.title,
      currencyId = event.transaction.itemData.currencyId,
      itemPrice = event.transaction.itemPrice,
      status = "NegotiatingDelivery"
    )
    Future.successful(List(insertUser, creatorSummary, winnerSummary))
  }

  def processDeliveryDetailsSubmitted(entityId: String, event: DeliveryDetailsSubmitted): Future[List[BoundStatement]] = {
    Future.successful(List.empty)
  }


  def processDeliveryPriceUpdated(entityId: String, event: DeliveryPriceUpdated): Future[List[BoundStatement]] = {
    Future.successful(List.empty)
  }


  def processDeliveryDetailsApproved(entityId: String, event: DeliveryDetailsApproved): Future[List[BoundStatement]] = {
    updateTransactionSummaryStatus(event.itemId, "PaymentPending")
  }


  def processPaymentDetailsSubmitted(entityId: String, event: PaymentDetailsSubmitted): Future[List[BoundStatement]] = {
    updateTransactionSummaryStatus(event.itemId, "PaymentSubmitted")
  }


  def processPaymentApproved(entityId: String, event: PaymentApproved): Future[List[BoundStatement]] = {
    updateTransactionSummaryStatus(event.itemId, "PaymentConfirmed")
  }


  def processPaymentRejected(entityId: String, event: PaymentRejected): Future[List[BoundStatement]] = {
    updateTransactionSummaryStatus(event.itemId, "PaymentPending")
  }

  // -------------------------------------------------------------------------------------------------------------------

  private def updateTransactionSummaryStatus(itemId: UUID, status: String): Future[List[BoundStatement]] = {
    selectTransactionUser(itemId).map {
      case None => throw new IllegalStateException(s"No transactionUsers found for itemId $itemId")
      case Some(transactionUser) =>
        val updateCreatorStatus = bindUpdateTransactionSummaryStatus(status, transactionUser.creatorId, itemId)
        val updateWinnerStatus = bindUpdateTransactionSummaryStatus(status, transactionUser.winnerId, itemId)
        List(updateCreatorStatus, updateWinnerStatus)
    }
  }

}

