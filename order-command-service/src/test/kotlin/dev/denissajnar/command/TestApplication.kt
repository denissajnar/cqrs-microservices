package dev.denissajnar.command

import dev.denissajnar.command.config.TestcontainersConfiguration
import org.springframework.boot.fromApplication
import org.springframework.boot.with

fun main(args: Array<String>) {
    fromApplication<OrderCommandServiceApplication>().with(TestcontainersConfiguration::class).run(*args)
}
