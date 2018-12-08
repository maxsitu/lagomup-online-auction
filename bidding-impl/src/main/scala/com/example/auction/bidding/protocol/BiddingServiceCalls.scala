package com.example.auction.bidding.protocol

import com.example.auction.bidding.api._
import akka.stream.scaladsl.Source
import akka.{Done, NotUsed}
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraSession

import com.lightbend.lagom.scaladsl.pubsub.{PubSubRegistry, TopicId}
import scala.concurrent.{ExecutionContext, Future}

trait BiddingServiceCalls {
  implicit val ec: ExecutionContext

  val entityRegistry: PersistentEntityRegistry
  val db: CassandraSession
  val pubSubRegistry: PubSubRegistry

  def _placeBid(itemId: String, request: PlaceBid): Future[BidResult] = {
    ???
  }

  def _getBids(itemId: String, request: NotUsed): Future[List[Bid]] = {
    ???
  }

}

