package com.applaudo.crosstraining.akastreams.producers

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.stream.IOResult
import akka.stream.alpakka.csv.scaladsl.CsvParsing
import akka.stream.scaladsl.{FileIO, Framing, Source}
import akka.util.ByteString
import com.applaudo.crosstraining.akastreams.models.ProducerClasses.{ListStrSource, StrSource}
import com.applaudo.crosstraining.akastreams.services.{ConsumerService, ProducerService}

import java.nio.file.{Path, Paths}
import scala.concurrent.Future

object MainProducer {
  implicit val system: ActorSystem = ActorSystem("csv-producer")

  import com.applaudo.crosstraining.akastreams.actors.ProducerActor
  import com.applaudo.crosstraining.akastreams.config.KafkaBrokerConfig._
  import com.applaudo.crosstraining.akastreams.services.{ConsumerServiceImpl, ProducerServiceImpl}

  val dataCSVFile: Path = Paths.get("src/main/resources/data.csv")
  val producerService: ProducerService = ProducerServiceImpl(restaurantProducer)
  val consumerService: ConsumerService = ConsumerServiceImpl(restaurantEntityProducer,
    sourceURLProducer, websiteProducer)
  val timeCounter: Long = System.nanoTime()

  val producerActor: ActorRef = system.actorOf(
    Props(classOf[ProducerActor], timeCounter, producerService, consumerService), "producer-actor")

  val csvSource: Source[String, Future[IOResult]] = FileIO.fromPath(dataCSVFile)
    .via(Framing.delimiter(ByteString("\n"), 1024 * 35, allowTruncation = true))
    .map { line =>
      line.utf8String
    }

  val source: Source[List[String], Future[IOResult]] = FileIO.fromPath(dataCSVFile)
    .via(CsvParsing.lineScanner(maximumLineLength = 1024 * 35))
    .map(_.map(_.utf8String))

  def main(args: Array[String]): Unit = {
    val csvProducer = new CSVProducer
    csvProducer.processCSVRestaurants(ListStrSource(source), producerActor, producerService)
  }
}
