package com.applaudo.crosstraining.akkastreams.consumers

import akka.kafka.ConsumerMessage
import akka.kafka.scaladsl.Consumer
import akka.kafka.testkit.ConsumerResultFactory
import akka.kafka.testkit.scaladsl.ConsumerControlFactory
import akka.stream.scaladsl.{Keep, Source}
import akka.testkit.TestProbe
import com.applaudo.crosstraining.akastreams.consumers.RestaurantConsumer
import com.applaudo.crosstraining.akastreams.models.ConsumerClasses.RestaurantToEntitiesException
import com.applaudo.crosstraining.akastreams.models.ProducerClasses.RestaurantMessage
import com.applaudo.crosstraining.akastreams.models.schemas.ProducerSchemas.restaurantSchema
import com.applaudo.crosstraining.akastreams.services.{ConsumerService, ConsumerServiceImpl}
import com.applaudo.crosstraining.akkastreams.BaseServiceTest
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{doNothing, when}

import java.util.concurrent.CompletableFuture

class RestaurantConsumerTest extends BaseServiceTest{

  var actorProbe: TestProbe = TestProbe()
  var mockConsumerService: ConsumerService = mock[ConsumerServiceImpl]
  var consumer: RestaurantConsumer = null
  var mockedKafkaConsumerSource: Source[ConsumerMessage.CommittableMessage[String, RestaurantMessage], Consumer.Control] = null

  override def beforeAll(): Unit = {
    val elements = (0 to 5).map { i =>
      val nextOffset = 1 + i
      ConsumerResultFactory.committableMessage(
        new ConsumerRecord("topic", 1, nextOffset, "key",
          RestaurantMessage(restaurantSchema, restaurantExpected)),
        ConsumerResultFactory.committableOffset("groupId",
          "topic", 1, nextOffset, s"metadata $i")
      )
    }

    mockedKafkaConsumerSource =
      Source(elements).viaMat(ConsumerControlFactory.controlFlow())(Keep.right)

    consumer = new RestaurantConsumer()
  }

  "restaurant consumer" should {
    "process messages and normalize" in {
      when(mockConsumerService.restaurantToEntities(any()))
        .thenReturn(restaurantEntitiesExpected)

      when(mockConsumerService.sendRestaurantEntities(any()))
        .thenReturn(Set(CompletableFuture.completedFuture(metadata)))

      //mock elements
      val result: Unit = consumer.normalizeRestaurant(mockedKafkaConsumerSource, mockConsumerService, actorProbe.ref)

      assert(result.isInstanceOf[Unit])
    }

    "throws custom exception when service fails" in{
      when(mockConsumerService.restaurantToEntities(any()))
        .thenThrow(RestaurantToEntitiesException("normalizing error"))
      val result: Unit = consumer.normalizeRestaurant(mockedKafkaConsumerSource, mockConsumerService, actorProbe.ref)

      assert(result.isInstanceOf[Unit])
    }
  }
}
