package com.smarttoolfactory.opensky.di


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

import com.smarttoolfactory.opensky.viewmodel.AircraftViewModel
import com.smarttoolfactory.opensky.viewmodel.AircraftViewModelFactory

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ViewModelModule {


    @Binds
    @IntoMap
    @ViewModelKey(AircraftViewModel::class)
    abstract fun bindAircraftViewModel(aircraftViewModel: AircraftViewModel): ViewModel

    @Binds
    abstract fun bindViewModelFactory(factory: AircraftViewModelFactory): ViewModelProvider.Factory
}
