package com.applaudo.crosstraining.akastreams.services

import com.applaudo.crosstraining.akastreams.models.ConsumerClasses._
import com.applaudo.crosstraining.akastreams.models.ProducerClasses.Restaurant
import com.applaudo.crosstraining.akastreams.models.schemas.ConsumerSchemas._
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord, RecordMetadata}

import java.util.concurrent.Future
import scala.util.{Failure, Success, Try}

trait ConsumerService {
  def restaurantToEntities(restaurant: Restaurant): RestaurantEntitiesMessage
  def sendRestaurantEntities(restaurantEntitiesMessage: RestaurantEntitiesMessage): Set[Future[RecordMetadata]]
}

case class ConsumerServiceImpl(restaurantEntityProducer: KafkaProducer[String, RestaurantEntityMessage],
                               sourceURLProducer: KafkaProducer[String, SourceURLMessage],
                               websiteProducer: KafkaProducer[String, WebsiteMessage],
                               schemaURL: Schema, schemaWebsite: Schema, restaurantEntitySchema: Schema,
                               restaurantEntityTopic: String, sourceURLTopic: String, websiteTopic: String) extends ConsumerService {

   override def restaurantToEntities(restaurant: Restaurant): RestaurantEntitiesMessage  = {
    val result = Try(
      RestaurantEntitiesMessage(
        restaurantToRestaurantEntity(restaurant),
        restaurantToSourceURLs(restaurant),
        restaurantToWebsite(restaurant)
      ))

     result match {
       case Failure(ex) =>
         throw RestaurantToEntitiesException(s"${ex.getClass.getName} " +
                 s"| ${ex.getMessage} - restaurant id: ${restaurant.id}")
       case Success(restaurantMessage) => restaurantMessage
     }
  }

  private def restaurantToSourceURLs(restaurant: Restaurant): List[SourceURLMessage] = {
    val urlsArray = restaurant.sourceURLs.replace("\"", "").split(",")
    urlsArray.map { url =>
      SourceURLMessage(
        schema = schemaURL,
        payload = RestaurantSourceURL(restaurant.id, url)
      )
    }.toList
  }

  private def restaurantToWebsite(restaurant: Restaurant): List[WebsiteMessage] = {
    val urlsArray = restaurant.websites.replace("\"", "").split(",")
    urlsArray.map { site =>
      WebsiteMessage(
        schema = schemaWebsite,
        payload = RestaurantWebSite(restaurant.id, site)
      )
    }.toList
  }

  private def restaurantToRestaurantEntity(restaurant: Restaurant): RestaurantEntityMessage = {
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

  override def sendRestaurantEntities(
    restaurantEntitiesMessage: RestaurantEntitiesMessage): Set[Future[RecordMetadata]] = {
    val id = restaurantEntitiesMessage.restaurantMessage.payload.id
    val record1 = new ProducerRecord[String, RestaurantEntityMessage](restaurantEntityTopic, id,
      restaurantEntitiesMessage.restaurantMessage)

    val restaurantMetadata = restaurantEntityProducer.send(record1)

    val urlsMetadata = restaurantEntitiesMessage.urls.map{url =>
      val recordURL = new ProducerRecord[String, SourceURLMessage](sourceURLTopic, id,url)
      sourceURLProducer.send(recordURL)
    }

    val websitesMetadata = restaurantEntitiesMessage.websites.map{website =>
      val recordWebsite = new ProducerRecord[String, WebsiteMessage](websiteTopic, id,website)
      websiteProducer.send(recordWebsite)
    }
    val list = restaurantMetadata +: (urlsMetadata ++: websitesMetadata)
    list.toSet
  }
}
