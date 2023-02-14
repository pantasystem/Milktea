package net.pantasystem.milktea.auth.viewmodel

import android.webkit.URLUtil
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import net.pantasystem.milktea.api.misskey.InstanceInfoAPIBuilder
import net.pantasystem.milktea.api.misskey.infos.InstanceInfosResponse
import net.pantasystem.milktea.common.ResultState
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.common.asLoadingStateFlow
import net.pantasystem.milktea.common.throwIfHasError
import net.pantasystem.milktea.model.instance.InstanceInfoService
import net.pantasystem.milktea.model.instance.InstanceInfoType
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val instancesInfosAPIBuilder: InstanceInfoAPIBuilder,
    private val instanceInfoService: InstanceInfoService
) : ViewModel() {

    @OptIn(FlowPreview::class)
    private val instancesInfosResponse = suspend {
        requireNotNull(
            instancesInfosAPIBuilder.build().getInstances()
                .throwIfHasError()
                .body()
        )
    }.asFlow().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private var _keyword = MutableStateFlow("")
    val keyword = _keyword.asStateFlow()

    private var _selectedInstanceUrl = MutableStateFlow<String?>("misskey.io")

    @OptIn(ExperimentalCoroutinesApi::class)
    val instanceInfo = combine(keyword, _selectedInstanceUrl) { it, selected ->
        selected?.let {
            "https://$it"
        } ?: if (it.startsWith("https://") || it.startsWith("http://")) {
            it
        } else {
            "https://$it"
        }
    }.filter {
        URLUtil.isNetworkUrl(it)
    }.flatMapLatest {
        suspend {
            instanceInfoService.find(it).getOrThrow()
        }.asLoadingStateFlow()
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        ResultState.Loading(StateContent.NotExist())
    )

    val uiState = combine(
        instancesInfosResponse,
        keyword,
        instanceInfo,
        _selectedInstanceUrl
    ) { infos, keyword, info, selected ->
        SignUpUiState(
            keyword = keyword,
            selected,
            info,
            infos,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        SignUpUiState()
    )

    fun onInputKeyword(value: String) {
        _keyword.value = value
    }

    fun onSelected(instancesInfosResponse: InstanceInfosResponse.InstanceInfo) {
        _selectedInstanceUrl.value = instancesInfosResponse.url
    }
}

data class SignUpUiState(
    val keyword: String = "",
    val selectedUrl: String? = "misskey.io",
    val instanceInfo: ResultState<InstanceInfoType> = ResultState.Loading(StateContent.NotExist()),
    val instancesInfosResponse: InstanceInfosResponse? = null
) {

    val filteredInfos = (instancesInfosResponse?.instancesInfos?.filter {
        it.url.contains(keyword) || it.name.contains(keyword)
    } ?: emptyList()).let { list ->
        val misskeyIo = list.firstOrNull {
            it.url == "misskey.io"
        }
        val otherInstances = list.filterNot {
            it.url == "misskey.io"
        }
        listOfNotNull(misskeyIo) + otherInstances
    }
}