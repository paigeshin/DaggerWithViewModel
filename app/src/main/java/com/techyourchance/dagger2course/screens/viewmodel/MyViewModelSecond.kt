package com.techyourchance.dagger2course.screens.viewmodel

import androidx.lifecycle.*
import com.techyourchance.dagger2course.questions.FetchQuestionDetailsUseCase
import com.techyourchance.dagger2course.questions.FetchQuestionsUseCase
import com.techyourchance.dagger2course.questions.Question
import kotlinx.coroutines.launch
import java.lang.RuntimeException
import javax.inject.Inject

class MyViewModelSecond @Inject constructor(
        private val fetchQuestionsUseCase: FetchQuestionsUseCase,
        private val fetchQuestionDetailsUseCase: FetchQuestionDetailsUseCase
) : SavedStateViewModel() {

    private lateinit var _questions: MutableLiveData<List<Question>>
    val question: LiveData<List<Question>> get() = _questions

    override fun init(savedStateHandle: SavedStateHandle) {
        _questions = savedStateHandle.getLiveData("questions")
        viewModelScope.launch {
            val result = fetchQuestionsUseCase.fetchLatestQuestions()
            if (result is FetchQuestionsUseCase.Result.Success) {
                _questions.value = result.questions
            } else {
                throw RuntimeException("Fetch Failed")
            }
        }
    }
}