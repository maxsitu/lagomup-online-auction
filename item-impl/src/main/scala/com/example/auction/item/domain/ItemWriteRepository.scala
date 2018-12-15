package com.example.auction.item.domain

import com.datastax.driver.core.BoundStatement
import com.example.auction.item.impl._

import scala.concurrent.Future

trait ItemWriteRepository {

  def bindInsertItemCreator(itemId: String, creatorId: String): BoundStatement

  def bindInsertItemSummaryByCreator(creatorId: String, itemId: String, title: String, currencyId: String, reservePrice: Int, status: String): BoundStatement

  def bindUpdateItemSummaryStatus(status: String, creatorId: String, itemId: String): BoundStatement

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

