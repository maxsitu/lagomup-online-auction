package com.example.auction.item.protocol

import java.util.UUID

import akka.persistence.query.Offset
import com.example.auction.item.{api, impl}
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.broker.TopicProducer
import com.lightbend.lagom.scaladsl.persistence.EventStreamElement

import scala.concurrent.{ExecutionContext, Future}

trait ItemTopics {

  val ports: impl.ItemPorts
  implicit val itemTopicsEC: ExecutionContext = ports.akkaComponents.ec

  def _itemEvents(): Topic[api.ItemEvent] = {
    // TODO: taggedStreamWithOffset
    TopicProducer.singleStreamWithOffset { fromOffset =>
      ports.entityRegistry.eventStream(impl.ItemEvent.Tag, fromOffset)
        .filter { e =>
          e.event match {
            case x@(_: impl.ItemCreated | _: impl.AuctionStarted | _: impl.AuctionFinished) => true
            case _ => false
          }
        }.mapAsync(1)(convertEvent)
    }
  }

  // Helpers -----------------------------------------------------------------------------------------------------------

  private def convertItem(item: impl.ItemAggregate): api.Item = {
    val itemData = api.ItemData(item.title, item.description, item.currencyId, item.increment, item.reservePrice, item.auctionDuration, None)
    api.Item(Some(item.id), item.creator, itemData, item.price, item.status, item.auctionStart,
      item.auctionEnd, item.auctionWinner)
  }

  private def convertEvent(eventStreamElement: EventStreamElement[impl.ItemEvent]): Future[(api.ItemEvent, Offset)] = {

    eventStreamElement match {
      case EventStreamElement(itemId, impl.AuctionStarted(_), offset) =>
        entityRefString(itemId).ask(impl.GetItem).map {
          case Some(item) =>
            (api.AuctionStarted(
              itemId = item.id,
              creator = item.creator,
              reservePrice = item.reservePrice,
              increment = item.increment,
              startDate = item.auctionStart.get,
              endDate = item.auctionEnd.get
            ), offset)
          case None =>
            // TODO: Not specified in example
            ???
        }
      case EventStreamElement(itemId, impl.AuctionFinished(winner, price), offset) =>
        entityRefString(itemId).ask(impl.GetItem).map {
          case Some(item) =>
            (api.AuctionFinished(
              itemId = item.id,
              item = convertItem(item)
            ), offset)
          case None =>
            // TODO: Not specified in example
            ???
        }
      case EventStreamElement(itemId, impl.ItemCreated(item), offset) =>
        Future.successful {
          (api.ItemUpdated(
            itemId = item.id,
            creator = item.creator,
            title = item.title,
            description = item.description,
            currencyId = item.currencyId,
            status = item.status // TODO: Enum
          ), offset)
        }
    }
  }

  private def entityRef(itemId: UUID) = entityRefString(itemId.toString)

  private def entityRefString(itemId: String) = ports.entityRegistry.refFor[impl.ItemEntity](itemId)


}

