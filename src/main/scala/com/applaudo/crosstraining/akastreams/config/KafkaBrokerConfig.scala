package com.applaudo.crosstraining.akastreams.config

import com.applaudo.crosstraining.akastreams.models.ConsumerClasses.{RestaurantEntityMessage, SourceURLMessage, WebsiteMessage}
import com.applaudo.crosstraining.akastreams.models.ProducerClasses.RestaurantMessage
import com.goyeau.kafka.streams.circe.CirceSerdes
import com.typesafe.config.ConfigFactory
import io.circe.generic.auto._
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig}

import java.util.Properties

object KafkaBrokerConfig {

  private val conf = ConfigFactory.load().getConfig("brokerConfig")

  val brokerHost: String = conf.getString("brokerHost")
  val brokerPort: String = conf.getString("brokerPort")
  val groupId: String = conf.getString("groupId")
  val restaurantTopic: String = conf.getString("restaurantTopic")
  val sourceRestaurantTopic: String = conf.getString("sourceRestaurantTopic")
  val restaurantEntityTopic: String = conf.getString("restaurantEntityTopic")
  val sourceURLTopic: String = conf.getString("sourceURLTopic")
  val websiteTopic: String = conf.getString("websiteTopic")

  val brokerProps: Properties = new Properties()
  brokerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, s"$brokerHost:$brokerPort")

  val restaurantProducer = new KafkaProducer[String,RestaurantMessage](brokerProps,
    CirceSerdes.serializer[String],
    CirceSerdes.serializer[RestaurantMessage])

  val restaurantEntityProducer = new KafkaProducer(brokerProps,
    CirceSerdes.serializer[String],
    CirceSerdes.serializer[RestaurantEntityMessage]
  )

  val sourceURLProducer = new KafkaProducer(brokerProps,
    CirceSerdes.serializer[String],
    CirceSerdes.serializer[SourceURLMessage],
  )

  val websiteProducer = new KafkaProducer(brokerProps,
    CirceSerdes.serializer[String],
    CirceSerdes.serializer[WebsiteMessage],
  )

}
