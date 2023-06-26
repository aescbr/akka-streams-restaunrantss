package com.applaudo.crosstraining.akkastreams.producers

import akka.Done
import akka.stream.scaladsl.Source
import akka.testkit.TestProbe
import com.applaudo.crosstraining.akastreams.models.ProducerClasses.{ListStrSource, StrSource, StringToRestaurantMapException}
import com.applaudo.crosstraining.akastreams.producers.CSVProducer
import com.applaudo.crosstraining.akastreams.services.{ProducerService, ProducerServiceImpl}
import com.applaudo.crosstraining.akkastreams.BaseServiceTest
import org.apache.kafka.clients.producer.RecordMetadata
import org.apache.kafka.common.TopicPartition
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{doReturn, when}

import java.util.concurrent.{CompletableFuture, Future}
import scala.concurrent.ExecutionContext.Implicits.global


class CSVProducerTest extends BaseServiceTest {

  var producer: CSVProducer = new CSVProducer
  var actorProbe: TestProbe = TestProbe()
  var mockService: ProducerService = mock[ProducerServiceImpl]

  var futureMetadata: Future[RecordMetadata] = null

  override def beforeAll(): Unit = {
    futureMetadata = CompletableFuture.completedFuture(metadata)
  }

  "csv producer" should {
    "process string input with actor" in {
      when(mockService.strToRestaurant(any(), any()))
        .thenReturn(restaurantExpected)

      when(mockService.sendMessage(any()))
        .thenReturn(futureMetadata)

      val result = producer.processCSVRestaurants(
        StrSource(Source.single(inputRestaurantStr)),
        actorProbe.ref,
        mockService
      )

      result.map{ r => assert(r.isInstanceOf[Done])}
    }

    "process string input with for each" in {
      when(mockService.strToRestaurant(any(), any()))
        .thenReturn(restaurantExpected)

      doReturn(futureMetadata).when(mockService).sendMessage(any())

      val result = producer.processCSVRestaurants(
        ListStrSource(Source.single(List(inputRestaurantStr))),
        actorProbe.ref,
        mockService
      )

      result.map{ r => assert(r.isInstanceOf[Done])}
    }

    "complete stream when exception received" in {
      when(mockService.strToRestaurant(any(), any()))
        .thenThrow(StringToRestaurantMapException("Invalid input"))

    val result = producer.processCSVRestaurants(
        StrSource(Source.single("Hello")),
        actorProbe.ref,
        mockService
      )
      result.map{ r => assert(r.isInstanceOf[Done])}
    }
  }
}

