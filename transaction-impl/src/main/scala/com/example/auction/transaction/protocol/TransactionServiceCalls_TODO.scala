package com.example.auction.transaction.protocol

import com.example.auction.item.api._

import com.example.auction.transaction.api._
import com.example.auction.transaction.impl.TransactionPorts
import akka.stream.scaladsl.Source
import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.stream.Materializer
import com.lightbend.lagom.scaladsl.pubsub.{PubSubRegistry, TopicId}
import com.lightbend.lagom.scaladsl.server.ServerServiceCall
import scala.concurrent.{ExecutionContext, Future}
import java.util.UUID

trait TransactionServiceCalls_TODO {

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
  ???
}

def _getTransactionsForUser(userId: UUID, status: String, pageNo: Option[String], pageSize: Option[String], request: NotUsed): Future[List[TransactionSummary]] = {
  ???
}



  def _authenticateSubmitDeliveryDetails[Request, Response](serviceCall: UUID => ServerServiceCall[Request, Response]): ServerServiceCall[Request, Response] = {
  ???
}

def _authenticateSetDeliveryPrice[Request, Response](serviceCall: UUID => ServerServiceCall[Request, Response]): ServerServiceCall[Request, Response] = {
  ???
}

def _authenticateApproveDeliveryDetails[Request, Response](serviceCall: UUID => ServerServiceCall[Request, Response]): ServerServiceCall[Request, Response] = {
  ???
}

def _authenticateSubmitPaymentDetails[Request, Response](serviceCall: UUID => ServerServiceCall[Request, Response]): ServerServiceCall[Request, Response] = {
  ???
}

def _authenticateSubmitPaymentStatus[Request, Response](serviceCall: UUID => ServerServiceCall[Request, Response]): ServerServiceCall[Request, Response] = {
  ???
}

def _authenticateGetTransaction[Request, Response](serviceCall: UUID => ServerServiceCall[Request, Response]): ServerServiceCall[Request, Response] = {
  ???
}

def _authenticateGetTransactionsForUser[Request, Response](serviceCall: UUID => ServerServiceCall[Request, Response]): ServerServiceCall[Request, Response] = {
  ???
}



}

