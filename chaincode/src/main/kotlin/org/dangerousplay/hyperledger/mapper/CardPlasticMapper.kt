package org.dangerousplay.hyperledger.mapper

import org.dangerousplay.hyperledger.Address
import org.dangerousplay.hyperledger.BlockPlasticRequest
import org.dangerousplay.hyperledger.BlockReason
import org.dangerousplay.hyperledger.CreatePlasticRequest
import org.dangerousplay.hyperledger.event.CardPlasticBlockEvent
import org.dangerousplay.hyperledger.model.CardPlastic
import java.lang.IllegalArgumentException


fun CreatePlasticRequest.toPlastic() =
    this.card.run {
        CardPlastic(
            accountNumber = this.accountNumber,
            deliveryAddress = this.deliveryAddress.toAddress(),
            embossingName = this.embossingName,
            lastDigitsPan = this.lastDigitsPan,
            orderId = this.orderId,
            productId = this.productId,
            productName = this.productName,
            recipientName = this.recipientName,
            virtual = this.virtual
        )
    }

fun Address.toAddress() =
    org.dangerousplay.hyperledger.model.Address(
        street = this.street,
        city = this.city,
        complement = this.complement,
        neighborhood = this.neighborhood,
        postalCode = this.postalCode,
        state = this.state
    )


fun BlockPlasticRequest.toEvent(plastic: CardPlastic) = CardPlasticBlockEvent(plastic, this.blockReason.toBlockReason(), this.protocol)


fun BlockReason.toBlockReason() = when(this) {
    BlockReason.DESTROYED -> org.dangerousplay.hyperledger.model.BlockReason.DESTROYED
    BlockReason.STOLEN_LOST -> org.dangerousplay.hyperledger.model.BlockReason.STOLEN_LOST
    BlockReason.FRAUD ->org.dangerousplay.hyperledger.model.BlockReason.FRAUD
    BlockReason.TEMPORARY ->org.dangerousplay.hyperledger.model.BlockReason.TEMPORARY
    BlockReason.PREVENTIVE -> org.dangerousplay.hyperledger.model.BlockReason.PREVENTIVE
    BlockReason.REPLACEMENT -> org.dangerousplay.hyperledger.model.BlockReason.REPLACEMENT
    BlockReason.OTHER -> org.dangerousplay.hyperledger.model.BlockReason.OTHER

    else -> throw IllegalArgumentException("Invalid BlockReason $this")
}
