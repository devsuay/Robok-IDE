package org.gampiot.robok.feature.util.di

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile

import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.singleOf
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

import org.gampiot.robok.feature.settings.compose.viewmodels.AppPreferencesViewModel
import org.gampiot.robok.feature.settings.compose.repositories.AppPreferencesRepository
import org.gampiot.robok.feature.settings.compose.screens.ui.rdkmanager.ConfigureRDKViewModel

const val APP_PREFERENCES = "app_preferences"

val appModule = module {
    singleOf(::AppPreferencesRepository)
    viewModelOf(::AppPreferencesViewModel)
    viewModelOf(::ConfigureRDKViewModel)
}

val appPreferencesModule = module { 
    single {
        PreferenceDataStoreFactory.create {
             androidContext().preferencesDataStoreFile(APP_PREFERENCES)
        }
    }
}