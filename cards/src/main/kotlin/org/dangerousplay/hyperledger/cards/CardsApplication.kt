package org.dangerousplay.hyperledger.cards

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
open class CardsApplication


fun main(args: Array<String>) {
    SpringApplication.run(CardsApplication::class.java, *args)
}
