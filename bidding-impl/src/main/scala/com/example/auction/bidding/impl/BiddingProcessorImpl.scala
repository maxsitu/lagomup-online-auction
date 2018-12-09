package com.example.auction.bidding.impl

import com.example.auction.bidding.domain.BiddingProcessor
import akka.Done
import com.datastax.driver.core.{BoundStatement, PreparedStatement}
import com.lightbend.lagom.scaladsl.persistence.ReadSideProcessor
import com.lightbend.lagom.scaladsl.persistence.cassandra.{CassandraReadSide, CassandraSession}
import scala.concurrent.{ExecutionContext, Future}

class BiddingProcessorImpl(
                            session: CassandraSession,
                            readSide: CassandraReadSide
                          )(implicit ec: ExecutionContext) extends ReadSideProcessor[BiddingEvent] with BiddingProcessor {

  var insertAuctionSchedule: PreparedStatement = _


  override def buildHandler() = readSide.builder[BiddingEvent]("bidding_offset")
    .setGlobalPrepare(prepareTables)
    .setPrepare(_ => prepareStatements())
    .setEventHandler[AuctionStarted](e => processAuctionStarted(e.event))
.setEventHandler[AuctionCancelled.type](e => processAuctionCancelled(e.event))
.setEventHandler[BidPlaced](e => processBidPlaced(e.event))
.setEventHandler[BiddingFinished.type](e => processBiddingFinished(e.event))

    .build()

  override def aggregateTags = Set(BiddingEvent.Tag)

  def prepareTables(): Future[Done] = {
    for {
      _ <- session.executeCreateTable("CREATE TABLE IF NOT EXISTS auctionSchedule (itemId TEXT, endAuction TIMESTAMP, PRIMARY KEY (itemId))")

    } yield Done
  }

  def prepareStatements(): Future[Done] = {
    for {
      _insertAuctionSchedule <- session.prepare("INSERT INTO auctionSchedule (itemId, endAuction) VALUES (?, ?)")

    } yield {
      insertAuctionSchedule = _insertAuctionSchedule

      Done
    }
  }

}

