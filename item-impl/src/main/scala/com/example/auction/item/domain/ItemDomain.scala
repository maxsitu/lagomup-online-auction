package com.example.auction.item.domain

import com.example.auction.item.impl._
import akka.Done
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity
import com.lightbend.lagom.scaladsl.pubsub.{PubSubRegistry, TopicId}

trait ItemDomain {
  this: ItemEntity with PersistentEntity =>

  def pubSubRegistry: PubSubRegistry

  def initialState: ItemAggregate = ???

  def onCreateItem(command: CreateItem, aggregate: ItemAggregate, ctx: CommandContext[Done]): Persist = {
  ???
}

def onStartAuction(command: StartAuction, aggregate: ItemAggregate, ctx: CommandContext[Done]): Persist = {
  ???
}

def onUpdatePrice(command: UpdatePrice, aggregate: ItemAggregate, ctx: CommandContext[Done]): Persist = {
  ???
}

def onFinishAuction(command: FinishAuction, aggregate: ItemAggregate, ctx: CommandContext[Done]): Persist = {
  ???
}


  def onGetItem(query: GetItem.type, aggregate: ItemAggregate, ctx: ReadOnlyCommandContext[ItemState]): Unit = {
  ???
}


  def onItemCreated(event: ItemCreated, aggregate: ItemAggregate): ItemAggregate = {
  ???
}

def onAuctionStarted(event: AuctionStarted, aggregate: ItemAggregate): ItemAggregate = {
  ???
}

def onPriceUpdated(event: PriceUpdated, aggregate: ItemAggregate): ItemAggregate = {
  ???
}

def onAuctionFinished(event: AuctionFinished, aggregate: ItemAggregate): ItemAggregate = {
  ???
}



}

