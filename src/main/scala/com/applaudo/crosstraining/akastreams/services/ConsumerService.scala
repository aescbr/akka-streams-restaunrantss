package com.applaudo.crosstraining.akastreams.services

import com.applaudo.crosstraining.akastreams.models.ProducerClasses.Restaurant
import com.applaudo.crosstraining.akastreams.models.ConsumerClasses._
import com.applaudo.crosstraining.akastreams.models.schemas.ConsumerSchemas._
import com.applaudo.crosstraining.akastreams.config.KafkaBrokerConfig.{restaurantEntityProducer, restaurantEntityTopic, sourceURLProducer, sourceURLTopic, websiteProducer, websiteTopic}
import org.apache.kafka.clients.producer.ProducerRecord

trait ConsumerService {
  def restaurantToEntities(restaurant: Restaurant): Any
  def sendRestaurantEntities(restaurantEntitiesMessage: RestaurantEntitiesMessage):Unit
}

class ConsumerServiceImpl extends ConsumerService {
  override def restaurantToEntities(restaurant: Restaurant): Any = {
    try{
      RestaurantEntitiesMessage(
        restaurantToRestaurantEntity(restaurant),
        restaurantToSourceURLs(restaurant),
        restaurantToWebsite(restaurant)
      )
    } catch {
      case ex: RuntimeException =>
        throw RestaurantToEntitiesException(s"${ex.getClass.getName} " +
          s"| ${ex.getMessage} - restaurant id: ${restaurant.id}")
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

  override def sendRestaurantEntities(restaurantEntitiesMessage: RestaurantEntitiesMessage):Unit = {
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
