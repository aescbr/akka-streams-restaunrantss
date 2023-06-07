package com.applaudo.crosstraining.akastreams.consumers

import com.applaudo.crosstraining.akastreams.models.ProducerClasses.RestaurantMessage
import com.goyeau.kafka.streams.circe.CirceSerdes
import io.circe.generic.auto._
import org.apache.kafka.common.serialization.Deserializer


class RestaurantMessageDeserializer extends Deserializer[RestaurantMessage]{

  override def deserialize(topic: String, data: Array[Byte]): RestaurantMessage = {
    val serde = CirceSerdes.deserializer[RestaurantMessage]
    serde.deserialize(topic, data)
  }
}
