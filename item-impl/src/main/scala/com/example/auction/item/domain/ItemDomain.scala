package com.example.auction.item.domain

import java.time.{Duration, Instant}

import akka.Done
import com.example.auction.item.impl._
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity
import com.lightbend.lagom.scaladsl.pubsub.PubSubRegistry

trait ItemDomain {
  this: ItemEntity with PersistentEntity =>

  def pubSubRegistry: PubSubRegistry

  def initialState: ItemAggregate = ItemAggregate(None, ItemStateStatus.NotCreated)

  def onCreateItem(command: CreateItem, aggregate: ItemAggregate, ctx: CommandContext[Done]): Persist = {
    aggregate.status match {
      case ItemStateStatus.NotCreated =>
        ctx.thenPersist(ItemCreated(command.item))(_ => ctx.reply(Done))
      case _ =>
        // TODO: Not specified in example
        ???
    }
  }

  def onStartAuction(command: StartAuction, aggregate: ItemAggregate, ctx: CommandContext[Done]): Persist = {
    aggregate.status match {
      case ItemStateStatus.Created =>
        if (aggregate.state.get.creator != command.userId) {
          ctx.invalidCommand("Only the creator of an auction can start it")
          ctx.done
        } else {
          ctx.thenPersist(AuctionStarted(Instant.now))(_ => ctx.reply(Done))
        }
      case ItemStateStatus.Auction | ItemStateStatus.Completed | ItemStateStatus.Cancelled =>
        done(ctx)
      case ItemStateStatus.NotCreated =>
        // TODO: Not specified in example
        ???
    }
  }

  def onUpdatePrice(command: UpdatePrice, aggregate: ItemAggregate, ctx: CommandContext[Done]): Persist = {
    aggregate.status match {
      case ItemStateStatus.Auction =>
        ctx.thenPersist(PriceUpdated(command.price))(_ => ctx.reply(Done))
      case ItemStateStatus.Completed | ItemStateStatus.Cancelled =>
        done(ctx)
      case ItemStateStatus.NotCreated | ItemStateStatus.Created =>
        // TODO: Not specified in example
        ???
    }
  }

  def onFinishAuction(command: FinishAuction, aggregate: ItemAggregate, ctx: CommandContext[Done]): Persist = {
    aggregate.status match {
      case ItemStateStatus.Auction =>
        ctx.thenPersist(AuctionFinished(command.winner, command.price))(_ => ctx.reply(Done))
      case ItemStateStatus.Completed | ItemStateStatus.Cancelled =>
        done(ctx)
      case ItemStateStatus.NotCreated | ItemStateStatus.Created =>
        // TODO: Not specified in example
        ???
    }
  }

  def onGetItem(query: GetItem.type, aggregate: ItemAggregate, ctx: ReadOnlyCommandContext[ItemState]): Unit = {
    // TODO: Example always had ItemState
    aggregate.state match {
      case Some(state) => ctx.reply(state)
      case None => ctx.invalidCommand(s"No state found in status ${aggregate.status}")
    }
  }

  def onItemCreated(event: ItemCreated, aggregate: ItemAggregate): ItemAggregate = {
    aggregate.copy(state = Some(event.item))
  }

  def onAuctionStarted(event: AuctionStarted, aggregate: ItemAggregate): ItemAggregate = {
    assert(aggregate.status == ItemStateStatus.Created)
    val itemState = aggregate.state.get
    aggregate.copy(
      state = Some(itemState.copy(
        status = ItemStateStatus.Auction.toString, // TODO: Needed?
        auctionStart = Some(event.startTime),
        auctionEnd = Some(event.startTime.plus(Duration.ofMinutes(itemState.auctionDuration)))
      )),
      status = ItemStateStatus.Auction
    )
  }

  def onPriceUpdated(event: PriceUpdated, aggregate: ItemAggregate): ItemAggregate = {
    assert(aggregate.status == ItemStateStatus.Auction)
    val itemState = aggregate.state.get
    aggregate.copy(
      state = Some(itemState.copy(price = Some(event.price)))
    )
  }

  def onAuctionFinished(event: AuctionFinished, aggregate: ItemAggregate): ItemAggregate = {
    assert(aggregate.status == ItemStateStatus.Auction)
    val itemState = aggregate.state.get
    aggregate.copy(
      state = Some(itemState.copy(
        status = ItemStateStatus.Completed.toString, // TODO: Needed?
        price = event.price,
        auctionWinner = event.winner
      )),
      status = ItemStateStatus.Completed
    )
  }

  // -------------------------------------------------------------------------------------------------------------------

  def done(ctx: CommandContext[Done]): Persist = {
    ctx.reply(Done)
    ctx.done
  }

}

