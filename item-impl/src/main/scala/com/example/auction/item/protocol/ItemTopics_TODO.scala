package com.example.auction.item.protocol


import com.example.auction.item.api._
import com.example.auction.item.impl.ItemPorts
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.broker.TopicProducer
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry
import scala.concurrent.{ExecutionContext, Future}

trait ItemTopics_TODO {

  val ports: ItemPorts

  def _itemEvents(): Topic[ItemEvent] = {
  ???
}



}

