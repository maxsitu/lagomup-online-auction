package com.example.auction.transaction.protocol

import java.util.UUID

import akka.{Done, NotUsed}
import com.example.auction.item.api
import com.example.auction.transaction.api._
import com.example.auction.transaction.impl.{TransactionSummary => _, _}
import com.example.auction.utils.ServerSecurity
import com.lightbend.lagom.scaladsl.api.transport.{Forbidden, NotFound}
import com.lightbend.lagom.scaladsl.server.ServerServiceCall

import scala.concurrent.Future

trait TransactionServiceCalls {

  val ports: TransactionPorts
  implicit val ec = ports.akkaComponents.ec

  def _submitDeliveryDetails(userId: UUID, itemId: UUID, request: DeliveryInfo): Future[Done] = {
    ???
  }

  def _setDeliveryPrice(userId: UUID, itemId: UUID, request: Int): Future[Done] = {
    ???
  }

  def _approveDeliveryDetails(userId: UUID, itemId: UUID, request: NotUsed): Future[Done] = {
    ???
  }

  def _submitPaymentDetails(userId: UUID, itemId: UUID, request: PaymentInfo): Future[Done] = {
    ???
  }

  def _submitPaymentStatus(userId: UUID, itemId: UUID, request: String): Future[Done] = {
    ???
  }

  def _getTransaction(userId: UUID, itemId: UUID, request: NotUsed): Future[TransactionInfo] = {
    // TODO: Need current state with status
    //    ports.entityRegistry.refFor[TransactionEntity](itemId.toString)
    //      .ask(GetTransaction(userId))
    //      .map {
    //        case None =>
    //          throw NotFound(s"Transaction for item $itemId not found")
    //        case Some(aggregate) =>
    //          toApi()
    //      }
    ports.entityRegistry.refFor[TransactionEntity](itemId.toString)
      .ask(StateSnapshot)
      .map {
        case TransactionState(None, _) =>
          throw NotFound(s"Transaction for item $itemId not found")
        case TransactionState(Some(aggregate), status) =>
          if (userId == aggregate.creator || userId == aggregate.winner) {
            toApi(aggregate, status)
          } else {
            throw Forbidden("Only the item owner and the auction winner can see transaction details")
          }
      }
  }

  // Authentication ----------------------------------------------------------------------------------------------------

  def _getTransactionsForUser(userId: UUID, status: String, pageNo: Option[String], pageSize: Option[String], request: NotUsed): Future[List[TransactionSummary]] = {
    ???
  }

  def _authenticateSubmitDeliveryDetails[Request, Response](serviceCall: UUID => ServerServiceCall[Request, Response]): ServerServiceCall[Request, Response] = {
    ServerSecurity.authenticated(serviceCall)
  }

  def _authenticateSetDeliveryPrice[Request, Response](serviceCall: UUID => ServerServiceCall[Request, Response]): ServerServiceCall[Request, Response] = {
    ServerSecurity.authenticated(serviceCall)
  }

  def _authenticateApproveDeliveryDetails[Request, Response](serviceCall: UUID => ServerServiceCall[Request, Response]): ServerServiceCall[Request, Response] = {
    ServerSecurity.authenticated(serviceCall)
  }

  def _authenticateSubmitPaymentDetails[Request, Response](serviceCall: UUID => ServerServiceCall[Request, Response]): ServerServiceCall[Request, Response] = {
    ServerSecurity.authenticated(serviceCall)
  }

  def _authenticateSubmitPaymentStatus[Request, Response](serviceCall: UUID => ServerServiceCall[Request, Response]): ServerServiceCall[Request, Response] = {
    ServerSecurity.authenticated(serviceCall)
  }

  def _authenticateGetTransaction[Request, Response](serviceCall: UUID => ServerServiceCall[Request, Response]): ServerServiceCall[Request, Response] = {
    ServerSecurity.authenticated(serviceCall)
  }

  def _authenticateGetTransactionsForUser[Request, Response](serviceCall: UUID => ServerServiceCall[Request, Response]): ServerServiceCall[Request, Response] = {
    ServerSecurity.authenticated(serviceCall)
  }

  // Helpers -----------------------------------------------------------------------------------------------------------

  private def toApi(aggregate: TransactionAggregate, status: TransactionAggregateStatus.Status): TransactionInfo = {
    TransactionInfo(
      aggregate.itemId,
      aggregate.creator,
      aggregate.winner,
      toApi(aggregate.itemData),
      aggregate.itemPrice,
      aggregate.deliveryData.map(toApi),
      aggregate.deliveryPrice,
      aggregate.payment.map(toApi),
      status.toString
    )
  }

  private def toApi(deliveryData: DeliveryData): DeliveryInfo = {
    DeliveryInfo(
      deliveryData.addressLine1,
      deliveryData.addressLine2,
      deliveryData.city,
      deliveryData.state,
      deliveryData.postalCode,
      deliveryData.country
    )
  }

  private def toApi(payment: Payment): PaymentInfo = {
    PaymentInfo(payment.comment)
  }

  private def toApi(itemData: ItemData): api.ItemData = {
    api.ItemData(
      itemData.title,
      itemData.description,
      itemData.currencyId,
      itemData.increment,
      itemData.reservePrice,
      itemData.auctionDuration,
      itemData.categoryId
    )
  }

}

