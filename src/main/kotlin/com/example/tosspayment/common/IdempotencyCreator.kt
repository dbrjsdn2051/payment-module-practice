package com.example.tosspayment.common

import java.util.UUID

class IdempotencyCreator {

    companion object {
        fun create(data: Any): String {
            return UUID.nameUUIDFromBytes(data.toString().toByteArray()).toString()
        }
    }
}