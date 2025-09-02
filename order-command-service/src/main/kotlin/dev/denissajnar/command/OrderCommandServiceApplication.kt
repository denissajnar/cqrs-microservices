package dev.denissajnar.command

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.mongodb.config.EnableMongoAuditing
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableMongoAuditing
@EnableScheduling
class OrderCommandServiceApplication

fun main(args: Array<String>) {
    runApplication<OrderCommandServiceApplication>(*args)
}
