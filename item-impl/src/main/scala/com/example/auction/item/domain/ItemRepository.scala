package com.example.auction.item.domain

import java.util.UUID

import com.datastax.driver.core.BoundStatement
import com.example.auction.item.impl._

import scala.concurrent.{ExecutionContext, Future}

trait ItemRepository extends ItemReadRepository {

  val environment: Environment

  implicit val ec: ExecutionContext = environment.ec

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

  def processAuctionStarted(entityId: String, event: AuctionStarted): Future[List[BoundStatement]] = {
    doUpdateItemSummaryStatus(entityId, "Auction")
  }

  def processPriceUpdated(event: PriceUpdated): Future[List[BoundStatement]] = {
    Future.successful(List.empty)
  }

  def processAuctionFinished(entityId: String, event: AuctionFinished): Future[List[BoundStatement]] = {
    doUpdateItemSummaryStatus(entityId, "Auction")
  }

  // -------------------------------------------------------------------------------------------------------------------

  private def doUpdateItemSummaryStatus(itemId: String, status: String): Future[List[BoundStatement]] = {
    val itemUuid = UUID.fromString(itemId)
    selectItemCreator(itemUuid).map {
      case None => throw new IllegalStateException("No itemCreator found for itemId " + itemId)
      case Some(creatorId) => List(
        bindUpdateItemSummaryStatus(
          status,
          creatorId,
          itemUuid
        )
      )
    }
  }

}

