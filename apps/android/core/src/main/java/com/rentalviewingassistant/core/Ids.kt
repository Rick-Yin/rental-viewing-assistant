package com.rentalviewingassistant.core

import java.util.UUID

object Ids {
    fun newId(prefix: String): String = "$prefix:${UUID.randomUUID()}"
}
