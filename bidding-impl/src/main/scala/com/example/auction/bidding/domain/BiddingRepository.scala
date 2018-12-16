package com.example.auction.bidding.domain

import com.datastax.driver.core.BoundStatement
import com.example.auction.bidding.impl._

import scala.concurrent.{ExecutionContext, Future}

trait BiddingRepository extends BiddingReadRepository {

  val environment: Environment
  implicit val ec: ExecutionContext = environment.ec

  def bindInsertAuction(itemId: String, endAuction: Long): BoundStatement

  def bindDeleteAuction(itemId: String): BoundStatement

  def processAuctionStarted(entityId: String, event: AuctionStarted): Future[List[BoundStatement]] = {
    Future.successful(List.empty)
  }

  def processAuctionCancelled(entityId: String, event: AuctionCancelled.type): Future[List[BoundStatement]] = {
    Future.successful(List.empty)
  }

  def processBidPlaced(entityId: String, event: BidPlaced): Future[List[BoundStatement]] = {
    Future.successful(List.empty)
  }

  def processBiddingFinished(entityId: String, event: BiddingFinished.type): Future[List[BoundStatement]] = {
    Future.successful(List.empty)
  }

}

