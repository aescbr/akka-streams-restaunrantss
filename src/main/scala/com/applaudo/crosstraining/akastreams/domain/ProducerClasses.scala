package com.applaudo.crosstraining.akastreams.domain

object ProducerClasses {
  import ProducerSchemas._

  //id,dateAdded,dateUpdated,address,categories,city,country,keys,latitude,longitude,name,postalCode,province,sourceURLs,websites
  case class Restaurant(id: String, dateAdded: String, dateUpdated: String, address: String, categories: String,
                        city: String, country: String, keys: String, latitude: Double, longitude: Double, name: String,
                        postalCode: String, province: String, sourceURLs: String, websites: String)

  case class RestaurantMessage(schema: Schema, payload: Restaurant)
}
