package dev.denissajnar.command

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.mongodb.config.EnableMongoAuditing

@SpringBootApplication
@EnableMongoAuditing
class OrderCommandServiceApplication

fun main(args: Array<String>) {
    runApplication<OrderCommandServiceApplication>(*args)
}