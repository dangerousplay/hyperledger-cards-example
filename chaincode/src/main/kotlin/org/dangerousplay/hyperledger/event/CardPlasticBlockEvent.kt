package org.dangerousplay.hyperledger.event

import org.dangerousplay.hyperledger.model.BlockReason
import org.dangerousplay.hyperledger.model.CardPlastic


data class CardPlasticBlockEvent(
    val plastic: CardPlastic,
    val reason: BlockReason,
    val protocol: String
) {
    companion object {
        const val EVENT_NAME: String = "CardPlasticBlockEvent"
    }
}


