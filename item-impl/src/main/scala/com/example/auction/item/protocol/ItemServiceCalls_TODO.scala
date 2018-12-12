package com.example.auction.item.protocol


import com.example.auction.item.api._
import com.example.auction.item.impl.ItemPorts
import akka.stream.scaladsl.Source
import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.stream.Materializer
import com.lightbend.lagom.scaladsl.pubsub.{PubSubRegistry, TopicId}
import com.lightbend.lagom.scaladsl.server.ServerServiceCall
import scala.concurrent.{ExecutionContext, Future}

trait ItemServiceCalls_TODO {

  val ports: ItemPorts

  def _createItem(userId: String, request: Item): Future[Item] = {
  ???
}

def _startAuction(id: String, userId: String, request: NotUsed): Future[Done] = {
  ???
}

def _getItem(id: String, request: NotUsed): Future[Item] = {
  ???
}

def _getItemForUser(id: String, status: String, page: Option[String], request: NotUsed): Future[ItemSummaryPagingState] = {
  ???
}



  def _createItemAuthentication[Request, Response](serviceCall: String => ServerServiceCall[Request, Response]): ServerServiceCall[Request, Response] = {
  ???
}

def _startAuctionAuthentication[Request, Response](serviceCall: String => ServerServiceCall[Request, Response]): ServerServiceCall[Request, Response] = {
  ???
}



}

