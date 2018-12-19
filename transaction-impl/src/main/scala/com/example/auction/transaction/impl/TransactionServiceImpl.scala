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

  override def submitDeliveryDetails(itemId: UUID) = ServiceCall { request =>
  _submitDeliveryDetails(itemId, request)
}

override def setDeliveryPrice(itemId: UUID) = ServiceCall { request =>
  _setDeliveryPrice(itemId, request)
}

override def approveDeliveryDetails(itemId: UUID) = ServiceCall { request =>
  _approveDeliveryDetails(itemId, request)
}

override def submitPaymentDetails(itemId: UUID) = ServiceCall { request =>
  _submitPaymentDetails(itemId, request)
}

override def submitPaymentStatus(itemId: UUID) = ServiceCall { request =>
  _submitPaymentStatus(itemId, request)
}

override def getTransaction(itemId: UUID) = ServiceCall { request =>
  _getTransaction(itemId, request)
}

override def getTransactionsForUser(status: String, pageNo: Option[String], pageSize: Option[String]) = ServiceCall { request =>
  _getTransactionsForUser(status, pageNo, pageSize, request)
}


  

}

