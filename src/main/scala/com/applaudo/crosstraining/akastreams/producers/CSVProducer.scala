package com.applaudo.crosstraining.akastreams.producers

import akka.actor.{ActorSystem, Props}
import akka.stream.{ActorAttributes, Supervision}
import akka.stream.scaladsl.{FileIO, Flow, Framing, Sink}
import akka.util.ByteString


import java.nio.file.Paths

object CSVProducer {
  import com.applaudo.crosstraining.akastreams.actors.ProducerActor
  import com.applaudo.crosstraining.akastreams.services.CSVServiceImpl
  import com.applaudo.crosstraining.akastreams.models.ProducerClasses.StringToRestaurantMapException
  import ProducerActor._

  def main(args: Array[String]): Unit = {

    implicit val system: ActorSystem = ActorSystem("csv-producer")

    val dataCSVFile = Paths.get("src/main/resources/data.csv")
    //val timeCounter = System.nanoTime()
    val producerActor = system.actorOf(Props[ProducerActor], "producer-actor")
    val csvService = new CSVServiceImpl()

    val source = FileIO.fromPath(dataCSVFile)
      .via(Framing.delimiter(ByteString("\n"), 1024 * 35, allowTruncation = true))
      .map { line =>
        line.utf8String
      }

    var num = 0
    val mapRestaurant = Flow[String].map{ strLine =>
      num += 1
      csvService.strToRestaurantWithHandler(num, strLine)
    }

    val sink = Sink.actorRefWithBackpressure(producerActor, InitStream, Ack, Complete, StreamFailure)
    val decider: Supervision.Decider ={
      case ex : StringToRestaurantMapException =>
        println(ex.message)
        Supervision.Resume
    }

    source
      .via(mapRestaurant)
      .withAttributes(ActorAttributes.supervisionStrategy(decider))
      .runWith(sink)
  }
}
