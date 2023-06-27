package com.applaudo.crosstraining.akastreams.consumers

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.kafka.{ConsumerMessage, ConsumerSettings, Subscriptions}
import akka.kafka.scaladsl.Consumer
import akka.stream.scaladsl.Source
import com.applaudo.crosstraining.akastreams.actors.ProducerActor
import com.applaudo.crosstraining.akastreams.config.KafkaBrokerConfig._
import com.applaudo.crosstraining.akastreams.models.ProducerClasses.RestaurantMessage
import com.applaudo.crosstraining.akastreams.models.schemas.ConsumerSchemas.{restaurantEntitySchema, schemaURL, schemaWebsite}
import com.applaudo.crosstraining.akastreams.models.schemas.ProducerSchemas.restaurantSchema
import com.applaudo.crosstraining.akastreams.services._
import org.apache.kafka.common.serialization.StringDeserializer

object MainConsumer {

  implicit val system: ActorSystem = ActorSystem.create("restaurant-consumer")
  val timeCounter: Long = System.nanoTime()
  val producerService: ProducerService = ProducerServiceImpl(restaurantProducer, restaurantSchema, restaurantTopic)
  val consumerService: ConsumerService = ConsumerServiceImpl(restaurantEntityProducer,
    sourceURLProducer, websiteProducer, schemaURL, schemaWebsite, restaurantEntitySchema, restaurantEntityTopic,
    sourceURLTopic, websiteTopic)

  val producerActor: ActorRef = system.actorOf(Props(classOf[ProducerActor], timeCounter,
    producerService, consumerService), "producer-actor")

  val consumerSettings: ConsumerSettings[String, RestaurantMessage] =
    ConsumerSettings[String, RestaurantMessage](system, new StringDeserializer, new RestaurantMessageDeserializer)
      .withBootstrapServers(s"$brokerHost:$brokerPort")
      .withGroupId(groupId)

  val consumerSource: Source[ConsumerMessage.CommittableMessage[String, RestaurantMessage], Consumer.Control] = Consumer
    .committableSource(consumerSettings, Subscriptions.topics(sourceRestaurantTopic))

  def main(args: Array[String]): Unit = {
    val consumer = new RestaurantConsumer()
    consumer.normalizeRestaurant(consumerSource, consumerService, producerActor)
  }

}
