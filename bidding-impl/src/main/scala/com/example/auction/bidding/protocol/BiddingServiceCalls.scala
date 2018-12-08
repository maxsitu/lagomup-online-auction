package com.example.auction.bidding.protocol

import akka.NotUsed
import com.example.auction.bidding.api._
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraSession
import com.lightbend.lagom.scaladsl.pubsub.PubSubRegistry
import com.lightbend.lagom.scaladsl.server.ServerServiceCall
import scala.concurrent.{ExecutionContext, Future}

trait BiddingServiceCalls {
  implicit val ec: ExecutionContext

  val entityRegistry: PersistentEntityRegistry
  val db: CassandraSession
  val pubSubRegistry: PubSubRegistry

  def _placeBid(userId: String, itemId: String, request: PlaceBid): Future[BidResult] = {
    ???
  }

  def _getBids(itemId: String, request: NotUsed): Future[List[Bid]] = {
    ???
  }

  def _placeBidAuthentication[Request, Response](serviceCall: String => ServerServiceCall[Request, Response]): ServerServiceCall[Request, Response] = {
    ServerSecurity.authenticated(serviceCall)
  }

}

