package com.example.auction.bidding.domain

import com.example.auction.bidding.impl._
import com.datastax.driver.core.BoundStatement
import scala.concurrent.Future
import java.util.UUID

trait BiddingWriteRepository_TODO {

  def bindInsertAuction(itemId: String, endAuction: Long): BoundStatement
def bindDeleteAuction(itemId: String): BoundStatement

  def processAuctionStarted(event: AuctionStarted): Future[List[BoundStatement]] = {
  Future.successful(List.empty)
}


def processAuctionCancelled(event: AuctionCancelled.type): Future[List[BoundStatement]] = {
  Future.successful(List.empty)
}


def processBidPlaced(event: BidPlaced): Future[List[BoundStatement]] = {
  Future.successful(List.empty)
}


def processBiddingFinished(event: BiddingFinished.type): Future[List[BoundStatement]] = {
  Future.successful(List.empty)
}



}

