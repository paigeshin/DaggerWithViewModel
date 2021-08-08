package com.techyourchance.dagger2course.screens.common.viewmodels

import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import com.techyourchance.dagger2course.questions.FetchQuestionDetailsUseCase
import com.techyourchance.dagger2course.questions.FetchQuestionsUseCase
import com.techyourchance.dagger2course.screens.viewmodel.MyViewModel
import com.techyourchance.dagger2course.screens.viewmodel.MyViewModelSecond
import com.techyourchance.dagger2course.screens.viewmodel.SavedStateViewModel
import java.lang.RuntimeException
import javax.inject.Inject
import javax.inject.Provider

//class ViewModelFactory @Inject constructor(
//        private val fetchQuestionDetailsUseCaseProvider: Provider<FetchQuestionDetailsUseCase>,
//        private val fetchQuestionsUseCaseProvider: Provider<FetchQuestionsUseCase>,
//        savedStateRegistryOwner: SavedStateRegistryOwner
//) : AbstractSavedStateViewModelFactory(savedStateRegistryOwner , null) {
//
//    override fun <T : ViewModel?> create(key: String, modelClass: Class<T>, handle: SavedStateHandle): T {
//        return when(modelClass) {
//            MyViewModel::class.java -> MyViewModel(fetchQuestionsUseCaseProvider.get(), fetchQuestionDetailsUseCaseProvider.get(), handle) as T
//            MyViewModelSecond::class.java -> MyViewModelSecond(fetchQuestionsUseCaseProvider.get(), fetchQuestionDetailsUseCaseProvider.get(), handle) as T
//            else -> throw RuntimeException("Unknown ViewModel")
//        }
//    }
//}

class ViewModelFactory @Inject constructor(
        private val providersMap: Map<Class<out ViewModel>, @JvmSuppressWildcards Provider<ViewModel>>,
        savedStateRegistryOwner: SavedStateRegistryOwner
) : AbstractSavedStateViewModelFactory(savedStateRegistryOwner , null) {

    override fun <T : ViewModel?> create(key: String, modelClass: Class<T>, handle: SavedStateHandle): T {
//        val viewModel =  when(modelClass) {
//            MyViewModel::class.java -> MyViewModel(fetchQuestionsUseCaseProvider.get(), fetchQuestionDetailsUseCaseProvider.get()) as T
//            MyViewModelSecond::class.java -> MyViewModelSecond(fetchQuestionsUseCaseProvider.get(), fetchQuestionDetailsUseCaseProvider.get()) as T
//            else -> throw RuntimeException("Unknown ViewModel")
//        }
        val provider = providersMap[modelClass]
        val viewModel = provider?.get() ?: throw RuntimeException("unsupported viewmodel type: $modelClass")
        if(viewModel is SavedStateViewModel) {
            viewModel.init(handle)
        }
        return viewModel as T
    }
}