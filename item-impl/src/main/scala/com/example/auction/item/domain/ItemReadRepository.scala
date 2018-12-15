package com.example.auction.item.domain

import com.datastax.driver.core.Row

import scala.concurrent.Future

trait ItemReadRepository {

  def selectItemCreator(itemId: String, limit: Int): Future[Seq[String]]

  def mapSelectItemCreatorResult(row: Row): String = {
    row.getString("userId") // TODO: UUID
  }

}

