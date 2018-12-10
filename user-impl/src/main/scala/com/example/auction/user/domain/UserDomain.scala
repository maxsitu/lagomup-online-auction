package com.example.auction.user.domain

import akka.Done
import com.example.auction.user.impl._
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity
import com.lightbend.lagom.scaladsl.pubsub.PubSubRegistry

trait UserDomain {
  this: UserEntity with PersistentEntity =>

  def pubSubRegistry: PubSubRegistry

  def initialState: UserAggregate = UserAggregate(None, UserStateStatus.NotCreated)

  def onCreateUser(command: CreateUser, aggregate: UserAggregate, ctx: CommandContext[Done]): Persist = {
    aggregate.status match {
      case UserStateStatus.NotCreated =>
        ctx.thenPersist(UserCreated(command.name))(_ => ctx.reply(Done))
      case UserStateStatus.Created =>
        ctx.invalidCommand("User already exists")
        ctx.done
    }
  }

  def onGetUser(query: GetUser.type, aggregate: UserAggregate, ctx: ReadOnlyCommandContext[UserState]): Unit = {
    // TODO: Example always had UserState
    // TODO: Replytype should be optional
    aggregate.state match {
      case Some(state) => ctx.reply(state)
      case None => ctx.invalidCommand(s"No state found in status ${aggregate.status}")
    }
  }

  def onUserCreated(event: UserCreated, aggregate: UserAggregate): UserAggregate = {
    UserAggregate(Some(UserState(event.name)), UserStateStatus.Created)
  }

}

