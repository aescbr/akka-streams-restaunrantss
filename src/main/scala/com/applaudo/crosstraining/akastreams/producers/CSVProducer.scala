package com.applaudo.crosstraining.akastreams.producers

import akka.actor.{ActorSystem, Props}
import akka.stream.scaladsl.{FileIO, Flow, Framing, Sink}
import akka.util.ByteString
import com.applaudo.crosstraining.akastreams.actors.ProducerActor

import java.nio.file.Paths

object CSVProducer {

  import ProducerActor._
  import com.applaudo.crosstraining.akastreams.domain.ProducerClasses._

  implicit val system: ActorSystem = ActorSystem("csv-producer")

  def main(args: Array[String]): Unit = {


    val dataCSVFile = Paths.get("src/main/resources/data.csv")
    //val timeCounter = System.nanoTime()

    val producerActor = system.actorOf(Props[ProducerActor], "producer-actor")

    val source = FileIO.fromPath(dataCSVFile)
      .via(Framing.delimiter(ByteString("\n"), 1024 * 35, allowTruncation = true))
      .map { line =>
        line.utf8String
      }

    val mapRestaurant = Flow[String].map { strLine =>
      val data = strLine.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)")

      val id = data(0)
      val dateAdded = data(1)
      val dateUpdated = data(2)
      val address = data(3)
      val categories = data(4)
      val city = data(5)
      val country = data(6)
      val keys = data(7)
      val latitude = data(8).toDouble
      val longitude = data(9).toDouble
      val name = data(10)
      val postalCode = data(11)
      val province = data(12)
      val sourceURLs = data(13)
      val websites = data(14)


      Restaurant(id, dateAdded, dateUpdated, address, categories, city, country, keys, latitude, longitude,
        name, postalCode, province, sourceURLs, websites)
    }

    val sink = Sink.actorRefWithBackpressure(producerActor, InitStream, Ack, Complete, StreamFailure)
    source
      .via(mapRestaurant)
      .runWith(sink)

  }
}
