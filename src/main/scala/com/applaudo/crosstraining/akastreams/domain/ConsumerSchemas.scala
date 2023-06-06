package com.applaudo.crosstraining.akastreams.domain

object ConsumerSchemas extends BaseSchema{

  val schemaWebsite: Schema =
    Schema(
      `type` = "struct",
      fields = List(
        SchemaNode(`type` = "string", optional = false, field = "restaurantId"),
        SchemaNode(`type` = "string", optional = false, field = "site")
      ),
      optional = false)

  val schemaURL: Schema =
    Schema(
      `type` = "struct",
      fields = List(
        SchemaNode(`type` = "string", optional = false, field = "restaurantId"),
        SchemaNode(`type` = "string", optional = false, field = "url")
      ),
      optional = false)

  val restaurantEntitySchema: Schema = Schema(
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
      SchemaNode(`type` = "string", optional = false, field = "province")
    ),
    optional = false
    //name = "restaurant"
  )

}
