package com.example.auction.item.protocol


import com.example.auction.item.api._
import akka.stream.scaladsl.Source
import akka.{Done, NotUsed}
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraSession
import com.lightbend.lagom.scaladsl.pubsub.{PubSubRegistry, TopicId}
import com.lightbend.lagom.scaladsl.server.ServerServiceCall

import scala.concurrent.{ExecutionContext, Future}

trait ItemServiceCalls {
  implicit val ec: ExecutionContext


  val entityRegistry: PersistentEntityRegistry
  val db: CassandraSession
  val pubSubRegistry: PubSubRegistry



  def _createItem(request: Item): Future[Item] = {
    ???
  }

  def _startAuction(id: String, request: NotUsed): Future[Done] = {
    ???
  }

  def _getItem(id: String, request: NotUsed): Future[Item] = {
    ???
  }

  def _getItemForUser(id: String, status: String, page: Option[String], request: NotUsed): Future[ItemSummaryPagingState] = {
    ???
  }





}

