package dev.denissajnar.query

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.transaction.annotation.EnableTransactionManagement

@SpringBootApplication
@EnableJpaRepositories
@EnableTransactionManagement
class OrderQueryServiceApplication

fun main(args: Array<String>) {
    runApplication<OrderQueryServiceApplication>(*args)
}