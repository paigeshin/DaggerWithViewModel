package com.techyourchance.dagger2course.common.dependnecyinjection

import androidx.lifecycle.ViewModel
import dagger.MapKey
import kotlin.reflect.KClass

//KClass<out ViewModel>  => I will return Kotlin Class and something that subclasses ViewModel
@MapKey
annotation class ViewModelKey(val value: KClass<out ViewModel>) {

}