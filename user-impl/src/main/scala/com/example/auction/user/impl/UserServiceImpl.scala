package com.example.auction.user.impl


import com.example.auction.user.api._
import com.example.auction.user.protocol._
import com.lightbend.lagom.scaladsl.api.ServiceCall
import akka.actor.ActorSystem
import akka.stream.Materializer
import com.lightbend.lagom.scaladsl.pubsub.PubSubRegistry
import akka.stream.scaladsl.Flow
import scala.concurrent.ExecutionContext
import com.lightbend.lagom.scaladsl.server.ServerServiceCall
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraSession


class UserServiceImpl(val entityRegistry: PersistentEntityRegistry, val db: CassandraSession, val pubSubRegistry: PubSubRegistry, val actorSystem: ActorSystem, val mat: Materializer)
                                         (implicit val ec: ExecutionContext)  extends UserService
  with UserServiceCalls   {

  
  override def createUser() = ServiceCall { request =>
  _createUser(request)
}

override def getUser(userId: String) = ServiceCall { request =>
  _getUser(userId, request)
}

override def getUsers() = ServiceCall { request =>
  _getUsers(request)
}


  

}

