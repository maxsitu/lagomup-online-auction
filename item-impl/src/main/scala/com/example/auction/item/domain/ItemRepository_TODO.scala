package com.example.auction.item.domain

import com.example.auction.item.impl._
import com.datastax.driver.core.BoundStatement
import scala.concurrent.Future
import java.util.UUID

trait ItemRepository_TODO extends ItemReadRepository_TODO {

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

