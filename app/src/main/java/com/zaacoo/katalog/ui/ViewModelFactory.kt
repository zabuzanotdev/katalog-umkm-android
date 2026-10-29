package com.zaacoo.katalog.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory

inline fun <reified T : ViewModel> createFactory(crossinline create: () -> T): ViewModelProvider.Factory =
    viewModelFactory {
        initializer { create() }
    }
