package com.applaudo.crosstraining.akkastreams.producers

import akka.Done
import akka.stream.scaladsl.Source
import akka.testkit.TestProbe
import com.applaudo.crosstraining.akastreams.models.ProducerClasses.{ListStrSource, StrSource, StringToRestaurantMapException}
import com.applaudo.crosstraining.akastreams.producers.CSVProducer
import com.applaudo.crosstraining.akastreams.services.{ProducerService, ProducerServiceImpl}
import com.applaudo.crosstraining.akkastreams.BaseServiceTest
import org.apache.kafka.clients.producer.RecordMetadata
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{doReturn, when}

import java.util.concurrent.{CompletableFuture, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Success


class CSVProducerTest extends BaseServiceTest {

  var producer: CSVProducer = new CSVProducer
  var actorProbe: TestProbe = TestProbe()
  var mockService: ProducerService = mock[ProducerServiceImpl]

  var futureMetadata: Future[RecordMetadata] = null

  override def beforeAll(): Unit = {
    futureMetadata = CompletableFuture.completedFuture(metadata)
  }

  "csv producer" should {
    "process string input" in {
      when(mockService.strToRestaurant(any()))
        .thenReturn(Success(restaurantExpected))

      doReturn(futureMetadata).when(mockService).sendMessage(any())

      val result = producer.processCSVRestaurants(
        Source.single(List(inputRestaurantStr)),
        mockService
      )

      result.map{ r => assert(r.isInstanceOf[Done])}
    }

    "complete stream when exception received" in {
      when(mockService.strToRestaurant(any()))
        .thenThrow(StringToRestaurantMapException("Invalid input"))

    val result = producer.processCSVRestaurants(
        Source.single("Hello"),
        mockService
      )
      result.map{ r => assert(r.isInstanceOf[Done])}
    }
  }
}

