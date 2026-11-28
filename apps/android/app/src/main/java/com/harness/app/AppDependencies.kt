package com.harness.app

import com.harness.app.network.ApiClient
import com.harness.app.network.ApiConfiguration
import com.harness.app.network.DefaultApiClient
import com.harness.app.network.HttpSession
import com.harness.app.network.UrlConnectionHttpSession
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Composition root: the only place where concrete implementations are created and wired.
 * View models, repositories and services receive their dependencies through their constructor and never read it.
 * Every `make…()` returns a new instance, without any DI framework.
 */
internal class AppDependencies {

    fun makeIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    fun makeApiConfiguration(): ApiConfiguration = ApiConfiguration(BuildConfig.API_BASE_URL)

    fun makeHttpSession(): HttpSession = UrlConnectionHttpSession(makeIoDispatcher())

    fun makeApiClient(): ApiClient = DefaultApiClient(makeApiConfiguration(), makeHttpSession())
}
