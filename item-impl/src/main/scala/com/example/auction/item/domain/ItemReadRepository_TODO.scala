package com.example.auction.item.domain

import com.example.auction.item.impl._
import akka.NotUsed
import akka.stream.scaladsl.Source
import com.datastax.driver.core.Row
import scala.concurrent.Future

trait ItemReadRepository_TODO {

  def selectItemCreator(itemId: String, limit: Int): Future[Seq[String]]



  def mapSelectItemCreatorResult(row: Row): String = {
  ???
}



}

