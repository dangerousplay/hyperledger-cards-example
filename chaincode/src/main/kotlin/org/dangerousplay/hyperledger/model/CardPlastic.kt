package org.dangerousplay.hyperledger.model


data class CardPlastic(val accountNumber: String,
                       val embossingName: String,
                       val productId: Long,
                       val productName: String,
                       val lastDigitsPan: String,
                       val recipientName: String,
                       val deliveryAddress: Address,
                       val virtual: Boolean,
                       val orderId: String,
                       val blockReason: BlockReason? = null
)

