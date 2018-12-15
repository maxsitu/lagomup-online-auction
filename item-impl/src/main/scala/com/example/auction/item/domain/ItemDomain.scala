package com.example.auction.item.domain

import java.time.{Duration, Instant}

import akka.Done
import com.example.auction.item.impl._
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity

trait ItemDomain {
  this: ItemEntity with PersistentEntity =>

  def itemEventStream: ItemEventStream

  def initialState: ItemState = ItemState(None, ItemAggregateStatus.NotCreated)

  def onCreateItem(command: CreateItem, aggregate: ItemState, ctx: CommandContext[Done]): Persist = {
    aggregate.status match {
      case ItemAggregateStatus.NotCreated =>
        ctx.thenPersist(ItemCreated(command.item))(_ => ctx.reply(Done))
      case _ =>
        // TODO: Not specified in example
        ???
    }
  }

  def onStartAuction(command: StartAuction, state: ItemState, ctx: CommandContext[Done]): Persist = {
    state.status match {
      case ItemAggregateStatus.Created =>
        if (state.aggregate.get.creator != command.userId) {
          ctx.invalidCommand("Only the creator of an auction can start it")
          ctx.done
        } else {
          ctx.thenPersist(AuctionStarted(Instant.now))(_ => ctx.reply(Done))
        }
      case ItemAggregateStatus.Auction | ItemAggregateStatus.Completed | ItemAggregateStatus.Cancelled =>
        done(ctx)
      case ItemAggregateStatus.NotCreated =>
        // TODO: Not specified in example
        ???
    }
  }

  def onUpdatePrice(command: UpdatePrice, aggregate: ItemState, ctx: CommandContext[Done]): Persist = {
    aggregate.status match {
      case ItemAggregateStatus.Auction =>
        ctx.thenPersist(PriceUpdated(command.price))(_ => ctx.reply(Done))
      case ItemAggregateStatus.Completed | ItemAggregateStatus.Cancelled =>
        done(ctx)
      case ItemAggregateStatus.NotCreated | ItemAggregateStatus.Created =>
        // TODO: Not specified in example
        ???
    }
  }

  def onFinishAuction(command: FinishAuction, aggregate: ItemState, ctx: CommandContext[Done]): Persist = {
    aggregate.status match {
      case ItemAggregateStatus.Auction =>
        ctx.thenPersist(AuctionFinished(command.winner, command.price))(_ => ctx.reply(Done))
      case ItemAggregateStatus.Completed | ItemAggregateStatus.Cancelled =>
        done(ctx)
      case ItemAggregateStatus.NotCreated | ItemAggregateStatus.Created =>
        // TODO: Not specified in example
        ???
    }
  }

  def onGetItem(query: GetItem.type, state: ItemState, ctx: ReadOnlyCommandContext[ItemAggregate]): Unit = {
    // TODO: Example always had ItemState, look at how UserService handles this
    state.aggregate match {
      case Some(aggregate) => ctx.reply(aggregate)
      case None => ctx.invalidCommand(s"No state found in status ${state.status}")
    }
  }

  def onItemCreated(event: ItemCreated, state: ItemState): ItemState = {
    state.copy(
      aggregate = Some(event.item),
      status = ItemAggregateStatus.Created
    )
  }

  def onAuctionStarted(event: AuctionStarted, state: ItemState): ItemState = {
    assert(state.status == ItemAggregateStatus.Created)
    val itemState = state.aggregate.get
    state.copy(
      aggregate = Some(itemState.copy(
        status = ItemAggregateStatus.Auction.toString, // TODO: Needed?
        auctionStart = Some(event.startTime),
        auctionEnd = Some(event.startTime.plus(Duration.ofMinutes(itemState.auctionDuration)))
      )),
      status = ItemAggregateStatus.Auction
    )
  }

  def onPriceUpdated(event: PriceUpdated, state: ItemState): ItemState = {
    assert(state.status == ItemAggregateStatus.Auction)
    val itemState = state.aggregate.get
    state.copy(
      aggregate = Some(itemState.copy(price = Some(event.price)))
    )
  }

  def onAuctionFinished(event: AuctionFinished, state: ItemState): ItemState = {
    assert(state.status == ItemAggregateStatus.Auction)
    val itemState = state.aggregate.get
    state.copy(
      aggregate = Some(itemState.copy(
        status = ItemAggregateStatus.Completed.toString, // TODO: Needed?
        price = event.price,
        auctionWinner = event.winner
      )),
      status = ItemAggregateStatus.Completed
    )
  }

  // -------------------------------------------------------------------------------------------------------------------

  def done(ctx: CommandContext[Done]): Persist = {
    ctx.reply(Done)
    ctx.done
  }

}

