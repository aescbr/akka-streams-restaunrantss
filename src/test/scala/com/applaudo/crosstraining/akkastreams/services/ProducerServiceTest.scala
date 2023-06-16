package com.applaudo.crosstraining.akkastreams.services

import com.applaudo.crosstraining.akastreams.models.ProducerClasses.StringToRestaurantMapException

class ProducerServiceTest  extends BaseServiceTest {
  "producer service" should {
    "map valid input string to a restaurant" in{

      val result = producerService.strToRestaurantWithHandler(1,Left(inputRestaurantStr))
      assert(restaurantExpected == result)
    }

    "throw exception when non valid input string" in {
      val inputStr = "1234,2023-06-08T16:06:25Z"

      assertThrows[StringToRestaurantMapException](producerService
        .strToRestaurantWithHandler(1,Left(inputStr)))
    }

    "map valid input list to a restaurant" in {
      val result = producerService.strToRestaurantWithHandler(1, Right(inputRestaurantList))
      assert(restaurantExpected == result)
    }

    "throw exception when non valid input list" in {
         assertThrows[StringToRestaurantMapException](producerService
        .strToRestaurantWithHandler(1,Right(nonValidRestaurantInputList)))
    }
  }
}
