package org.dangerousplay.hyperledger.contract

import arrow.core.*
import arrow.fx.IO
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.dangerousplay.hyperledger.*
import org.dangerousplay.hyperledger.event.CardPlasticBlockEvent
import org.dangerousplay.hyperledger.mapper.toBlockReason
import org.dangerousplay.hyperledger.mapper.toEvent
import org.dangerousplay.hyperledger.mapper.toPlastic
import org.dangerousplay.hyperledger.model.CardPlastic
import org.hyperledger.fabric.contract.Context
import org.hyperledger.fabric.contract.ContractInterface
import org.hyperledger.fabric.contract.annotation.*
import org.hyperledger.fabric.shim.ChaincodeException
import org.hyperledger.fabric.shim.ledger.CompositeKey
import java.util.*
import java.util.logging.Logger


const val CARD_CONTRACT_NAME = "CardPlastic"

@Default
@Contract(
    name = CARD_CONTRACT_NAME,
    info = Info(
        title = "Card plastics management",
        description = "Card plastics management",
        version = "0.0.1-SNAPSHOT",
        contact = Contact(
            email = "card@example.com",
            name = "Davi Henrique",
            url = "https://hyperledger.example.com"
        )
    )
)
class CardsContract: ContractInterface {

    private val logger = Logger.getLogger(this::class.java.name);

    private val objectMapper = jacksonObjectMapper()

    private val PLASTIC_PREFIX = "PLASTIC"


    @Transaction(intent = Transaction.TYPE.EVALUATE)
    fun init(ctx: Context) {

    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    fun createPlastic(ctx: Context) {
        val body = Base64.getDecoder().decode(ctx.stub.parameters[0])

        val request = body.parseOrThrow(CreatePlasticRequest::parseFrom)

        val id = CompositeKey(PLASTIC_PREFIX, request.id).toString()

        val plastic = request.toPlastic()

        ctx.stub.putState(id, this.objectMapper.writeValueAsBytes(plastic))

        logger.fine { "Created a plastic with ID: $id" }
    }


    @Transaction(intent = Transaction.TYPE.EVALUATE)
    fun getPlastic(ctx: Context): CardPlastic {
        val body = Base64.getDecoder().decode(ctx.stub.parameters[0])

        val request = body.parseOrThrow(GetPlasticRequest::parseFrom)

        return getPlasticById(ctx, request.plasticId)
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    fun listPlastics(ctx: Context): List<CardPlastic> {
        val body = Base64.getDecoder().decode(ctx.stub.parameters[0])

        val request = body.parseOrThrow(ListPlasticRequest::parseFrom)

        return ctx.stub.getStateByPartialCompositeKeyWithPagination(CompositeKey(PLASTIC_PREFIX), request.pageSize, request.bookMark)
            .toList()
            .map { objectMapper.readValue(it.stringValue, CardPlastic::class.java) }
    }


    @Transaction(intent = Transaction.TYPE.SUBMIT)
    fun blockPlastic(ctx: Context) {
        val body = Base64.getDecoder().decode(ctx.stub.parameters[0])

        val request = body.parseOrThrow(BlockPlasticRequest::parseFrom)

        val plasticId = request.plasticId

        val plastic = getPlasticById(ctx, plasticId.toString())

        if (plastic.blockReason != null) throw ChaincodeException("Plastic '${request.plasticId}' is already blocked")

        val blockEvent = request.toEvent(plastic)

        val blockedPlastic = plastic.copy(blockReason = request.blockReason.toBlockReason())

        ctx.stub.putStringState(plasticId.toString(), this.objectMapper.writeValueAsString(blockedPlastic))

        logger.fine { "Blocked plastic with ID: $plasticId" }

        ctx.stub.setEvent(CardPlasticBlockEvent.EVENT_NAME, this.objectMapper.writeValueAsBytes(blockEvent))

        logger.fine { "Emitting an event for Plastic Block with ID: $plasticId" }
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    fun isBlocked(ctx: Context): Boolean {
        val body = Base64.getDecoder().decode(ctx.stub.parameters[0])

        val request = body.parseOrThrow(IsBlockedRequest::parseFrom)

        return this.getPlasticById(ctx, request.id).blockReason.toOption().isDefined()
    }



    private fun getPlasticById(
        ctx: Context,
        plasticId: String
    ): CardPlastic {
        return ctx.stub.getStringState(plasticId)
            .toOption()
            .filter { it.isNotBlank() }
            .toEither { ChaincodeException("Plastic not found with the id: $plasticId") }
            .map { objectMapper.readValue(it, CardPlastic::class.java) }
            .getOrHandle { throw it }
    }
}
