package com.applaudo.crosstraining.akastreams.actors

import akka.actor.{Actor, ActorLogging, ActorSystem}
import com.applaudo.crosstraining.akastreams.models.ConsumerClasses._
import com.applaudo.crosstraining.akastreams.models.ProducerClasses._
import com.applaudo.crosstraining.akastreams.services.{ConsumerService, ProducerService}

object ProducerActor {
  case object InitStream
  case object Ack
  case object Complete
  final case class StreamFailure(ex: Throwable)
}
class ProducerActor(counter: Long, producerService: ProducerService,
                    consumerService: ConsumerService) extends Actor with ActorLogging{
  import ProducerActor._
  implicit val system: ActorSystem = context.system

  override def receive: Receive = {
    case _: InitStream.type =>
      log.info("Init stream...")
      sender ! Ack
    case restaurant: Restaurant =>
      producerService.sendMessage(restaurant)
      log.info(s"sending ${restaurant.id}")
      sender ! Ack
    case restaurantEntitiesMessage: RestaurantEntitiesMessage =>
      consumerService.sendRestaurantEntities(restaurantEntitiesMessage)
      log.info(s"processing ${restaurantEntitiesMessage.restaurantMessage.payload.id}")
      sender ! Ack
    case _ : Complete.type =>
      log.info(s"stream completed! in: ${System.nanoTime() - counter}" )
  }

}
