package com.example.auction.item.domain

import com.datastax.driver.core.Row
import com.example.auction.item.impl.ItemSummaryByCreator

import scala.concurrent.Future

trait ItemReadRepository {

  def selectItemsByCreatorInStatus(creatorId: String, status: String): Future[Seq[ItemSummaryByCreator]]

  def selectItemCreator(itemId: String): Future[Seq[String]]

  def mapSelectItemsByCreatorInStatusResult(row: Row): ItemSummaryByCreator = {
    ???
  }

  def mapSelectItemCreatorResult(row: Row): String = {
    row.getString("userId") // TODO: UUID
  }

}

