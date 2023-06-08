package com.applaudo.crosstraining.akastreams.producers

import akka.Done
import akka.actor.{ActorSystem, Props}
import akka.stream.{ActorAttributes, Supervision}
import akka.stream.scaladsl.{FileIO, Flow, Framing, Sink}
import akka.util.ByteString

import java.nio.file.Paths
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

object CSVProducer {
  import com.applaudo.crosstraining.akastreams.actors.ProducerActor
  import com.applaudo.crosstraining.akastreams.services.ProducerServiceImpl
  import com.applaudo.crosstraining.akastreams.models.ProducerClasses._
  import ProducerActor._
  import com.applaudo.crosstraining.akastreams.models.schemas.ProducerSchemas.restaurantSchema
  import com.applaudo.crosstraining.akastreams.producers.CSVProducerConfig.{restaurantProducer, restaurantTopic}
  import org.apache.kafka.clients.producer.ProducerRecord

  def main(args: Array[String]): Unit = {

    implicit val system: ActorSystem = ActorSystem("csv-producer")

    val dataCSVFile = Paths.get("src/main/resources/data.csv")
    val timeCounter = System.nanoTime()

    val producerActor = system.actorOf(Props(classOf[ProducerActor], timeCounter), "producer-actor")
    val producerService = new ProducerServiceImpl()

    val source = FileIO.fromPath(dataCSVFile)
      .via(Framing.delimiter(ByteString("\n"), 1024 * 35, allowTruncation = true))
      .map { line =>
        line.utf8String
      }

    var num = 0
    val mapRestaurant = Flow[String].map{ strLine =>
      num += 1
      producerService.strToRestaurantWithHandler(num, strLine)
    }

//    val actorSink = Sink.actorRefWithBackpressure(producerActor, InitStream, Ack, Complete, StreamFailure)
    val decider: Supervision.Decider ={
      case ex : StringToRestaurantMapException =>
        println(ex.message)
        Supervision.Resume
    }

    val simpleSink: Sink[Restaurant, Future[Done]] = Sink.foreach(sendMessage)

    val stream  = source
      .via(mapRestaurant)
      .withAttributes(ActorAttributes.supervisionStrategy(decider))
      .runWith(simpleSink)

    stream.onComplete {
      case Failure(exception) =>
        println(s"${exception.getMessage}")
      case Success(_) =>
        println(s"stream completed in:${System.nanoTime() - timeCounter}" )
    }
  }

  def sendMessage(restaurant: Restaurant): Unit = {
    val value = RestaurantMessage(schema = restaurantSchema, payload = restaurant)
    val record = new ProducerRecord(restaurantTopic, restaurant.id, value)
    restaurantProducer.send(record)
  }
}
