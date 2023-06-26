package com.applaudo.crosstraining.akkastreams.services

import com.applaudo.crosstraining.akastreams.models.ProducerClasses._
import com.applaudo.crosstraining.akkastreams.BaseServiceTest
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when

import java.util.concurrent.CompletableFuture

class ProducerServiceTest  extends BaseServiceTest {
  "producer service" should {
    "map valid input string to a restaurant" in{

      val result = producerService.strToRestaurant(1,StrInput(inputRestaurantStr))
      assert(restaurantExpected == result)
    }

    "throw exception when non valid input string" in {
      val inputStr = "1234,2023-06-08T16:06:25Z"

      assertThrows[StringToRestaurantMapException](producerService
        .strToRestaurant(1,StrInput(inputStr)))
    }

    "map valid input list to a restaurant" in {
      val result = producerService.strToRestaurant(1, ListInput(inputRestaurantList))
      assert(restaurantExpected == result)
    }

    "throw exception when non valid input list" in {
         assertThrows[StringToRestaurantMapException](producerService
        .strToRestaurant(1, ListInput(nonValidRestaurantInputList)))
    }

    "return metadata whe message sent successfully " in {
      when(mockProducer.send(any()))
        .thenReturn(CompletableFuture.completedFuture(metadata))

      val result = producerService.sendMessage(restaurantExpected)
      val metaResult = result.get()
      assert( metaResult.topic() == metadata.topic())
    }
  }
}
