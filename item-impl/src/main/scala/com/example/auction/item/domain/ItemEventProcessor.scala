package com.example.auction.item.domain

import com.datastax.driver.core.{BoundStatement, PreparedStatement}
import com.example.auction.item.impl._

import scala.concurrent.Future

trait ItemEventProcessor {

  def insertItemCreator: PreparedStatement

  def insertItemSummaryByCreator: PreparedStatement

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

