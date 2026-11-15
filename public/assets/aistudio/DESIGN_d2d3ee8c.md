---
name: Warm Clinical Solace
colors:
  surface: '#fff8f7'
  surface-dim: '#fdcfd2'
  surface-bright: '#fff8f7'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#fff0f0'
  surface-container: '#ffe9e9'
  surface-container-high: '#ffe1e3'
  surface-container-highest: '#ffdadc'
  on-surface: '#2d1417'
  on-surface-variant: '#5c403f'
  inverse-surface: '#45282b'
  inverse-on-surface: '#ffeced'
  outline: '#906f6e'
  outline-variant: '#e5bdbb'
  surface-tint: '#bf0229'
  primary: '#9e001f'
  on-primary: '#ffffff'
  primary-container: '#c8102e'
  on-primary-container: '#ffdad8'
  inverse-primary: '#ffb3b1'
  secondary: '#b12a39'
  on-secondary: '#ffffff'
  secondary-container: '#fc616b'
  on-secondary-container: '#640014'
  tertiary: '#5b484c'
  on-tertiary: '#ffffff'
  tertiary-container: '#745f63'
  on-tertiary-container: '#f7dbe0'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#ffdad8'
  primary-fixed-dim: '#ffb3b1'
  on-primary-fixed: '#410007'
  on-primary-fixed-variant: '#92001c'
  secondary-fixed: '#ffdad9'
  secondary-fixed-dim: '#ffb3b3'
  on-secondary-fixed: '#400009'
  on-secondary-fixed-variant: '#8f0c24'
  tertiary-fixed: '#f7dce1'
  tertiary-fixed-dim: '#dac0c5'
  on-tertiary-fixed: '#26171b'
  on-tertiary-fixed-variant: '#554246'
  background: '#fff8f7'
  on-background: '#2d1417'
  surface-variant: '#ffdadc'
typography:
  display-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 40px
    fontWeight: '700'
    lineHeight: 48px
    letterSpacing: -0.02em
  display-lg-mobile:
    fontFamily: Plus Jakarta Sans
    fontSize: 32px
    fontWeight: '700'
    lineHeight: 40px
    letterSpacing: -0.02em
  headline-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 28px
    fontWeight: '700'
    lineHeight: 36px
    letterSpacing: -0.01em
  headline-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 22px
    fontWeight: '600'
    lineHeight: 28px
  headline-sm:
    fontFamily: Plus Jakarta Sans
    fontSize: 18px
    fontWeight: '600'
    lineHeight: 24px
  body-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 18px
    fontWeight: '400'
    lineHeight: 28px
  body-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  body-sm:
    fontFamily: Plus Jakarta Sans
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  label-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 16px
    fontWeight: '600'
    lineHeight: 20px
    letterSpacing: 0.01em
  label-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 14px
    fontWeight: '600'
    lineHeight: 18px
    letterSpacing: 0.01em
  label-sm:
    fontFamily: Plus Jakarta Sans
    fontSize: 12px
    fontWeight: '600'
    lineHeight: 16px
    letterSpacing: 0.02em
rounded:
  sm: 0.5rem
  DEFAULT: 1rem
  md: 1.5rem
  lg: 2rem
  xl: 3rem
  full: 9999px
spacing:
  gutter: 1rem
  gutter-desktop: 1.5rem
  margin: 1rem
  margin-tablet: 2rem
  margin-desktop: 3rem
  space-xs: 0.25rem
  space-sm: 0.5rem
  space-md: 1rem
  space-lg: 1.5rem
  space-xl: 2rem
---

## Brand & Style

This design system delivers a compassionate, calm, and medically supportive environment for managing chronic endometriosis symptoms. It rejects the cold, sterile, clinical whiteout of typical medical software in favor of an empathetic sanctuary that acknowledges chronic pain while maintaining crisp legibility and high utility.

- **Brand Personality:** Empathetic, validating, protective, lucid, and measured.
- **Design Movement:** Soft Modern Organic Tactility. Generous internal padding, pill-shaped interactions, expansive continuous curves, and subtle tinted borders remove harsh friction points.
- **Emotional Impact:** Feelings of being heard, organized, and physically supported during flare-ups or appointments. The visual weight should feel effortless during brain fog or intense pain episodes.
- **Target Experience:** Mobile-first, single-thumb navigability, instant feedback, and gentle visual hierarchy that reduces cognitive load during high-stress episodes.

## Colors

The palette balances authoritative medical precision with human tenderness, strictly operating in light mode to preserve tonal clarity and daytime visibility during doctor visits or symptom logs.

- **Primary (`#C8102E`):** Vivid cherry red. Used for high-intent actions (log entry, pain peak trackers, primary button fills, active step indicators).
- **Secondary (`#8E0B23`):** Deep cherry. Used for elevated interactive states, active pressed states, icons, and emphasized health analytics.
- **Tertiary / Base Tint (`#FFE3E8`):** Soft blush. Serves as the global backdrop tint and tag/chip background fills to soften the visual canvas without creating stark white glare.
- **Surface Neutral (`#FFFFFF`):** Crisp white. Reserved for elevated actionable cards, modular tracking surfaces, and modals to maintain strict structural definition against the soft blush canvas.
- **Structural Border Tint (`#F5D0D6`):** Calming, warm structural boundary used for all container outlines, divider rules, and input borders.
- **Text & Content Hierarchy:**
  - High-Emphasis Content: `#2B1215` (Deep espresso-cherry) providing AAA contrast against both `#FFE3E8` and `#FFFFFF`.
  - Medium-Emphasis / Metadata: `#6D5558` (Muted red-gray) for timestamps, secondary labels, disabled indicators, and unit markers.

## Typography

The typographic system utilizes `Plus Jakarta Sans` across all roles for its balanced combination of humanist warmth and geometric accessibility. The rounded apertures and sturdy stems remain readable during fatigue and acute physical distress.

- **Weight Discipline:** Restrict styling to `400` (Regular), `600` (Semi-Bold), and `700` (Bold) to prevent visual chaos and maintain clean scannability.
- **Numbers & Metrics:** Use tabular or proportional lining numbers for cycle days, pain scales (1–10), and medication dosages to keep clinical timelines crisp and predictable.
- **Contrast Integrity:** Body text must strictly use `#2B1215` for running clinical notes and `#6D5558` for supporting captions and metric units.

## Layout & Spacing

A mobile-first fluid layout prioritizing immediate tap accessibility. The grid adapts from an effortless single-column mobile view to a wider dual-pane layout on tablets and desktops for doctor-facing export views.

- **Mobile Rhythm:** 4-column fluid layout with an edge margin of `margin` (16px) and `gutter` (16px). All primary action buttons, inputs, and selection tiles maintain a mandatory minimum height or width of 48px to prevent tap fatigue and missed touches.
- **Tablet / Desktop Rhythm:** 8 to 12 columns maxing out at an internal container width of 840px for mobile-centric web dashboards, maintaining content intimacy rather than wide, unreachable horizontal stretches.
- **Vertical Hierarchy:** Use `space-lg` (24px) between grouped symptom cards, and `space-sm` (8px) within related interactive clusters (such as symptom severity chips or timestamp pills).

## Elevation & Depth

Visual separation is constructed using tonal surfaces and perimeter warmth rather than harsh, heavy drop shadows that create visual grit.

- **Layer 0 (Canvas):** Continuous background washed in `#FFE3E8`.
- **Layer 1 (Cards & Modular Trays):** Solid `#FFFFFF` surfaces bounded by a crisp 1px or 1.5px solid border in `#F5D0D6`.
- **Layer 2 (Overlays & Bottom Sheets):** Crisp `#FFFFFF` elevated with a wide, ultra-soft ambient shadow: `0 12px 32px -4px rgba(142, 11, 35, 0.08)` to ground floating modal sheets using the warm cherry tint.
- **Active State Feedback:** Avoid high-contrast elevation changes on tap; rely instead on subtle inward border changes (e.g., swapping border from `#F5D0D6` to `#C8102E`) and light surface washes.

## Shapes

The design uses soft, organic, and pill-shaped curvature to evoke reassurance and comfort.

- **Full Pill (`9999px`):** Applied to all interactive chips, tags, primary pill buttons, cycle day markers, and segmented selectors.
- **Modular Cards & Containers:** Scaled systematically using `rounded-2xl` (16px) and `rounded-3xl` (24px) to remove all rigid visual barriers.
- **Bottom Drawers & Modals:** `rounded-t-3xl` (24px or 32px) on top edges to embrace the screen surface as a safe physical container.

## Components

### Buttons
- **Primary:** Full pill-shaped (`rounded-full`), minimum 48px height. Background `#C8102E`, text `#FFFFFF`, typography `label-lg`. Active state shifts to `#8E0B23`.
- **Secondary / Soft:** Full pill-shaped, 48px height. Background `#FFE3E8`, text `#8E0B23`, border 1px solid `#F5D0D6`.
- **Ghost:** Minimum 48px touch target. Transparent background with text `#8E0B23`.

### Chips & Symptom Tags
- **Default State:** Fully pill-shaped, height 36px to 44px, padding 0 16px. Background `#FFFFFF`, border 1px solid `#F5D0D6`, text `#6D5558`, typography `label-md`.
- **Selected State:** Background `#FFE3E8`, border 1.5px solid `#C8102E`, text `#8E0B23`, weight `600`.

### Cards
- **Base Card:** Background `#FFFFFF`, border 1.5px solid `#F5D0D6`, radius `rounded-2xl` (16px) or `rounded-3xl` (24px). Internal padding `space-lg` (24px).
- **Log Entry Summary Card:** Stacked inside `#FFFFFF` with muted `#6D5558` metadata, clear `#2B1215` header text, and small embedded status pills.

### Input Fields & Selectors
- **Text Inputs:** Height 52px, radius `rounded-xl` (16px), background `#FFFFFF`, border 1px solid `#F5D0D6`, text `#2B1215`, placeholder `#6D5558`. Focused state: border 2px solid `#C8102E`.
- **Pain Level Slider / Scale (1-10):** Modular segment with circular 48px touch nodes. Nodes default to `#FFFFFF` with `#F5D0D6` outline; active selected pain node transitions to `#C8102E` with white bold numbering.

### Checkboxes & Radio Buttons
- **Target Container:** 48px bounding area.
- **Visual Indicator:** 22px diameter. Radio buttons are circular; checkboxes use 6px rounded corners.
- **Selected:** Fill `#C8102E` with crisp white checkmark/dot. Unselected: `#FFFFFF` fill with 1.5px border `#F5D0D6`.

### Pain & Symptom Intensity Dials
- Tactile circular or continuous pill sliders with distinct step increments (Mild, Moderate, Severe, Incapacitating) tinted from `#FFE3E8` to `#C8102E` to offer immediate non-verbal logging during severe flare-ups.