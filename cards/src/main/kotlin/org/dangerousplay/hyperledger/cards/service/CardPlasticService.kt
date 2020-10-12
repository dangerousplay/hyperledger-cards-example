package org.dangerousplay.hyperledger.cards.service

import org.dangerousplay.hyperledger.CardPlastic
import org.dangerousplay.hyperledger.CreatePlasticRequest
import org.dangerousplay.hyperledger.ListPlasticRequest
import org.dangerousplay.hyperledger.PlasticMode
import org.hyperledger.fabric.gateway.Contract
import org.hyperledger.fabric.gateway.Gateway
import org.springframework.stereotype.Service
import xyz.downgoon.snowflake.Snowflake
import java.util.*
import javax.annotation.PostConstruct

@Service
class CardPlasticService(private val gateway: Gateway) {

    @PostConstruct
    fun doIt() {
        val network = this.gateway.getNetwork("mychannel")

        network.channel.initialize()

        val contract: Contract = network.getContract("cards")



        val snowflake = Snowflake(1, 1)

        val id = snowflake.nextId().toString()

        val request = CreatePlasticRequest.newBuilder()
            .setCard(
                CardPlastic.newBuilder()
                    .setAccountNumber("0111")
                    .setPlasticMode(PlasticMode.MULTIPLE)
                    .setEmbossingName("DAVI F HENRIQUE")
            ).setId(id)
            .build().toByteArray()

        contract.submitTransaction("createPlastic", String(Base64.getEncoder().encode(request)))

        val listRequest = ListPlasticRequest.newBuilder().setPageSize(10).build().toByteArray()

        val result = contract.evaluateTransaction("listPlastics", String(Base64.getEncoder().encode(listRequest)))

        val rr = String(result)

        println("\n")
    }

}
