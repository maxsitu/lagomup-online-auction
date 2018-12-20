package com.example.auction.transaction.protocol

import java.util.UUID

import akka.Done
import com.example.auction.item.api._
import com.example.auction.transaction.impl
import com.example.auction.transaction.impl.{StartTransaction, TransactionAggregate, TransactionEntity}
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry

import scala.concurrent.Future

trait ItemEventSubscriber {

  val entityRegistry: PersistentEntityRegistry

  def onItemEvent(event: ItemEvent): Future[Done] = {
    event match {
      case AuctionFinished(itemId, item) =>
        item.auctionWinner match {
          case None =>
            // If an auction doesn't have a winner, then we can't start a transaction
            Future.successful(Done)
          case Some(winner) =>
            entityRegistry.refFor[TransactionEntity](itemId.toString)
              .ask(StartTransaction(toImpl(item, winner)))
        }
      case _ =>
        Future.successful(Done)
    }
  }

  // -------------------------------------------------------------------------------------------------------------------

  def toImpl(itemData: ItemData): impl.ItemData = {
    impl.ItemData(
      itemData.title,
      itemData.description,
      itemData.currencyId,
      itemData.increment,
      itemData.reservePrice,
      itemData.auctionDuration,
      itemData.categoryId
    )
  }

  def toImpl(item: Item, winner: UUID): TransactionAggregate = {
    TransactionAggregate(
      item.id.get, item.creator, winner, toImpl(item.itemData), item.price.get, None, None, None
    )
  }

}

