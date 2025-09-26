# SaldoSimple — Product and Technical Specification

Version: 1.0.0
Last updated: 2025-09-26
Owner: Product/Engineering

1. Vision and Principles
- Purpose: A minimal, fast, fully-offline expense tracker for everyday use.
- Privacy: No accounts, no tracking, no ads. Data lives locally on device.
- Speed: Add an expense in ≤3 taps; instant app responsiveness.
- Clarity: Simple UI, monthly view with total and categories. No clutter.
- Extensibility: Architecture ready to add auth, custom categories/types, and optional cloud sync without breaking the offline-first experience.

2. Scope (MVP)
- Create, view, edit, delete expenses.
- Fields per expense:
  - amount (EUR, decimal, > 0)
  - date (default today, editable)
  - category (predefined, editable later in future releases)
  - note (optional text)
- Home screen: Monthly list of expenses + monthly total.
- Localization: English (en) and Spanish (es).
- Storage: 100% local (Room). No network required.
- Performance: Cold start < 2s, add expense flow ≤ 3 taps, list scroll at 60fps.

3. Non-Goals (MVP)
- User accounts or authentication.
- Cloud sync or remote backups.
- Recurring transactions (future).
- Budgets, analytics dashboards (future, limited summaries may come later).

4. User Personas & Primary Use Cases
- Persona A: “Quick Adder” — wants to log coffee/groceries right after paying.
  - Goal: Open app, add expense in seconds, close app.
- Persona B: “Monthly Reviewer” — checks month totals and scans entries.
  - Goal: Review monthly total, filter by category, edit any mistakes quickly.

5. UX and Interaction Guidelines
5.1 Navigation
- Single-activity Compose app with bottom or top app bar.
- Home: Month selector (left/right chevrons), month name, monthly total, list below.
- FAB/Add button: prominent; opens Add Expense sheet/dialog.

5.2 Add/Edit Expense Flow (≤3 taps target)
- Default date = today; default category = last used; amount focused on open.
- Keyboard numeric by default; decimal support.
- Tap sequence (ideal): open Add -> type amount -> Save (category defaulted) = 2 actions + typing. Optional edits add taps.
- Quick category chips visible; one tap to change category.

5.3 List
- Grouped by day within the month; day headers sticky.
- Each item: amount (€ aligned right), category icon/name, note (if present), date if not grouped.
- Swipe to delete with undo Snackbar.
- Tap item to edit.

5.4 Localization (en/es)
- All user-facing strings localized.
- Date/time formats localized (use device locale). Currency symbol: €.

5.5 Accessibility
- Minimum 48dp touch targets.
- Content descriptions for icons.
- Sufficient contrast; dynamic type support.
- TalkBack announcements for add/delete/undo.

6. Information Architecture & Data Model
6.1 Entities
- Expense
  - id: Long (auto)
  - amountCents: Long (store in cents to avoid float error)
  - date: LocalDate (persist as epochDay or ISO string)
  - categoryCode: String (e.g., "food", "transport", "shopping", "health", "leisure", "other")
  - note: String? (nullable)
  - createdAt: Instant
  - updatedAt: Instant

- Category (predefined in code for MVP)
  - code: String (stable id)
  - name: Localized via resources (display only)
  - icon: Mapping to Material icon

6.2 Room Schema (v1)
- Tables: expenses
- Indexes: date (for monthly queries), categoryCode
- Migrations: v1 initial. Future: add user/accountId column for auth; add table categories for customization.

Example entities (illustrative):
```kotlin path=null start=null
@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amountCents: Long,
    val epochDay: Long,
    val categoryCode: String,
    val note: String?,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)
```

7. Core Features and Requirements
7.1 Create Expense
- Default values: date=today, category=last used (fallback "other").
- Validation: amount > 0; categoryCode in allowed set; note length ≤ 200.
- Autocomplete last category, remember last used per session/persisted.
- Feedback: Snackbar "Expense added" with Undo.

7.2 Edit Expense
- Load existing values; same validations.
- Feedback: Snackbar "Expense updated".

7.3 Delete Expense
- Swipe-to-delete with Undo; or long-press menu.
- Feedback: Snackbar with Undo 5s.

7.4 Monthly View and Total
- Month selector: arrows and tap month title to open calendar month picker.
- Query: expenses where date in [monthStart, monthEnd].
- Display total in euros with locale formatting.

7.5 Categories (MVP)
- Fixed set; codes: food, transport, shopping, health, leisure, home, income?, other.
- Future: customizable categories/types without migration breakage.

8. Architecture
- Pattern: MVVM + Repository + UseCases; single-activity Compose.
- Layers:
  - Presentation: Compose screens, ViewModels (Kotlin coroutines/Flow, savedStateHandle).
  - Domain: Use cases (AddExpense, UpdateExpense, DeleteExpense, GetMonthlyExpenses, GetMonthlyTotal, GetLastUsedCategory).
  - Data: Room DAO/Entities; mappers to/from domain models.
- DI: Start simple with manual DI. Future-ready for Hilt without breaking APIs.
- Offline-first: No networking in MVP.
- Time handling: java.time (ThreeTenABP on older if needed); store epoch-based ints/longs for Room.

9. Performance Requirements
- Cold start < 2s; warm < 1s on mid-tier device.
- Add expense interaction: first frame < 16ms after tapping FAB; keyboard up within 250ms.
- Scrolling list sustained 60fps for 1,000 items.
- DB operations on IO dispatcher; list queries paginated or batched if needed.

10. Privacy & Security
- No accounts, no telemetry, no ads.
- Data stored locally; optional export/import stays on-device unless user selects a destination.
- Backups: Allow Android Auto Backup unless user opts out in settings (future).
- Sensitive permissions: none beyond storage if export/import (future).

11. Localization and Internationalization
- String resources in values/strings.xml and values-es/strings.xml.
- Numbers, dates via Locale.
- Currency symbol: €, amount stored in cents; formatted as 12,34 € (es-ES), €12.34 (en-IE/GB) depending on locale.

12. Error Handling & Empty States
- Empty month: show friendly illustration/text with CTA to add first expense.
- Errors: show non-technical messages; log locally only during debug builds.
- Undo actions for destructive operations.

13. Telemetry and Logging
- None in release. Debug logs only with feature flags.

14. Settings (Future)
- Language selection override.
- Export/Import local data (JSON/CSV).
- Category management (add/edit/delete with safe migrations).
- Authentication toggle and cloud sync (optional).

15. Extensibility Plan (Non-breaking Evolution)
- Authentication: Add user table and accountId FK to expenses. Default accountId = null for legacy local mode; queries continue to work with COALESCE(accountId, local) mapping.
- Custom Categories: Introduce categories table with user-defined rows; keep categoryCode in expenses as FK; provide migration that maps old codes to seeded rows.
- Cloud Sync: Add sync metadata columns (uuid, updatedAt, deletedAt, syncState). Keep offline as default. Sync module opt-in; repository composed to local+remote without changing UI contracts.

16. Testing Strategy
- Unit: ViewModels, use cases, mappers. 80%+ coverage for domain and data.
- Instrumented: Room DAO with in-memory DB; time edge cases across DST/locale.
- UI: Compose testing for add/edit/delete flows; accessibility checks.
- Performance: Macrobenchmark basic startup and scroll; DB query timings under 100ms for month view.

17. Release Criteria (MVP)
- Feature completeness: CRUD, monthly list/total, localization en/es, swipe delete with undo.
- Performance: Meet Section 9.
- Quality: No crashes in smoke tests; lint/detekt clean; unit coverage ≥ 80% on domain/data.
- Accessibility: Touch targets, labels, contrast validated.

18. Acceptance Criteria (Selected)
- Add Expense ≤ 3 taps when using default category and today’s date.
- Monthly total equals sum of listed expenses (validated by tests).
- Localization: Switching device language toggles all strings; date/currency formats follow locale.
- Offline: Works in airplane mode without degraded UX.

19. Sample Resources (Strings)
- English (values/strings.xml)
  - app_name: SaldoSimple
  - add_expense: Add expense
  - amount: Amount
  - date: Date
  - category: Category
  - note: Note (optional)
  - save: Save
  - cancel: Cancel
  - total: Total
  - this_month: This month
  - undo: Undo
  - deleted: Deleted
- Spanish (values-es/strings.xml)
  - app_name: SaldoSimple
  - add_expense: Añadir gasto
  - amount: Importe
  - date: Fecha
  - category: Categoría
  - note: Nota (opcional)
  - save: Guardar
  - cancel: Cancelar
  - total: Total
  - this_month: Este mes
  - undo: Deshacer
  - deleted: Eliminado

20. Visual Design Notes (Guidance)
- Material 3 with dynamic color; primary actions high-contrast.
- Use category icons: food, local_taxi, shopping_bag, health_and_safety, sports_esports, home, more_horiz.
- Amounts right-aligned; negative style not needed (expenses only). Income optional later.

21. Folder Structure (Proposed)
- app/src/main/java/.../feature/home
- app/src/main/java/.../feature/edit
- app/src/main/java/.../data/db, /data/repo, /data/model
- app/src/main/java/.../domain/usecase
- app/src/main/res/values, values-es, drawable

22. Risks & Mitigations
- Floating point errors — store cents as long.
- Locale formatting quirks — rely on java.text/NumberFormat and java.time.
- Future migrations — design FKs and UUIDs early; write forward-compatible mappers.

23. Open Questions
- Do we include “Income” in MVP? Default is expenses only. If included, add type field.
- Export/import format preference: CSV vs JSON.

Appendix A — Example Use Cases
- UC1: Add coffee expense: Open app -> tap + -> type 2,30 -> Save. Entry appears; total updates.
- UC2: Edit grocery: Tap item -> change amount -> Save -> Snackbar updated.
- UC3: Delete expense: Swipe left -> Snackbar Undo -> tap Undo restores.
