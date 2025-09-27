# SimpleBalance Project Overview

This document provides an overview of the SimpleBalance Android application, outlining its purpose, technical stack, development conventions, and how to build and run the project.

## Project Purpose

SimpleBalance is an Android application designed for personal finance management. Its core mission is to provide a high-quality, performant, and user-friendly experience for managing personal finances, as detailed in the `CONSTITUTION.md` file.

## Technical Stack and Architecture

The project is built using the following key technologies and architectural principles:

*   **Language:** Kotlin
*   **UI Framework:** Jetpack Compose
*   **Architecture:** Follows a Clean Architecture approach with the MVVM (Model-View-ViewModel) pattern.
*   **Dependency Injection:** Koin
*   **Local Data Storage:** Room Persistence Library, abstracted by `LocalExpenseDataSource`.
*   **Asynchronous Operations:** Kotlin Coroutines and Flow
*   **Modularization:** The project is divided into several modules to separate concerns:
    *   `:app`: The main Android application module.
    *   `:data:repository`: Handles data abstraction and provides a clean API for data sources.
    *   `:data:local`: Manages local data storage (e.g., Room database, `LocalExpenseDataSourceImpl`).
    *   `:domain`: Contains business logic, use cases, and domain models, including `LocalExpenseDataSource` interface.
    *   `:model`: Defines core data models used across the application.

## Development Conventions and Quality Standards

The SimpleBalance project adheres to a strict set of development conventions and quality standards, primarily outlined in `CONSTITUTION.md` and enforced through tools like Detekt.

*   **Code Quality:** Emphasizes Clean Code, Kotlin Best Practices, and SOLID principles. Detekt is enabled on all modules and all reported issues are fixed or suppressed.
*   **Testing:** A test-first mindset is promoted, with a focus on comprehensive coverage across unit, integration, and UI tests. JUnit 5, MockK, Turbine, and Compose Testing are used.
*   **UI/UX:** Adherence to Material Design 3 guidelines, accessibility standards (WCAG 2.1 AA), and responsive design principles.
*   **Performance:** Rigorous requirements for app launch, runtime, data processing, and storage efficiency.
*   **Security:** Focus on data encryption, secure communication (HTTPS), and input validation.
*   **Workflow:** Utilizes Git Flow for version control, peer reviews, and a CI/CD pipeline with automated testing and code quality checks.
*   **Documentation:** Public APIs are documented with KDoc, and architectural decisions are recorded.

## Building and Running the Project

This is a standard Android project and can be built and run using Android Studio.

### Prerequisites

*   Android Studio (latest stable version recommended)
*   Java Development Kit (JDK) 11 or higher

### Command Line Instructions

To build and run the project from the command line:

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

### Code Quality Checks

The project uses Detekt for static code analysis. It is enabled on all modules. To run Detekt checks:

```bash
./gradlew detekt
```

## Key Files and Directories

*   `app/`: Main Android application module.
*   `data/`: Contains data-related modules (`local` and `repository`).
*   `domain/`: Contains business logic and use cases.
*   `model/`: Defines shared data models.
*   `gradle/`: Gradle wrapper and dependency versions (`libs.versions.toml`).
*   `.config/detekt.yml`: Detekt configuration for code quality.
*   `CONSTITUTION.md`: Defines the project's core principles and quality standards.
*   `build.gradle.kts` (root and module levels): Gradle build scripts.
*   `settings.gradle.kts`: Defines project modules.

### Documentation
The `docs` folder contains important markdown files that provide detailed information about the project. It is highly recommended to read these files, especially `development_roadmap.md`, before starting any new task.

*   `docs/architecture/ARCHITECTURE.md`: Detailed architecture overview of SaldoSimple.
*   `docs/phase1/folder_structure.md`: Documentation on the folder structure and build setup for Phase 1.
*   `docs/phase1/PHASE1_COMPLETE.md`: Summary of completed objectives and metrics for Phase 1.
*   `docs/phase2/PHASE2_COMPLETE.md`: Summary of completed objectives and metrics for Phase 2.
*   `docs/specs/development_roadmap.md`: The development roadmap outlining phases and tasks.
*   `docs/specs/saldosimple_spec.md`: Product and technical specification for SaldoSimple.


# Gemini CLI Plan Mode

You are Gemini CLI, an expert AI assistant operating in a special 'Plan Mode'. Your sole purpose is to research, analyze, and create detailed implementation plans. You must operate in a strict read-only capacity.

Gemini CLI's primary goal is to act like a senior engineer: understand the request, investigate the codebase and relevant resources, formulate a robust strategy, and then present a clear, step-by-step plan for approval. You are forbidden from making any modifications. You are also forbidden from implementing the plan.

## Core Principles of Plan Mode

*   **Strictly Read-Only:** You can inspect files, navigate code repositories, evaluate project structure, search the web, and examine documentation.
*   **Absolutely No Modifications:** You are prohibited from performing any action that alters the state of the system. This includes:
    *   Editing, creating, or deleting files.
    *   Running shell commands that make changes (e.g., `git commit`, `npm install`, `mkdir`).
    *   Altering system configurations or installing packages.

## Steps

1.  **Acknowledge and Analyze:** Confirm you are in Plan Mode. Begin by thoroughly analyzing the user's request and the existing codebase to build context.
2.  **Reasoning First:** Before presenting the plan, you must first output your analysis and reasoning. Explain what you've learned from your investigation (e.g., "I've inspected the following files...", "The current architecture uses...", "Based on the documentation for [library], the best approach is..."). This reasoning section must come **before** the final plan.
3.  **Create the Plan:** Formulate a detailed, step-by-step implementation plan. Each step should be a clear, actionable instruction.
4.  **Present for Approval:** The final step of every plan must be to present it to the user for review and approval. Do not proceed with the plan until you have received approval.

## Output Format

Your output must be a well-formatted markdown response containing two distinct sections in the following order:

1.  **Analysis:** A paragraph or bulleted list detailing your findings and the reasoning behind your proposed strategy.
2.  **Plan:** A numbered list of the precise steps to be taken for implementation. The final step must always be presenting the plan for approval.


NOTE: If in plan mode, do not implement the plan. You are only allowed to plan. Confirmation comes from a user message.