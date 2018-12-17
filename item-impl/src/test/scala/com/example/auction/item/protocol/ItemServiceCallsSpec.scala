package com.example.auction.item.protocol

import java.util.UUID

import com.example.auction.item.api.{Item, ItemData, ItemService}
import com.example.auction.item.impl.ItemApplication
import com.example.auction.item.protocol.ClientSecurity._
import com.lightbend.lagom.scaladsl.api.AdditionalConfiguration
import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.{ServiceTest, TestTopicComponents}
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, Matchers}
import play.api.Configuration

class ItemServiceCallsSpec extends AsyncWordSpec with Matchers with BeforeAndAfterAll {

//  val server = ServiceTest.startServer(ServiceTest.defaultSetup.withCassandra()) { ctx =>
//    new ItemApplication(ctx) with LocalServiceLocator with TestTopicComponents {
//      override def additionalConfiguration: AdditionalConfiguration = {
//        super.additionalConfiguration ++ Configuration.from(Map(
//          "cassandra-query-journal.eventual-consistency-delay" -> "0"
//        ))
//      }
//    }
//  }
//
//  val itemService = server.serviceClient.implement[ItemService]

  //import server.materializer

  //override def afterAll(): Unit = server.stop()

  "The item service" should {

    // TODO: Find a better way to do DI with cakes

    "allow creating items" in {
//      val creatorId = UUID.randomUUID
//      createItem(creatorId, sampleItem(creatorId)).map { result =>
//        result should ===("TODO")
//      }

      pending

//      for {
//        created <- createItem(creatorId, sampleItem(creatorId))
//        retrieved <- retrieveItem(created)
//      } yield {
//        created should ===(retrieved)
//      }
    }

    "return all items for a given user" in {
      pending
    }

    "emit auction started event" in {
      pending
    }

  }

  // Helpers -----------------------------------------------------------------------------------------------------------


//  private def sampleItem(creatorId: UUID) = {
//    Item(
//      id = None,
//      creator = creatorId,
//      itemData = ItemData(
//        "title", "description", "USD", 10, 10, 10, None
//      ),
//      price = None,
//      status = "Created",
//      auctionStart = None, auctionEnd = None, auctionWinner = None
//    )
//  }
//
//  private def createItem(creatorId: UUID, createItem: Item) = {
//    itemService.createItem().handleRequestHeader(authenticate(creatorId)).invoke(createItem)
//  }
//
//  private def retrieveItem(item: Item) = {
//    // TODO: ID generation was method on Item
//    itemService.getItem(item.id.getOrElse(UUID.randomUUID())).invoke
//  }

}
