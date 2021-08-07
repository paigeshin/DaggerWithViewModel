# DaggerWithViewModel

# v.1.0 - Dependency Injection with ViewModelFactory

- ViewModelActivity

```kotlin
class ViewModelActivity : BaseActivity() {

    @Inject lateinit var screensNavigator: ScreensNavigator
    @Inject lateinit var myViewModelFactory: MyViewModelFactory

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

    class MyViewModelFactory @Inject constructor(
            private val fetchQuestionsUseCaseProvider: Provider<FetchQuestionsUseCase>,
            private val fetchQuestionDetailsUseCaseProvider: Provider<FetchQuestionDetailsUseCase>
    ): ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return MyViewModel(fetchQuestionsUseCaseProvider.get(), fetchQuestionDetailsUseCaseProvider.get()) as T
        }
    }

}
```

- MyViewModel

```kotlin
class MyViewModel @Inject constructor(private val fetchQuestionsUseCase: FetchQuestionsUseCase,
                                      private val fetchQuestionDetailsUseCase: FetchQuestionDetailsUseCase) : ViewModel() {

    private val _questions = MutableLiveData<List<Question>>()
    val question: LiveData<List<Question>> = _questions

    init {
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
```
