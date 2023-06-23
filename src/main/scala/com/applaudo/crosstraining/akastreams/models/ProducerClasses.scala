package com.applaudo.crosstraining.akastreams.models

import akka.stream.scaladsl.Source

object ProducerClasses {
  import com.applaudo.crosstraining.akastreams.models.schemas.ProducerSchemas._

  //id,dateAdded,dateUpdated,address,categories,city,country,keys,latitude,longitude,name,postalCode,province,sourceURLs,websites
  case class Restaurant(id: String, dateAdded: String, dateUpdated: String, address: String, categories: String,
                        city: String, country: String, keys: String, latitude: Double, longitude: Double, name: String,
                        postalCode: String, province: String, sourceURLs: String, websites: String)

  case class RestaurantMessage(schema: Schema, payload: Restaurant)
  case class StringToRestaurantMapException(message: String) extends RuntimeException

  sealed trait ProducerInput
  case class StrInput(strLine: String) extends ProducerInput
  case class ListInput(list: List[String]) extends ProducerInput

  sealed trait ProducerSource
  case class StrSource(strSource : Source[String, Any]) extends ProducerSource
  case class ListStrSource(listSource: Source[List[String], Any]) extends ProducerSource

}
