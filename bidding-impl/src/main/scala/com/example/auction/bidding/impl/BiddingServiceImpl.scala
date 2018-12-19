package com.example.auction.bidding.impl


import com.example.auction.bidding.api._
import com.example.auction.bidding.protocol._
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


class BiddingServiceImpl(val ports: BiddingPorts) extends BiddingService
  with BiddingServiceCalls with BiddingTopics {

  override def placeBid(itemId: UUID) = _authenticatePlaceBid(userId => ServerServiceCall { request =>
  _placeBid(userId, itemId, request)
})

override def getBids(itemId: UUID) = ServiceCall { request =>
  _getBids(itemId, request)
}


  override def bidEvents() = {
  _bidEvents()
}



}

