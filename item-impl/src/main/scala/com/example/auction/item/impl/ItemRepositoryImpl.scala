package com.example.auction.item.impl

import com.example.auction.item.domain.{ItemReadRepository, ItemWriteRepository}
import akka.stream.scaladsl.Source
import akka.{Done, NotUsed}
import com.datastax.driver.core.{BoundStatement, PreparedStatement}
import com.lightbend.lagom.scaladsl.persistence.ReadSideProcessor
import com.lightbend.lagom.scaladsl.persistence.cassandra.{CassandraReadSide, CassandraSession}
import scala.concurrent.{ExecutionContext, Future}

class ItemRepositoryImpl(db: CassandraSession, readSide: CassandraReadSide)(implicit ec: ExecutionContext)
  extends ReadSideProcessor[ItemEvent]
    with ItemWriteRepository
    with ItemReadRepository {

  // Tables
  val createItemCreatorTableStatement = """CREATE TABLE IF NOT EXISTS itemCreator (
  itemId timeuuid PRIMARY KEY,
  creatorId UUID
)
"""
val createItemSummaryByCreatorTableStatement = """CREATE TABLE IF NOT EXISTS itemSummaryByCreator (
  creatorId UUID,
  itemId timeuuid,
  title text,
  currencyId text,
  reservePrice int,
  status text,
  PRIMARY KEY (creatorId, itemId)
) WITH CLUSTERING ORDER BY (itemId DESC)
"""
val createItemSummaryByCreatorAndStatusTableStatement = """CREATE MATERIALIZED VIEW IF NOT EXISTS itemSummaryByCreatorAndStatus AS
  SELECT * FROM itemSummaryByCreator
  WHERE status IS NOT NULL AND itemId IS NOT NULL
  PRIMARY KEY (creatorId, status, itemId)
  WITH CLUSTERING ORDER BY (status ASC, itemId DESC)
"""


  // Writes
  val insertItemCreatorStatement = """INSERT INTO itemCreator (
  itemId,
  creatorId
) VALUES (?, ?)
"""
var insertItemCreator: PreparedStatement = _
val insertItemSummaryByCreatorStatement = """INSERT INTO itemSummaryByCreator (
  creatorId,
  itemId,
  title,
  currencyId,
  reservePrice,
  status
) VALUES (?, ?, ?, ?, ?, ?)
"""
var insertItemSummaryByCreator: PreparedStatement = _
val updateItemSummaryStatusStatement = """UPDATE itemSummaryByCreator
  SET status = ?
WHERE
  creatorId = ? AND itemId = ?
"""
var updateItemSummaryStatus: PreparedStatement = _


  // Reads
  val selectItemCreatorStatement = """SELECT * FROM itemCreator WHERE itemId = ? LIMIT ?"""
var selectItemCreator: PreparedStatement = _


  def selectItemCreator(itemId: String, limit: Int): Future[Seq[String]] = {
  db.selectAll(bindSelectItemCreator(itemId, limit)).map(_.map(mapSelectItemCreatorResult))
}



  def bindInsertItemCreator(itemId: String, creatorId: String): BoundStatement = {
  val boundStatement = insertItemCreator.bind()
  boundStatement.setString("itemId", itemId)
boundStatement.setString("creatorId", creatorId)
  boundStatement
}

def bindInsertItemSummaryByCreator(creatorId: String, itemId: String, title: String, currencyId: String, reservePrice: Int, status: String): BoundStatement = {
  val boundStatement = insertItemSummaryByCreator.bind()
  boundStatement.setString("creatorId", creatorId)
boundStatement.setString("itemId", itemId)
boundStatement.setString("title", title)
boundStatement.setString("currencyId", currencyId)
boundStatement.setInt("reservePrice", reservePrice)
boundStatement.setString("status", status)
  boundStatement
}

def bindUpdateItemSummaryStatus(status: String, creatorId: String, itemId: String): BoundStatement = {
  val boundStatement = updateItemSummaryStatus.bind()
  boundStatement.setString("status", status)
boundStatement.setString("creatorId", creatorId)
boundStatement.setString("itemId", itemId)
  boundStatement
}



  def bindSelectItemCreator(itemId: String, limit: Int): BoundStatement = {
  val boundStatement = selectItemCreator.bind()
  boundStatement.setString("itemId", itemId)
  boundStatement.setInt("[LIMIT]", limit)
  boundStatement
}



  override def buildHandler() = readSide.builder[ItemEvent]("item_offset")
  .setGlobalPrepare(() =>
    for {
      _ <- db.executeCreateTable(createItemCreatorTableStatement)
_ <- db.executeCreateTable(createItemSummaryByCreatorTableStatement)
_ <- db.executeCreateTable(createItemSummaryByCreatorAndStatusTableStatement)
    } yield Done
  )
  .setPrepare(_ =>
    for {
      _insertItemCreator <- db.prepare(insertItemCreatorStatement)
_insertItemSummaryByCreator <- db.prepare(insertItemSummaryByCreatorStatement)
_updateItemSummaryStatus <- db.prepare(updateItemSummaryStatusStatement)
      _selectItemCreator <- db.prepare(selectItemCreatorStatement)
    } yield {
      insertItemCreator = _insertItemCreator
insertItemSummaryByCreator = _insertItemSummaryByCreator
updateItemSummaryStatus = _updateItemSummaryStatus
      selectItemCreator = _selectItemCreator
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

