package com.applaudo.crosstraining.akastreams.services

import com.applaudo.crosstraining.akastreams.models.ProducerClasses.Restaurant
import com.applaudo.crosstraining.akastreams.models.ConsumerClasses._
import com.applaudo.crosstraining.akastreams.models.schemas.ConsumerSchemas._

trait ConsumerService {
  def restaurantToEntities(restaurant: Restaurant): Any
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
}
