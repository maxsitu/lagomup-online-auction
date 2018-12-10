package com.example.auction.user.impl

import com.example.auction.utils.JsonFormats._
import com.example.auction.user.domain.UserDomain
import akka.{Done, NotUsed}
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import com.lightbend.lagom.scaladsl.persistence.{AggregateEvent, AggregateEventTag, PersistentEntity}
import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}
import com.lightbend.lagom.scaladsl.pubsub.PubSubRegistry
import play.api.libs.json.{Format, Json}
import java.time.Instant

class UserEntity(val pubSubRegistry: PubSubRegistry) extends PersistentEntity
  with UserDomain {

  override type State = UserAggregate
  override type Command = UserCommand
  override type Event = UserEvent

  override def behavior: Behavior = {
    Actions()
      .onCommand[CreateUser, Done] {
  case (command: CreateUser, ctx, state) =>
    onCreateUser(command, state, ctx)
}

      .onReadOnlyCommand[GetUser.type, Option[UserState]] {
  case (query: GetUser.type, ctx, state) =>
    onGetUser(query, state, ctx)
}

      .onEvent {
  case (event: UserCreated, state) =>
    onUserCreated(event, state)
}

  }

}

case class UserState(name: String) 

object UserState {
  implicit val format: Format[UserState] = Json.format
}



object UserStateStatus extends Enumeration {
  val NotCreated, Created = Value
  type Status = Value
  implicit val format: Format[Status] = enumFormat(UserStateStatus)
}

case class UserAggregate(state: Option[UserState], status: UserStateStatus.Status)

object UserAggregate {
  implicit val format: Format[UserAggregate] = Json.format
}

sealed trait UserCommand

case class CreateUser(name: String) extends UserCommand with ReplyType[Done]

object CreateUser {
  implicit val format: Format[CreateUser] = Json.format
}

case object GetUser extends UserCommand with ReplyType[Option[UserState]] {
  implicit val format: Format[GetUser.type] = JsonSerializer.emptySingletonFormat(GetUser)
}

sealed trait UserEvent extends AggregateEvent[UserEvent] {
  def aggregateTag = UserEvent.Tag
}

object UserEvent {
  val Tag = AggregateEventTag[UserEvent]
}

case class UserCreated(name: String) extends UserEvent

object UserCreated {
  implicit val format: Format[UserCreated] = Json.format
}

object UserSerializerRegistry extends JsonSerializerRegistry {
  override def serializers = List(
    JsonSerializer[UserAggregate],
JsonSerializer[UserState],
JsonSerializer[CreateUser],
JsonSerializer[GetUser.type],
JsonSerializer[UserCreated]
  )
}

