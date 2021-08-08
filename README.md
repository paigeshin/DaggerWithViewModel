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

# v.2.0 - Refactoring in order not to provide more arguments

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

    class MyViewModelFactory @Inject constructor(
            private val myViewModelProvider: Provider<MyViewModel>
    ): ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return myViewModelProvider.get() as T
        }
    }

}
```

- ViewModelActivity

```kotlin
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
```

# v.3.0 - Centralized Factory for ViewModels

### ViewModelFactory.kt

```kotlin
class ViewModelFactory @Inject constructor(
        private val myViewModelProvider: Provider<MyViewModel>,
        private val myViewModelProviderSecond: Provider<MyViewModelSecond>
): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return when(modelClass) {
            MyViewModel::class.java -> myViewModelProvider.get() as T
            MyViewModelSecond::class.java -> myViewModelProviderSecond.get() as T
            else -> throw RuntimeException("Unsupported ViewModel Type: $modelClass")
        }
    }
}
```

⇒ This basically acts like BaseViewModelFactory

### MyViewModel.kt

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

### MyViewModelSecond.kt

```kotlin
class MyViewModelSecond @Inject constructor(private val fetchQuestionsUseCase: FetchQuestionsUseCase,
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

### ViewModelActivity.kt

```kotlin
class ViewModelActivity : BaseActivity() {

    @Inject lateinit var screensNavigator: ScreensNavigator
    @Inject lateinit var myViewModelFactory: ViewModelFactory

    private lateinit var myViewModel: MyViewModel
    private lateinit var myViewModelSecond: MyViewModelSecond

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
        myViewModelSecond = ViewModelProvider(this, myViewModelFactory).get(MyViewModelSecond::class.java)

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
```

# @Binds

- this @Binds annotation bascially maps from one type to another

# v.4.0, MultiBinding

⇒ Not recommended to use.

⇒ Very complex convention. But you can use it.

### Define `ViewModelKey.kt`

```kotlin
//KClass<out ViewModel>  => I will return Kotlin Class and something that subclasses ViewModel
@MapKey
annotation class ViewModelKey(val value: KClass<out ViewModel>) {

}
```

### Create ViewModelsModule

```kotlin
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
    abstract fun myViewModel2(myViewModelSecond: MyViewModelSecond): ViewModel

}
```

### PresentationComponent.kt

```kotlin
@PresentationScope
@Subcomponent(modules = [ViewModelsModule::class])
interface PresentationComponent {
    fun inject(fragment: QuestionsListFragment)
    fun inject(activity: QuestionDetailsActivity)
    fun inject(questionsListActivity: QuestionsListActivity)
    fun inject(viewModelActivity: ViewModelActivity)
}
```

### Refactor ViewModelFactory

- Before refactoring

```kotlin
class ViewModelFactory @Inject constructor(
        private val myViewModelProvider: Provider<MyViewModel>,
        private val myViewModelProviderSecond: Provider<MyViewModelSecond>
): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return when(modelClass) {
            MyViewModel::class.java -> myViewModelProvider.get() as T
            MyViewModelSecond::class.java -> myViewModelProviderSecond.get() as T
            else -> throw RuntimeException("Unsupported ViewModel Type: $modelClass")
        }
    }
}
```

- After refactoring

```kotlin
class ViewModelFactory @Inject constructor(
        private val providers: Map<Class<out ViewModel>, @JvmSuppressWildcards Provider<ViewModel>>  // Input & Output
): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        val provider = providers[modelClass]
        return provider?.get() as T ?: throw RuntimeException("Unsupported viewmodel type: $modelClass")
    }
}
```

### Dagger Conventions - Multi-binding

- @IntoMap annotation can be used to bind multiple services of the same type into Map data structure
- Keys of individual services in the Map are defined with a custom annotation, annotated with @MapKey annotation
- Dagger will automatically provide the following Map:

  `Map<Key_Type, Provider<Service_Type>>`

- Use @JvmSupressWildcards at injection site to make it work in Kotlin

# v.5.0 - ViewModel with SavedState

⇒ Save ViewModel State

### Dependency

```kotlin
implementation 'androidx.lifecycle:lifecycle-viewmodel-savedstate:2.2.0'
```

### ViewModel with additional argument, `private val savedStateHandle: SavedStateHandle`

```kotlin
class MyViewModel @Inject constructor(
        private val fetchQuestionsUseCase: FetchQuestionsUseCase,
        private val fetchQuestionDetailsUseCase: FetchQuestionDetailsUseCase,
        private val savedStateHandle: SavedStateHandle
) : ViewModel() {

//    private val _questions = MutableLiveData<List<Question>>()

    private val _questions: MutableLiveData<List<Question>> = savedStateHandle.getLiveData("questions")
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

### Refactor ViewModelFactory.kt

- Before refactoring

```kotlin
class ViewModelFactory @Inject constructor(
        private val myViewModelProvider: Provider<MyViewModel>,
        private val myViewModelProviderSecond: Provider<MyViewModelSecond>
): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return when(modelClass) {
            MyViewModel::class.java -> myViewModelProvider.get() as T
            MyViewModelSecond::class.java -> myViewModelProviderSecond.get() as T
            else -> throw RuntimeException("Unsupported ViewModel Type: $modelClass")
        }
    }
}
```

⇒ This implements `ViewModelProvider.Factory`

- after refactoring

```kotlin
class ViewModelFactory @Inject constructor(
        private val fetchQuestionDetailsUseCaseProvider: Provider<FetchQuestionDetailsUseCase>,
        private val fetchQuestionsUseCaseProvider: Provider<FetchQuestionsUseCase>,
        savedStateRegistryOwner: SavedStateRegistryOwner
) : AbstractSavedStateViewModelFactory(savedStateRegistryOwner , null) {

    override fun <T : ViewModel?> create(key: String, modelClass: Class<T>, handle: SavedStateHandle): T {
        return when(modelClass) {
            MyViewModel::class.java -> MyViewModel(fetchQuestionsUseCaseProvider.get(), fetchQuestionDetailsUseCaseProvider.get(), handle) as T
            MyViewModelSecond::class.java -> MyViewModel(fetchQuestionsUseCaseProvider.get(), fetchQuestionDetailsUseCaseProvider.get(), handle) as T
            else -> throw RuntimeException("Unknown ViewModel")
        }
    }
}
```

⇒ This extends `AbstractSavedStateViewModelFactory`

### Create PresentationModule.kt to provide savedStateRegisterOwner

```kotlin
@Module
class PresentationModule(private val savedStateRegistryOwner: SavedStateRegistryOwner) {

    @Provides
    fun savedStateRegistryOwner() =savedStateRegistryOwner

}
```

### Provide it as Module

```kotlin
@PresentationScope
@Subcomponent(modules = [PresentationModule::class])
interface PresentationComponent {
    fun inject(fragment: QuestionsListFragment)
    fun inject(activity: QuestionDetailsActivity)
    fun inject(questionsListActivity: QuestionsListActivity)
    fun inject(viewModelActivity: ViewModelActivity)
}
```

### Refactor all initialization

```kotlin
open class BaseActivity: AppCompatActivity() {

    private val appComponent get() = (application as MyApplication).appComponent

    val activityComponent by lazy {
        appComponent.newActivityComponentBuilder()
                .activity(this)
                .build()
    }

    private val presentationComponent by lazy {
        activityComponent.newPresentationComponent(PresentationModule(this))
    }

    protected val injector get() = presentationComponent
}
```

```kotlin
open class BaseDialog: DialogFragment() {

    private val presentationComponent by lazy {
        (requireActivity() as BaseActivity).activityComponent.newPresentationComponent(PresentationModule(this))
    }

    protected val injector get() = presentationComponent
}
```

```kotlin
open class BaseFragment: Fragment() {

    private val presentationComponent by lazy {
        (requireActivity() as BaseActivity).activityComponent.newPresentationComponent(PresentationModule(this))
    }

    protected val injector get() = presentationComponent
}
```

⇒ PresentationModule(this)

# v.6.0 - Why ViewModel is So Complex

### Why ViewModel is that Complex?

- Instead of Injecting ViewModel, inject ViewModel Factory
- Violation of the Low of Demeter
- Injection of runtime data structure into constructor (savedStateHandle)

# v.7.0 - Simplification of ViewModel with SavedState

### ViewModel's sources of complexity

- Extended Lifecycle **by Framework Design**
- Violation of the law of Demeter **by Framework Design**
- Injection of SavedStateHandle data structure into constructor **(Accidental)**

### Create `SavedStateViewModel`

```kotlin
abstract class SavedStateViewModel: ViewModel() {
    abstract fun init(savedStateHandle: SavedStateHandle)
}
```

### Refactor ViewModel

- Before refactoring

````kotlin
```kotlin
class ViewModelFactory @Inject constructor(
        private val fetchQuestionDetailsUseCaseProvider: Provider<FetchQuestionDetailsUseCase>,
        private val fetchQuestionsUseCaseProvider: Provider<FetchQuestionsUseCase>,
        savedStateRegistryOwner: SavedStateRegistryOwner
) : AbstractSavedStateViewModelFactory(savedStateRegistryOwner , null) {

    override fun <T : ViewModel?> create(key: String, modelClass: Class<T>, handle: SavedStateHandle): T {
        return when(modelClass) {
            MyViewModel::class.java -> MyViewModel(fetchQuestionsUseCaseProvider.get(), fetchQuestionDetailsUseCaseProvider.get(), handle) as T
            MyViewModelSecond::class.java -> MyViewModel(fetchQuestionsUseCaseProvider.get(), fetchQuestionDetailsUseCaseProvider.get(), handle) as T
            else -> throw RuntimeException("Unknown ViewModel")
        }
    }
}
````

````

- After refactoring

```kotlin
class MyViewModel @Inject constructor(
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
````

### Create ViewModelKey, ViewModelModule

```kotlin
@MapKey
annotation class ViewModelKey(val value: KClass<out ViewModel>) {
}

@Module
abstract class ViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(MyViewModel::class)
    abstract fun myViewModel(myViewModel: MyViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MyViewModelSecond::class)
    abstract fun myViewModelSecond(myViewModelSecond: MyViewModelSecond): ViewModel

}

@PresentationScope
@Subcomponent(modules = [PresentationModule::class, ViewModelModule::class])
interface PresentationComponent {
    fun inject(fragment: QuestionsListFragment)
    fun inject(activity: QuestionDetailsActivity)
    fun inject(questionsListActivity: QuestionsListActivity)
    fun inject(viewModelActivity: ViewModelActivity)
}
```

### Refactor ViewModelFactory

- Before refactoring

```kotlin
class ViewModelFactory @Inject constructor(
        private val fetchQuestionDetailsUseCaseProvider: Provider<FetchQuestionDetailsUseCase>,
        private val fetchQuestionsUseCaseProvider: Provider<FetchQuestionsUseCase>,
        savedStateRegistryOwner: SavedStateRegistryOwner
) : AbstractSavedStateViewModelFactory(savedStateRegistryOwner , null) {

    override fun <T : ViewModel?> create(key: String, modelClass: Class<T>, handle: SavedStateHandle): T {
        return when(modelClass) {
            MyViewModel::class.java -> MyViewModel(fetchQuestionsUseCaseProvider.get(), fetchQuestionDetailsUseCaseProvider.get(), handle) as T
            MyViewModelSecond::class.java -> MyViewModelSecond(fetchQuestionsUseCaseProvider.get(), fetchQuestionDetailsUseCaseProvider.get(), handle) as T
            else -> throw RuntimeException("Unknown ViewModel")
        }
    }
}
```

- After refactoring

```kotlin
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
```
