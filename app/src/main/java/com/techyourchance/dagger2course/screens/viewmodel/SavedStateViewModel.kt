package com.techyourchance.dagger2course.screens.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

abstract class SavedStateViewModel: ViewModel() {
    abstract fun init(savedStateHandle: SavedStateHandle)
}