package com.example.auction.item.impl

import com.example.auction.bidding.api._

import com.example.auction.item.api._
import com.example.auction.item.protocol._
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

class ItemServiceImpl(val ports: ItemPorts) extends ItemService
  with ItemServiceCalls with ItemTopics {

  override def createItem() = _authenticateCreateItem(userId => ServerServiceCall { request =>
    _createItem(userId, request)
  })

  override def startAuction(id: UUID) = _authenticateStartAuction(userId => ServerServiceCall { request =>
    _startAuction(userId, id, request)
  })

  override def getItem(id: UUID) = ServiceCall { request =>
    _getItem(id, request)
  }

  override def getItemsForUser(id: UUID, status: String, page: Option[String]) = ServiceCall { request =>
    _getItemsForUser(id, status, page, request)
  }

  override def itemEvents() = {
    _itemEvents()
  }

}

