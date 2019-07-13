package com.smarttoolfactory.opensky.di


import com.smarttoolfactory.opensky.MapsActivity

import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityContributorModule {

    @ContributesAndroidInjector(modules = [FragmentContributorModule::class])
    abstract fun contributeMapsActivity(): MapsActivity

}
