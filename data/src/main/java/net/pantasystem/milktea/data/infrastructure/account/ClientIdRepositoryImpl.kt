package net.pantasystem.milktea.data.infrastructure.account

import android.content.SharedPreferences
import androidx.core.content.edit
import net.pantasystem.milktea.model.account.ClientId
import net.pantasystem.milktea.model.account.ClientIdRepository
import javax.inject.Inject

class ClientIdRepositoryImpl @Inject constructor(
    private val sharedPreferences: SharedPreferences
): ClientIdRepository {
    override fun getOrCreate(): ClientId {
        val clientId = sharedPreferences.getString("milktea.CLIENT_ID", null)?.let {
            ClientId(it)
        }
        val newClientId = ClientId.createOrNothing(clientId)
        if(clientId == null) {
            sharedPreferences.edit {
                putString(newClientId.clientId, "milktea.CLIENT_ID")
            }
        }
        return newClientId
    }
}