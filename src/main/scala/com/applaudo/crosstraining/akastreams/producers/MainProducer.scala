package com.applaudo.crosstraining.akastreams.producers

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.stream.IOResult
import akka.stream.alpakka.csv.scaladsl.CsvParsing
import akka.stream.scaladsl.{FileIO, Framing, Source}
import akka.util.ByteString
import com.applaudo.crosstraining.akastreams.models.ProducerClasses.{ListStrSource, StrSource}
import com.applaudo.crosstraining.akastreams.models.schemas.ConsumerSchemas.{restaurantEntitySchema, schemaURL, schemaWebsite}
import com.applaudo.crosstraining.akastreams.models.schemas.ProducerSchemas.restaurantSchema
import com.applaudo.crosstraining.akastreams.services.{ConsumerService, ProducerService}
import org.slf4j.{Logger, LoggerFactory}

import java.nio.file.{Path, Paths}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

object MainProducer {
  implicit val system: ActorSystem = ActorSystem("csv-producer")

  import com.applaudo.crosstraining.akastreams.actors.ProducerActor
  import com.applaudo.crosstraining.akastreams.config.KafkaBrokerConfig._
  import com.applaudo.crosstraining.akastreams.services.{ConsumerServiceImpl, ProducerServiceImpl}

  val dataCSVFile: Path = Paths.get("src/main/resources/data.csv")
  val producerService: ProducerService = ProducerServiceImpl(restaurantProducer, restaurantSchema, restaurantTopic)
  val consumerService: ConsumerService = ConsumerServiceImpl(restaurantEntityProducer,
    sourceURLProducer, websiteProducer, schemaURL, schemaWebsite, restaurantEntitySchema, restaurantEntityTopic,
    sourceURLTopic, websiteTopic)
  val timeCounter: Long = System.nanoTime()
  val log: Logger = LoggerFactory.getLogger(getClass)

  val producerActor: ActorRef = system.actorOf(
    Props(classOf[ProducerActor], timeCounter, producerService, consumerService), "producer-actor")

  val csvRegexSource: Source[String, Future[IOResult]] = FileIO.fromPath(dataCSVFile)
    .via(Framing.delimiter(ByteString("\n"), 1024 * 35, allowTruncation = true))
    .map { line =>
      line.utf8String
    }

  val csvSource: Source[List[String], Future[IOResult]] = FileIO.fromPath(dataCSVFile)
    .via(CsvParsing.lineScanner(maximumLineLength = 1024 * 35))
    .map(_.map(_.utf8String))

  def main(args: Array[String]): Unit = {
    val csvProducer = new CSVProducer
    val result = csvProducer.processCSVRestaurants(csvSource, producerService)

    result.onComplete {
      case Failure(ex) =>
        log.error(ex.getMessage)
      case Success(_) =>
        log.info(s"Stream completed in: ${System.nanoTime() - timeCounter}")

    }
  }
}
