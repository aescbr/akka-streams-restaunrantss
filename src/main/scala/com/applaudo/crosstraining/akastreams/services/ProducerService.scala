package com.applaudo.crosstraining.akastreams.services
import com.applaudo.crosstraining.akastreams.models.ProducerClasses._
import com.applaudo.crosstraining.akastreams.models.schemas.ProducerSchemas._
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord, RecordMetadata}

import java.util.concurrent.Future
import scala.util.Try

trait ProducerService {
  def strToRestaurant(input: ProducerInput): Try[Restaurant]
  def sendMessage(restaurant: Restaurant):  Future[RecordMetadata]
}

case class ProducerServiceImpl(
  restaurantProducer: KafkaProducer[String, RestaurantMessage], restaurantSchema: Schema,
  restaurantTopic: String) extends ProducerService {

  override def strToRestaurant(input: ProducerInput): Try[Restaurant] = {
     {
      input match {
        case StrInput(strLine) =>
          processMapper(strLine.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)").toList)
        case ListInput(list) =>
          processMapper(list)
      }
    }
  }

  private def mapListToRestaurant(data : List[String]) : Restaurant = {
      val id = data.head
      val dateAdded = data(1)
      val dateUpdated = data(2)
      val address = data(3)
      val categories = data(4)
      val city = data(5)
      val country = data(6)
      val keys = data(7)
      val latitude = data(8).toDouble
      val longitude = data(9).toDouble
      val name = data(10)
      val postalCode = data(11)
      val province = data(12)
      val sourceURLs = data(13)
      val websites = data(14)

      Restaurant(id, dateAdded, dateUpdated, address, categories, city, country, keys, latitude, longitude,
        name, postalCode, province, sourceURLs, websites)
  }

  override def sendMessage(restaurant: Restaurant): Future[RecordMetadata] = {
    val value = RestaurantMessage(schema = restaurantSchema, payload = restaurant)
    val record = new ProducerRecord(restaurantTopic, restaurant.id, value)
    restaurantProducer.send(record)
  }

  private def processMapper(list: List[String]): Try[Restaurant] = {
    Try(mapListToRestaurant(list))
  }

}
