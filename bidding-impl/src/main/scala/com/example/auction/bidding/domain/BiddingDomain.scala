package com.example.auction.bidding.domain

import java.time.Instant

import akka.Done
import com.example.auction.bidding.domain.BiddingDomain._
import com.example.auction.bidding.impl._
import com.lightbend.lagom.scaladsl.api.transport.{TransportErrorCode, TransportException}
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity

trait BiddingDomain {
  this: BiddingEntity with PersistentEntity =>

  def biddingEventStream: BiddingEventStream

  def initialState: BiddingState = BiddingState(None, AuctionAggregateStatus.NotStarted)

  def onStartAuction(command: StartAuction, state: BiddingState, ctx: CommandContext[Done]): Persist = {
    state.status match {
      case AuctionAggregateStatus.NotStarted =>
        ctx.thenPersist(AuctionStarted(command.auction))(_ => ctx.reply(Done))
      case _ =>
        done(ctx)
    }
  }

  def onCancelAuction(command: CancelAuction.type, state: BiddingState, ctx: CommandContext[Done]): Persist = {
    state.status match {
      case AuctionAggregateStatus.Cancelled =>
        done(ctx)
      case _ =>
        ctx.thenPersist(AuctionCancelled)(_ => ctx.done)
    }
  }

  def onPlaceBid(command: PlaceBid, state: BiddingState, ctx: CommandContext[PlaceBidResult]): Persist = {
    state.status match {
      case AuctionAggregateStatus.NotStarted =>
        reply(ctx, createResult(NotStartedStatus, state))
      case AuctionAggregateStatus.UnderAuction =>
        handlePlaceBidWhileUnderAuction(command, ctx, state)
      case AuctionAggregateStatus.Complete =>
        reply(ctx, createResult(FinishedStatus, state))
      case AuctionAggregateStatus.Cancelled =>
        reply(ctx, createResult(CancelledStatus, state))
    }
  }

  def onFinishBidding(command: FinishBidding.type, state: BiddingState, ctx: CommandContext[Done]): Persist = {
    state.status match {
      case AuctionAggregateStatus.NotStarted =>
        // TODO: Not specified in example
        ???
      case AuctionAggregateStatus.UnderAuction =>
        ctx.thenPersist(BiddingFinished)(_ => ctx.reply(Done))
      case _ =>
        done(ctx)
    }
  }

  def onAuctionStarted(event: AuctionStarted, state: BiddingState): BiddingState = {
    // TODO: Remove status if not used
    BiddingState(Some(AuctionAggregate(event.auction, state.status.toString, Nil)), AuctionAggregateStatus.UnderAuction)
  }

  def onAuctionCancelled(event: AuctionCancelled.type, state: BiddingState): BiddingState = {
    state.copy(status = AuctionAggregateStatus.Cancelled)
  }

  def onBidPlaced(event: BidPlaced, state: BiddingState): BiddingState = {
    val BiddingState(Some(auctionState@AuctionAggregate(_, _, biddingHistory)), _) = state
    if (biddingHistory.headOption.exists(_.bidder == event.bid.bidder)) {
      state.copy(aggregate = Some(auctionState.copy(biddingHistory = event.bid +: biddingHistory.tail)))
    } else {
      state.copy(aggregate = Some(auctionState.copy(biddingHistory = event.bid +: biddingHistory)))
    }
  }

  def onBiddingFinished(event: BiddingFinished.type, state: BiddingState): BiddingState = {
    state.copy(status = AuctionAggregateStatus.Complete)
  }

  // -------------------------------------------------------------------------------------------------------------------

  // TODO: Status enum
  private def createResult(status: String, state: BiddingState): PlaceBidResult = {
    state.aggregate.flatMap(_.biddingHistory.headOption) match {
      case Some(Bid(bidder, _, price, _)) =>
        PlaceBidResult(status, price, Some(bidder))
      case None =>
        PlaceBidResult(status, 0, None)
    }
  }

  /**
    * The main logic for handling of bids.
    */
  private def handlePlaceBidWhileUnderAuction(bid: PlaceBid, ctx: CommandContext[PlaceBidResult], state: BiddingState): Persist = {

    val BiddingState(Some(AuctionAggregate(auction, _, history)), _) = state
    val now = Instant.now

    // Even though we're not in the finished state yet, we should check
    if (auction.endTime.isBefore(now)) {
      reply(ctx, createResult(FinishedStatus, state))
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
          reply(ctx, createResult(TooLowStatus, state))
        case Some(Bid(_, _, currentPrice, _)) if bid.bidPrice < currentPrice + auction.increment =>
          reply(ctx, createResult(TooLowStatus, state))

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

}

/**
  * Exception thrown when a bid fails validation.
  */
case class BidValidationException(message: String) extends TransportException(TransportErrorCode.PolicyViolation, message)

object BiddingDomain {
  val AcceptedStatus = "Accepted"
  val AcceptedOutbidStatus = "AcceptedOutbid"
  val AcceptedBelowReserveStatus = "AcceptedBelowReserve"
  val TooLowStatus = "TooLow"
  val NotStartedStatus = "NotStarted"
  val FinishedStatus = "Finished"
  val CancelledStatus = "Cancelled"
}
