package com.mathroda.datasource.providers

import com.mathroda.core.state.UserState
import kotlinx.coroutines.flow.Flow

interface ProvidersRepository {
    fun userStateProvider (
        function: () -> Unit
    ): Flow<UserState>
}