package com.example.auction.bidding.domain

import java.time.Instant

import akka.Done
import com.example.auction.bidding.impl._
import com.lightbend.lagom.scaladsl.api.transport.{TransportErrorCode, TransportException}
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity
import com.lightbend.lagom.scaladsl.pubsub.PubSubRegistry

trait BiddingDomain {
  this: BiddingEntity with PersistentEntity =>

  def pubSubRegistry: PubSubRegistry

  def initialState: BiddingAggregate = ???

  def onStartAuction(command: StartAuction, aggregate: BiddingAggregate, ctx: CommandContext[Done]): Persist = {
    aggregate.status match {
      case AuctionStateStatus.NotStarted =>
        ctx.thenPersist(AuctionStarted(command.auction))(_ => ctx.reply(Done))
      case _ =>
        done(ctx)
    }
  }

  def onCancelAuction(command: CancelAuction.type, aggregate: BiddingAggregate, ctx: CommandContext[Done]): Persist = {
    aggregate.status match {
      case AuctionStateStatus.Cancelled =>
        done(ctx)
      case _ =>
        ctx.thenPersist(AuctionCancelled)(_ => ctx.done)
    }
  }

  def onPlaceBid(command: PlaceBid, aggregate: BiddingAggregate, ctx: CommandContext[PlaceBidResult]): Persist = {
    aggregate.status match {
      case AuctionStateStatus.NotStarted =>
        reply(ctx, createResult(NotStartedStatus, aggregate))
      case AuctionStateStatus.UnderAuction =>
        handlePlaceBidWhileUnderAuction(command, ctx, aggregate)
      case AuctionStateStatus.Complete =>
        reply(ctx, createResult(FinishedStatus, aggregate))
      case AuctionStateStatus.Cancelled =>
        reply(ctx, createResult(CancelledStatus, aggregate))
    }
  }

  def onFinishBidding(command: FinishBidding.type, aggregate: BiddingAggregate, ctx: CommandContext[Done]): Persist = {
    aggregate.status match {
      case AuctionStateStatus.NotStarted =>
        ??? // TODO: Not specified
      case AuctionStateStatus.UnderAuction =>
        ctx.thenPersist(BiddingFinished)(_ => ctx.reply(Done))
      case _ =>
        done(ctx)
    }
  }

  def onGetAuction(query: GetAuction.type, aggregate: BiddingAggregate, ctx: ReadOnlyCommandContext[AuctionState]): Unit = {
    ???
  }

  def onAuctionStarted(event: AuctionStarted, aggregate: BiddingAggregate): BiddingAggregate = {
    ???
  }

  def onAuctionCancelled(event: AuctionCancelled.type, aggregate: BiddingAggregate): BiddingAggregate = {
    ???
  }

  def onBidPlaced(event: BidPlaced, aggregate: BiddingAggregate): BiddingAggregate = {
    ???
  }

  def onBiddingFinished(event: BiddingFinished.type, aggregate: BiddingAggregate): BiddingAggregate = {
    ???
  }

  // -------------------------------------------------------------------------------------------------------------------

  val AcceptedStatus = "Accepted"
  val AcceptedOutbidStatus = "AcceptedOutbid"
  val AcceptedBelowReserveStatus = "AcceptedBelowReserve"
  val TooLowStatus = "TooLow"
  val NotStartedStatus = "NotStarted"
  val FinishedStatus = "Finished"
  val CancelledStatus = "Cancelled"

  // TODO: Status enum
  private def createResult(status: String, aggregate: BiddingAggregate): PlaceBidResult = {
    aggregate.state.flatMap(_.biddingHistory.headOption) match {
      case Some(Bid(bidder, _, price, _)) =>
        PlaceBidResult(status, price, Some(bidder))
      case None =>
        PlaceBidResult(status, 0, None)
    }
  }

  /**
    * The main logic for handling of bids.
    */
  private def handlePlaceBidWhileUnderAuction(bid: PlaceBid, ctx: CommandContext[PlaceBidResult], aggregate: BiddingAggregate): Persist = {

    val BiddingAggregate(Some(AuctionState(auction, _, history)), _) = aggregate
    val now = Instant.now

    // Even though we're not in the finished state yet, we should check
    if (auction.endTime.isBefore(now)) {
      reply(ctx, createResult(FinishedStatus, aggregate))
    } else if (auction.creator == bid.bidder) {
      throw BidValidationException("An auctions creator cannot bid in their own auction.")
    } else {

      history.headOption match {

        // Allow the current bidder to update their bid
        case Some(Bid(currentBidder, _, currentPrice, _)) if bid.bidPrice >= currentPrice && bid.bidder == currentBidder
          && bid.bidPrice >= auction.reservePrice =>
          ctx.thenPersist(BidPlaced(Bid(bid.bidder, now, currentPrice, bid.bidPrice))) { _ =>
            ctx.reply(PlaceBidResult(AcceptedStatus, currentPrice, Some(bid.bidder)))
          }

        // Bid too low
        case None if bid.bidPrice < auction.increment =>
          reply(ctx, createResult(TooLowStatus, aggregate))
        case Some(Bid(_, _, currentPrice, _)) if bid.bidPrice < currentPrice + auction.increment =>
          reply(ctx, createResult(TooLowStatus, aggregate))

        // Automatic outbid
        case Some(currentBid@Bid(_, _, _, currentMaximum)) if bid.bidPrice <= currentMaximum =>
          handleAutomaticOutbid(bid, ctx, auction, now, currentBid)

        // Accepted (below reserve)
        case _ if bid.bidPrice < auction.reservePrice =>
          ctx.thenPersist(BidPlaced(Bid(bid.bidder, now, bid.bidPrice, bid.bidPrice))) { _ =>
            ctx.reply(PlaceBidResult(AcceptedBelowReserveStatus, bid.bidPrice, Some(bid.bidder)))
          }

        // Accepted
        case Some(Bid(_, _, _, currentMaximum)) =>
          val nextIncrement = Math.min(currentMaximum + auction.increment, bid.bidPrice)
          ctx.thenPersist(BidPlaced(Bid(bid.bidder, now, nextIncrement, bid.bidPrice))) { _ =>
            ctx.reply(PlaceBidResult(AcceptedStatus, nextIncrement, Some(bid.bidder)))
          }
        case None =>
          // Ensure that the bid is both at least the reserve, and at least the increment
          val firstBid = Math.max(auction.reservePrice, auction.increment)
          ctx.thenPersist(BidPlaced(Bid(bid.bidder, now, firstBid, bid.bidPrice))) { _ =>
            ctx.reply(PlaceBidResult(AcceptedStatus, firstBid, Some(bid.bidder)))
          }

      }

    }

  }

  private def handleAutomaticOutbid(bid: PlaceBid, ctx: CommandContext[PlaceBidResult], auction: Auction, now: Instant, currentBid: Bid): Persist = {
    // Adjust the bid so that the increment for the current maximum makes the current maximum a valid bid
    val adjustedBidPrice = Math.min(bid.bidPrice, currentBid.maximumBid - auction.increment)
    val newBidPrice = adjustedBidPrice + auction.increment

    ctx.thenPersistAll(
      BidPlaced(Bid(bid.bidder, now, adjustedBidPrice, bid.bidPrice)),
      BidPlaced(Bid(currentBid.bidder, now, newBidPrice, currentBid.maximumBid))
    ) { () =>
      ctx.reply(PlaceBidResult(AcceptedOutbidStatus, newBidPrice, Some(currentBid.bidder)))
    }
  }

  private def reply(ctx: CommandContext[PlaceBidResult], result: PlaceBidResult): Persist = {
    ctx.reply(result)
    ctx.done
  }

  private def done(ctx: CommandContext[Done]): Persist = {
    ctx.reply(Done)
    ctx.done
  }

  /**
    * Exception thrown when a bid fails validation.
    */
  case class BidValidationException(message: String) extends TransportException(TransportErrorCode.PolicyViolation, message)


}





























