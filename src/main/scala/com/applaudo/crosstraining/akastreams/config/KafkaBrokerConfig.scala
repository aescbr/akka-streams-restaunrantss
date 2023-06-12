package com.applaudo.crosstraining.akastreams.config

import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.producer.ProducerConfig

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
}
