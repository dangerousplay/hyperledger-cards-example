package org.dangerousplay.hyperledger.contract

import arrow.syntax.function.pipe
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.dangerousplay.hyperledger.CardTransactionRequest
import org.dangerousplay.hyperledger.IsBlockedRequest
import org.hyperledger.fabric.contract.Context
import org.hyperledger.fabric.contract.ContractInterface
import org.hyperledger.fabric.contract.annotation.Contact
import org.hyperledger.fabric.contract.annotation.Contract
import org.hyperledger.fabric.contract.annotation.Info
import org.hyperledger.fabric.contract.annotation.Transaction
import org.hyperledger.fabric.shim.ChaincodeException
import org.hyperledger.fabric.shim.ledger.CompositeKey
import java.util.*
import java.util.logging.Logger


@Contract(
    name = "CardTransaction",
    info = Info(
        title = "Card transaction management",
        description = "Card transaction management",
        version = "0.0.1-SNAPSHOT",
        contact = Contact(
            email = "card@example.com",
            name = "Davi Henrique",
            url = "https://hyperledger.example.com"
        )
    )
)
class CardTransaction : ContractInterface {
    private val logger = Logger.getLogger(this::class.java.name);

    private val objectMapper = jacksonObjectMapper()

    private val TRANSACTION_PREFIX = "TRANSACTION"


    @Transaction(intent = Transaction.TYPE.EVALUATE)
    fun init(ctx: Context) {

    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    fun issueTransaction(ctx: Context) {
        val body = Base64.getDecoder().decode(ctx.stub.parameters[0])

        val request = body.parseOrThrow(CardTransactionRequest::parseFrom)

        val plasticId = request.plasticId

        val isBlocked = ctx.stub.invokeChaincode(
            CardsContract::isBlocked.name,
            listOf(IsBlockedRequest.newBuilder().setId(plasticId).build().toByteArray())
        ).pipe { it.stringPayload!!.toBoolean() }

        if (isBlocked) throw ChaincodeException("The given plastic is blocked: $plasticId")

        val transactionId = CompositeKey(TRANSACTION_PREFIX, plasticId).toString()

        ctx.stub.putState(transactionId, objectMapper.writeValueAsBytes(request))

        logger.fine { "Transaction registered for plastic: $plasticId" }
    }

}
