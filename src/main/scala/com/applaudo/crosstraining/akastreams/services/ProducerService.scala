package com.applaudo.crosstraining.akastreams.services
import com.applaudo.crosstraining.akastreams.models.ProducerClasses._


trait ProducerService {
  def strToRestaurantWithHandler(numLine: Integer, line: String): Any
}

class ProducerServiceImpl extends ProducerService {

  private def strToRestaurant(line: String): Any = {
    val data = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)")

    val id = data(0)
    val dateAdded = data(1)
    val dateUpdated = data(2)
    val address = data(3)
    val categories = data(4)
    val city = data(5)
    val country = data(6)
    val keys = data(7)
    val latitude = data(8).toDouble
    val longitude = data(9).toDouble
    val name = data(10)
    val postalCode = data(11)
    val province = data(12)
    val sourceURLs = data(13)
    val websites = data(14)

    Restaurant(id, dateAdded, dateUpdated, address, categories, city, country, keys, latitude, longitude,
      name, postalCode, province, sourceURLs, websites)
  }

  override def strToRestaurantWithHandler(numLine: Integer, line: String): Any = {
    try {
      strToRestaurant(line)
    }
    catch {
      case ex: RuntimeException =>
        throw StringToRestaurantMapException(s"${ex.getClass.getName} | ${ex.getMessage} - in line: $numLine")
    }
  }
}
