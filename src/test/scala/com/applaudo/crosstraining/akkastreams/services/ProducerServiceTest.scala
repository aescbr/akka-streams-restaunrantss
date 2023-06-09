package com.applaudo.crosstraining.akkastreams.services

import com.applaudo.crosstraining.akastreams.models.ProducerClasses.StringToRestaurantMapException
import com.applaudo.crosstraining.akastreams.services.ProducerServiceImpl

class ProducerServiceTest  extends BaseServiceTest {
  override def beforeAll(): Unit = {
    optProducerService = Some(new ProducerServiceImpl())
  }

  "producer service" should {
    "convert valid input string to a restaurant" in{
      val service = optProducerService.get

      val result = service.strToRestaurantWithHandler(1,inputRestaurantStr)
      assert(restaurantExpected == result)
    }

    "throw exception when non valid string" in {
      val service = optProducerService.get
      val inputStr = "1234,2023-06-08T16:06:25Z"

      assertThrows[StringToRestaurantMapException](service.strToRestaurantWithHandler(1,inputStr))
    }
  }
}
