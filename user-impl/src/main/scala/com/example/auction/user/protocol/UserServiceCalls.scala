package com.example.auction.user.protocol


import com.example.auction.user.api._
import akka.stream.scaladsl.Source
import akka.{Done, NotUsed}
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraSession

import com.lightbend.lagom.scaladsl.pubsub.{PubSubRegistry, TopicId}
import com.lightbend.lagom.scaladsl.server.ServerServiceCall
import scala.concurrent.{ExecutionContext, Future}

trait UserServiceCalls {
  implicit val ec: ExecutionContext


  val entityRegistry: PersistentEntityRegistry
val db: CassandraSession
val pubSubRegistry: PubSubRegistry



  def _createUser(request: CreateUser): Future[User] = {
  ???
}

def _getUser(request: NotUsed): Future[User] = {
  ???
}

def _getUsers(request: NotUsed): Future[List[User]] = {
  ???
}





}

