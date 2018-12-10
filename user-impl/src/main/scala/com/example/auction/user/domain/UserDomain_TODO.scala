package com.example.auction.user.domain

import com.example.auction.user.impl._
import akka.Done
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity
import com.lightbend.lagom.scaladsl.pubsub.{PubSubRegistry, TopicId}

trait UserDomain_TODO {
  this: UserEntity with PersistentEntity =>

  def pubSubRegistry: PubSubRegistry

  def initialState: UserAggregate = ???

  def onCreateUser(command: CreateUser, aggregate: UserAggregate, ctx: CommandContext[Done]): Persist = {
  ???
}


  def onGetUser(query: GetUser.type, aggregate: UserAggregate, ctx: ReadOnlyCommandContext[Option[UserState]]): Unit = {
  ???
}


  def onUserCreated(event: UserCreated, aggregate: UserAggregate): UserAggregate = {
  ???
}



}
