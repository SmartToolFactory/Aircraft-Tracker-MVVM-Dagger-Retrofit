package com.smarttoolfactory.opensky.di

import com.smarttoolfactory.opensky.ui.AircraftMapFragment

import dagger.Module
import dagger.android.ContributesAndroidInjector


/**
 * FragmentContributorModule is used inside ActivityContributorModule
 * With @ContributesAndroidInjector(modules = FragmentContributorModule.class)
 * defines which module will be used to inject objects to declared fragments
 */
@Module
abstract class FragmentContributorModule {

    @ContributesAndroidInjector
    abstract fun contributeMapFragment(): AircraftMapFragment
}

