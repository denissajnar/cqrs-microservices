package dev.denissajnar.command.util

import io.restassured.specification.RequestSpecification

fun RequestSpecification.whenever(): RequestSpecification = this.`when`()
