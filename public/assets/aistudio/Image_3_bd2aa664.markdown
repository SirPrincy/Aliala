---
name: Ma Vie Mobile Hub
colors:
  surface: '#faf8ff'
  surface-dim: '#d2d9f4'
  surface-bright: '#faf8ff'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f2f3ff'
  surface-container: '#eaedff'
  surface-container-high: '#e2e7ff'
  surface-container-highest: '#dae2fd'
  on-surface: '#131b2e'
  on-surface-variant: '#3d494c'
  inverse-surface: '#283044'
  inverse-on-surface: '#eef0ff'
  outline: '#6d797d'
  outline-variant: '#bcc9cd'
  surface-tint: '#00687a'
  primary: '#00687a'
  on-primary: '#ffffff'
  primary-container: '#06b6d4'
  on-primary-container: '#00424f'
  inverse-primary: '#4cd7f6'
  secondary: '#712ae2'
  on-secondary: '#ffffff'
  secondary-container: '#8a4cfc'
  on-secondary-container: '#fffbff'
  tertiary: '#005ac2'
  on-tertiary: '#ffffff'
  tertiary-container: '#78a5ff'
  on-tertiary-container: '#00397f'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#acedff'
  primary-fixed-dim: '#4cd7f6'
  on-primary-fixed: '#001f26'
  on-primary-fixed-variant: '#004e5c'
  secondary-fixed: '#eaddff'
  secondary-fixed-dim: '#d2bbff'
  on-secondary-fixed: '#25005a'
  on-secondary-fixed-variant: '#5a00c6'
  tertiary-fixed: '#d8e2ff'
  tertiary-fixed-dim: '#adc6ff'
  on-tertiary-fixed: '#001a42'
  on-tertiary-fixed-variant: '#004395'
  background: '#faf8ff'
  on-background: '#131b2e'
  surface-variant: '#dae2fd'
typography:
  headline-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 30px
    fontWeight: '700'
    lineHeight: 38px
  headline-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 22px
    fontWeight: '700'
    lineHeight: 28px
  headline-sm:
    fontFamily: Plus Jakarta Sans
    fontSize: 18px
    fontWeight: '600'
    lineHeight: 24px
  title-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 16px
    fontWeight: '600'
    lineHeight: 22px
  body-lg:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  body-md:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  body-sm:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '400'
    lineHeight: 16px
  label-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 14px
    fontWeight: '600'
    lineHeight: 18px
  label-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 12px
    fontWeight: '600'
    lineHeight: 16px
  label-sm:
    fontFamily: Plus Jakarta Sans
    fontSize: 10px
    fontWeight: '700'
    lineHeight: 12px
  metric-display:
    fontFamily: Plus Jakarta Sans
    fontSize: 26px
    fontWeight: '800'
    lineHeight: 32px
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  gutter: 0.75rem
  margin: 1rem
  space-xs: 0.25rem
  space-sm: 0.5rem
  space-md: 0.75rem
  space-lg: 1rem
  space-xl: 1.5rem
---

## Brand & Style

This design system powers a comprehensive personal management dashboard on Android. The experience is intimate, organized, and motivating. The aesthetic pairs a modern, tactile, card-driven interface with clean Material-inspired ergonomics tuned specifically for handheld productivity.

The design movement balances modern clean corporate precision with vibrant, approachable wellness interfaces:
- Crisp white tactile cards floating above a cool, calming canvas (`#F8FAFC`).
- High-chroma categorical accents to visually segment disparate domains of everyday life (Habitudes, Finances, Santé, etc.) without clutter.
- Generous, tap-friendly layouts optimized for one-handed operation on tall modern viewport ratios (412x915).
- Gentle micro-elevations rather than harsh borders, providing an airy, stress-free space where everyday tracking feels effortless and rewarding.

## Colors

The palette establishes an ultra-clean base with vivid module accents, making data visualization instantly scannable across personal lifecycle domains.

### Core Canvas & Neutrals
- **Canvas Background**: `#F8FAFC` (Slate 50) – soft, cool background reducing eye fatigue.
- **Card Surfaces**: `#FFFFFF` – pure white for foreground cards, sheets, and elevated modules.
- **Primary Text**: `#0F172A` (Slate 900) – high contrast, accessible typography for fast parsing.
- **Muted / Subtitles**: `#64748B` (Slate 500) – tertiary descriptions, timestamps, and placeholder copy.
- **Dividers & Outlines**: `#E2E8F0` (Slate 200) – low-contrast separators used sparingly.

### Core Accents & Feedback
- **Primary**: `#06B6D4` (Cyan 500) – overall brand touchpoints, progress bars, active bottom navigation.
- **Secondary**: `#7C3AED` (Violet 600) – focal secondary actions, premium highlights, analytics toggles.
- **Success**: `#22C55E` – task completions, positive budgets, streaks.
- **Warning**: `#F59E0B` – approaching limits, impending due dates.
- **Danger**: `#EF4444` – overdue actions, balance deficits, deletion confirmation.

### Domain Module Accents
Each life module uses a distinct signature color applied to icon containers, tag borders, and metric summaries:
- **Tâches (Tasks)**: Cyan `#06B6D4` (Surface tint: `#ECFEFF`)
- **Habitudes (Habits)**: Green `#22C55E` (Surface tint: `#F0FDF4`)
- **Finances (Finance)**: Blue `#3B82F6` (Surface tint: `#EFF6FF`)
- **Emprunts (Loans)**: Violet `#7C3AED` (Surface tint: `#F5F3FF`)
- **Business**: Orange `#F97316` (Surface tint: `#FFF7ED`)
- **Santé (Health)**: Pink `#EC4899` (Surface tint: `#FDF2F8`)
- **Allemand (Language)**: Indigo `#6366F1` (Surface tint: `#EEF2FF`)
- **Objectifs (Goals)**: Yellow `#EAB308` (Surface tint: `#FEFCE8`)
- **Sauvegarde (Backup)**: Slate `#64748B` (Surface tint: `#F1F5F9`)

## Typography

The type system blends the energetic geometry of **Plus Jakarta Sans** with the structural legibility of **Inter**.

- **Display & Headings (Plus Jakarta Sans)**: Carries welcoming round curves. Ideal for the French language's extended vowel combinations and accents (é, è, ê, à). Used across page greetings ("Bonjour, Thomas"), module titles, and dashboard KPI values.
- **Body & Data Lists (Inter)**: Handles descriptive notes, checklist items, and multi-line finance logs with neutral precision, preventing visual fatigue across compact mobile views.
- **Numerical Formatting**: Currency amounts (`€`), habit counters (`12/30 jours`), and time logs default to **Plus Jakarta Sans** semi-bold or bold tabular figures to keep vertical lists neatly aligned.

## Layout & Spacing

Designed around the Android standard baseline of **412 × 915 dp**:
- **Canvas Bounds**: Fixed 16px (`1rem`) horizontal screen padding.
- **Card-to-Card Grid Gap**: 12px (`0.75rem`) vertical and horizontal spacing between dashboard widgets and list cards.
- **Touch Target Integrity**: Strict adherence to a 48 × 48 dp interactive footprint for all buttons, checkboxes, chips, and quick-add affordances.
- **Rhythm**: Internal card padding uses 16px (`1rem`) for full-width cards and 12px (`0.75rem`) for compact 2-column metric cards.
- **Bottom Clearance**: Dedicated 84dp bottom safe-zone clearance to prevent overlap with the floating bottom navigation bar and system gesture indicators.

## Elevation & Depth

To maintain a light, airy, and tactile Android feeling, this design system avoids heavy shadows, instead employing ambient, diffused drops paired with tinted surfaces.

- **Level 0 (Canvas)**: Background `#F8FAFC`. Completely flat.
- **Level 1 (Default Widget Card)**: Pure white `#FFFFFF`, shadow: `0px 2px 8px -2px rgba(15, 23, 42, 0.05), 0px 4px 16px -4px rgba(15, 23, 42, 0.04)`.
- **Level 2 (Active/Pressed Card & Dropdowns)**: Pure white, shadow: `0px 4px 12px -2px rgba(15, 23, 42, 0.08), 0px 8px 24px -4px rgba(15, 23, 42, 0.06)`.
- **Level 3 (Modals, Bottom Sheets & Floating Action Buttons)**: Pure white, shadow: `0px 8px 24px -4px rgba(15, 23, 42, 0.12), 0px 16px 36px -6px rgba(15, 23, 42, 0.08)`.
- **Module Tile Surfaces**: Icon badges and contextual tag pills use a 10% opacity tint of their respective module accent (e.g., `#06B6D41A` for Tâches) without drop shadows to preserve flat clarity inside elevated cards.

## Shapes

The design uses friendly, human contours that echo the curvature of physical handheld devices:
- **Cards & Primary Modules**: 20px border radius (`rounded-xl` equivalent in this system) for a distinctive, friendly container look.
- **Input Fields & Large Buttons**: 14px to 16px radius for ergonomic touch feedback.
- **Interactive Badges, Filter Chips & Tabs**: Full pill shape (`rounded-full`, 9999px) for clear tactile affordance.
- **Checkboxes**: 6px slightly softened squares to ensure quick toggle recognition.
- **Progress Bars**: 8px fully rounded track capsules.

## Components

### 1. Dashboard Cards (Cards)
- **Geometry**: `#FFFFFF`, exactly 20px radius, 12px outer spacing, 16px inner padding.
- **Structure**: Header with module icon container (36x36 dp with 10px radius and module-tinted background), bold title (`Plus Jakarta Sans`), optional quick-action menu, followed by body metrics or checklist items.

### 2. Buttons
- **Primary Action (Cyan)**: `#06B6D4` background, white label, 48dp minimum height, 16px horizontal padding, 14px border radius. Ripple effect with 20% white overlay on tap.
- **Secondary Action (Violet)**: `#7C3AED` background with white text, or an outline version with 1.5px `#7C3AED` border and `#7C3AED` text.
- **Floating Action Button (FAB)**: 56x56 dp, `#06B6D4` fill with white glyph, Level 3 elevation, located 16dp above bottom navigation.

### 3. Chips & Category Filters
- **Form**: 36dp height, pill-shaped (`9999px`), 12px horizontal padding.
- **Default State**: Surface `#FFFFFF`, border 1px `#E2E8F0`, text `#64748B`.
- **Active State**: Module accent background at 15% opacity, border 1.5px solid module color, text matched to module color at 100% saturation.

### 4. Interactive List Rows (Checklists & Habits)
- **Form**: Min-height 52dp to guarantee seamless tapping.
- **Checkboxes**: 22x22 dp, 6px radius. Empty state: 2px `#CBD5E1` border. Checked state: `#06B6D4` (or module accent) fill with white vector checkmark. Label toggles to muted `#94A3B8` with strikethrough.

### 5. Input Fields
- **Form**: 52dp height, `#FFFFFF` fill, 12px radius, 1px `#E2E8F0` border.
- **Focus State**: Border color `#06B6D4` with a 3px soft focus ring (`rgba(6, 182, 212, 0.15)`). Floating label in `#0F172A`.

### 6. Module Metric Widgets (Quick Overview)
- **Two-Column Grid**: 188dp nominal width each, 20px corner radius.
- **Content**: Top-row icon + domain label, mid-row bold numerical metric (`metric-display`), bottom-row delta indicator (e.g. `+12% ce mois` in `#22C55E`).