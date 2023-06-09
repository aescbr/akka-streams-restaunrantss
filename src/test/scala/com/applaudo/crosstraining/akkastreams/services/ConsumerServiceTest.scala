package com.applaudo.crosstraining.akkastreams.services

import com.applaudo.crosstraining.akastreams.models.ConsumerClasses.RestaurantToEntitiesException
import com.applaudo.crosstraining.akastreams.services.ConsumerServiceImpl

class ConsumerServiceTest extends BaseServiceTest {
  override def beforeAll(): Unit = {
    optConsumerService = Some(new ConsumerServiceImpl())
  }

  "consumer service" should {
    "normalize Restaurant to entities" in {
      val service = optConsumerService.get

      val result = service.restaurantToEntities(restaurantExpected)
      assert(restaurantEntitiesExpected == result)
    }
  }
}
