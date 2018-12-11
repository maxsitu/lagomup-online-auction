package com.example.auction.item.impl

import com.example.auction.item.domain.ItemProcessor
import akka.Done
import com.datastax.driver.core.{BoundStatement, PreparedStatement}
import com.lightbend.lagom.scaladsl.persistence.ReadSideProcessor
import com.lightbend.lagom.scaladsl.persistence.cassandra.{CassandraReadSide, CassandraSession}
import scala.concurrent.{ExecutionContext, Future}

class ItemProcessorImpl(
                            db: CassandraSession,
                            readSide: CassandraReadSide
                          )(implicit ec: ExecutionContext) extends ReadSideProcessor[ItemEvent] with ItemProcessor {

  var insertItemCreator: PreparedStatement = _
var insertItemSummaryByCreator: PreparedStatement = _


  override def buildHandler() = readSide.builder[ItemEvent]("item_offset")
    .setGlobalPrepare(() =>
  for {
    _ <- db.executeCreateTable("CREATE TABLE IF NOT EXISTS itemCreator (itemId TIMEUUID, creatorId UUID, PRIMARY KEY ((itemId)))")
_ <- db.executeCreateTable("CREATE TABLE IF NOT EXISTS itemSummaryByCreator (creatorId UUID, itemId TIMEUUID, title TEXT, currencyId TEXT, reservePrice INT, status TEXT, PRIMARY KEY ((creatorId), itemId)) WITH CLUSTERING ORDER BY (itemId DESC)")

  } yield Done
)

    .setPrepare(_ =>
  for {
    _insertItemCreator <- db.prepare("INSERT INTO itemCreator (itemId, creatorId) VALUES (?, ?)")
_insertItemSummaryByCreator <- db.prepare("INSERT INTO itemSummaryByCreator (creatorId, itemId, title, currencyId, reservePrice, status) VALUES (?, ?, ?, ?, ?, ?)")

  } yield {
    insertItemCreator = _insertItemCreator
insertItemSummaryByCreator = _insertItemSummaryByCreator

    Done
  }
)

    .setEventHandler[ItemCreated](e => processItemCreated(e.event))
.setEventHandler[AuctionStarted](e => processAuctionStarted(e.event))
.setEventHandler[PriceUpdated](e => processPriceUpdated(e.event))
.setEventHandler[AuctionFinished](e => processAuctionFinished(e.event))

    .build()

  override def aggregateTags = Set(ItemEvent.Tag)

}

