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

  def _submitDeliveryDetails(itemId: UUID, request: DeliveryInfo): Future[Done] = {
  ???
}

def _setDeliveryPrice(itemId: UUID, request: Int): Future[Done] = {
  ???
}

def _approveDeliveryDetails(itemId: UUID, request: NotUsed): Future[Done] = {
  ???
}

def _submitPaymentDetails(itemId: UUID, request: PaymentInfo): Future[Done] = {
  ???
}

def _submitPaymentStatus(itemId: UUID, request: String): Future[Done] = {
  ???
}

def _getTransaction(itemId: UUID, request: NotUsed): Future[TransactionInfo] = {
  ???
}

def _getTransactionsForUser(status: String, pageNo: Option[String], pageSize: Option[String], request: NotUsed): Future[List[TransactionSummary]] = {
  ???
}



  

}

