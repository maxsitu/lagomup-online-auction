package com.example.auction.item.impl


import com.example.auction.item.api._
import com.example.auction.item.protocol._
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.pubsub.PubSubRegistry
import akka.stream.scaladsl.Flow
import scala.concurrent.ExecutionContext
import com.lightbend.lagom.scaladsl.server.ServerServiceCall


class ItemServiceImpl(val pubSubRegistry: PubSubRegistry)
                                         (implicit val ec: ExecutionContext)  extends ItemService
  with ItemServiceCalls   {

  
  override def createItem() = ServiceCall { request =>
  _createItem(request)
}

override def startAuction(id: String) = ServiceCall { request =>
  _startAuction(id, request)
}

override def getItem(id: String) = ServiceCall { request =>
  _getItem(id, request)
}

override def getItemForUser(id: String, status: String, page: Option[String]) = ServiceCall { request =>
  _getItemForUser(id, status, page, request)
}


  

}

