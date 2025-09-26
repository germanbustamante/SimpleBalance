# SaldoSimple - Architecture Overview

Version: 1.0.0
Last Updated: 2025-09-26

## Architecture Principles

SaldoSimple follows **Clean Architecture** principles with **MVVM** pattern for the presentation layer, ensuring:
- **Separation of concerns** across distinct layers
- **Testability** through dependency injection and interfaces
- **Maintainability** with clear boundaries and single responsibilities
- **Extensibility** for future features without breaking existing functionality

---

## Layer Structure

```
┌─────────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                       │
│  ┌─────────────────┐    ┌─────────────────────────────────┐ │
│  │   Compose UI    │    │      ViewModels                 │ │
│  │   - Screens     │◄───┤  - HomeViewModel                │ │
│  │   - Components  │    │  - AddExpenseViewModel          │ │
│  │   - Navigation  │    │  - State Management             │ │
│  └─────────────────┘    └─────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
                                     │
                                     ▼
┌─────────────────────────────────────────────────────────────┐
│                     DOMAIN LAYER                            │
│  ┌─────────────────┐    ┌─────────────────────────────────┐ │
│  │   Use Cases     │    │      Models & Interfaces        │ │
│  │  - AddExpense   │    │  - Expense (domain model)       │ │
│  │  - GetMonthly   │    │  - ExpenseRepository (interface)│ │
│  │  - DeleteExp... │    │  - Category                     │ │
│  └─────────────────┘    └─────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
                                     │
                                     ▼
┌─────────────────────────────────────────────────────────────┐
│                      DATA LAYER                             │
│  ┌─────────────────┐    ┌─────────────────────────────────┐ │
│  │  Repository     │    │       Local Database            │ │
│  │  Implementation │    │  - Room Database                │ │
│  │  - Caching      │    │  - ExpenseDao                   │ │
│  │  - Mapping      │    │  - ExpenseEntity                │ │
│  └─────────────────┘    └─────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

---

## Technology Stack

### Core Framework
- **Kotlin** - Primary language with coroutines for async operations
- **Jetpack Compose** - Modern declarative UI toolkit
- **Material 3** - Design system and components
- **Android Architecture Components** - ViewModel, Navigation

### Data Persistence
- **Room Database** - Local SQLite abstraction
- **kotlinx-datetime** - Date/time handling with timezone safety

### Dependency Management
- **Manual DI** for MVP (Hilt-ready architecture for future)
- Constructor injection pattern throughout

### Testing
- **JUnit 5** - Unit testing framework
- **MockK** - Mocking for Kotlin
- **Compose Testing** - UI testing
- **Turbine** - Testing Flow emissions

---

## Package Structure

```
com.germandebustamante.saldosimple/
├── di/                          # Dependency injection setup
├── presentation/
│   ├── ui/
│   │   ├── home/               # Home screen & ViewModel
│   │   ├── expense/            # Add/Edit expense screens
│   │   ├── components/         # Reusable UI components
│   │   └── theme/              # Material 3 theming
│   └── navigation/             # Navigation configuration
├── domain/
│   ├── model/                  # Domain models (Expense, Category)
│   ├── repository/             # Repository interfaces
│   └── usecase/                # Business logic use cases
└── data/
    ├── local/
    │   ├── database/           # Room database & DAOs
    │   ├── entity/             # Room entities
    │   └── mapper/             # Entity ↔ Domain mapping
    └── repository/             # Repository implementations
```

---

## Key Architectural Decisions

### 1. Single Activity Architecture
- **Decision**: One Activity with Compose Navigation
- **Rationale**: Simpler state management, better performance, aligns with modern Android practices
- **Trade-offs**: All navigation logic in one place, shared ViewModel scoping

### 2. Offline-First Data Strategy
- **Decision**: Room as single source of truth, no network layer in MVP
- **Rationale**: Privacy focus, simplicity, fast performance
- **Future**: Network layer can be added without changing interfaces

### 3. Clean Architecture with MVVM
- **Decision**: Strict layer separation with dependency inversion
- **Rationale**: Testability, maintainability, clear separation of concerns
- **Implementation**: Use cases mediate between ViewModels and Repository

### 4. Money Storage Strategy
- **Decision**: Store amounts as Long (cents) to avoid floating-point errors
- **Rationale**: Financial accuracy, consistent calculations
- **Implementation**: Domain layer works with BigDecimal, data layer with Long

### 5. Date Handling Strategy
- **Decision**: kotlinx-datetime with LocalDate for expense dates
- **Rationale**: Type safety, timezone handling, modern API
- **Storage**: Persist as epochDay (Long) for Room efficiency

### 6. State Management Pattern
- **Decision**: Unidirectional data flow with Compose State and Flow
- **Rationale**: Predictable state updates, reactive UI
- **Implementation**: ViewModels expose StateFlow/Flow, UI collects as State

---

## Data Flow

### Adding an Expense
```
1. User taps FAB → Compose UI
2. UI calls ViewModel.addExpense() 
3. ViewModel calls AddExpenseUseCase
4. UseCase validates and calls Repository.insertExpense()
5. Repository maps domain → entity → Room DAO
6. DAO inserts to database
7. Repository emits updated Flow<List<Expense>>
8. ViewModel updates State
9. Compose UI recomposes with new data
```

### Monthly View Updates
```
1. User changes month → Compose UI
2. UI calls ViewModel.loadMonth(yearMonth)
3. ViewModel calls GetMonthlyExpensesUseCase  
4. UseCase calls Repository.getExpensesByMonth()
5. Repository queries Room DAO with date range
6. DAO returns Flow<List<ExpenseEntity>>
7. Repository maps entities → domain models
8. ViewModel updates expenses State and total State
9. Compose UI recomposes with filtered list
```

---

## Dependency Injection Strategy

### MVP Approach (Manual DI)
```kotlin path=null start=null
object AppContainer {
    private val database by lazy { 
        Room.databaseBuilder(context, ExpenseDatabase::class.java, "expenses.db")
            .build() 
    }
    
    private val repository by lazy { 
        ExpenseRepositoryImpl(database.expenseDao()) 
    }
    
    val addExpenseUseCase by lazy { AddExpenseUseCase(repository) }
    val getMonthlyExpensesUseCase by lazy { GetMonthlyExpensesUseCase(repository) }
    // ... other use cases
}
```

### Future Koin Migration
- Replace manual DI with Koin modules
- No changes needed in business logic layer
- Repository interfaces remain unchanged

---

## Testing Strategy by Layer

### Domain Layer (Unit Tests)
```kotlin path=null start=null
class AddExpenseUseCaseTest {
    @Test
    fun `given valid expense, when add expense, then expense is saved`() {
        // Arrange: Mock repository
        // Act: Call use case
        // Assert: Verify repository.insertExpense() called
    }
}
```

### Data Layer (Integration Tests)
```kotlin path=null start=null
class ExpenseRepositoryImplTest {
    @Test
    fun `given expenses in database, when get monthly, then returns filtered expenses`() {
        // Arrange: Insert test data to in-memory database
        // Act: Call repository.getExpensesByMonth()
        // Assert: Verify correct expenses returned
    }
}
```

### Presentation Layer (UI Tests)
```kotlin path=null start=null
class AddExpenseScreenTest {
    @Test
    fun `given add expense screen, when amount entered and save clicked, then expense is added`() {
        // Arrange: Set up Compose test rule
        // Act: Enter amount, click save
        // Assert: Verify UI state and ViewModel calls
    }
}
```

---

## Performance Optimizations

### Database Performance
- **Indexing**: Index on date column for monthly queries
- **Query Optimization**: Use specific projections, avoid N+1 queries
- **Background Threading**: All DB operations on IO dispatcher

### UI Performance
- **Lazy Loading**: Use LazyColumn for expense lists
- **State Optimization**: Minimize recompositions with stable keys
- **Memory Management**: Proper lifecycle handling of ViewModels

### Cold Start Optimization
- **Lazy Initialization**: Database and repository creation on-demand
- **Minimal Startup Work**: Move heavy operations off main thread
- **ProGuard**: Code shrinking and obfuscation for smaller APK

---

## Extensibility Points

### 1. Authentication Layer
```kotlin path=null start=null
// Future: Add user context to repository calls
interface ExpenseRepository {
    suspend fun getExpensesByMonth(
        yearMonth: YearMonth,
        userId: String? = null  // Default null for local-only
    ): Flow<List<Expense>>
}
```

### 2. Network Layer
```kotlin path=null start=null
// Future: Compose local + remote data sources
class ExpenseRepositoryImpl(
    private val localDataSource: LocalExpenseDataSource,
    private val remoteDataSource: RemoteExpenseDataSource? = null
) : ExpenseRepository
```

### 3. Custom Categories
```kotlin path=null start=null
// Future: Replace enum with database-backed categories
@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val code: String,
    val name: String,
    val iconName: String,
    val userId: String? = null
)
```

---

## Security Considerations

### Data Protection
- **Local Storage**: Room database with SQLCipher (future enhancement)
- **Input Validation**: All user inputs validated at domain layer
- **No Sensitive Data**: No passwords, tokens, or PII stored

### Privacy by Design
- **No Telemetry**: Zero data collection or tracking
- **Local-Only**: All data processing on device
- **Minimal Permissions**: No network or sensitive permissions required

---

This architecture ensures SaldoSimple remains simple, maintainable, and ready for future enhancements while delivering excellent performance and user experience.