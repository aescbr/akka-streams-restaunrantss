package com.applaudo.crosstraining.akastreams

import akka.actor.{ActorSystem, Props}
import akka.stream.scaladsl.{FileIO, Flow, Framing, Sink}
import akka.util.ByteString

import java.nio.file.Paths
import java.util.concurrent.atomic.AtomicInteger

object CSVProducerConfig{


  //id,dateAdded,dateUpdated,address,categories,city,country,keys,latitude,longitude,name,postalCode,province,sourceURLs,websites
  case class Restaurant(id: String, dateAdded: String, dateUpdated: String, address: String, categories: String,
                        city: String, country: String, keys: String, latitude: Double, longitude: Double, name: String,
                        postalCode: String, province: String, sourceURLs: String, websites: String)

  case class Schema(`type`: String, fields: List[SchemaNode], optional: Boolean)

  case class SchemaNode(`type`: String, optional: Boolean, field: String)

  case class RestaurantMessage(schema: Schema, payload: Restaurant)

  val restaurantSchema = Schema(
    `type` = "struct",
    fields = List(
      SchemaNode(`type` = "string", optional = false, field = "id"),
      SchemaNode(`type` = "string", optional = false, field = "dateAdded"),
      SchemaNode(`type` = "string", optional = false, field = "dateUpdated"),
      SchemaNode(`type` = "string", optional = false, field = "address"),
      SchemaNode(`type` = "string", optional = false, field = "categories"),
      SchemaNode(`type` = "string", optional = false, field = "city"),
      SchemaNode(`type` = "string", optional = false, field = "country"),
      SchemaNode(`type` = "string", optional = false, field = "keys"),
      SchemaNode(`type` = "float", optional = false, field = "latitude"),
      SchemaNode(`type` = "float", optional = false, field = "longitude"),
      SchemaNode(`type` = "string", optional = false, field = "name"),
      SchemaNode(`type` = "string", optional = false, field = "postalCode"),
      SchemaNode(`type` = "string", optional = false, field = "province"),
      SchemaNode(`type` = "string", optional = false, field = "sourceURLs"),
      SchemaNode(`type` = "string", optional = false, field = "websites")
    ),
    optional = false
    //name = "restaurant"
  )
}

object CSVProducer {
  import CSVProducerConfig._
  import ProducerActor._

  implicit val system = ActorSystem("csv-producer")

  def main(args: Array[String]) ={


    val dataCSVFile = Paths.get("src/main/resources/data.csv")

    val counter0 = new AtomicInteger()
    val producerActor = system.actorOf(Props[ProducerActor], "producer-actor")

    val source = FileIO.fromPath(dataCSVFile)
      .via(Framing.delimiter(ByteString("\n"), 1024*35, allowTruncation = true))
      .map { line =>
        counter0.incrementAndGet()
        line.utf8String
      }

    val mapRestaurant = Flow[String].map {strLine =>
      val data = strLine.split(",(?=([^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)").toList
      Restaurant(data(0), data(1), data(2), data(3), data(4), data(5), data(6), data(7), data(8).toDouble,
        data(9).toDouble, data(10), data(11), data(12), data(13), data(14))
    }

    val sink = Sink.actorRefWithBackpressure(producerActor, InitStream, Ack, Complete, StreamFailure)

    val stream =
      source
        .via(mapRestaurant)
        .runWith(sink)

//    stream.onComplete{
//      case Success(value) => value.foreach{ restaurant =>
//        println(restaurant.sourceURLs)
//      }
//   }
  }
}
