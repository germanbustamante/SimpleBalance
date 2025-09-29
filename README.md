# SimpleBalance Project

## Overview

SimpleBalance is an Android application for personal finance management, designed to be minimal, fast, and fully offline. Its primary goal is to provide a high-quality, performant, and user-friendly experience for tracking daily expenses. The application is built with a focus on privacy, speed, and clarity, ensuring that users can manage their finances without accounts, tracking, or ads.

## Core Mission

The core mission of SimpleBalance is to deliver a top-tier, intuitive, and efficient Android application for personal finance management. All development decisions are guided by the principles outlined in the [SimpleBalance Project Constitution](CONSTITUTION.md).

## Technical Stack and Architecture

SimpleBalance is built with a modern Android technology stack and follows Clean Architecture principles to ensure separation of concerns, testability, and maintainability.

- **Language:** Kotlin
- **UI Framework:** Jetpack Compose
- **Architecture:** Clean Architecture with MVVM (Model-View-ViewModel)
- **Dependency Injection:** Koin
- **Local Data Storage:** Room Persistence Library
- **Asynchronous Operations:** Kotlin Coroutines and Flow
- **Modularization:** The project is divided into `app`, `data`, and `domain` modules to separate concerns.

For a detailed overview of the architecture, see the [Architecture Overview](docs/architecture/ARCHITECTURE.md) document.

## Development Conventions and Quality Standards

The project adheres to a strict set of development conventions and quality standards to ensure code quality, consistency, and maintainability.

- **Code Quality:** Clean Code, Kotlin Best Practices, and SOLID principles, enforced with Detekt.
- **Testing:** A test-first mindset with comprehensive coverage using JUnit 5, MockK, and Turbine.
- **UI/UX:** Adherence to Material Design 3 guidelines and accessibility standards (WCAG 2.1 AA).
- **Performance:** Rigorous requirements for app launch, runtime, and data processing.
- **Security:** Focus on data encryption, secure communication, and input validation.

For more details, refer to the [SimpleBalance Project Constitution](CONSTITUTION.md).

## Building and Running the Project

This is a standard Android project and can be built and run using Android Studio.

### Prerequisites

- Android Studio (latest stable version recommended)
- Java Development Kit (JDK) 11 or higher

### Command Line Instructions

1.  **Build the debug APK:**
    ```bash
    ./gradlew assembleDebug
    ```
2.  **Install the debug APK on a connected device or emulator:**
    ```bash
    ./gradlew installDebug
    ```
3.  **Run unit tests:**
    ```bash
    ./gradlew test
    ```
4.  **Run connected Android (instrumented) tests:**
    ```bash
    ./gradlew connectedAndroidTest
    ```
5.  **Run Detekt checks:**
    ```bash
    ./gradlew detekt
    ```

## Key Files and Directories

- `app/`: Main Android application module.
- `data/`: Contains data-related modules (`local` and `repository`).
- `domain/`: Contains business logic and use cases.
- `model/`: Defines shared data models.
- `gradle/`: Gradle wrapper and dependency versions (`libs.versions.toml`).
- `.config/detekt.yml`: Detekt configuration for code quality.
- `CONSTITUTION.md`: Defines the project's core principles and quality standards.
- `docs/`: Contains detailed documentation, including architecture, specifications, and development roadmap.

## Roadmap

The development of SimpleBalance is divided into several phases to ensure an incremental and high-quality delivery. The roadmap includes the following key phases:

1.  **Foundation & Core Data Layer**
2.  **Modularization & Dependency Injection**
3.  **Basic UI Foundation**
4.  **Add/Edit Expense Flow**
5.  **Delete & List Interactions**
6.  **Localization & Polish**
7.  **Testing & Quality Assurance**
8.  **Release Preparation**

For a detailed breakdown of each phase, see the [Development Roadmap](docs/specs/development_roadmap.md).
