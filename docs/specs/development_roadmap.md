# SaldoSimple - Development Roadmap

Version: 1.0.0
Created: 2025-09-26

## Implementation Strategy

This roadmap breaks down the SaldoSimple development into logical phases, ensuring we deliver a working MVP incrementally while maintaining code quality and extensibility.

---

## Phase 1: Foundation & Core Data Layer (Week 1-2)

### Goals
- Establish project structure and architecture
- Implement data models and database
- Set up dependency injection foundation
- Create basic domain layer

### Tasks
1. **Project Setup**
   - Configure build.gradle.kts for required dependencies
   - Set up folder structure according to clean architecture
   - Configure ProGuard and build optimization
   - Set up detekt, lint, and formatting rules

2. **Dependencies Setup**
   ```kotlin
   // Core dependencies to add to app/build.gradle.kts
   implementation("androidx.room:room-runtime:2.6.1")
   implementation("androidx.room:room-ktx:2.6.1")
   kapt("androidx.room:room-compiler:2.6.1")
   
   implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0")
   implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
   implementation("androidx.navigation:navigation-compose:2.7.6")
   
   // Testing
   testImplementation("androidx.room:room-testing:2.6.1")
   testImplementation("app.cash.turbine:turbine:1.0.0")
   testImplementation("io.mockk:mockk:1.13.8")
   ```

3. **Data Models**
   - Create `ExpenseEntity` for Room
   - Create domain `Expense` model
   - Create `Category` enum/data class
   - Implement mappers between entity and domain models

4. **Database Setup**
   - Room database configuration
   - ExpenseDao with CRUD operations
   - Database version 1 schema
   - Basic queries for monthly filtering

5. **Repository Pattern**
   - `ExpenseRepository` interface (domain layer)
   - `ExpenseRepositoryImpl` (data layer)
   - Basic CRUD operations with Flow return types

6. **Initial Use Cases**
   - `AddExpenseUseCase`
   - `GetMonthlyExpensesUseCase`
   - `GetMonthlyTotalUseCase`

### Deliverables
- ✅ Compiling project with clean architecture structure
- ✅ Working database with basic CRUD operations
- ✅ Unit tests for data layer (80%+ coverage)
- ✅ Repository pattern implementation

### Success Criteria
- Can insert and retrieve expenses from database
- All unit tests pass
- No lint or detekt violations

---

## Phase 2: Modularization & Dependency Injection (Week 2)

### Goals
- Modularize the codebase into `data` and `domain` layers for better separation of concerns.
- Integrate Koin as the dependency injection framework.
- Replace the manual `AppContainer` with a more robust and scalable Koin setup.

### Tasks
1.  **Module Creation & Code Migration**
    - Create `data` and `domain` Gradle modules.
    - Move data-layer classes (repositories, DAOs, database, entities) from the `app` module to the `data` module.
    - Move domain-layer classes (use cases, repository interfaces, domain models) from the `app` module to the `domain` module.
    - Update `build.gradle.kts` files to declare module dependencies (e.g., `app` depends on `data` and `domain`).

2.  **Koin Integration**
    - Add Koin dependencies (`koin-android`, `koin-androidx-compose`) to the project.
    - Create a `di` package within the `app` module.
    - Define Koin modules (`appModule`, `dataModule`, `domainModule`) to provide dependencies for each layer.
    - Initialize Koin in a custom `Application` class.

3.  **Replace Manual DI**
    - Safely remove the `AppContainer.kt` file.
    - Refactor all call sites that used `AppContainer` to resolve dependencies via Koin injection.

4.  **Detekt Activation and Fixing**
    - Enable Detekt in `app/build.gradle.kts` and other relevant modules.
    - Run `./gradlew detekt` to identify code quality issues.
    - Fix all reported Detekt violations to ensure adherence to code quality standards.

5.  **Introduce LocalExpenseDataSource Abstraction**
    - Create `LocalExpenseDataSource` interface in the `domain` module.
    - Create `LocalExpenseDataSourceImpl` in the `data:local` module, implementing `LocalExpenseDataSource` and injecting `ExpenseDao`.
    - Update `ExpenseRepositoryImpl` to depend on `LocalExpenseDataSource` instead of `ExpenseDao`.
    - Update Koin `dataModule` to provide `LocalExpenseDataSourceImpl` as `LocalExpenseDataSource`.

### Deliverables
- ✅ Codebase successfully modularized into `app`, `data`, and `domain` layers.
- ✅ Koin is integrated and provides all dependencies for the application.
- ✅ The manual `AppContainer` is completely removed.
- ✅ The project compiles and all existing tests pass.
- ✅ Detekt is enabled on all modules and all reported issues are fixed or suppressed.
- ✅ LocalExpenseDataSource abstraction is implemented and integrated.

### Success Criteria
- Improved build times due to modularization.
- Dependencies are managed by Koin and can be easily swapped for testing.
- The overall architecture is more scalable and maintainable.
- All modules are compliant with Detekt code quality standards.
- Data access layer is further decoupled through LocalExpenseDataSource.

---

## Phase 3: Basic UI Foundation (Week 2-3)

### Goals
- Set up Compose navigation
- Create basic home screen with month selector
- Implement expense list display
- Add basic theming and Material 3 setup

### Tasks
1. **Navigation Setup**
   - Single-activity with Compose Navigation
   - Define routes for Home and Add/Edit screens
   - Set up navigation host

2. **Home Screen Foundation**
   - Month selector (previous/next buttons)
   - Monthly total display
   - Empty state for no expenses
   - Basic list layout for expenses

3. **Theming and Design System**
   - Material 3 theme setup
   - Define color scheme
   - Typography scale
   - Category icons mapping

4. **Basic ViewModels**
   - `HomeViewModel` with monthly data loading
   - State management with Compose State
   - Basic error handling

### Deliverables
- ✅ Home screen displaying monthly expenses
- ✅ Working month navigation
- ✅ Material 3 theming applied
- ✅ Basic state management

### Success Criteria
- App launches and shows home screen
- Can navigate between months
- Displays sample expense data correctly
- Follows Material 3 design guidelines

---

## Phase 4: Add/Edit Expense Flow (Week 3-4)

### Goals
- Implement add expense functionality
- Create edit expense capability
- Achieve ≤3 taps for adding expense
- Add form validation and user feedback

### Tasks
1. **Add Expense Screen/Dialog**
   - Bottom sheet or dialog for adding expenses
   - Amount input with numeric keyboard
   - Date picker (default to today)
   - Category selector with chips/dropdown
   - Optional note field

2. **Edit Expense Functionality**
   - Navigate to edit from list item tap
   - Pre-populate existing values
   - Same validation as add flow

3. **Form Validation**
   - Amount > 0 validation
   - Category selection validation
   - Note length limit (200 chars)
   - Real-time validation feedback

4. **User Experience Optimizations**
   - Remember last used category
   - Auto-focus amount field
   - Quick category switching
   - Keyboard optimization

5. **Use Cases Implementation**
   - `UpdateExpenseUseCase`
   - `GetLastUsedCategoryUseCase`
   - `ValidateExpenseUseCase`

### Deliverables
- ✅ Working add expense flow (≤3 taps)
- ✅ Edit expense functionality
- ✅ Form validation with user feedback
- ✅ Optimized UX for quick input

### Success Criteria
- Can add expense in ≤3 taps with defaults
- Form validation prevents invalid data
- Edit flow pre-populates correctly
- Last used category is remembered

---

## Phase 5: Delete & List Interactions (Week 4-5)

### Goals
- Implement swipe-to-delete functionality
- Add undo capability
- Enhance list display and grouping
- Improve overall list UX

### Tasks
1. **Delete Functionality**
   - Swipe-to-delete gesture
   - Delete confirmation with Undo Snackbar
   - 5-second undo timeout
   - `DeleteExpenseUseCase`

2. **List Enhancements**
   - Group expenses by day within month
   - Sticky day headers
   - Proper expense item layout
   - Category icons in list items

3. **List Interactions**
   - Tap to edit expense
   - Swipe gestures
   - Loading states
   - Error states handling

4. **State Management**
   - Undo operation state
   - List refresh after operations
   - Optimistic updates for better UX

### Deliverables
- ✅ Swipe-to-delete with undo functionality
- ✅ Enhanced list display with grouping
- ✅ Smooth list interactions
- ✅ Proper state management

### Success Criteria
- Can delete expenses with undo option
- List groups expenses by day correctly
- All interactions feel responsive
- No data loss during operations

---

## Phase 6: Localization & Polish (Week 5-6)

### Goals
- Implement English and Spanish localization
- Add proper currency and date formatting
- Enhance accessibility
- Performance optimization

### Tasks
1. **Localization Setup**
   - Extract all strings to resources
   - Create Spanish translations (values-es)
   - Implement currency formatting per locale
   - Date formatting based on locale

2. **Accessibility Improvements**
   - Content descriptions for all interactive elements
   - Minimum 48dp touch targets
   - TalkBack announcements
   - Color contrast validation

3. **Performance Optimization**
   - Database query optimization
   - List performance for large datasets
   - Memory usage optimization
   - Cold start time optimization

4. **Error Handling & Empty States**
   - Comprehensive error handling
   - User-friendly error messages
   - Empty state illustrations
   - Loading states

### Deliverables
- ✅ Full English/Spanish localization
- ✅ Accessibility compliance
- ✅ Performance targets met
- ✅ Polish and error handling

### Success Criteria
- App works correctly in both languages
- Meets accessibility standards
- Performance targets achieved (cold start <2s)
- Graceful error handling throughout

---

## Phase 7: Testing & Quality Assurance (Week 6-7)

### Goals
- Comprehensive test coverage
- UI testing for critical flows
- Performance testing
- Release preparation

### Tasks
1. **Unit Testing**
   - ViewModels unit tests
   - Use cases unit tests
   - Repository tests with in-memory DB
   - Mapper tests
   - Target: 80%+ coverage on domain/data layers

2. **UI Testing**
   - Compose testing for critical user flows
   - Add expense flow test
   - Delete with undo test
   - Month navigation test
   - Accessibility testing

3. **Integration Testing**
   - End-to-end user scenarios
   - Database operations under load
   - Locale switching tests
   - State preservation tests

4. **Performance Testing**
   - Cold start benchmarking
   - List scrolling performance
   - Memory usage profiling
   - Database query performance

5. **Quality Assurance**
   - Manual testing on various devices
   - Different screen sizes testing
   - Locale testing
   - Edge case testing

### Deliverables
- ✅ 80%+ test coverage
- ✅ All critical flows tested
- ✅ Performance benchmarks met
- ✅ Quality issues resolved

### Success Criteria
- All tests pass consistently
- Performance requirements met
- No critical bugs or crashes
- Ready for release

---

## Phase 8: Release Preparation (Week 7-8)

### Goals
- Prepare for initial release
- Documentation completion
- Release configuration
- Store listing preparation

### Tasks
1. **Release Configuration**
   - ProGuard configuration for release
   - Signing configuration
   - Version management setup
   - Release build testing

2. **Documentation**
   - User documentation
   - API documentation (if applicable)
   - Setup instructions
   - Troubleshooting guide

3. **Store Preparation**
   - App icons and screenshots
   - Store listing copy (EN/ES)
   - Privacy policy
   - Beta testing preparation

4. **Final Polish**
   - Last-minute bug fixes
   - Performance fine-tuning
   - UI polish and animations
   - Final accessibility review

### Deliverables
- ✅ Release-ready APK
- ✅ Complete documentation
- ✅ Store listing materials
- ✅ Beta testing setup

### Success Criteria
- App passes all release criteria
- Documentation is complete
- Ready for store submission
- Beta testing infrastructure ready

---

## Post-MVP: Future Phases

### Phase 9: Settings & Export (Future)
- Settings screen
- Export/Import functionality
- Language selection override
- App preferences

### Phase 10: Enhanced Categories (Future)
- Custom category creation
- Category management
- Category icons customization
- Category analytics

### Phase 11: Authentication & Sync (Future)
- Optional user accounts
- Cloud synchronization
- Multi-device support
- Data backup/restore

---

## Risk Mitigation

### Technical Risks
1. **Performance Issues**
   - Mitigation: Early performance testing, proper indexing
   
2. **Locale Formatting Complexity**
   - Mitigation: Use system formatting APIs, thorough testing

3. **Database Migration Complexity**
   - Mitigation: Design schema with future in mind, test migrations

### Schedule Risks
1. **Scope Creep**
   - Mitigation: Strict MVP definition, feature freeze after Phase 6
   
2. **Quality Issues**
   - Mitigation: Continuous testing, early quality gates

### Success Metrics

#### MVP Release Criteria
- [ ] All core features implemented (CRUD, monthly view, localization)
- [ ] Performance targets met (cold start <2s, 60fps scrolling)
- [ ] Test coverage ≥80% on domain/data layers
- [ ] Zero crashes in smoke testing
- [ ] Accessibility standards met
- [ ] Both languages working correctly

#### Key Performance Indicators
- Cold start time: <2 seconds
- Add expense flow: ≤3 taps
- Memory usage: <100MB typical
- Battery impact: Minimal (offline app)
- User satisfaction: Focus on simplicity and speed

This roadmap ensures a systematic approach to building SaldoSimple while maintaining quality and extensibility for future enhancements.