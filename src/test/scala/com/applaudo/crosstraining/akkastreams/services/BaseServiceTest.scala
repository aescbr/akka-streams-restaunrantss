package com.applaudo.crosstraining.akkastreams.services

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import com.applaudo.crosstraining.akastreams.models.ConsumerClasses._
import com.applaudo.crosstraining.akastreams.models.ProducerClasses.Restaurant
import com.applaudo.crosstraining.akastreams.models.schemas.ConsumerSchemas._
import com.applaudo.crosstraining.akastreams.services.{ConsumerServiceImpl, ProducerServiceImpl}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class BaseServiceTest extends TestKit(ActorSystem("system"))
  with ImplicitSender
  with AnyWordSpecLike
  with Matchers
  with BeforeAndAfterAll{

  var optProducerService :Option[ProducerServiceImpl] = None
  var optConsumerService :Option[ConsumerServiceImpl] = None

  val inputRestaurantStr: String =
    "id,2017-06-19T16:06:25Z,2018-04-07T23:40:34Z,1232 main street,Fast Food,city,US," +
      "us/ooh/hello/1232world/-1161002137,39.80607,-84.03013,restaurant,12345,OOH," +
      "\"https://sourceURL1.com/menu,https://sourceURL2.com/menu\",http://webiste1.com"

  val restaurantExpected : Restaurant = Restaurant(
    id="id",
    dateAdded="2017-06-19T16:06:25Z",
    dateUpdated="2018-04-07T23:40:34Z",
    address="1232 main street",
    categories= "Fast Food",
    city="city",
    country="US",
    keys="us/ooh/hello/1232world/-1161002137",
    latitude= "39.80607".toDouble,
    longitude="-84.03013".toDouble,
    name = "restaurant",
    postalCode="12345",
    province="OOH",
    sourceURLs= "\"https://sourceURL1.com/menu,https://sourceURL2.com/menu\"",
    websites= "http://webiste1.com"
  )

  val restaurantPayload: RestaurantPayload = RestaurantPayload(
    restaurantExpected.id,
    restaurantExpected.dateAdded,
    restaurantExpected.dateUpdated,
    restaurantExpected.address,
    restaurantExpected.categories,
    restaurantExpected.city,
    restaurantExpected.country,
    restaurantExpected.keys,
    restaurantExpected.latitude,
    restaurantExpected.longitude,
    restaurantExpected.name,
    restaurantExpected.postalCode,
    restaurantExpected.province
  )

  var restaurantEntitiesExpected: RestaurantEntitiesMessage = RestaurantEntitiesMessage(
    RestaurantEntityMessage(restaurantEntitySchema, restaurantPayload),
    List(SourceURLMessage(
      schema = schemaURL,
      payload = RestaurantSourceURL(restaurantExpected.id, "https://sourceURL1.com/menu")
    ), SourceURLMessage(
      schema = schemaURL,
      payload = RestaurantSourceURL(restaurantExpected.id, "https://sourceURL2.com/menu")
    )
    ),
    List( WebsiteMessage(
      schema = schemaWebsite,
      payload = RestaurantWebSite(restaurantExpected.id, "http://webiste1.com")
    ))
  )
}