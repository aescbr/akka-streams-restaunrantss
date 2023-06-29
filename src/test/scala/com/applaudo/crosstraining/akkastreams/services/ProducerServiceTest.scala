package com.applaudo.crosstraining.akkastreams.services

import com.applaudo.crosstraining.akastreams.models.ProducerClasses._
import com.applaudo.crosstraining.akastreams.models.schemas.ProducerSchemas.restaurantSchema
import com.applaudo.crosstraining.akastreams.services.{ProducerService, ProducerServiceImpl}
import com.applaudo.crosstraining.akkastreams.BaseServiceTest
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when

import java.util.concurrent.CompletableFuture
import scala.util.{Failure, Success}

class ProducerServiceTest  extends BaseServiceTest {

  var optService :Option[ProducerService] = None

  override protected def beforeEach(): Unit = {
    optService = Some(ProducerServiceImpl(mockProducer, restaurantSchema, "test-topic"))
  }

  "producer service" should {
    "map valid input string to a restaurant" in{
      val producerService = optService.get

      val result = producerService.strToRestaurant(StrInput(inputRestaurantStr))
      assert(Success(restaurantExpected) == result)
    }

    "return Failure when non valid input string" in {
      val producerService = optService.get
      val inputStr = "1234,2023-06-08T16:06:25Z"

      val result = producerService.strToRestaurant(StrInput(inputStr))
      assert(result.isInstanceOf[Failure[Restaurant]])
    }

    "map valid input list to a restaurant" in {
      val producerService = optService.get
      val result = producerService.strToRestaurant(ListInput(inputRestaurantList))
      assert(Success(restaurantExpected) == result)
    }

    "return Failure when non valid input list" in {
      val producerService = optService.get
      val result = producerService.strToRestaurant(ListInput(nonValidRestaurantInputList))
      assert(result.isInstanceOf[Failure[Restaurant]])
    }

    "map valid input string to a restaurant alternative" in{
      val producerService = optService.get

      val result = producerService.strToRestaurantAlt(inputRestaurantStr)
      assert(Success(restaurantExpected) == result)
    }

    "return Failure when non valid input string alternative" in {
      val producerService = optService.get
      val inputStr = "1234,2023-06-08T16:06:25Z"

      val result = producerService.strToRestaurantAlt(inputStr)
      assert(result.isInstanceOf[Failure[Restaurant]])
    }

    "map valid input list to a restaurant alternative" in {
      val producerService = optService.get
      val result = producerService.strToRestaurantAlt(inputRestaurantList)
      assert(Success(restaurantExpected) == result)
    }

    "return Failure when non valid input list alternative" in {
      val producerService = optService.get
      val result = producerService.strToRestaurantAlt(nonValidRestaurantInputList)
      assert(result.isInstanceOf[Failure[Restaurant]])
    }

    "return metadata whe message sent successfully " in {
      val producerService = optService.get
      when(mockProducer.send(any()))
        .thenReturn(CompletableFuture.completedFuture(metadata))

      val result = producerService.sendMessage(restaurantExpected)
      val metaResult = result.get()
      assert( metaResult.topic() == metadata.topic())
    }
  }
}
