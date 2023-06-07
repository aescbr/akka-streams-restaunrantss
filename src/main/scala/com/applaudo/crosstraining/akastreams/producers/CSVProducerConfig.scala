package com.applaudo.crosstraining.akastreams.producers

import com.applaudo.crosstraining.akastreams.models.ConsumerClasses.{RestaurantEntityMessage, SourceURLMessage, WebsiteMessage}
import com.applaudo.crosstraining.akastreams.models.ProducerClasses.RestaurantMessage
import com.goyeau.kafka.streams.circe.CirceSerdes
import io.circe.generic.auto._
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig}

import java.util.Properties

object CSVProducerConfig {

  val props: Properties = new Properties()
  props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")

  val restaurantProducer = new KafkaProducer(props,
    CirceSerdes.serializer[String],
    CirceSerdes.serializer[RestaurantMessage]
  )

  val restaurantEntityProducer = new KafkaProducer(props,
    CirceSerdes.serializer[String],
    CirceSerdes.serializer[RestaurantEntityMessage]
  )

  val sourceURLProducer = new KafkaProducer(props,
    CirceSerdes.serializer[String],
    CirceSerdes.serializer[SourceURLMessage],
  )

  val websiteProducer = new KafkaProducer(props,
    CirceSerdes.serializer[String],
    CirceSerdes.serializer[WebsiteMessage],
  )

  val restaurantTopic = "first-topic"
  val restaurantEntityTopic = "stream-output-topic"
  val sourceURLTopic = "stream-output-topic2"
  val websiteTopic = "stream-output-topic3"

}
