package com.example.auction.user.api


import akka.{Done, NotUsed}
import akka.stream.scaladsl.Source
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.api.broker.kafka.{KafkaProperties, PartitionKeyStrategy}
import akka.util.ByteString
import com.lightbend.lagom.scaladsl.api.deser.MessageSerializer
import com.lightbend.lagom.scaladsl.api.{Service, ServiceCall}
import com.lightbend.lagom.scaladsl.playjson.JsonSerializer
import julienrf.json.derived
import play.api.libs.json._
import java.time.Instant

trait UserService extends Service {
  def createUser(): ServiceCall[CreateUser, User]

def getUser(userId: String): ServiceCall[NotUsed, User]

def getUsers(): ServiceCall[NotUsed, List[User]]


  
  override def descriptor = {
  import Service._

  named("user")
    .withCalls(
  pathCall("/api/user", createUser _)(implicitly[MessageSerializer[CreateUser, ByteString]], implicitly[MessageSerializer[User, ByteString]]),
pathCall("/api/user/:id", getUser _)(implicitly[MessageSerializer[NotUsed, ByteString]], implicitly[MessageSerializer[User, ByteString]]),
pathCall("/api/user", getUsers _)(implicitly[MessageSerializer[NotUsed, ByteString]], implicitly[MessageSerializer[List[User], ByteString]])
)

    
    .withAutoAcl(true)
}

      
}


case class CreateUser(name: String) 

object CreateUser {
  implicit val format: Format[CreateUser] = Json.format
}

case class User(id: String, name: String) 

object User {
  implicit val format: Format[User] = Json.format
}



