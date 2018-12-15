package com.example.auction.item.domain

import java.util.UUID

import com.datastax.driver.core.Row
import com.example.auction.item.impl.ItemSummaryByCreator

import scala.concurrent.Future

trait ItemReadRepository {

  def selectItemsByCreatorInStatus(creatorId: UUID, status: String): Future[Seq[ItemSummaryByCreator]]

  def selectItemCreator(itemId: UUID): Future[Seq[String]]

  def mapSelectItemsByCreatorInStatusResult(row: Row): ItemSummaryByCreator = {
    ???
  }

  def mapSelectItemCreatorResult(row: Row): String = {
    row.getString("userId") // TODO: UUID
  }

}

