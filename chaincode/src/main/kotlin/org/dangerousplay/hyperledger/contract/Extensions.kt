package org.dangerousplay.hyperledger.contract

import arrow.core.getOrHandle
import arrow.fx.IO
import org.hyperledger.fabric.shim.ChaincodeException

fun <A> ByteArray.parseOrThrow(parser: (ByteArray) -> A): A {
    return IO { parser(this) }
        .attempt()
        .unsafeRunSync()
        .getOrHandle { throw ChaincodeException("Invalid request, bad data", this, it) }
}
