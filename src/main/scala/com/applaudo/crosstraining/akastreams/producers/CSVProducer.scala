package com.applaudo.crosstraining.akastreams.producers

import akka.Done
import akka.actor.{ActorRef, ActorSystem}
import akka.stream.scaladsl.{Flow, Sink}
import akka.stream.{ActorAttributes, Supervision}
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}


class CSVProducer()(implicit system: ActorSystem) {

  import com.applaudo.crosstraining.akastreams.models.ProducerClasses._
  import com.applaudo.crosstraining.akastreams.services.ProducerService

  val log: Logger = LoggerFactory.getLogger(getClass)

  def processCSVRestaurants(producerSource: ProducerSource, producerActor: ActorRef,
                            producerService: ProducerService): Unit = {

    val simpleSink: Sink[Restaurant, Future[Done]] = Sink.foreach(producerService.sendMessage)
    processCSV(producerSource, System.nanoTime(), producerService, simpleSink)
  }

  private def processCSV(producerSource: ProducerSource, timeCounter: Long,
                         producerService: ProducerService, sink: Sink[Restaurant, Future[Done]]): Unit = {

    val decider: Supervision.Decider = {
      case ex: StringToRestaurantMapException =>
        log.error(ex.message)
        Supervision.Resume
    }

    val graph = producerSource match {
      case StrSource(strSource) =>
        strSource
          .zipWithIndex
          .via(Flow[(String, Long)].map { tuple =>
            producerService.strToRestaurant(tuple._2 + 1, StrInput(tuple._1))
          })

      case ListStrSource(listSource) =>
        listSource
          .zipWithIndex
          .via(Flow[(List[String], Long)].map { tuple =>
            producerService.strToRestaurant(tuple._2 + 1, ListInput(tuple._1))
          })
    }

    val result = graph
      .withAttributes(ActorAttributes.supervisionStrategy(decider))
      .runWith(sink)

    result.onComplete {
      case Failure(exception) =>
        log.error(exception.getMessage)
      case Success(_) =>
        log.info(s"Stream completed in ${System.nanoTime() - timeCounter}")
    }
  }
}
