package net.pantasystem.milktea.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import net.pantasystem.milktea.api.misskey.infos.InstanceInfosResponse
import net.pantasystem.milktea.auth.suggestions.InstanceSuggestionsPagingModel
import net.pantasystem.milktea.common.*
import net.pantasystem.milktea.model.instance.InstanceInfoService
import net.pantasystem.milktea.model.instance.InstanceInfoType
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val instanceInfoService: InstanceInfoService,
    private val instancePagingModel: InstanceSuggestionsPagingModel,
    loggerFactory: Logger.Factory,
) : ViewModel() {

    private var _keyword = MutableStateFlow("")
    val keyword = _keyword.asStateFlow()

//    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
//    private val instancesInfosResponse = keyword.flatMapLatest { name ->
//        suspend {
//            requireNotNull(
//                instancesInfosAPIBuilder.build().getInstances(
//                    name = name
//                ).throwIfHasError()
//                    .body()
//            ).distinctBy {
//                it.url
//            }
//        }.asFlow()
//    }.catch {
//        logger.error("インスタンス情報の取得に失敗", it)
//    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val instancesInfosResponse = instancePagingModel.state.map {
        (it.content as? StateContent.Exist)?.rawContent
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList(),
    )


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
            infos ?: emptyList(),
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        SignUpUiState()
    )

    init {
        instancePagingModel.onLoadNext(viewModelScope)
    }

    fun onInputKeyword(value: String) {
        viewModelScope.launch {
            _keyword.value = value
            instancePagingModel.setQueryName(value)
            instancePagingModel.onLoadNext(this)
        }
    }

    fun onSelected(instancesInfosResponse: InstanceInfosResponse.InstanceInfo) {
        _selectedInstanceUrl.value = instancesInfosResponse.url
    }

    fun onBottomReached() {
        instancePagingModel.onLoadNext(viewModelScope)
    }
}

data class SignUpUiState(
    val keyword: String = "",
    val selectedUrl: String? = "misskey.io",
    val instanceInfo: ResultState<InstanceInfoType> = ResultState.Loading(StateContent.NotExist()),
    val instancesInfosResponse: List<InstanceInfosResponse.InstanceInfo> = emptyList(),
) {

    val filteredInfos = instancesInfosResponse
}