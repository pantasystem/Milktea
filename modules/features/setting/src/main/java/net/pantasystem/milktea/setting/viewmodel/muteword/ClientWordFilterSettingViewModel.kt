package net.pantasystem.milktea.setting.viewmodel.muteword

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import net.pantasystem.milktea.common.mapCancellableCatching
import net.pantasystem.milktea.model.notes.muteword.WordFilterConfigRepository
import net.pantasystem.milktea.model.notes.muteword.WordFilterConfigTextParser
import javax.inject.Inject

@HiltViewModel
class ClientWordFilterSettingViewModel @Inject constructor(
    private val repository: WordFilterConfigRepository
) : ViewModel() {


    var muteWordsFieldState by mutableStateOf("")
        private set

    init {
        viewModelScope.launch {
            repository.get().mapCancellableCatching {
                WordFilterConfigTextParser.fromConfig(it).getOrThrow()
            }.onSuccess {
                muteWordsFieldState = it
            }
        }
    }

    fun updateText(text: String) {
        muteWordsFieldState = text
    }

    fun save() {
        viewModelScope.launch {
            WordFilterConfigTextParser.fromText(muteWordsFieldState).mapCancellableCatching {
                repository.save(it).getOrThrow()
            }.onSuccess {

            }

        }
    }
}