package com.applaudo.crosstraining.akastreams.producers

import akka.Done
import akka.actor.ActorSystem
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

  def processCSVRestaurants(producerSource: ProducerSource,
                            producerService: ProducerService): Future[Done] = {

    processCSV(producerSource, producerService)
  }

  private def processCSV(producerSource: ProducerSource,
                         producerService: ProducerService): Future[Done] = {

    val decider: Supervision.Decider = {
      case ex: StringToRestaurantMapException =>
        log.error(ex.message)
        Supervision.Resume
      case ex : MessageNotDeliveredException =>
        log.error(ex.message)
        Supervision.Stop
      case ex =>
        log.error(s"unexpected error ${ex.getMessage}")
        Supervision.Stop
    }

    val sendMessageFlow = Flow[Restaurant].map { restaurant =>
      val result = producerService.sendMessage(restaurant)
      val futureResult = Future(result.get())

      futureResult.onComplete {
        case Success(metadata) =>
          log.info(s"message sent key: ${restaurant.id} " +
            s"- topic: ${metadata.topic()} partition: ${metadata.partition()}")
        case Failure(ex) =>
          throw MessageNotDeliveredException(s"${ex.getClass.getName} | ${ex.getMessage} - key: ${restaurant.id}")
      }
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

    graph
      .via(sendMessageFlow)
      .withAttributes(ActorAttributes.supervisionStrategy(decider))
      .runWith(Sink.ignore)
  }
}
