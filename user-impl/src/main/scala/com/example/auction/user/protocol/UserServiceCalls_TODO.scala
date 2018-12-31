package com.example.auction.user.protocol

import com.example.auction.user.api._
import com.example.auction.user.impl.UserPorts
import akka.stream.scaladsl.Source
import akka.{ Done, NotUsed }
import akka.actor.ActorSystem
import akka.stream.Materializer
import com.lightbend.lagom.scaladsl.pubsub.{ PubSubRegistry, TopicId }
import com.lightbend.lagom.scaladsl.server.ServerServiceCall
import scala.concurrent.{ ExecutionContext, Future }
import java.util.UUID

trait UserServiceCalls_TODO {

  val ports: UserPorts

  def _createUser(request: CreateUser): Future[User] = {
    ???
  }

  def _getUser(userId: UUID, request: NotUsed): Future[User] = {
    ???
  }

  def _getUsers(request: NotUsed): Future[List[User]] = {
    ???
  }

}

