package com.example.auction.bidding.protocol


import com.example.auction.bidding.api._
import com.example.auction.bidding.impl
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.broker.TopicProducer
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry

import scala.concurrent.{ExecutionContext, Future}

trait BiddingTopics {

  implicit val ec: ExecutionContext
  val entityRegistry: PersistentEntityRegistry

  def _bidEvents(): Topic[BidEvent] = {

    // TODO: taggedStreamWithOffset
    TopicProducer.singleStreamWithOffset { fromOffset =>
      entityRegistry.eventStream(impl.BiddingEvent.Tag, fromOffset)
        .filter { e =>
          // TODO: Docs have a different filtering approach
          e.event.isInstanceOf[BidPlaced] || e.event.isInstanceOf[impl.BiddingFinished.type]
        }
        .mapAsync(1) { e =>
          e.event match {
            case impl.BidPlaced(bid) =>
              val message = BidPlaced(e.entityId, convertBid(bid))
              Future.successful(message, e.offset)
            case impl.BiddingFinished =>
              entityRegistry.refFor[impl.BiddingEntity](e.entityId).ask(impl.GetAuction).map { auctionState =>
                val maybeWinningBid = auctionState
                  .biddingHistory
                  .headOption
                  .filter(_.bidPrice >= auctionState.auction.reservePrice)
                  .map(convertBid)
                val message = BiddingFinished(e.entityId, maybeWinningBid)
                (message, e.offset)
              }
            // TODO: Get rid of "match may not be exhaustive"
          }
        }
    }
  }

  private def convertBid(bid: impl.Bid): Bid = {
    Bid(bid.bidder, bid.bidTime, bid.bidPrice, bid.maximumBid)
  }

}

