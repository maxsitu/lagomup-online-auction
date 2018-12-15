package com.example.auction.item.domain

import java.util.UUID

import com.datastax.driver.core.Row
import com.example.auction.item.impl.ItemSummaryByCreator

import scala.concurrent.Future

trait ItemReadRepository {

  def selectItemsByCreatorInStatus(creatorId: UUID, status: String): Future[Seq[ItemSummaryByCreator]]

  def selectItemCreator(itemId: UUID): Future[Option[UUID]]

  /*
  creatorId UUID,
  itemId timeuuid,
  title text,
  currencyId text,
  reservePrice int,
  status text,
   */
  def mapSelectItemsByCreatorInStatusResult(row: Row): ItemSummaryByCreator = {
    ItemSummaryByCreator(
      row.getUUID("creatorId"),
      row.getUUID("itemId"),
      row.getString("title"),
      row.getString("currencyId"),
      row.getInt("reservePrice"),
      row.getString("status")
    )
  }

  def mapSelectItemCreatorResult(row: Row): UUID = {
    row.getUUID("creatorId") // TODO: UUID
  }

}

