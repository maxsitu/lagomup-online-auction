package com.example.auction.bidding.protocol

import java.util.UUID

import com.example.auction.bidding.api._
import com.example.auction.bidding.impl
import com.example.auction.bidding.impl.BiddingPorts
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.broker.TopicProducer

import scala.concurrent.{ExecutionContext, Future}

trait BiddingTopics {

  val ports: BiddingPorts
  implicit val topicsEC: ExecutionContext = ports.akkaComponents.ec

  def _bidEvents(): Topic[BidEvent] = {

    // TODO: taggedStreamWithOffset
    TopicProducer.singleStreamWithOffset { fromOffset =>
      ports.entityRegistry.eventStream(impl.BiddingEvent.Tag, fromOffset)
        .filter { e =>
          // TODO: Docs have a different filtering approach
          e.event.isInstanceOf[BidPlaced] || e.event.isInstanceOf[impl.BiddingFinished.type]
        }
        .mapAsync(1) { e =>
          e.event match {
            case impl.BidPlaced(bid) =>
              val message = BidPlaced(UUID.fromString(e.entityId), convertBid(bid))
              Future.successful(message, e.offset)
            case impl.BiddingFinished =>
              ports.entityRegistry.refFor[impl.BiddingEntity](e.entityId).ask(impl.GetAuction).map {
                case Some(aggregate) =>
                  val maybeWinningBid = aggregate
                    .biddingHistory
                    .headOption
                    .filter(_.bidPrice >= aggregate.auction.reservePrice)
                    .map(convertBid)
                  val message = BiddingFinished(UUID.fromString(e.entityId), maybeWinningBid)
                  (message, e.offset)
                case None =>
                  val message = BiddingFinished(UUID.fromString(e.entityId), None)
                  (message, e.offset)
              }
            case _ =>
              ??? // TODO: Not specified in example
          }
        }
    }
  }

  private def convertBid(bid: impl.Bid): Bid = {
    Bid(bid.bidder, bid.bidTime, bid.bidPrice, bid.maximumBid)
  }

}

