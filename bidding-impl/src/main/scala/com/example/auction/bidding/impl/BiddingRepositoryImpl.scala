package com.example.auction.bidding.impl

import com.example.auction.bidding.domain.BiddingRepository
import akka.stream.scaladsl.Source
import akka.{ Done, NotUsed }
import com.datastax.driver.core.{ BoundStatement, PreparedStatement }
import com.lightbend.lagom.scaladsl.persistence.ReadSideProcessor
import com.lightbend.lagom.scaladsl.persistence.cassandra.{ CassandraReadSide, CassandraSession }
import scala.concurrent.{ ExecutionContext, Future }
import play.api.libs.json.{ Format, Json }
import java.util.UUID
import java.time.Instant

class BiddingRepositoryImpl(db: CassandraSession, readSide: CassandraReadSide, val akkaComponents: AkkaComponents)
  extends ReadSideProcessor[BiddingEvent] with BiddingRepository {

  // Tables
  val createAuctionScheduleTableStatement = """CREATE TABLE IF NOT EXISTS auctionSchedule (
  itemId uuid,
  endAuction timestamp,
  PRIMARY KEY (itemId)
)
"""
  val createAuctionScheduleIndexTableStatement = """CREATE INDEX IF NOT EXISTS auctionScheduleIndex
  on auctionSchedule (endAuction)
"""

  // Writes
  val insertAuctionStatement = """INSERT INTO auctionSchedule(itemId, endAuction) VALUES (?, ?)"""
  var insertAuction: PreparedStatement = _
  val deleteAuctionStatement = """DELETE FROM auctionSchedule where itemId = ?"""
  var deleteAuction: PreparedStatement = _

  // Reads
  val endedAuctionsStatement = """SELECT itemId FROM auctionSchedule WHERE endAuction < toTimestamp(now()) allow filtering"""
  var endedAuctions: PreparedStatement = _

  def endedAuctions(endAuction: Long): Source[String, NotUsed] = {
    db.select(bindEndedAuctions(endAuction)).map(mapEndedAuctionsResult)
  }

  def bindInsertAuction(itemId: UUID, endAuction: Long): BoundStatement = {
    val boundStatement = insertAuction.bind()
    boundStatement.setUUID("itemId", itemId)
    boundStatement.setLong("endAuction", endAuction)
    boundStatement
  }

  def bindDeleteAuction(itemId: UUID): BoundStatement = {
    val boundStatement = deleteAuction.bind()
    boundStatement.setUUID("itemId", itemId)
    boundStatement
  }

  def bindEndedAuctions(endAuction: Long): BoundStatement = {
    val boundStatement = endedAuctions.bind()
    boundStatement.setLong("endAuction", endAuction)

    boundStatement
  }

  override def buildHandler() = readSide.builder[BiddingEvent]("bidding_offset")
    .setGlobalPrepare(() =>
      for {
        _ <- db.executeCreateTable(createAuctionScheduleTableStatement)
        _ <- db.executeCreateTable(createAuctionScheduleIndexTableStatement)
      } yield Done)
    .setPrepare(_ =>
      for {
        _insertAuction <- db.prepare(insertAuctionStatement)
        _deleteAuction <- db.prepare(deleteAuctionStatement)
        _endedAuctions <- db.prepare(endedAuctionsStatement)
      } yield {
        insertAuction = _insertAuction
        deleteAuction = _deleteAuction
        endedAuctions = _endedAuctions
        Done
      })
    .setEventHandler[AuctionStarted](e => processAuctionStarted(e.entityId, e.event))
    .setEventHandler[AuctionCancelled.type](e => processAuctionCancelled(e.entityId, e.event))
    .setEventHandler[BidPlaced](e => processBidPlaced(e.entityId, e.event))
    .setEventHandler[BiddingFinished.type](e => processBiddingFinished(e.entityId, e.event))
    .build()

  override def aggregateTags = Set(BiddingEvent.Tag)

}

