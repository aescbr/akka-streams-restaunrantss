package com.applaudo.crosstraining.akastreams.consumers

import akka.NotUsed
import akka.actor.{ActorRef, ActorSystem}
import akka.kafka.ConsumerMessage
import akka.kafka.ConsumerMessage.CommittableMessage
import akka.kafka.scaladsl.Consumer
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import akka.stream.{ActorAttributes, Supervision}
import com.applaudo.crosstraining.akastreams.actors.ProducerActor
import com.applaudo.crosstraining.akastreams.models.ConsumerClasses.{RestaurantEntitiesMessage, RestaurantToEntitiesException}
import com.applaudo.crosstraining.akastreams.services.ConsumerService
import org.slf4j.{Logger, LoggerFactory}

class RestaurantConsumer()(implicit system: ActorSystem) {

  import ProducerActor._
  import com.applaudo.crosstraining.akastreams.models.ProducerClasses._

  val log: Logger = LoggerFactory.getLogger(getClass)

  def normalizeRestaurant(source: Source[ConsumerMessage
  .CommittableMessage[String, RestaurantMessage], Consumer.Control],
                          consumerService: ConsumerService,
                          producerActor: ActorRef
                         ): Unit = {

    val mapRestaurant: Flow[CommittableMessage[String, RestaurantMessage], RestaurantEntitiesMessage, NotUsed] =
      Flow[CommittableMessage[String, RestaurantMessage]].map { msg =>
        val recordMsg = msg.record.value()
        consumerService.restaurantToEntities(recordMsg.payload)
      }

    val sink = Sink.actorRefWithBackpressure(producerActor, InitStream, Ack, Complete, StreamFailure)
    val decider: Supervision.Decider = {
      case ex: RestaurantToEntitiesException =>
        log.error(ex.message)
        Supervision.Resume
      case ex =>
        log.error(s"unexpected error ${ex.getMessage}")
        Supervision.Stop
    }

    source
      .via(mapRestaurant)
      .withAttributes(ActorAttributes.supervisionStrategy(decider))
      .runWith(sink)
  }
}
