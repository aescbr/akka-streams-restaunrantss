package com.applaudo.crosstraining.akkastreams.consumers

import akka.kafka.ConsumerMessage
import akka.kafka.scaladsl.Consumer
import akka.kafka.testkit.ConsumerResultFactory
import akka.kafka.testkit.scaladsl.ConsumerControlFactory
import akka.stream.scaladsl.{Keep, Source}
import akka.testkit.TestProbe
import com.applaudo.crosstraining.akastreams.consumers.RestaurantConsumer
import com.applaudo.crosstraining.akastreams.models.ProducerClasses.RestaurantMessage
import com.applaudo.crosstraining.akastreams.models.schemas.ProducerSchemas.restaurantSchema
import com.applaudo.crosstraining.akastreams.services.{ConsumerService, ConsumerServiceImpl}
import com.applaudo.crosstraining.akkastreams.BaseServiceTest
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{doNothing, when}

class RestaurantConsumerTest extends BaseServiceTest{

  var actorProbe: TestProbe = TestProbe()
  var mockService: ConsumerService = mock[ConsumerServiceImpl]

  "restaurant consumer" should {
    "process messages and normalize" in {
      when(mockService.restaurantToEntities(any()))
        .thenReturn(restaurantEntitiesExpected)

      doNothing().when(mockService).sendRestaurantEntities(any())

      //mock elements
      val elements = (0 to 5).map { i =>
        val nextOffset = 1 + i
        ConsumerResultFactory.committableMessage(
          new ConsumerRecord("topic", 1, nextOffset, "key",
            RestaurantMessage(restaurantSchema, restaurantExpected)),
          ConsumerResultFactory.committableOffset("groupId",
            "topic", 1, nextOffset, s"metadata $i")
        )
      }

      val mockedKafkaConsumerSource: Source[ConsumerMessage.CommittableMessage[String, RestaurantMessage], Consumer.Control] =
        Source(elements).viaMat(ConsumerControlFactory.controlFlow())(Keep.right)

      val consumer = new RestaurantConsumer()
      val result: Unit = consumer.normalizeRestaurant(mockedKafkaConsumerSource, mockService, actorProbe.ref)
      assert(result.isInstanceOf[Unit])
    }
  }
}
