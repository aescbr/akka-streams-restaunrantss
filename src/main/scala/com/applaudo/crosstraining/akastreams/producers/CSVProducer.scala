package com.applaudo.crosstraining.akastreams.producers

import akka.Done
import akka.actor.{ActorSystem, Props}
import akka.stream.{ActorAttributes, Supervision}
import akka.stream.scaladsl.{FileIO, Flow, Framing, Sink}
import akka.util.ByteString
import org.slf4j.LoggerFactory

import java.nio.file.Paths
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}



object CSVProducer {
  import com.applaudo.crosstraining.akastreams.actors.ProducerActor
  import com.applaudo.crosstraining.akastreams.models.ProducerClasses._
  import com.applaudo.crosstraining.akastreams.config.KafkaBrokerConfig._
  import com.applaudo.crosstraining.akastreams.services.ConsumerServiceImpl
  import com.applaudo.crosstraining.akastreams.services.ProducerServiceImpl

  def main(args: Array[String]): Unit = {

    implicit val system: ActorSystem = ActorSystem("csv-producer")

    val dataCSVFile = Paths.get("src/main/resources/data.csv")
    val timeCounter = System.nanoTime()
    val log = LoggerFactory.getLogger(getClass)
    val producerService = ProducerServiceImpl(restaurantProducer)
//    val consumerService = ConsumerServiceImpl(restaurantEntityProducer, sourceURLProducer, websiteProducer)
//
//    val producerActor = system.actorOf(
//     Props(classOf[ProducerActor], timeCounter, producerService, consumerService), "producer-actor")

    val source = FileIO.fromPath(dataCSVFile)
      .via(Framing.delimiter(ByteString("\n"), 1024 * 35, allowTruncation = true))
      .map { line =>
        line.utf8String
      }

    var lineNum = 0
    val mapRestaurant = Flow[String].map{ strLine =>
      lineNum += 1
      producerService.strToRestaurantWithHandler(lineNum, Left(strLine))
    }

    //val actorSink = Sink.actorRefWithBackpressure(producerActor, InitStream, Ack, Complete, StreamFailure)
    val decider: Supervision.Decider ={
      case ex : StringToRestaurantMapException =>
        log.error(ex.message)
        Supervision.Resume
    }

    val simpleSink: Sink[Restaurant, Future[Done]] = Sink.foreach(producerService.sendMessage)

    val stream  =
      source
      .via(mapRestaurant)
      .withAttributes(ActorAttributes.supervisionStrategy(decider))
      .runWith(simpleSink)

    stream.onComplete {
      case Failure(exception) =>
        log.info(s"${exception.getMessage}")
      case Success(_) =>
        log.info(s"stream completed in:${System.nanoTime() - timeCounter}" )
    }
  }
}
