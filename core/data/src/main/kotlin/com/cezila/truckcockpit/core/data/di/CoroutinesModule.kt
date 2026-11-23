package com.cezila.truckcockpit.core.data.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

/** Scope that lives as long as the app, for flows shared across screens. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
internal annotation class ApplicationScope

@Qualifier
@Retention(AnnotationRetention.BINARY)
internal annotation class DefaultDispatcher

@Module
@InstallIn(SingletonComponent::class)
internal object CoroutinesModule {

    @Provides
    @DefaultDispatcher
    fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(@DefaultDispatcher dispatcher: CoroutineDispatcher): CoroutineScope =
        CoroutineScope(SupervisorJob() + dispatcher)
}
