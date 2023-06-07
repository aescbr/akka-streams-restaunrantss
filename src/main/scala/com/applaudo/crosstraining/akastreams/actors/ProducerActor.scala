package com.applaudo.crosstraining.akastreams.actors

import akka.actor.{Actor, ActorLogging, ActorSystem}
import com.applaudo.crosstraining.akastreams.models.ConsumerClasses._
import com.applaudo.crosstraining.akastreams.models.ProducerClasses._
import com.applaudo.crosstraining.akastreams.models.schemas.ProducerSchemas._
import org.apache.kafka.clients.producer.ProducerRecord

object ProducerActor {
  case object InitStream
  case object Ack
  case object Complete
  final case class StreamFailure(ex: Throwable)
}


class ProducerActor extends Actor with ActorLogging{
  import ProducerActor._
  import com.applaudo.crosstraining.akastreams.producers.CSVProducerConfig._

  implicit val system: ActorSystem = context.system


  override def receive: Receive = {
    case _: InitStream.type =>
      println("Init stream...")
      sender ! Ack
    case restaurant: Restaurant =>
      sendMessage(restaurant)
      println(s"sending ${restaurant.id}")
      sender ! Ack
    case restaurantEntitiesMessage: RestaurantEntitiesMessage =>
      sendRestaurantEntities(restaurantEntitiesMessage)
      println(s"processing ${restaurantEntitiesMessage.restaurantMessage.payload.id}")
      sender ! Ack

    case _ : Complete.type =>
      println("stream completed!")
     // system.terminate()

  }

  def sendMessage(restaurant: Restaurant): Unit = {
    val value = RestaurantMessage(schema = restaurantSchema, payload = restaurant)
    val record = new ProducerRecord(restaurantTopic, restaurant.id, value)

    restaurantProducer.send(record)
  }

  def sendRestaurantEntities(restaurantEntitiesMessage: RestaurantEntitiesMessage):Unit = {
    val id = restaurantEntitiesMessage.restaurantMessage.payload.id
    val record1 = new ProducerRecord[String, RestaurantEntityMessage](restaurantEntityTopic, id,
      restaurantEntitiesMessage.restaurantMessage)

    restaurantEntityProducer.send(record1)

    restaurantEntitiesMessage.urls.foreach{url =>
      val recordURL = new ProducerRecord[String, SourceURLMessage](sourceURLTopic, id,url)
      sourceURLProducer.send(recordURL)
    }

    restaurantEntitiesMessage.websites.foreach{website =>
      val recordWebsite = new ProducerRecord[String, WebsiteMessage](websiteTopic, id,website)
      websiteProducer.send(recordWebsite)
    }

  }
}
