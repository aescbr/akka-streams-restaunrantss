package com.applaudo.crosstraining.akastreams.consumers

import akka.NotUsed
import akka.actor.{ActorSystem, Props}
import akka.kafka.ConsumerMessage.CommittableMessage
import akka.kafka.ConsumerSettings
import akka.kafka.scaladsl.Consumer
import akka.stream.scaladsl.{Flow, Sink}
import com.applaudo.crosstraining.akastreams.RestaurantMessageDeserializer
import com.applaudo.crosstraining.akastreams.actors.ProducerActor
import org.apache.kafka.common.serialization.StringDeserializer

object RestaurantConsumer extends App {

  import ProducerActor._
  import akka.kafka.Subscriptions
  import com.applaudo.crosstraining.akastreams.domain.ConsumerClasses._
  import com.applaudo.crosstraining.akastreams.domain.schemas.ConsumerSchemas._
  import com.applaudo.crosstraining.akastreams.domain.ProducerClasses._

  implicit val system: ActorSystem = ActorSystem.create("restaurant-consumer")

  val producerActor = system.actorOf(Props[ProducerActor], "producer-actor")

  val consumerSettings: ConsumerSettings[String, RestaurantMessage] =
    ConsumerSettings[String, RestaurantMessage](system, new StringDeserializer, new RestaurantMessageDeserializer)
      .withBootstrapServers("localhost:9092")
      .withGroupId("stream-group")


  val mapRestaurant: Flow[CommittableMessage[String, RestaurantMessage], RestaurantEntitiesMessage, NotUsed] =
    Flow[CommittableMessage[String, RestaurantMessage]].map { msg =>
      val restaurant = msg.record.value().payload
      RestaurantEntitiesMessage(
        restaurantToRestaurantEntity(restaurant),
        restaurantToSourceURLs(restaurant),
        restaurantToWebsite(restaurant)
      )
    }
  val sink = Sink.actorRefWithBackpressure(producerActor, InitStream, Ack, Complete, StreamFailure)

  val consumer = Consumer
    .committableSource(consumerSettings, Subscriptions.topics("second-topic"))
    .via(mapRestaurant)
    .runWith(sink)


  def restaurantToSourceURLs(restaurant: Restaurant): List[SourceURLMessage] = {
    val urlsArray = restaurant.sourceURLs.replace("\"", "").split(",")
    urlsArray.map { url =>
      SourceURLMessage(
        schema = schemaURL,
        payload = RestaurantSourceURL(restaurant.id, url)
      )
    }.toList
  }

  def restaurantToWebsite(restaurant: Restaurant): List[WebsiteMessage] = {
    val urlsArray = restaurant.websites.replace("\"", "").split(",")
    urlsArray.map { site =>
      WebsiteMessage(
        schema = schemaWebsite,
        payload = RestaurantWebSite(restaurant.id, site)
      )
    }.toList
  }

  def restaurantToRestaurantEntity(restaurant: Restaurant): RestaurantEntityMessage = {
    RestaurantEntityMessage(restaurantEntitySchema,
      RestaurantPayload(restaurant.id,
        restaurant.dateAdded,
        restaurant.dateUpdated,
        restaurant.address,
        restaurant.categories,
        restaurant.city,
        restaurant.country,
        restaurant.keys,
        restaurant.latitude,
        restaurant.longitude,
        restaurant.name,
        restaurant.postalCode,
        restaurant.province)
    )
  }
}
