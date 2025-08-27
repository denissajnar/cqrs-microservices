package dev.denissajnar.query.util

import io.restassured.specification.RequestSpecification

fun RequestSpecification.whenever(): RequestSpecification = this.`when`()
