package com.techyourchance.dagger2course.screens.viewmodel

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.techyourchance.dagger2course.R
import com.techyourchance.dagger2course.questions.FetchQuestionDetailsUseCase
import com.techyourchance.dagger2course.questions.FetchQuestionsUseCase
import com.techyourchance.dagger2course.screens.common.ScreensNavigator
import com.techyourchance.dagger2course.screens.common.activities.BaseActivity
import com.techyourchance.dagger2course.screens.common.dialogs.DialogsNavigator
import com.techyourchance.dagger2course.screens.common.toolbar.MyToolbar
import com.techyourchance.dagger2course.screens.common.viewsmvc.ViewMvcFactory
import kotlinx.coroutines.*
import javax.inject.Inject
import javax.inject.Provider

class ViewModelActivity : BaseActivity() {

    @Inject lateinit var screensNavigator: ScreensNavigator
    @Inject lateinit var myViewModelFactory: MyViewModel.MyViewModelFactory

    private lateinit var myViewModel: MyViewModel

    private lateinit var toolbar: MyToolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        injector.inject(this)
        super.onCreate(savedInstanceState)

        setContentView(R.layout.layout_view_model)

        toolbar = findViewById(R.id.toolbar)
        toolbar.setNavigateUpListener {
            screensNavigator.navigateBack()
        }

        myViewModel = ViewModelProvider(this, myViewModelFactory).get(MyViewModel::class.java)

        myViewModel.question.observe(this, Observer { questions ->
            Toast.makeText(this, "fetched ${questions.size} questions", Toast.LENGTH_SHORT).show()
        })
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, ViewModelActivity::class.java)
            context.startActivity(intent)
        }
    }



}