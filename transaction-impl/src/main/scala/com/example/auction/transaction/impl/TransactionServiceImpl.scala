package com.example.auction.transaction.impl

import com.example.auction.item.api._

import com.example.auction.transaction.api._
import com.example.auction.transaction.protocol._
import com.lightbend.lagom.scaladsl.api.ServiceCall
import akka.actor.ActorSystem
import akka.stream.Materializer
import com.lightbend.lagom.scaladsl.pubsub.PubSubRegistry
import akka.stream.scaladsl.Flow
import scala.concurrent.ExecutionContext
import com.lightbend.lagom.scaladsl.server.ServerServiceCall
import java.util.UUID
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraSession


class TransactionServiceImpl(val ports: TransactionPorts) extends TransactionService
  with TransactionServiceCalls  {

  override def submitDeliveryDetails(itemId: UUID) = _authenticateSubmitDeliveryDetails(userId => ServerServiceCall { request =>
  _submitDeliveryDetails(userId, itemId, request)
})

override def setDeliveryPrice(itemId: UUID) = _authenticateSetDeliveryPrice(userId => ServerServiceCall { request =>
  _setDeliveryPrice(userId, itemId, request)
})

override def approveDeliveryDetails(itemId: UUID) = _authenticateApproveDeliveryDetails(userId => ServerServiceCall { request =>
  _approveDeliveryDetails(userId, itemId, request)
})

override def submitPaymentDetails(itemId: UUID) = _authenticateSubmitPaymentDetails(userId => ServerServiceCall { request =>
  _submitPaymentDetails(userId, itemId, request)
})

override def submitPaymentStatus(itemId: UUID) = _authenticateSubmitPaymentStatus(userId => ServerServiceCall { request =>
  _submitPaymentStatus(userId, itemId, request)
})

override def getTransaction(itemId: UUID) = _authenticateGetTransaction(userId => ServerServiceCall { request =>
  _getTransaction(userId, itemId, request)
})

override def getTransactionsForUser(status: String, pageNo: Option[String], pageSize: Option[String]) = _authenticateGetTransactionsForUser(userId => ServerServiceCall { request =>
  _getTransactionsForUser(userId, status, pageNo, pageSize, request)
})


  

}

