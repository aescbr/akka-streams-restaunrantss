package com.applaudo.crosstraining.akastreams.producers

import akka.Done
import akka.actor.ActorSystem
import akka.stream.alpakka.csv.scaladsl.CsvParsing
import akka.stream.scaladsl.{FileIO, Flow, Sink}
import akka.stream.{ActorAttributes, Supervision}
import com.applaudo.crosstraining.akastreams.services.ProducerServiceImpl
import org.slf4j.LoggerFactory

import java.nio.file.Paths
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

object CSVProducerAlt {

  import com.applaudo.crosstraining.akastreams.config.KafkaBrokerConfig._
  import com.applaudo.crosstraining.akastreams.models.ProducerClasses._

  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem = ActorSystem("csv-producer")

    val dataCSVFile = Paths.get("src/main/resources/data.csv")
    val timeCounter = System.nanoTime()
    val log = LoggerFactory.getLogger(getClass)
    val producerService = ProducerServiceImpl(restaurantProducer)

    val source = FileIO.fromPath(dataCSVFile)
      .via(CsvParsing.lineScanner(maximumLineLength = 1024 * 35))
      .map(_.map(_.utf8String))


    var lineNum = 0
    val mapRestaurant = Flow[List[String]].map{ list =>
      lineNum += 1
      producerService.strToRestaurantWithHandler(lineNum, Right(list))
    }

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

    stream.onComplete{
      case Success(_) => println(s"stream completed in: ${System.nanoTime() - timeCounter} line processed $lineNum")
      case Failure(exception) => println(s"${exception.getMessage}")
    }
  }
}
