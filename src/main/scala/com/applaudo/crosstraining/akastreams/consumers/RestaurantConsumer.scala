package com.applaudo.crosstraining.akastreams.consumers

import akka.NotUsed
import akka.actor.{ActorSystem, Props}
import akka.kafka.ConsumerMessage.CommittableMessage
import akka.kafka.ConsumerSettings
import akka.kafka.scaladsl.Consumer
import akka.stream.scaladsl.{Flow, Sink}
import akka.stream.{ActorAttributes, Supervision}
import com.applaudo.crosstraining.akastreams.actors.ProducerActor
import com.applaudo.crosstraining.akastreams.models.ConsumerClasses.RestaurantToEntitiesException
import com.applaudo.crosstraining.akastreams.services.ConsumerServiceImpl
import org.apache.kafka.common.serialization.StringDeserializer
import org.slf4j.LoggerFactory

object RestaurantConsumer {

  import ProducerActor._
  import akka.kafka.Subscriptions
  import com.applaudo.crosstraining.akastreams.models.ProducerClasses._
  import com.applaudo.crosstraining.akastreams.config.KafkaBrokerConfig._

  def main(args: Array[String]): Unit = {

    implicit val system: ActorSystem = ActorSystem.create("restaurant-consumer")
    val timeCounter = System.nanoTime()

    val producerActor = system.actorOf(Props(classOf[ProducerActor], timeCounter), "producer-actor")
    val consumerService = new ConsumerServiceImpl()
    val log = LoggerFactory.getLogger(getClass)

    val consumerSettings: ConsumerSettings[String, RestaurantMessage] =
      ConsumerSettings[String, RestaurantMessage](system, new StringDeserializer, new RestaurantMessageDeserializer)
        .withBootstrapServers(s"$brokerHost:$brokerPort")
        .withGroupId(groupId)

    val mapRestaurant: Flow[CommittableMessage[String, RestaurantMessage], Any, NotUsed] =
      Flow[CommittableMessage[String, RestaurantMessage]].map { msg =>
        val restaurant = msg.record.value().payload
        consumerService.restaurantToEntities(restaurant)
      }

    val sink = Sink.actorRefWithBackpressure(producerActor, InitStream, Ack, Complete, StreamFailure)
    val decider: Supervision.Decider ={
      case ex : RestaurantToEntitiesException =>
        log.error(ex.message)
        Supervision.Resume
    }

    //val consumer =
    Consumer
      .committableSource(consumerSettings, Subscriptions.topics(sourceRestaurantTopic))
      .via(mapRestaurant)
      .withAttributes(ActorAttributes.supervisionStrategy(decider))
      .runWith(sink)

  }

}
