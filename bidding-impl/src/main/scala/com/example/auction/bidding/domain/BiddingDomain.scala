package com.example.auction.bidding.domain

import akka.Done
import com.example.auction.bidding.impl._
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity
import com.lightbend.lagom.scaladsl.pubsub.PubSubRegistry

trait BiddingDomain {
  this: BiddingEntity with PersistentEntity =>

  def pubSubRegistry: PubSubRegistry

  def initialState: BiddingAggregate = ???

  def onStartAuction(command: StartAuction, aggregate: BiddingAggregate, ctx: CommandContext[Done]): Persist = {
    ???
  }

  def onCancelAuction(command: CancelAuction.type, aggregate: BiddingAggregate, ctx: CommandContext[Done]): Persist = {
    ???
  }

  def onPlaceBid(command: PlaceBid, aggregate: BiddingAggregate, ctx: CommandContext[PlaceBidResult]): Persist = {
    ???
  }

  def onFinishBidding(command: FinishBidding.type, aggregate: BiddingAggregate, ctx: CommandContext[Done]): Persist = {
    ???
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

}

