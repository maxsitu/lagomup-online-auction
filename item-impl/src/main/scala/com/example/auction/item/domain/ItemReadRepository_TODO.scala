package com.example.auction.item.domain

import com.example.auction.item.impl._
import akka.NotUsed
import akka.stream.scaladsl.Source
import com.datastax.driver.core.Row
import scala.concurrent.Future

trait ItemReadRepository_TODO {

  def selectItemsByCreatorInStatus(creatorId: String, status: String, limit: Int): Future[Seq[ItemSummaryByCreator]]

def selectItemCreator(itemId: String, limit: Int): Future[Seq[String]]



  def mapSelectItemsByCreatorInStatusResult(row: Row): ItemSummaryByCreator = {
  ???
}

def mapSelectItemCreatorResult(row: Row): String = {
  ???
}



}

