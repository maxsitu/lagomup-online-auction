package com.example.auction.bidding.protocol


import com.example.auction.bidding.api
import com.example.auction.bidding.impl._
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.broker.TopicProducer
import com.lightbend.lagom.scaladsl.persistence.{EventStreamElement, PersistentEntityRegistry}

import scala.concurrent.{ExecutionContext, Future}

trait BiddingTopics {

  implicit val ec: ExecutionContext
  val entityRegistry: PersistentEntityRegistry

  def _bidEvents(): Topic[api.BidEvent] = {

    // TODO: taggedStreamWithOffset
    TopicProducer.singleStreamWithOffset { fromOffset =>
      entityRegistry.eventStream(BiddingEvent.Tag, fromOffset)
        .filter { e =>
          // TODO: Docs have a different filtering approach
          e.event.isInstanceOf[BidPlaced] || e.event.isInstanceOf[BiddingFinished.type]
        }
        .mapAsync(1) { e =>
          e.event match {
            case BidPlaced(bid) =>
              val message = api.BidPlaced(e.entityId, convertBid(bid))
              Future.successful(message, e.offset)
            case BiddingFinished =>
              entityRegistry.refFor[BiddingEntity](e.entityId).ask(GetAuction).map { auctionState =>
                val maybeWinningBid = auctionState
                  .biddingHistory
                  .headOption
                  .filter(_.bidPrice >= auctionState.auction.reservePrice)
                  .map(convertBid)
                val message = api.BiddingFinished(e.entityId, maybeWinningBid)
                (message, e.offset)
              }
          }
        }
    }
  }

  private def convertBid(bid: Bid): api.Bid = {
    api.Bid(bid.bidder, bid.bidTime, bid.bidPrice, bid.maximumBid)
  }

}

