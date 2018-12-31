package com.example.auction.item.domain

import com.example.auction.item.impl._
import akka.NotUsed
import akka.stream.scaladsl.Source
import com.datastax.driver.core.Row
import scala.concurrent.Future
import java.util.UUID

trait ItemReadRepository_TODO {

  def selectItemsByCreatorInStatus(creatorId: UUID, status: String): Future[Seq[ItemSummaryByCreator]]

  def selectItemCreator(itemId: UUID): Future[Option[UUID]]

  def mapSelectItemsByCreatorInStatusResult(row: Row): ItemSummaryByCreator = {
    ???
  }

  def mapSelectItemCreatorResult(row: Row): UUID = {
    ???
  }

}

