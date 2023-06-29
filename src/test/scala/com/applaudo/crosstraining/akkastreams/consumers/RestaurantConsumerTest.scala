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
import org.mockito.Mockito.when

import java.util.concurrent.CompletableFuture

class RestaurantConsumerTest extends BaseServiceTest{

  var actorProbe: TestProbe = TestProbe()
  var mockConsumerService: ConsumerService = mock[ConsumerServiceImpl]
  var optConsumer: Option[RestaurantConsumer] = None
  var optSourceConsumer:
    Option[Source[ConsumerMessage.CommittableMessage[String, RestaurantMessage], Consumer.Control]]= None

  override def beforeEach(): Unit = {
    val elements = (0 to 5).map { i =>
      val nextOffset = 1 + i
      ConsumerResultFactory.committableMessage(
        new ConsumerRecord("topic", 1, nextOffset, "key",
          RestaurantMessage(restaurantSchema, restaurantExpected)),
        ConsumerResultFactory.committableOffset("groupId",
          "topic", 1, nextOffset, s"metadata $i")
      )
    }

    optSourceConsumer =
     Some( Source(elements).viaMat(ConsumerControlFactory.controlFlow())(Keep.right))

    optConsumer = Option(new RestaurantConsumer())
  }

  "restaurant consumer" should {
    "process messages and normalize" in {
      val consumer = optConsumer.get
      val mockedKafkaConsumerSource = optSourceConsumer.get

      when(mockConsumerService.restaurantToEntities(any()))
        .thenReturn(restaurantEntitiesExpected)

      when(mockConsumerService.sendRestaurantEntities(any()))
        .thenReturn(Set(CompletableFuture.completedFuture(metadata)))

      //mock elements
      val result: Unit = consumer.normalizeRestaurant(mockedKafkaConsumerSource, mockConsumerService, actorProbe.ref)

      assert(result.isInstanceOf[Unit])
    }

    "throws custom exception when service fails" in{
      val consumer = optConsumer.get
      val mockedKafkaConsumerSource = optSourceConsumer.get
      when(mockConsumerService.restaurantToEntities(any()))
        .thenThrow(RestaurantToEntitiesException("normalizing error"))
      val result: Unit = consumer.normalizeRestaurant(mockedKafkaConsumerSource, mockConsumerService, actorProbe.ref)

      assert(result.isInstanceOf[Unit])
    }
  }
}
