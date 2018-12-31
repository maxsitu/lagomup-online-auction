package com.example.auction.transaction.domain

import com.example.auction.transaction.impl._
import com.datastax.driver.core.BoundStatement
import scala.concurrent.{ ExecutionContext, Future }
import java.util.UUID

trait TransactionRepository_TODO extends TransactionReadRepository_TODO {

  val akkaComponents: AkkaComponents
  implicit val ec: ExecutionContext = akkaComponents.ec

  def bindInsertTransactionUser(itemId: UUID, creatorId: UUID, winnerId: UUID): BoundStatement
  def bindInsertTransactionSummaryByUser(userId: UUID, itemId: UUID, creatorId: UUID, winnerId: UUID, itemTitle: String, currencyId: String, itemPrice: Int, status: String): BoundStatement
  def bindUpdateTransactionSummaryStatus(status: String, userId: UUID, itemId: UUID): BoundStatement

  def processTransactionStarted(entityId: String, event: TransactionStarted): Future[List[BoundStatement]] = {
    Future.successful(List.empty)
  }

  def processDeliveryDetailsSubmitted(entityId: String, event: DeliveryDetailsSubmitted): Future[List[BoundStatement]] = {
    Future.successful(List.empty)
  }

  def processDeliveryPriceUpdated(entityId: String, event: DeliveryPriceUpdated): Future[List[BoundStatement]] = {
    Future.successful(List.empty)
  }

  def processDeliveryDetailsApproved(entityId: String, event: DeliveryDetailsApproved): Future[List[BoundStatement]] = {
    Future.successful(List.empty)
  }

  def processPaymentDetailsSubmitted(entityId: String, event: PaymentDetailsSubmitted): Future[List[BoundStatement]] = {
    Future.successful(List.empty)
  }

  def processPaymentApproved(entityId: String, event: PaymentApproved): Future[List[BoundStatement]] = {
    Future.successful(List.empty)
  }

  def processPaymentRejected(entityId: String, event: PaymentRejected): Future[List[BoundStatement]] = {
    Future.successful(List.empty)
  }

}

