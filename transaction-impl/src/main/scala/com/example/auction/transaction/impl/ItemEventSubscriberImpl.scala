package com.example.auction.transaction.impl

import akka.stream.scaladsl.Flow
import com.example.auction.item.api.{ItemEvent, ItemService}
import com.example.auction.transaction.protocol.ItemEventSubscriber

class ItemEventSubscriberImpl(itemService: ItemService) extends ItemEventSubscriber {

  itemService.itemEvents.subscribe.atLeastOnce(Flow[ItemEvent].mapAsync(1) { e =>
    onItemEvent(e)
  })

}
