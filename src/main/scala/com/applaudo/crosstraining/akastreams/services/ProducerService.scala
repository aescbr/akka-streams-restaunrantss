package com.applaudo.crosstraining.akastreams.services
import com.applaudo.crosstraining.akastreams.config.KafkaBrokerConfig.restaurantTopic
import com.applaudo.crosstraining.akastreams.models.ProducerClasses._
import com.applaudo.crosstraining.akastreams.models.schemas.ProducerSchemas.restaurantSchema
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord, RecordMetadata}

import java.util.concurrent.Future
import scala.util.{Failure, Success, Try}

trait ProducerService {
  def strToRestaurant(numLine: Long, input: ProducerInput): Restaurant
  def sendMessage(restaurant: Restaurant):  Future[RecordMetadata]
}

case class ProducerServiceImpl(
  restaurantProducer: KafkaProducer[String, RestaurantMessage]) extends ProducerService {

  override def strToRestaurant(numLine: Long, input: ProducerInput): Restaurant = {
    val result : Try[Restaurant] = {
      input match {
        case StrInput(strLine) =>
         Try(mapListToRestaurant(strLine.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)").toList))
        case ListInput(list) =>
          Try(mapListToRestaurant(list))
      }
    }

    result match {
      case Failure(ex) =>
        throw StringToRestaurantMapException(s"${ex.getClass.getName} | ${ex.getMessage} - in line: $numLine")
      case Success(restaurant) => restaurant
    }
  }

  private def mapListToRestaurant(data : List[String]) : Restaurant={
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
}
