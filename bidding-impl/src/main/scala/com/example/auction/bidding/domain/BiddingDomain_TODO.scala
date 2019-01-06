package com.example.auction.bidding.domain

import com.example.auction.bidding.impl._
import akka.Done
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity

trait BiddingDomain_TODO {
  self: BiddingEntity with PersistentEntity =>

  //def biddingEventStream: BiddingEventStream

  def initialState: BiddingState = ???

  def onStartAuction(command: StartAuction, state: BiddingState, ctx: CommandContext[Done]): Persist = {
    ???
  }

  def onCancelAuction(command: CancelAuction.type, state: BiddingState, ctx: CommandContext[Done]): Persist = {
    ???
  }

  def onPlaceBid(command: PlaceBid, state: BiddingState, ctx: CommandContext[PlaceBidResult]): Persist = {
    ???
  }

  def onFinishBidding(command: FinishBidding.type, state: BiddingState, ctx: CommandContext[Done]): Persist = {
    ???
  }

  def onAuctionStarted(event: AuctionStarted, state: BiddingState): BiddingState = {
    ???
  }

  def onAuctionCancelled(event: AuctionCancelled.type, state: BiddingState): BiddingState = {
    ???
  }

  def onBidPlaced(event: BidPlaced, state: BiddingState): BiddingState = {
    ???
  }

  def onBiddingFinished(event: BiddingFinished.type, state: BiddingState): BiddingState = {
    ???
  }

}

