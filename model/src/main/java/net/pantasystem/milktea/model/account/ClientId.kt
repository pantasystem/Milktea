package net.pantasystem.milktea.model.account

import java.util.*

data class ClientId(val clientId: String) {
    companion object {
        fun createOrNothing(clientId: ClientId?): ClientId {
            return if (clientId == null) {
                ClientId(UUID.randomUUID().toString())
            } else {
                clientId
            }
        }
    }
}