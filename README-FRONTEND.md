# Manikandan — Frontend Integration & Dashboard

This module connects the existing Spring Boot controllers and MySQL-backed modules through one responsive Thymeleaf interface. There is no separate frontend server or JavaScript framework.

## Completed work

- Home dashboard with live workspace counts and links to recently added problems.
- Shared navigation, footer, stylesheet, and feedback messages across all pages.
- Searchable problem bank, problem details, create/edit forms, and a link to solve the selected problem.
- Code submission form with language selection, source character count, retained input on validation errors, and correct problem preselection.
- Submission history and results styled consistently with the dashboard.
- Registration, login, and profile pages using the existing user controller.
- Responsive layouts, keyboard focus, skip navigation, labeled inputs, error messages, and empty/error states.

## Open the local demo

Install JDK 21 or newer and make sure `JAVA_HOME` points to that JDK. The project compiles for Java 21; the older main README's Java 25 requirement is not the compiler target.

From this repository, on Windows PowerShell:

```powershell
.\run-preview.cmd
```

On macOS/Linux:

```sh
sh run-preview.sh
```

Open <http://127.0.0.1:8081/>. Leave the terminal running. Press Ctrl+C to stop. This opt-in preview uses a temporary H2 database with the project's sample problems. Demo records disappear when it stops. It listens only on this computer. The first run needs internet access for Maven dependencies.

The scripts invoke the Maven Wrapper with `spring-boot.run.useTestClasspath=true` so the test-scoped H2 dependency is available, and activate `preview`. Do not use the preview profile for shared deployment or with the packaged JAR.

## Run with the team's MySQL database

1. Create your own local `judgeit_db` database.
2. Create the ignored `src/main/resources/application-local.properties` with your own connection settings, as described in the main README. Never commit credentials.
3. Run `.\mvnw.cmd spring-boot:run` on Windows or `sh mvnw spring-boot:run` on macOS/Linux. The existing default `local` profile connects to MySQL.
4. Open <http://localhost:8081/>.

This frontend introduces no database tables or schema migrations. Dashboard counts use existing problem and submission records.

## Routes and team integration

| Page | Existing module owner | Route |
| --- | --- | --- |
| Dashboard | Manikandan | `/` |
| Problems and management | Sabari | `/problems`, `/problems/new`, `/problems/edit/{id}` |
| Problem details | Manikandan / Sabari | `/problems/{id}` |
| Code editor | Thamizhanban | `/submissions/new?problemId={id}` |
| Results and history | Dharma | `/submissions`, `/submissions/{id}` |
| Account | Rian Frankklin | `/register`, `/login`, `/profile`, `/logout` |

Sivaram's execution API remains separate. Saving a submission still records `PENDING`; it does not invoke an executor or invent a score. Connect and test a secure runner before enabling automated judging. History and dashboard counts currently cover the shared workspace because the existing submission entity has no user ownership relationship.

Templates use `fragments/layout.html` for `head(pageTitle)`, `nav(active)`, `feedback`, and `footer`. Shared assets are in `static/css/judgeit.css` and `static/js/judgeit.js`. Use `th:href`/`th:action` when adding routes, and `th:text` for user content.

Small integration fixes included: add the missing `spring-security-crypto` dependency used by the user module; fetch submission problems before the service transaction closes; bind the editor's selected problem in its form object; preserve the update URL after a rejected edit; show CRUD feedback and a helpful message when related records prevent deletion. Missing problems and submissions return HTTP 404.

## Validation

```powershell
.\mvnw.cmd test
.\mvnw.cmd package
```

Use `sh mvnw test` / `sh mvnw package` on macOS/Linux. Tests use an isolated H2 database and do not require MySQL credentials.

Nine tests cover context startup, page rendering, shared navigation, escaping of stored content, selected-problem binding, rejected-form input preservation, edit retry, submission save/result/history, real dashboard counts, account registration/login/profile, linked-problem deletion feedback, empty states, and 404 pages. The build and tests passed on OpenJDK 27 with Java 21 compilation. Browser checks also cover desktop/mobile presentation, search, difficulty filtering, and the problem-to-submission flow.

MySQL-specific behavior must also be checked in the team's configured local environment before merging. The preview and automated tests use H2 in MySQL compatibility mode.

## Team handoff

The work belongs on `feature/ui`. Review the pull request into `sabareeshwaran1302/JudgeIt:main`, resolve any overlapping template edits with teammates, and let Sabari merge after review. Then everyone can pull `main` and run the integrated application against their own local MySQL configuration.

Manual acceptance: open the dashboard, search/filter problems, view a problem, choose Solve, save code, open its result/history, create/edit a problem including a validation error, and register/log in. Repeat key pages at mobile width and check keyboard navigation.
