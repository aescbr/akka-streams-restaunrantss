package com.applaudo.crosstraining.akastreams.domain

class BaseSchema {
  case class Schema(`type`: String, fields: List[SchemaNode], optional: Boolean)

  case class SchemaNode(`type`: String, optional: Boolean, field: String)
}
