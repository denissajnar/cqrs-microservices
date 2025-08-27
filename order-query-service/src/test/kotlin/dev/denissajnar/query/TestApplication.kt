package dev.denissajnar.query

import dev.denissajnar.query.config.TestcontainersConfiguration
import org.springframework.boot.fromApplication
import org.springframework.boot.with

fun main(args: Array<String>) {
    fromApplication<OrderQueryServiceApplication>().with(TestcontainersConfiguration::class).run(*args)
}
