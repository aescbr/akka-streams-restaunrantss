package com.applaudo.crosstraining.akkastreams.services

import com.applaudo.crosstraining.akastreams.models.ConsumerClasses.RestaurantToEntitiesException
import com.applaudo.crosstraining.akkastreams.BaseServiceTest
import org.scalatestplus.mockito.MockitoSugar

class ConsumerServiceTest extends BaseServiceTest
with MockitoSugar{

  "consumer service" should {
    "normalize Restaurant to entities" in {

      val result = consumerService.restaurantToEntities(restaurantExpected)
      assert(restaurantEntitiesExpected == result)
    }

    "throw exception when non valid input restaurant" in {
      assertThrows[RestaurantToEntitiesException](consumerService.restaurantToEntities(null))
    }
  }
}
