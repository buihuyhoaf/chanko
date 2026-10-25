> v1.4:11 core screens dùng các asset này nay là official visual baseline; không cần sourcev0.2. Current completeness/approval/runtime states trong FINAL_QA_REPORT; toàn bộ asset binary giữ nguyênv1.2.

# Canonical and asset set v1.2

Canonical: [mascot_skeptical.png](canonical/mascot_skeptical.png), copied byte-for-byte from the user attachment. SHA-256 `c4b0de06a1175464607e80372f9615060b89892a16b58e8e4f57087c74e947d7`. [chanko_user_approved.png](canonical/chanko_user_approved.png) is a compatibility alias with identical bytes. User approval is carried from prior context, not newly inferred.

All eight expression PNGs and WebPs are preserved unchanged from v1.1. They visually retain identity; they were generated against the v1.1 canonical rendition, whose alpha equals the original but RGB differed by at most 2/255. Do not claim they were newly generated against the byte-exact original. No new mascot, placeholder, inferred user approval or new pose was introduced. Original master files are immutable in this release.

Canonical is an identity reference, not the verdict drawable. BUY/WAIT/SKIP always use `expressions/chanko_neutral.png` or the pixel-equivalent WebP. Encouraging is supportive dialogue only, never a BUY reward. Welcome maps empty; Curious maps free-chat replies; Relaxed maps history empty/offline; Thinking maps pending; Concerned maps unknown/error; Sleeping maps archived; Neutral maps headers and default assistants. [Exact slot mapping](screen_asset_mapping.csv) is authoritative for presentation.

Use full-image aspect fit, centered, no crop or recoloring. Header 40×44 reference dp (32×36 at narrow width), assistant32×34, verdict/status72×76, hero200×208 (176×184 narrow). These are sizing conventions from the now-approvedv1.2 visual source; native dp/sp behavior is verified during implementation. Interactive controls around decorative art still have ≥48dp targets. At small size the emotion is supplementary; state labels remain explicit.

8 presentation names do not replace the 9 domain emotions. [Domain fallback policy](EMOTION_PRESENTATION.md) retains the whitelist. No animated deliverable is supplied; reduced motion uses these static assets. Decode master WebP to desired size through the target image pipeline; do not decode multiple 1231px images into 32dp slots at full resolution. No speculative density exports or visual alpha cleanup were applied to masters.

[Nunito variable font and OFL](fonts/README.md) are bundled. [Manifest](asset_manifest.csv) includes font and exact hashes. Historical v1.1 generation log remains under12 for audit only; active [provenance](generation_files.json) uses portable relative paths.
