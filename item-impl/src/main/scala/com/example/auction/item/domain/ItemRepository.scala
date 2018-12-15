package com.example.auction.item.domain

import java.util.UUID

import com.datastax.driver.core.BoundStatement
import com.example.auction.item.impl._

import scala.concurrent.Future

trait ItemRepository extends ItemReadRepository {

  def bindInsertItemCreator(itemId: UUID, creatorId: UUID): BoundStatement

  def bindInsertItemSummaryByCreator(creatorId: UUID, itemId: UUID, title: String, currencyId: String, reservePrice: Int, status: String): BoundStatement

  def bindUpdateItemSummaryStatus(status: String, creatorId: UUID, itemId: UUID): BoundStatement

  def processItemCreated(event: ItemCreated): Future[List[BoundStatement]] = {
    Future.successful(List(
      bindInsertItemCreator(
        event.item.id,
        event.item.creator
      ),
      bindInsertItemSummaryByCreator(
        event.item.creator,
        event.item.id,
        event.item.title,
        event.item.currencyId,
        event.item.reservePrice,
        event.item.status
      )
    ))
  }

  def processAuctionStarted(event: AuctionStarted): Future[List[BoundStatement]] = {
    Future.successful(List.empty)
  }

  def processPriceUpdated(event: PriceUpdated): Future[List[BoundStatement]] = {
    Future.successful(List.empty)
  }

  def processAuctionFinished(event: AuctionFinished): Future[List[BoundStatement]] = {
    Future.successful(List.empty)
  }

  // -------------------------------------------------------------------------------------------------------------------

  private def doUpdateItemSummaryStatus(itemId: String, status: String) = {
    val itemUuid = UUID.fromString(itemId)
    //selectItemCreator(itemUuid).flatMap {
    //  case None => throw new IllegalStateException("No itemCreator found for itemId " + itemId)
    //  case Some(row) => ???
    //}
    // TODO: Need Execution Context
    ???
  }

}

