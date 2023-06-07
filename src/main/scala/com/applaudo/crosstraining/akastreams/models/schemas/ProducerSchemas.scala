package com.applaudo.crosstraining.akastreams.models.schemas

object ProducerSchemas extends BaseSchema {
  val restaurantSchema: ProducerSchemas.Schema = Schema(
    `type` = "struct",
    fields = List(
      SchemaNode(`type` = "string", optional = false, field = "id"),
      SchemaNode(`type` = "string", optional = false, field = "dateAdded"),
      SchemaNode(`type` = "string", optional = false, field = "dateUpdated"),
      SchemaNode(`type` = "string", optional = false, field = "address"),
      SchemaNode(`type` = "string", optional = false, field = "categories"),
      SchemaNode(`type` = "string", optional = false, field = "city"),
      SchemaNode(`type` = "string", optional = false, field = "country"),
      SchemaNode(`type` = "string", optional = false, field = "keys"),
      SchemaNode(`type` = "float", optional = false, field = "latitude"),
      SchemaNode(`type` = "float", optional = false, field = "longitude"),
      SchemaNode(`type` = "string", optional = false, field = "name"),
      SchemaNode(`type` = "string", optional = false, field = "postalCode"),
      SchemaNode(`type` = "string", optional = false, field = "province"),
      SchemaNode(`type` = "string", optional = false, field = "sourceURLs"),
      SchemaNode(`type` = "string", optional = false, field = "websites")
    ),
    optional = false
    //name = "restaurant"
  )
}
