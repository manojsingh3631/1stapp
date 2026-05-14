# Finance Calculator — Android

Native Android client for the Finance Calculator backend. Built with Kotlin, Jetpack Compose, Hilt, Retrofit, Room, and MPAndroidChart. Mirrors the calculations of the web app (SIP, SWP, Lumpsum, EMI, ELSS, PPF) and lets signed-in users save and sync their history to the FastAPI backend.

## Stack

- **Language**: Kotlin (JVM 17)
- **UI**: Jetpack Compose + Material 3
- **Architecture**: MVVM (ViewModel + StateFlow + Repository)
- **DI**: Hilt
- **Networking**: Retrofit + OkHttp + Moshi
- **Local cache**: Room
- **Auth**: Google Sign-In (`com.google.android.gms:play-services-auth`) — ID token exchanged with backend for a Bearer access token
- **Charts**: MPAndroidChart
- **Secure storage**: `EncryptedSharedPreferences` (for the access token)
- **Min SDK**: 24 (Android 7.0) · **Target/Compile SDK**: 34

## Project layout

```
app/src/main/java/com/financeapp/calculator/
├── FinanceApp.kt              # Hilt @HiltAndroidApp entry point
├── MainActivity.kt            # Single activity, installs splash, hosts Compose
├── data/
│   ├── api/                   # FinanceApi, AuthInterceptor, DTOs
│   ├── local/                 # Room: AppDatabase, CalculationDao, CalculationEntity
│   ├── model/                 # User, CalculatorType, CalculationResult, YearPoint
│   └── repository/            # AuthRepository, CalculationRepository, SettingsRepository
├── di/                        # Hilt modules (Network, Database, Preferences)
├── ui/
│   ├── AppNavGraph.kt         # Compose Navigation setup
│   ├── Destinations.kt
│   ├── components/            # NumberField, ResultCard, GrowthChart
│   ├── screens/               # splash/login/dashboard/calculators/history/settings
│   └── theme/                 # Material 3 theming + colors + typography
├── utils/
│   ├── CalculationEngine.kt   # Pure financial math (SIP/SWP/Lumpsum/EMI/ELSS/PPF)
│   ├── Formatters.kt          # INR currency formatting
│   ├── InputValidator.kt
│   ├── Outcome.kt             # Result wrapper + safeCall
│   ├── SecureTokenStore.kt    # EncryptedSharedPreferences wrapper
│   └── GoogleAuthClient.kt    # Wrapper around GoogleSignInClient
└── viewmodel/                 # AppViewModel, LoginViewModel, CalculatorViewModel, HistoryViewModel, SettingsViewModel
```

## Building

You need **JDK 17** and **Android Studio Hedgehog or newer** (Iguana / Koala work too).

Open the project, let Gradle sync, then either run from Android Studio or:

```bash
./gradlew assembleDebug         # APK at app/build/outputs/apk/debug/
./gradlew test                  # Unit tests
./gradlew connectedAndroidTest  # Instrumented tests (needs a device / emulator)
```

## Configuration — two values you must set

Both live in `gradle.properties` and are wired into `BuildConfig` at build time.

| Property | Purpose |
| --- | --- |
| `BACKEND_BASE_URL` | Root URL of the FastAPI backend. Must end with `/`. |
| `GOOGLE_WEB_CLIENT_ID` | OAuth 2.0 **Web** client ID from Google Cloud Console (yes, web — that's what `requestIdToken` requires). |

### Backend URL — what value to use

- Android emulator → host machine: `http://10.0.2.2:8000/`
- Physical device on the same LAN: `http://<your-laptop-LAN-IP>:8000/`
- Production: `https://api.example.com/`

> The bundled `network_security_config.xml` only allows cleartext for `10.0.2.2`, `localhost`, and `127.0.0.1`. Any non-loopback host must be HTTPS, or you'll need to widen the config.

### Google Sign-In setup

1. **Google Cloud Console** → create a project (or pick an existing one).
2. **APIs & Services → Credentials → Create credentials → OAuth client ID**:
   - One **Android** client. You need a package name (`com.financeapp.calculator`, or `com.financeapp.calculator.debug` for the debug variant) and the **SHA-1 fingerprint** of the signing key:
     ```bash
     # Debug:
     keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
     # Release: same command pointed at your release keystore.
     ```
   - One **Web application** client. This is the one whose ID goes into `GOOGLE_WEB_CLIENT_ID` — the Android SDK uses it as the `audience` of the ID token, which is what the backend will validate.
3. Make sure the backend is configured to accept tokens issued for that web client ID.

### Where the values land in code

`BuildConfig.BACKEND_BASE_URL` is read by `NetworkModule.provideRetrofit`. `BuildConfig.GOOGLE_WEB_CLIENT_ID` is read by `GoogleAuthClient.signInIntent`.

## Backend contract

The app talks to these endpoints (all paths relative to `BACKEND_BASE_URL`):

| Method | Path | Auth | Purpose |
| --- | --- | --- | --- |
| GET | `api/status` | — | Health check |
| POST | `api/auth/session` | — | Exchange Google ID token for backend session/access token |
| GET | `api/auth/me` | Bearer | Current user |
| POST | `api/auth/logout` | Bearer | Best-effort server-side logout |
| POST | `api/calculations` | Bearer | Save a calculation |
| GET | `api/calculations` | Bearer | List saved calculations |
| DELETE | `api/calculations/{id}` | Bearer | Delete one |

Request/response shapes are defined in `data/api/dto/Dtos.kt`.

## Calculation formulas

All formulas live in `utils/CalculationEngine.kt`. Each function is pure and returns a `CalculationResult` with the headline number, a metrics map, and a year-by-year series for charting. Unit tests in `app/src/test/java/com/financeapp/calculator/CalculationEngineTest.kt` lock the math down with known values:

- **SIP**: monthly contribution, compounded monthly. FV = Σ contribute-then-grow each month.
- **SWP**: principal grows monthly, withdraw at month end; stops at horizon or depletion.
- **Lumpsum**: `P · (1 + r)^n` annual compounding.
- **EMI**: `P · r · (1+r)^n / ((1+r)^n − 1)`; series is amortization outstanding balance per year.
- **ELSS**: same growth math as SIP, plus 80C deduction (`min(annual_investment, 150_000) · slab`) on top.
- **PPF**: yearly deposit at start of year, interest credited at year end.

## Security notes

- Access token is stored in `EncryptedSharedPreferences` (AES256-GCM), excluded from auto-backup via `xml/backup_rules.xml` and `xml/data_extraction_rules.xml`.
- HTTP-only cookies issued by the backend would also work — OkHttp's default cookie jar isn't installed by default, so add one in `NetworkModule.provideOkHttp` if your backend prefers cookies over Bearer.
- Certificate pinning hook is left as a commented `// .certificatePinner(...)` in `NetworkModule`. Fill in your production cert's SHA-256 pin before shipping.
- ProGuard/R8 rules in `app/proguard-rules.pro` keep DTOs, Retrofit, Moshi codegen output, and MPAndroidChart.

## Theming

- `ui/theme/Theme.kt` provides a Material 3 color scheme keyed to a `ThemeMode` (Light / Dark / System), persisted via DataStore in `SettingsRepository`. Dynamic color (Material You, API 31+) is on by default.
- The system splash screen runs `Theme.FinanceCalculator.Splash` (`res/values/themes.xml`), held on screen by `MainActivity` until the auth probe completes.

## Known limitations / next steps

- **PDF / Excel export**: not implemented. The repository and ViewModels already expose `CalculationResult.series`, so wiring up an export with iText/Apache POI is straightforward but out of scope here.
- **Deep linking**: nav graph routes are simple string paths (`calculator/{type}`). To support `https://example.com/sip` style deep links, add `deepLink { uriPattern = ... }` to the matching `composable` in `AppNavGraph`.
- **Pending-sync offline mode**: the schema has a `pendingSync` flag on `CalculationEntity` but the WorkManager job that retries pending saves isn't built yet.

## License

Sample / starter project. Add your preferred license here.
