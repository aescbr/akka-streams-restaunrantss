package com.applaudo.crosstraining.akastreams

import akka.NotUsed
import akka.actor.{ActorSystem, Props}
import akka.kafka.ConsumerMessage.CommittableMessage
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream.scaladsl.{Flow, Sink}
import com.applaudo.crosstraining.akastreams.CSVProducerConfig.{Restaurant, RestaurantMessage, Schema, SchemaNode}
import org.apache.kafka.common.serialization.StringDeserializer

object RestaurantConsumerConfig {
  case class RestaurantWebSite(restaurantId: String, site: String)
  case class RestaurantSourceURL(restaurantId: String, url: String)
  case class SourceURLMessage(schema: Schema, payload: RestaurantSourceURL)
  case class WebsiteMessage(schema: Schema, payload: RestaurantWebSite)
  case class RestaurantPayload(id: String, dateAdded: String, dateUpdated: String, address: String, categories: String,
                               city: String, country: String, keys: String, latitude: Double, longitude: Double,
                               name: String, postalCode: String, province: String)
  case class RestaurantEntitiesMessage(restaurantMessage: RestaurantEntityMessage, urls: List[SourceURLMessage],
                                       websites: List[WebsiteMessage])
  case class RestaurantEntityMessage(schema: Schema, payload: RestaurantPayload)

  val schemaWebsite: Schema =
    Schema(
      `type` = "struct",
      fields = List(
        SchemaNode(`type` = "string", optional = false, field = "restaurantId"),
        SchemaNode(`type` = "string", optional = false, field = "site")
      ),
      optional = false)

  val schemaURL: Schema =
    Schema(
      `type` = "struct",
      fields = List(
        SchemaNode(`type` = "string", optional = false, field = "restaurantId"),
        SchemaNode(`type` = "string", optional = false, field = "url")
      ),
      optional = false)

  val restaurantEntitySchema: Schema = Schema(
    `type` = "struct",
    fields = List(
      SchemaNode(`type` = "string", optional = false, field = "id"),
      SchemaNode(`type` = "string", optional = false, field = "dateAdded"),
      SchemaNode(`type` = "string", optional = false, field = "dateUpdated"),
      SchemaNode(`type` = "string", optional = false, field = "address"),
      SchemaNode(`type` = "string", optional = false, field = "categories"),
      SchemaNode(`type` = "string", optional = false, field = "city"),
      SchemaNode(`type` = "string", optional = false, field = "country"),
      SchemaNode(`type` = "string", optional = false, field = "keys"),
      SchemaNode(`type` = "float", optional = false, field = "latitude"),
      SchemaNode(`type` = "float", optional = false, field = "longitude"),
      SchemaNode(`type` = "string", optional = false, field = "name"),
      SchemaNode(`type` = "string", optional = false, field = "postalCode"),
      SchemaNode(`type` = "string", optional = false, field = "province")
    ),
    optional = false
    //name = "restaurant"
  )
}

object RestaurantConsumer extends App {

  import ProducerActor._
  import RestaurantConsumerConfig._

  implicit val system: ActorSystem = ActorSystem.create("restaurant-consumer")

  val producerActor = system.actorOf(Props[ProducerActor], "producer-actor")

  val consumerSettings = ConsumerSettings[String, RestaurantMessage](system, new StringDeserializer,
    new RestaurantMessageDeserializer)
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


