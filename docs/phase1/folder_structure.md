# Phase 1: Folder Structure and Build Setup

## Clean Architecture Package Structure

La estructura de carpetas sigue los principios de Clean Architecture con separación clara de responsabilidades:

```
app/src/main/java/com/germandebustamante/simplebalance/
├── data/                           # Data Layer
│   ├── local/
│   │   ├── database/              # Room database configuration
│   │   ├── entity/                # Room entities (ExpenseEntity)
│   │   └── mapper/                # Entity ↔ Domain model mappers
│   └── repository/                # Repository implementations
├── domain/                         # Domain Layer (Business Logic)
│   ├── model/                     # Domain models (Expense, Category)
│   ├── repository/                # Repository interfaces
│   └── usecase/                   # Use cases (business rules)
├── presentation/                   # Presentation Layer
│   ├── ui/
│   │   ├── home/                  # Home screen composables & ViewModel
│   │   ├── expense/               # Add/Edit expense screens
│   │   ├── components/            # Reusable UI components
│   │   └── theme/                 # Material 3 theming
│   └── navigation/                # Navigation configuration
└── di/                            # Dependency Injection setup
```

## Test Structure

```
app/src/test/java/com/germandebustamante/simplebalance/
├── data/
│   ├── local/                     # Room DAO tests
│   └── repository/                # Repository implementation tests
├── domain/usecase/                # Use case unit tests
└── util/                          # Test utilities and helpers

app/src/androidTest/java/com/germandebustamante/simplebalance/
├── data/                          # Integration tests for database
└── presentation/                  # UI tests with Compose Testing
```

## Build Configuration Changes

### KSP Instead of KAPT

**Decisión:** Usar KSP (Kotlin Symbol Processing) en lugar de KAPT para Room.

**Razón:** 
- KSP es significativamente más rápido que KAPT
- Mejor integración con Kotlin
- Recomendado oficialmente para nuevos proyectos
- Mejor rendimiento de compilación

**Implementación:**
```kotlin
// En gradle/libs.versions.toml
ksp = "2.0.21-1.0.25"

// En app/build.gradle.kts
plugins {
    alias(libs.plugins.ksp)
}

dependencies {
    ksp(libs.androidx.room.compiler)  // KSP en lugar de kapt
}
```

### Namespace y Application ID

**Configurado** como `com.germandebustamante.simplebalance` - nombre técnico del proyecto.

**Nota:** "SaldoSimple" es el nombre de usuario en español, pero el package usa `simplebalance` para consistencia técnica.

### ProGuard Habilitado

**Configuración:** `isMinifyEnabled = true` en release build para optimizar el APK final según los requerimientos de performance.

## Dependency Versions (Phase 1)

### Core Dependencies
- **Room**: 2.6.1 - Base de datos local con KSP
- **kotlinx-datetime**: 0.6.1 - Manejo seguro de fechas y tiempo
- **Compose ViewModel**: 2.8.6 - ViewModels optimizados para Compose
- **Navigation Compose**: 2.8.2 - Navegación declarativa

### Testing Dependencies
- **Turbine**: 1.1.0 - Testing de Flow y StateFlow
- **MockK**: 1.13.12 - Mocking library para Kotlin
- **Coroutines Test**: 1.8.1 - Testing de corrutinas
- **Room Testing**: 2.6.1 - In-memory database para tests

### Code Quality
- **Detekt**: 1.23.6 - Static analysis para Kotlin

## Key Architectural Decisions

### 1. KSP for Room
- **Performance**: ~2x faster compilation compared to kapt
- **Future-proof**: KAPT is being deprecated in favor of KSP
- **Room support**: Officially supported since Room 2.4.0

### 2. Package Structure
- **Domain-centric**: Domain layer no depende de frameworks externos
- **Testability**: Cada capa tiene su estructura de tests correspondiente
- **Separation**: Data y Presentation están completamente separadas

### 3. Naming Convention
- **Entities**: `*Entity` (e.g., `ExpenseEntity`) para Room
- **Domain Models**: Sin sufijo (e.g., `Expense`) para lógica de negocio
- **Use Cases**: `*UseCase` (e.g., `AddExpenseUseCase`)
- **Repositories**: `*Repository` (interface) y `*RepositoryImpl` (implementation)

## Next Steps

1. ✅ Build configuration and dependencies
2. ✅ Folder structure setup
3. 🔄 Implement core data models and entities
4. 🔄 Set up Room database with ExpenseDao
5. 🔄 Implement Repository pattern
6. 🔄 Create initial use cases
7. 🔄 Set up dependency injection
8. 🔄 Write comprehensive tests

## Notes

- Todas las dependencias están configuradas usando el version catalog (`libs.versions.toml`)
- La estructura es extensible para futuras funcionalidades sin romper la arquitectura existente
- Los tests están organizados por tipo (unit, integration, UI) siguiendo las mejores prácticas