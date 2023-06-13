package com.applaudo.crosstraining.akkastreams.services

import com.applaudo.crosstraining.akastreams.models.ProducerClasses.{RestaurantMessage, StringToRestaurantMapException}
import com.applaudo.crosstraining.akastreams.services.ProducerServiceImpl
import org.apache.kafka.clients.producer.KafkaProducer
import org.scalatestplus.mockito.MockitoSugar

class ProducerServiceTest  extends BaseServiceTest
with MockitoSugar {

  override def beforeAll(): Unit = {
    val mockProducer: KafkaProducer[String, RestaurantMessage] = mock[KafkaProducer[String, RestaurantMessage]]
    optProducerService = Some(
      ProducerServiceImpl(mockProducer))
  }

  "producer service" should {
    "map valid input string to a restaurant" in{
      val service = optProducerService.get

      val result = service.strToRestaurantWithHandler(1,Left(inputRestaurantStr))
      assert(restaurantExpected == result)
    }

    "throw exception when non valid input string" in {
      val service = optProducerService.get
      val inputStr = "1234,2023-06-08T16:06:25Z"

      assertThrows[StringToRestaurantMapException](service.strToRestaurantWithHandler(1,Left(inputStr)))
    }

    "map valid input list to a restaurant" in {
      val service = optProducerService.get

      val result = service.strToRestaurantWithHandler(1, Right(inputRestaurantList))
      assert(restaurantExpected == result)
    }

    "throw exception when non valid input list" in {
      val service = optProducerService.get

      assertThrows[StringToRestaurantMapException](service
        .strToRestaurantWithHandler(1,Right(nonValidRestaurantInputList)))
    }
  }
}
