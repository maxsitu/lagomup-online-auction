package com.example.auction.transaction.impl

import akka.stream.scaladsl.Flow
import com.example.auction.item.api.{ItemEvent, ItemService}
import com.example.auction.transaction.protocol.ItemEventSubscriber
import com.lightbend.lagom.scaladsl.api.broker.{Subscriber, Topic}

class ItemEventSubscriberImpl(val ports: TransactionPorts) extends ItemEventSubscriber {

  println("hello")
  val itemService: ItemService = ports.itemService
  val itemEvents: Topic[ItemEvent] = itemService.itemEvents
  val subscription: Subscriber[ItemEvent] = itemEvents.subscribe

  subscription.atLeastOnce(Flow[ItemEvent].mapAsync(1) { e =>
    onItemEvent(e)
  })

}
