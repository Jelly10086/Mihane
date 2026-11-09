# mihane UI

The frontend uses uwuSDK and Material 3 Expressive. Dependency versions are defined in `gradle/libs.versions.toml`; the SDK source is pinned by `third_party/uwu-sdk`.

- Use `MishkaTheme` as the only theme entry. Resolve dark mode from `ThemeConfig`; use `MaterialTheme.colorScheme`, typography, shapes and motion tokens inside the UI.
- Dynamic system colors take precedence. With dynamic colors disabled, seed colors use Google's `SchemeExpressive`. Preserve persisted theme mode, pure black and density scale.
- UI text uses the Outfit / original MiSans / system fallback chain. Logs and source editors retain monospace. Font files keep their original licenses; MiSans is supplied from an official local archive and checked before packaging.
- Page titles use SDK `SettingsCollapsingAppBarScaffold` / `SettingsScreenScaffold`; preserve the SDK toolbar appearance and insets. Do not substitute a regular `TopAppBar`.
- Preferences and selection rows call SDK `PreferenceRow`, `SwitchPreferenceRow`, `RadioPreferenceRow` and `SettingsPreferenceGroup`. Lazy groups pass visible item positions to SDK rows, without painting a second rounded surface.
- Rich data sections use SDK `CustomPreferenceRow`; responsive sections use `SettingsAdaptiveGrid` and content widths use `SettingsContent`. Do not recreate these surfaces with app-local cards.
- Search uses SDK `SettingsSearchLayout`. Menus use SDK `SettingsDropdownMenu`, `SettingsMenuGroup` and `SettingsMenuItem`. Buttons use SDK `SettingsActionButton` / `SettingsToolbarActionButton`.
- Use official Expressive APIs for capabilities without a matching SDK slot, including selectable `ListItem`, disabled/progress icon actions, `ShortNavigationBar`, wide navigation rail, dialogs, sheets, badges and wavy progress. Retain default shapes, typography, motion and minimum touch targets.
- Form adapters only translate application state and labels into official text fields. A dialog action belongs in the official action slot. Do not build custom alert surfaces or place page radio cards inside menus.
- Phone navigation uses a Material Expressive short navigation bar and the official pager; wide windows use a wide navigation rail. Retain the four destinations and all secondary route parameters.
- Navigation 3 owns destination lifetime and saveable state. Keep all routes serializable, and do not start services from composition or replay restored navigation as a new business action.
- Content consumes Scaffold insets exactly once. Lists remain full-width for scrolling; constrain the content through padding. Do not hard-code the physical device dimensions.
- Keep stable lazy keys and per-row virtualization. Collect high-frequency traffic only in the sections that render it. Read animation values during measurement or drawing where possible.
- Menus use SDK menu items and selection styling. Dialogs and sheets use Material components with accessible dismissal, keyboard and window insets.
- Keep all existing callbacks, error handling, service state and data ownership. UI-only migrations must not change subscription processing, VPN/ROOT lifecycle, JNI or database contracts.
- Retire miuix-specific effects and settings rather than leaving disabled compatibility stubs. Existing unused preference keys can remain in storage.
- Validate Kotlin compilation, dependency resolution, lint and the requested APK variants. Report device appearance separately from build success. Do not add unit test files unless requested.
