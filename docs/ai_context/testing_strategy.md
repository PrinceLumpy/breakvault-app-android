# Testing Strategy (QA Agent)

## Frameworks
- **Unit Testing:** JUnit 4.
- **Mocking:** MockK (`io.mockk`).
- **Coroutines:** `kotlinx-coroutines-test`.
- **Architecture:** `androidx.arch.core:core-testing` (for LiveData).

## Scope
- **Priority:** `ViewModel` logic (complex calculations, data mapping, import/export).
- **Secondary:** DAOs (integration tests if needed, but usually mocked in Unit tests).

## Test Structure Pattern
1.  **Setup:**
    - Use `InstantTaskExecutorRule` for LiveData.
    - Set `Dispatchers.Main` to `StandardTestDispatcher`.
    - Mock `android.util.Log` (crucial for ViewModel tests).
    - Mock `AppDB` singleton and DAOs using `mockkObject(AppDB)`.

2.  **Execution:**
    - Use `runTest` for coroutine support.
    - Populate `MutableLiveData` mocks to simulate DB updates.
    - Call `advanceUntilIdle()` to ensure Coroutines/LiveData updates propagate.

3.  **Verification:**
    - Assert return values.
    - Verify DAO calls were made (`coVerify`).

## Coverage
- **MoveViewModel**: Core logic for combo generation, filters, and data export/import.
- **BattleViewModel**: Battle session management, timer logic, and state updates.
- **GoalViewModel**: Goal creation, updating, archiving, and stage progress logic (including clamping and parent timestamp updates).

## Example Template
```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class MoveViewModelTest {
    @get:Rule val rule = InstantTaskExecutorRule()
    private val testDispatcher = StandardTestDispatcher()
    
    // Mocks
    private val app = mockk<Application>(relaxed = true)
    private val db = mockk<AppDB>()
    private val dao = mockk<MoveTagDao>(relaxed = true)
    
    // LiveData Sources
    private val movesLiveData = MutableLiveData<List<MoveWithTags>>()

    @Before fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        // 1. Mock Logging to prevent "Method not mocked" crash
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0
        // ... add others if needed (i, w, v)

        // 2. Mock Database Singleton
        mockkObject(AppDB)
        every { AppDB.getDatabase(any()) } returns db
        every { db.moveTagDao() } returns dao
        
        // 3. Mock Data Sources
        every { dao.getMovesWithTags() } returns movesLiveData
    }
    
    @After fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test fun `test function logic`() = runTest {
        // Given
        val viewModel = MoveViewModel(app)
        movesLiveData.value = listOf(/* ... test data ... */)
        advanceUntilIdle() // Wait for LiveData to update

        // When
        viewModel.doSomething()

        // Then
        coVerify { dao.insertSomething(any()) }
    }
}
```
