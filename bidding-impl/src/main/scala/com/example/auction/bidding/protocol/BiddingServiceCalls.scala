package com.example.auction.bidding.protocol

import akka.NotUsed
import com.example.auction.bidding.api._
import com.example.auction.bidding.impl
import com.example.auction.bidding.impl.{BiddingEntity, GetAuction}
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
    entityRegistry.refFor[BiddingEntity](itemId).ask(impl.PlaceBid(request.maximumBidPrice, userId)).map { result =>
      // TODO: result.status enum
      BidResult(result.currentPrice, result.status, result.currentBidder)
    }
  }

  def _getBids(itemId: String, request: NotUsed): Future[List[Bid]] = {
    entityRegistry.refFor[BiddingEntity](itemId).ask(GetAuction).map { auction =>
      auction.biddingHistory.map(convertBid).reverse
    }
  }

  def _placeBidAuthentication[Request, Response](serviceCall: String => ServerServiceCall[Request, Response]): ServerServiceCall[Request, Response] = {
    ServerSecurity.authenticated(serviceCall)
  }

  // -------------------------------------------------------------------------------------------------------------------

  private def convertBid(bid: impl.Bid): Bid = Bid(bid.bidder, bid.bidTime, bid.bidPrice, bid.maximumBid)

}

