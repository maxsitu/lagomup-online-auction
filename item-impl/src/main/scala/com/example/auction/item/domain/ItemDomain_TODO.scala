package com.example.auction.item.domain

import com.example.auction.item.impl._
import akka.Done
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity

trait ItemDomain_TODO {
  this: ItemEntity with PersistentEntity =>

  //def itemEventStream: ItemEventStream

  def initialState: ItemState = ???

  def onCreateItem(command: CreateItem, state: ItemState, ctx: CommandContext[Done]): Persist = {
    ???
  }

  def onStartAuction(command: StartAuction, state: ItemState, ctx: CommandContext[Done]): Persist = {
    ???
  }

  def onUpdatePrice(command: UpdatePrice, state: ItemState, ctx: CommandContext[Done]): Persist = {
    ???
  }

  def onFinishAuction(command: FinishAuction, state: ItemState, ctx: CommandContext[Done]): Persist = {
    ???
  }

  def onGetItem(query: GetItem.type, state: ItemState, ctx: ReadOnlyCommandContext[Option[ItemAggregate]]): Unit = {
    ???
  }

  def onItemCreated(event: ItemCreated, state: ItemState): ItemState = {
    ???
  }

  def onAuctionStarted(event: AuctionStarted, state: ItemState): ItemState = {
    ???
  }

  def onPriceUpdated(event: PriceUpdated, state: ItemState): ItemState = {
    ???
  }

  def onAuctionFinished(event: AuctionFinished, state: ItemState): ItemState = {
    ???
  }

}

