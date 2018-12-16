package com.example.auction.item.impl


import com.example.auction.item.api._
import com.example.auction.item.protocol._
import com.lightbend.lagom.scaladsl.api.ServiceCall
import akka.actor.ActorSystem
import akka.stream.Materializer
import com.lightbend.lagom.scaladsl.pubsub.PubSubRegistry
import akka.stream.scaladsl.Flow
import scala.concurrent.ExecutionContext
import com.lightbend.lagom.scaladsl.server.ServerServiceCall
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraSession
import java.util.UUID


class ItemServiceImpl(val ports: ItemPorts) extends ItemService
  with ItemServiceCalls with ItemTopics  {

  
  override def createItem() = _createItemAuthentication(userId => ServerServiceCall { request =>
  _createItem(userId, request)
})

override def startAuction(id: UUID) = _startAuctionAuthentication(userId => ServerServiceCall { request =>
  _startAuction(userId, id, request)
})

override def getItem(id: UUID) = ServiceCall { request =>
  _getItem(id, request)
}

override def getItemForUser(id: UUID, status: String, page: Option[String]) = ServiceCall { request =>
  _getItemForUser(id, status, page, request)
}


  override def itemEvents() = {
  _itemEvents()
}



}

