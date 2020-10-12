package org.dangerousplay.hyperledger.model


data class Address(
    val street: String,
    val complement: String,
    val neighborhood: String,
    val postalCode: String,
    val city: String,
    val state: String
)
