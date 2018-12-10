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


class ItemServiceImpl(val entityRegistry: PersistentEntityRegistry, val db: CassandraSession, val pubSubRegistry: PubSubRegistry, val actorSystem: ActorSystem, val mat: Materializer)
                                         (implicit val ec: ExecutionContext)  extends ItemService
  with ItemServiceCalls   {

  
  override def createItem() = _createItemAuthentication(userId => ServerServiceCall { request =>
  _createItem(userId, request)
})

override def startAuction(id: String) = _startAuctionAuthentication(userId => ServerServiceCall { request =>
  _startAuction(userId, id, request)
})

override def getItem(id: String) = ServiceCall { request =>
  _getItem(id, request)
}

override def getItemForUser(id: String, status: String, page: Option[String]) = ServiceCall { request =>
  _getItemForUser(id, status, page, request)
}


  

}

