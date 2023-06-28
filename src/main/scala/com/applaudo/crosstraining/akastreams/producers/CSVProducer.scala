package com.applaudo.crosstraining.akastreams.producers

import akka.Done
import akka.actor.ActorSystem
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.stream.{ActorAttributes, Supervision}
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}


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
        addIndex(strSource)
          .via(Flow[(String, Long)].map { tuple =>
            processMapResult(tuple._2,
              producerService.strToRestaurant(StrInput(tuple._1)))
          })

      case ListStrSource(listSource) =>
        addIndex(listSource)
          .via(Flow[(List[String], Long)].map { tuple =>
            processMapResult(tuple._2,
              producerService.strToRestaurant(ListInput(tuple._1)))
          })
    }

    graph
      .via(sendMessageFlow)
      .withAttributes(ActorAttributes.supervisionStrategy(decider))
      .runWith(Sink.ignore)
  }

  private def processMapResult(lineNum: Long, result: Try[Restaurant]): Restaurant = {
    result match {
      case Failure(ex) =>
        throw StringToRestaurantMapException(s"${ex.getClass.getName} | ${ex.getMessage} - in line: $lineNum")
      case Success(restaurant) => restaurant
    }
  }

  private def addIndex[E](source : Source[E, Any]) = {
    var i = 0L
    source.map {element =>
      i += 1
      (element, i)
    }
  }
}
