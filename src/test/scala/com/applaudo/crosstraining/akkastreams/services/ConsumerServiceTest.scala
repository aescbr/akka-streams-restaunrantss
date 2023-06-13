package com.applaudo.crosstraining.akkastreams.services

import com.applaudo.crosstraining.akastreams.models.ConsumerClasses.{RestaurantEntityMessage, RestaurantToEntitiesException, SourceURLMessage, WebsiteMessage}
import com.applaudo.crosstraining.akastreams.services.ConsumerServiceImpl
import org.apache.kafka.clients.producer.KafkaProducer
import org.scalatestplus.mockito.MockitoSugar

class ConsumerServiceTest extends BaseServiceTest
with MockitoSugar{
  override def beforeAll(): Unit = {
    val mockRestaurantEntityProducer: KafkaProducer[String, RestaurantEntityMessage] =
      mock[KafkaProducer[String, RestaurantEntityMessage]]

    val mockSourceURLProducer: KafkaProducer[String, SourceURLMessage] =
      mock[ KafkaProducer[String, SourceURLMessage]]

    val mockWebsiteProducer: KafkaProducer[String,WebsiteMessage] =
      mock[KafkaProducer[String,WebsiteMessage]]

    optConsumerService = Some(ConsumerServiceImpl(mockRestaurantEntityProducer,
      mockSourceURLProducer, mockWebsiteProducer))
  }

  "consumer service" should {
    "normalize Restaurant to entities" in {
      val service = optConsumerService.get

      val result = service.restaurantToEntities(restaurantExpected)
      assert(restaurantEntitiesExpected == result)
    }

    "throw exception when non valid input restaurant" in {
      val service = optConsumerService.get
      assertThrows[RestaurantToEntitiesException](service.restaurantToEntities(null))
    }
  }
}
