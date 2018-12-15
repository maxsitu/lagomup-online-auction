package com.example.auction.item.impl

import com.example.auction.item.domain.ItemRepository
import akka.stream.scaladsl.Source
import akka.{Done, NotUsed}
import com.datastax.driver.core.{BoundStatement, PreparedStatement}
import com.lightbend.lagom.scaladsl.persistence.ReadSideProcessor
import com.lightbend.lagom.scaladsl.persistence.cassandra.{CassandraReadSide, CassandraSession}
import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.json.{Format, Json}
import java.util.UUID

class ItemRepositoryImpl(db: CassandraSession, readSide: CassandraReadSide)(implicit ec: ExecutionContext)
  extends ReadSideProcessor[ItemEvent] with ItemRepository {

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
  val selectItemsByCreatorInStatusStatement = """SELECT * FROM itemSummaryByCreatorAndStatus
WHERE creatorId = ? AND status = ?
ORDER BY status ASC, itemId DESC
LIMIT ?
"""
var selectItemsByCreatorInStatus: PreparedStatement = _
val selectItemCreatorStatement = """SELECT * FROM itemCreator WHERE itemId = ?"""
var selectItemCreator: PreparedStatement = _


  def selectItemsByCreatorInStatus(creatorId: UUID, status: String): Future[Seq[ItemSummaryByCreator]] = {
  db.selectAll(bindSelectItemsByCreatorInStatus(creatorId, status)).map(_.map(mapSelectItemsByCreatorInStatusResult))
}

def selectItemCreator(itemId: UUID): Future[Option[UUID]] = {
  db.selectOne(bindSelectItemCreator(itemId)).map(_.map(mapSelectItemCreatorResult))
}



  def bindInsertItemCreator(itemId: UUID, creatorId: UUID): BoundStatement = {
  val boundStatement = insertItemCreator.bind()
  boundStatement.setUUID("itemId", itemId)
boundStatement.setUUID("creatorId", creatorId)
  boundStatement
}

def bindInsertItemSummaryByCreator(creatorId: UUID, itemId: UUID, title: String, currencyId: String, reservePrice: Int, status: String): BoundStatement = {
  val boundStatement = insertItemSummaryByCreator.bind()
  boundStatement.setUUID("creatorId", creatorId)
boundStatement.setUUID("itemId", itemId)
boundStatement.setString("title", title)
boundStatement.setString("currencyId", currencyId)
boundStatement.setInt("reservePrice", reservePrice)
boundStatement.setString("status", status)
  boundStatement
}

def bindUpdateItemSummaryStatus(status: String, creatorId: UUID, itemId: UUID): BoundStatement = {
  val boundStatement = updateItemSummaryStatus.bind()
  boundStatement.setString("status", status)
boundStatement.setUUID("creatorId", creatorId)
boundStatement.setUUID("itemId", itemId)
  boundStatement
}



  def bindSelectItemsByCreatorInStatus(creatorId: UUID, status: String): BoundStatement = {
  val boundStatement = selectItemsByCreatorInStatus.bind()
  boundStatement.setUUID("creatorId", creatorId)
boundStatement.setString("status", status)
  
  boundStatement
}

def bindSelectItemCreator(itemId: UUID): BoundStatement = {
  val boundStatement = selectItemCreator.bind()
  boundStatement.setUUID("itemId", itemId)
  
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
      _selectItemsByCreatorInStatus <- db.prepare(selectItemsByCreatorInStatusStatement)
_selectItemCreator <- db.prepare(selectItemCreatorStatement)
    } yield {
      insertItemCreator = _insertItemCreator
insertItemSummaryByCreator = _insertItemSummaryByCreator
updateItemSummaryStatus = _updateItemSummaryStatus
      selectItemsByCreatorInStatus = _selectItemsByCreatorInStatus
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

case class ItemSummaryByCreator(creatorId: UUID, id: UUID, title: String, currencyId: String, reservePrice: Int, status: String) 

object ItemSummaryByCreator {
  implicit val format: Format[ItemSummaryByCreator] = Json.format
}

