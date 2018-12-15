package com.example.auction.item.domain

import com.example.auction.item.impl._
import akka.NotUsed
import akka.stream.scaladsl.Source
import com.datastax.driver.core.Row
import scala.concurrent.Future

trait ItemReadRepository_TODO {

  def selectItemsByCreatorInStatus(creatorId: String, status: String): Future[Seq[ItemSummaryByCreator]]

def selectItemCreator(itemId: String): Future[Seq[String]]



  def mapSelectItemsByCreatorInStatusResult(row: Row): ItemSummaryByCreator = {
  ???
}

def mapSelectItemCreatorResult(row: Row): String = {
  ???
}



}

