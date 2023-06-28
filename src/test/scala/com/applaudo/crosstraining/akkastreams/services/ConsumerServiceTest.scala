package com.applaudo.crosstraining.akkastreams.services

import com.applaudo.crosstraining.akastreams.models.ConsumerClasses.RestaurantToEntitiesException
import com.applaudo.crosstraining.akastreams.models.ProducerClasses.Restaurant
import com.applaudo.crosstraining.akkastreams.BaseServiceTest
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar

import java.util.concurrent.CompletableFuture

class ConsumerServiceTest extends BaseServiceTest
with MockitoSugar{

  "consumer service" should {
    "normalize Restaurant to entities" in {

      val result = consumerService.restaurantToEntities(restaurantExpected)
      assert(restaurantEntitiesExpected == result)
    }

    "throw exception when non valid input restaurant" in {
      assertThrows[NullPointerException](consumerService.restaurantToEntities(null))
    }

    "return list of metadata when send valid message" in {
      when(mockRestaurantEntityProducer.send(any()))
        .thenReturn(CompletableFuture.completedFuture(metadata))
      when(mockSourceURLProducer.send(any()))
        .thenReturn(CompletableFuture.completedFuture(metadata))
      when(mockWebsiteProducer.send(any()))
        .thenReturn(CompletableFuture.completedFuture(metadata))

      val result = consumerService.sendRestaurantEntities(restaurantEntitiesExpected)
      assert(result.head.get().topic() == metadata.topic())
    }
  }
}
