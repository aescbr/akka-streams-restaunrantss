package com.applaudo.crosstraining.akastreams.domain

object ConsumerClasses {
  import com.applaudo.crosstraining.akastreams.domain.schemas.ConsumerSchemas._

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
}
