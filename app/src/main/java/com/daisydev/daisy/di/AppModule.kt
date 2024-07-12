package com.daisydev.daisy.di

import android.content.Context
import com.daisydev.daisy.repository.local.SessionDataStore
import com.daisydev.daisy.repository.remote.AppWriteRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.appwrite.Client
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Named
import javax.inject.Singleton
import com.daisydev.daisy.util.Constants

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // Function that provides the base URL of the API
    @Provides
    @Named("WEB_API")
    fun baseUrl(): String = Constants().baseUrl

    // Function that provides the project ID of AppWrite
    @Provides
    @Named("PROJECT_ID")
    fun projectId(): String = Constants().projectId

    // Function that provides the AppWrite client
    @Singleton
    @Provides
    fun provideAppWriteClient(
        @ApplicationContext context: Context,
        @Named("WEB_API") baseUrl: String,
        @Named("PROJECT_ID") projectId: String
    ): Client =
        Client(context).setEndpoint(baseUrl).setProject(projectId)

    // Function that provides the coroutine dispatcher
    @Provides
    fun provideCoroutineDispatcher(): CoroutineDispatcher {
        return Dispatchers.IO
    }

    // Function that provides the AppWrite repository
    @Provides
    fun provideAppWriteRepository(
        @ApplicationContext context: Context,
        client: Client,
        dispatcher: CoroutineDispatcher
    ): AppWriteRepository =
        AppWriteRepository(context, client, dispatcher)

    // Function that provides the session DataStore
    @Singleton
    @Provides
    fun provideSessionDataStore(@ApplicationContext context: Context) =
        SessionDataStore(context)
}