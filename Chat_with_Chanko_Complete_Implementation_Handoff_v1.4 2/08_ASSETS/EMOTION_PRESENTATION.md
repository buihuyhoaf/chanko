# Domain emotions and presentation fallback

Source whitelist retained from SPEC07 snapshot: Idle, Happy, Curious, Thinking, Suspicious, Surprised, Smug, Supportive, Sleepy. The table is the approvedA5 static presentation mapping for implementation, not an API enum change or recovered 34-decision approval.

| Validated emotion | Static asset | Policy |
|---|---|---|
| Idle | neutral | Default |
| Happy | neutral | Conservative fallback; no victory/reward near verdict |
| Curious | curious | Free-chat dialogue |
| Thinking | thinking | Only actual request state supports a thinking status |
| Suspicious | neutral | No unsupported skeptical derivative in verdict |
| Surprised | neutral | No unprovided pose |
| Smug | neutral | No shaming expression |
| Supportive | encouraging | Reassuring dialogue only; verdict override below |
| Sleepy | sleeping | Decorative idle/archived; do not imply lifecycle from emotion |
| Unknown/invalid | neutral | Reject unsupported enum at adapter; safe presentation only |

State/slot overrides take precedence: every header and verdict Neutral; offline Relaxed; error Concerned; empty Welcome; history empty Relaxed; archived Sleeping. Emoji/LLM text never selects a business action. Domain emotion does not override lifecycle or request state. Reduced motion uses the same static file, no autoplay sound. If an asset cannot load, omit decoration and retain state text; never substitute another cat. Option-B predefined reactions still need native implementation verification. A5 approval is recorded; native production behavior still requires implementation/runtime evidence, not another user approval.
