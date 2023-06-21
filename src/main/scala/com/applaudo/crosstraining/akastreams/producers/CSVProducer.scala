package com.applaudo.crosstraining.akastreams.producers

import akka.actor.{ActorRef, ActorSystem}
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.stream.{ActorAttributes, Supervision}
import akka.{Done, NotUsed}
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}


class CSVProducer()(implicit system: ActorSystem) {

  import com.applaudo.crosstraining.akastreams.actors.ProducerActor._
  import com.applaudo.crosstraining.akastreams.models.ProducerClasses._
  import com.applaudo.crosstraining.akastreams.services.ProducerService

  val log: Logger = LoggerFactory.getLogger(getClass)
  var lineNum = 0

  def processCSVWithActor(source: Source[String, Any], producerActor: ActorRef,
                          producerService: ProducerService): Any = {
    val actorSink: Sink[Restaurant, NotUsed] = Sink.actorRefWithBackpressure(producerActor, InitStream,
      Ack, Complete, StreamFailure)
    processCSV(source, System.nanoTime(), producerService, actorSink)
  }

  def processCSVWithForEachSink(source: Source[String, Any],
                                producerService: ProducerService): Unit = {
    val simpleSink: Sink[Restaurant, Future[Done]] = Sink.foreach(producerService.sendMessage)
    processCSV(source, System.nanoTime(), producerService, simpleSink)
  }


  private def processCSV(source: Source[String, Any], timeCounter: Long,
                               producerService: ProducerService, sink: Sink[Restaurant, Any]): Unit = {

    val mapRestaurant = Flow[String].map { strLine =>
      lineNum += 1
      producerService.strToRestaurantWithHandler(lineNum, Left(strLine))
    }

    val decider: Supervision.Decider = {
      case ex: StringToRestaurantMapException =>
        log.error(ex.message)
        Supervision.Resume
    }

    val result = source
      .via(mapRestaurant)
      .withAttributes(ActorAttributes.supervisionStrategy(decider))
      .runWith(sink)

    result match {
      case stream @ (_: Future[Done]) =>
        stream.onComplete{
          case Success(_) => log.info(s"stream completed in: ${System.nanoTime() - timeCounter} line processed $lineNum")
          case Failure(exception) => log.error(s"${exception.getMessage}")
        }
      case _ =>
    }

  }
}
