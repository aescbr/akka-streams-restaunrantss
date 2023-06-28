package com.applaudo.crosstraining.akastreams.actors

import akka.actor.{Actor, ActorLogging, ActorSystem}
import com.applaudo.crosstraining.akastreams.models.ConsumerClasses._
import com.applaudo.crosstraining.akastreams.models.ProducerClasses._
import com.applaudo.crosstraining.akastreams.services.{ConsumerService, ProducerService}
import org.apache.kafka.clients.producer.RecordMetadata

import java.util.concurrent.{Future => JFuture}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

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
      val msg = restaurantEntitiesMessage.restaurantMessage.payload
      log.info(s"processing ${msg.id}")
      val results = consumerService.sendRestaurantEntities(restaurantEntitiesMessage)
      processResults(msg.id, results)
      sender ! Ack
    case _ : Complete.type =>
      log.info(s"stream completed! in: ${System.nanoTime() - counter}" )
  }

  private def processResults(restaurantId :String , results:  Set[JFuture[RecordMetadata]]): Unit = {
    results.foreach{ result =>
      val futureResult = Future {result.get()}
      futureResult.onComplete {
        case Failure(ex) =>
          log.error(ex.getMessage)
        case Success(metadata) =>
          log.info(s"message sent to key: $restaurantId" +
            s" topic: ${metadata.topic()} partition: ${metadata.partition()}")
      }
    }
  }

}
