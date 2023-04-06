package com.mathroda.coins_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mathroda.coins_screen.state.CoinsState
import com.mathroda.coins_screen.state.PaginationState
import com.mathroda.core.util.Resource
import com.mathroda.datasource.core.DashCoinRepository
import com.mathroda.domain.Coins
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import javax.inject.Inject

@HiltViewModel
class CoinsViewModel @Inject constructor(
    private val dashCoinRepository: DashCoinRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CoinsState())
    val state = _state.asStateFlow()

    private val _paginationState = MutableStateFlow(PaginationState())
    val paginationState = _paginationState.asStateFlow()


    private val _isRefresh = MutableStateFlow(false)
    val isRefresh: StateFlow<Boolean> = _isRefresh

    fun getCoins() {
       dashCoinRepository.getCoins(skip = _paginationState.value.skip)
           .distinctUntilChanged()
           .onEach { result ->
                when (result) {
                    is Resource.Success -> result.data?.let { data -> onRequestSuccess(data) }
                    is Resource.Error -> onRequestError(result.message)
                    is Resource.Loading -> onRequestLoading()
                }
           }
           .launchIn(viewModelScope + SupervisorJob())
    }

    fun getCoinsPaginated() {
        if (!_paginationState.value.endReached && _state.value.coins.isNotEmpty()) {
            if (_paginationState.value.isLoading) return
            getCoins()
        }
    }

    fun onRequestSuccess(
        data: List<Coins>
    ) {
        updateState(
            coins = _state.value.coins + data,
        )

        val listSize = _state.value.coins.size

        updatePaginationState(
            skip = listSize,
            endReached = data.isEmpty() || listSize >= COINS_LIMIT,
            isLoading = false
        )
    }

    fun onRequestError(
        message: String?
    ) {
        updateState(
            error = message ?: "Unexpected Error Occurred"
        )
    }
    fun onRequestLoading() {
        if (_state.value.coins.isEmpty()) {
            updateState(isLoading = true)
            return
        }

        updatePaginationState(
            isLoading = true
        )
    }

    fun refresh() {
        viewModelScope.launch(Dispatchers.IO) {
            updateRefreshState(true)
            updatePaginationState()
            updateState()
            getCoins()
            updateRefreshState(false)
        }

    }

    private fun updateRefreshState(
        value: Boolean
    ) = _isRefresh.update { value }

    fun updateState(
        isLoading: Boolean = false,
        coins: List<Coins> = emptyList(),
        error: String = ""
    ) {
        _state.update {
            it.copy(
                isLoading = isLoading,
                coins = coins,
                error = error
            )
        }
    }

    fun updatePaginationState(
        skip: Int = 0,
        endReached: Boolean = false,
        isLoading: Boolean = false
    ) {
        _paginationState.update {
            it.copy(
                skip = skip,
                endReached = endReached,
                isLoading = isLoading
            )
        }
    }

    companion object {
        const val COINS_LIMIT = 400
    }
}