package com.techyourchance.dagger2course.common.dependnecyinjection.presentation

import androidx.lifecycle.ViewModel
import com.techyourchance.dagger2course.common.dependnecyinjection.ViewModelKey
import com.techyourchance.dagger2course.screens.viewmodel.MyViewModel
import com.techyourchance.dagger2course.screens.viewmodel.MyViewModelSecond
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ViewModelsModule {

    //Remember this Binds annotation bascially maps from one type to another
    @Binds
    @IntoMap
    @ViewModelKey(MyViewModel::class)
    abstract fun myViewModel(myViewModel: MyViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MyViewModelSecond::class)
    abstract fun myViewModelSecond(myViewModelSecond: MyViewModelSecond): ViewModel

}