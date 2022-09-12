package net.pantasystem.milktea.data.infrastructure.account

import android.content.SharedPreferences
import androidx.core.content.edit
import net.pantasystem.milktea.model.account.ClientId
import net.pantasystem.milktea.model.account.ClientIdRepository
import javax.inject.Inject

private const val CLIENT_ID = "milktea.CLIENT_ID"
class ClientIdRepositoryImpl @Inject constructor(
    private val sharedPreferences: SharedPreferences
): ClientIdRepository {
    override fun getOrCreate(): ClientId {
        val clientId = sharedPreferences.getString(CLIENT_ID, null)?.let {
            ClientId(it)
        }
        val newClientId = ClientId.createOrNothing(clientId)
        if(clientId == null) {
            sharedPreferences.edit {
                putString(CLIENT_ID, newClientId.clientId)
            }
        }
        return newClientId
    }
}