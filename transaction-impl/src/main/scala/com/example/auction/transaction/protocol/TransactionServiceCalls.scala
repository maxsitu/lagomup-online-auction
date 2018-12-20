package com.example.auction.transaction.protocol

import java.util.UUID

import akka.{Done, NotUsed}
import com.example.auction.transaction.api._
import com.example.auction.transaction.impl.{TransactionSummary => _, _}
import com.example.auction.utils.ServerSecurity
import com.lightbend.lagom.scaladsl.api.transport.NotFound
import com.lightbend.lagom.scaladsl.server.ServerServiceCall
import controllers.Assets

import scala.concurrent.Future

trait TransactionServiceCalls {

  val ports: TransactionPorts

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
    ???
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

  private def toApi(aggregate: TransactionAggregate): TransactionInfo = {
    // TODO: Need current state with status
//    TransactionInfo(
//      aggregate.itemId,
//      aggregate.creator,
//      aggregate.winner,
//      aggregate.itemData,
//      aggregate.itemPrice,
//      aggregate.deliveryData.map(toApi),
//      aggregate.deliveryPrice,
//      aggregate.payment.map(toApi),
//      aggregate.
//    )
    ???
  }

  private def toApi(deliveryData: DeliveryData): DeliveryInfo = {
    ???
  }

  private def toApi(payment: Payment): PaymentInfo = {
    ???
  }

}

