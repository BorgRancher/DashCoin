package com.mathroda.common.coroutines

import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class AppCoroutineScope(
    val context: CoroutineContext = SupervisorJob() + Dispatchers.Main
) {

    private val dashCoinScope = object : CoroutineScope {
        override val coroutineContext: CoroutineContext
            get() = context
    }


    fun launch(
        context: CoroutineContext = EmptyCoroutineContext,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.() -> Unit,
    ): Job = dashCoinScope.launch(context, start, block)
}