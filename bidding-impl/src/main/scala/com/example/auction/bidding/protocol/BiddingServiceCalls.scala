package com.example.auction.bidding.protocol

import java.util.UUID

import akka.NotUsed
import com.example.auction.bidding.api._
import com.example.auction.bidding.impl
import com.example.auction.bidding.impl.{BiddingEntity, BiddingPorts, GetAuction}
import com.lightbend.lagom.scaladsl.server.ServerServiceCall

import scala.concurrent.{ExecutionContext, Future}

trait BiddingServiceCalls {

  val ports: BiddingPorts
  implicit val serviceEC: ExecutionContext = ports.environment.ec

  def _placeBid(itemId: UUID, userId: UUID, request: PlaceBid): Future[BidResult] = {
    ports.entityRegistry.refFor[BiddingEntity](itemId.toString).ask(impl.PlaceBid(request.maximumBidPrice, userId)).map { result =>
      // TODO: result.status enum
      BidResult(result.currentPrice, result.status, result.currentBidder)
    }
  }

  def _getBids(itemId: UUID, request: NotUsed): Future[List[Bid]] = {
    ports.entityRegistry.refFor[BiddingEntity](itemId.toString).ask(GetAuction).map {
      case Some(aggregate) => aggregate.biddingHistory.map(convertBid).reverse
      case None => List.empty
    }
  }

  def _placeBidAuthentication[Request, Response](serviceCall: UUID => ServerServiceCall[Request, Response]): ServerServiceCall[Request, Response] = {
    ServerSecurity.authenticated(serviceCall)
  }

  // -------------------------------------------------------------------------------------------------------------------

  private def convertBid(bid: impl.Bid): Bid = Bid(bid.bidder, bid.bidTime, bid.bidPrice, bid.maximumBid)

}
