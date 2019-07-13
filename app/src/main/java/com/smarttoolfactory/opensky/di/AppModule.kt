package com.smarttoolfactory.opensky.di


import com.smarttoolfactory.opensky.api.OpenSkyApi
import com.smarttoolfactory.opensky.repo.AircraftRepository
import com.smarttoolfactory.opensky.repo.remote.WebService
import com.smarttoolfactory.opensky.utils.AppExecutors

import javax.inject.Singleton

import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


@Module(includes = [ViewModelModule::class])
class AppModule {

    @Singleton
    @Provides
     fun provideOpenSkyApi(): OpenSkyApi {
        return Retrofit.Builder()
                .baseUrl(OpenSkyApi.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build().create(OpenSkyApi::class.java)
    }

    @Singleton
    @Provides
    fun provideAircraftRepository(appExecutors: AppExecutors, webService: WebService): AircraftRepository {
        return AircraftRepository(appExecutors, webService)
    }

    @Singleton
    @Provides
    fun provideAppExecutors(): AppExecutors {
        return AppExecutors()
    }

}
