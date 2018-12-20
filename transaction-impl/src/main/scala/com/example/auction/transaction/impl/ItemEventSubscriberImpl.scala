package com.example.auction.transaction.impl

import akka.stream.scaladsl.Flow
import com.example.auction.item.api.{ItemEvent, ItemService}
import com.example.auction.transaction.protocol.ItemEventSubscriber
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry

class ItemEventSubscriberImpl(itemService: ItemService, val entityRegistry: PersistentEntityRegistry) extends ItemEventSubscriber {

  itemService.itemEvents.subscribe.atLeastOnce(Flow[ItemEvent].mapAsync(1) { e =>
    onItemEvent(e)
  })

}
