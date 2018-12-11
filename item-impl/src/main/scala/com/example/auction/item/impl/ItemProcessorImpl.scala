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

  

  override def buildHandler() = readSide.builder[ItemEvent]("item_offset")
    
    
    .setEventHandler[ItemCreated](e => processItemCreated(e.event))
.setEventHandler[AuctionStarted](e => processAuctionStarted(e.event))
.setEventHandler[PriceUpdated](e => processPriceUpdated(e.event))
.setEventHandler[AuctionFinished](e => processAuctionFinished(e.event))

    .build()

  override def aggregateTags = Set(ItemEvent.Tag)

}

