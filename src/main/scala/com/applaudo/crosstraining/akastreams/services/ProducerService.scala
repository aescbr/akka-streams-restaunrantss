package com.applaudo.crosstraining.akastreams.services
import com.applaudo.crosstraining.akastreams.config.KafkaBrokerConfig.{brokerProps, restaurantTopic}
import com.applaudo.crosstraining.akastreams.models.ProducerClasses._
import com.applaudo.crosstraining.akastreams.models.schemas.ProducerSchemas.restaurantSchema
import com.goyeau.kafka.streams.circe.CirceSerdes
import io.circe.generic.auto._
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}


trait ProducerService {
  def strToRestaurantWithHandler(numLine: Integer, input: Either[String, List[String]]): Restaurant
  def sendMessage(restaurant: Restaurant): Unit
}

class ProducerServiceImpl extends ProducerService {

  val restaurantProducer = new KafkaProducer(brokerProps,
    CirceSerdes.serializer[String],
    CirceSerdes.serializer[RestaurantMessage]
  )

  override def strToRestaurantWithHandler(numLine: Integer, input: Either[String, List[String]]): Restaurant = {
    try {
      input match {
        case Left(strLine) =>
          val data = strLine.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)").toList
          mapListToRestaurant(data)
        case Right(list) =>
          mapListToRestaurant(list)
      }
    }
    catch {
      case ex: RuntimeException =>
        throw StringToRestaurantMapException(s"${ex.getClass.getName} | ${ex.getMessage} - in line: $numLine")

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

  override def sendMessage(restaurant: Restaurant): Unit = {
    val value = RestaurantMessage(schema = restaurantSchema, payload = restaurant)
    val record = new ProducerRecord(restaurantTopic, restaurant.id, value)
    restaurantProducer.send(record)
  }
}
