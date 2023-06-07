package com.applaudo.crosstraining.akastreams.actors

import akka.actor.{Actor, ActorLogging, ActorSystem}
import com.applaudo.crosstraining.akastreams.models.ConsumerClasses._
import com.applaudo.crosstraining.akastreams.models.ProducerClasses._
import com.applaudo.crosstraining.akastreams.models.schemas.ProducerSchemas._
import com.goyeau.kafka.streams.circe.CirceSerdes
import io.circe.generic.auto._
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}

import java.util.Properties

object ProducerActor {
  case object InitStream
  case object Ack
  case object Complete
  final case class StreamFailure(ex: Throwable)

  val props: Properties = new Properties()
  props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")

  val producer = new KafkaProducer(props,
    CirceSerdes.serializer[String],
    CirceSerdes.serializer[RestaurantMessage]
  )

  val producer1 = new KafkaProducer(props,
    CirceSerdes.serializer[String],
    CirceSerdes.serializer[RestaurantEntityMessage]
  )

  val producer2 = new KafkaProducer(props,
    CirceSerdes.serializer[String],
    CirceSerdes.serializer[SourceURLMessage],
  )

  val producer3 = new KafkaProducer(props,
    CirceSerdes.serializer[String],
    CirceSerdes.serializer[WebsiteMessage],
  )

  val topic = "first-topic"
  val outputStreamTopic = "stream-output-topic"
  val outputStreamTopic2 = "stream-output-topic2"
  val outputStreamTopic3 = "stream-output-topic3"
}


class ProducerActor extends Actor with ActorLogging{
  import ProducerActor._

  implicit val system: ActorSystem = context.system


  override def receive: Receive = {
    case _: InitStream.type =>
      println("Init stream...")
      sender ! Ack
    case restaurant: Restaurant =>
      sendMessage(restaurant)
      println(s"sending ${restaurant.id}")
      sender ! Ack
    case restaurantEntitiesMessage: RestaurantEntitiesMessage =>
      sendRestaurantEntities(restaurantEntitiesMessage)
      println(s"processing ${restaurantEntitiesMessage.restaurantMessage.payload.id}")
      sender ! Ack

    case _ : Complete.type =>
      println("stream completed!")
     // system.terminate()

  }

  def sendMessage(restaurant: Restaurant): Unit = {
    val value = RestaurantMessage(schema = restaurantSchema, payload = restaurant)

    val record = new ProducerRecord(topic, restaurant.id, value)
    //println(s"sending: $record")
    producer.send(record)

  }

  def sendRestaurantEntities(restaurantEntitiesMessage: RestaurantEntitiesMessage):Unit = {
    val id = restaurantEntitiesMessage.restaurantMessage.payload.id
    val record1 = new ProducerRecord[String, RestaurantEntityMessage](outputStreamTopic, id,
      restaurantEntitiesMessage.restaurantMessage)

    producer1.send(record1)

    restaurantEntitiesMessage.urls.foreach{url =>
      val recordURL = new ProducerRecord[String, SourceURLMessage](outputStreamTopic2, id,url)
      producer2.send(recordURL)
    }

    restaurantEntitiesMessage.websites.foreach{website =>
      val recordWebsite = new ProducerRecord[String, WebsiteMessage](outputStreamTopic3, id,website)
      producer3.send(recordWebsite)
    }

  }
}
