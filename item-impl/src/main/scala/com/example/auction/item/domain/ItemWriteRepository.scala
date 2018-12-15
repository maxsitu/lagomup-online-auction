package com.example.auction.item.domain

import java.util.UUID

import com.datastax.driver.core.BoundStatement
import com.example.auction.item.impl._

import scala.concurrent.Future

trait ItemWriteRepository {

  def bindInsertItemCreator(itemId: UUID, creatorId: UUID): BoundStatement

  def bindInsertItemSummaryByCreator(creatorId: UUID, itemId: UUID, title: String, currencyId: String, reservePrice: Int, status: String): BoundStatement

  def bindUpdateItemSummaryStatus(status: String, creatorId: UUID, itemId: UUID): BoundStatement

  def processItemCreated(event: ItemCreated): Future[List[BoundStatement]] = {
    Future.successful(List.empty)
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

}

