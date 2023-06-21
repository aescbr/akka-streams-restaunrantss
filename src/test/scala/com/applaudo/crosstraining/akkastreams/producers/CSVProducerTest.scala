package com.applaudo.crosstraining.akkastreams.producers

import akka.stream.scaladsl.Source
import akka.testkit.TestProbe
import com.applaudo.crosstraining.akastreams.producers.CSVProducer
import com.applaudo.crosstraining.akastreams.services.ProducerServiceImpl
import com.applaudo.crosstraining.akkastreams.BaseServiceTest
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.{doNothing, when}

class CSVProducerTest extends BaseServiceTest{

  var producer: CSVProducer = new CSVProducer
  var actorProbe: TestProbe = TestProbe()
  var mockService: ProducerServiceImpl = mock[ProducerServiceImpl]

  "csv producer" should {
    "process string input with actor" in {
      when(mockService.strToRestaurantWithHandler(any(), any()))
        .thenReturn(restaurantExpected)

      doNothing().when(mockService).sendMessage(any())

      val result = producer.processCSVWithActor(
        Source.single(inputRestaurantStr),
        actorProbe.ref,
        mockService
      )

      assert(result.isInstanceOf[Unit])
    }

    "process string input with for each" in {
      when(mockService.strToRestaurantWithHandler(any(), any()))
        .thenReturn(restaurantExpected)

      doNothing().when(mockService).sendMessage(any())

      val result: Unit = producer.processCSVWithForEachSink(
        Source.single(inputRestaurantStr),
        mockService
      )

      assert(result.isInstanceOf[Unit])
    }

  }
}

