# v1.2 accessible presentation correction

Applies to derived references and implementation guidance only. Does not modify approved DS/SPEC/TECH source. Primary brand remains#8965F6; cream#FAF8F5, white surfaces, lavender/pink decoration and original soft3D mascot preserve Candy Pop identity. Corrections are reference presentation choices, reference implementation conventions under the newly approvedv1.2 visual baseline, not native runtime measurements.

| Role | Foreground | Background | Rule |
|---|---|---|---|
| Body/title/primary label | #151329 | #FFFFFF, #FAF8F5, #EEE7FF, #8965F6 | ≥4.5 for all text sizes |
| Secondary/status/placeholder | #51485F | #FFFFFF, #FAF8F5, #EEE7FF, #FFF2D8, #FFF3F6, #EEE8F1 | ≥4.5 |
| Links/icons/selected mark | #5030A0 | #FFFFFF, #FAF8F5, #EEE7FF | ≥4.5 text, ≥3 essential icons |
| Destructive label/icon | #9C2341 | #FFF3F6, #FFFFFF | Text+icon, ≥4.5 |
| Input/secondary control border | #6B5A7E | #FFFFFF, #FAF8F5, #EEE7FF, #EEE8F1 | ≥3 against adjacent fill/exterior |
| Focus ring | #151329 | Surrounding surface | 3px reference outline +3px offset; native focus behavior must be tested |

White text on#8965F6 failed4.5:1; v1.2 uses dark ink on the same approved brand fill. No text is placed on a gradient. Lavender-to-pink gradients are abstract product fixture thumbnails only, noninteractive decoration; essential labels and selected radio marks sit on plain surfaces. DecisionCard remains white; BUY/WAIT/SKIP use identical typography/colors/Neutral pose and outlined actions. No reward effects.

All active controls have minimum48×48 reference px, mapped to proposed dp at100% font; primary actions min56. Visual icons24; radio marks24 sit inside ≥48dp row targets. Labels wrap, buttons grow, actions wrap to rows, fields and modals grow. At200% font the visual reference expands vertically and scrolls instead of truncating. No fixed844dp clipping contract;844 is minimum reference canvas height. Main app requires safe-area/IME/native-scroll handling. Preserve 48dp minimum independent of text scale.

Composer: offline/pending/archived disable send and picker with a reason. UnknownOutcome keeps draft editable in the current view, but disables new Send/picker until reconciliation; no endpoint, retention guarantee or new idempotency key is inferred. This spells out the existing logical guard, not an API policy. Options/dialog background is noninteractive; focus must be trapped and returned in native UI. Source HTML is a visual document with inert buttons, not functional app behavior.

[Contrast measurements](../10_ACCEPTANCE_QA/evidence/contrast_checks.json) and [layout measurements](../10_ACCEPTANCE_QA/evidence/layout_checks.json) certify the reference only. Native TalkBack, keyboard/IME, focus order, actual sp/dp, font metrics and runtime image decoding are not tested here.
