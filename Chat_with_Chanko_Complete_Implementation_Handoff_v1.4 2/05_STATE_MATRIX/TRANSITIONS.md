# State transitions
11 named UX states are combined content/modal/request dimensions, not11 database statuses. S03 keeps named label Item Chat with Verdict; pre-verdict item subcase is documented, not state12.

| From + event | To | Guard/effect |
|---|---|---|
| S01 send | S02+S07 or item-content+S07 | Valid text, online,1intent; fake mode label |
| Item Quick Check early verdict | S03+S07→S03 completed or S08 | Owner/item/capability; validated evaluation; no business side effect from verdict |
| S02 select existing item +explicit confirm | S05→same thread S03 pre-verdict after bind ack | Keep thread/transcript/draft IDs; no auto item create/evaluation; version+key+context guards; Cancel FREE |
| Chat History | S04→thread | Read owner-scoped identity, not stale object |
| Any readable cache loses network | content+S06 | Disable writes/AI, retain draft under policy |
| S06 reconnect | refetch→server state | No auto-send/replay; pending outcome reconcile |
| S07 timeout | S08 UnknownOutcome | Keep key, no new intent blindly |
| Thread More | S09→S10 | Capability; no mutation on opening |
| S10 cancel | previous content | Zero effect |
| S10 confirm/ack | S04 or Empty | A1/A3 approved; real behavior requires implemented backend/capability |
| Archive ack | S11 | No item archive; production Q03 |
| Restore ack | active thread/list | No item restore/history reset; Q03 |
| Remote delete/archive/send race | refetch/reconcile/unavailable | A3+A2 amendment approved: commit order+lifecycle_epoch+context_revision fences; no client-clock winner |
| Account switch | reset rendered owner state | No leaked cache/draft/transcript |

Back closes IME then modal per Android conventions R; preserves source routes/list scroll. UI cancel does not imply backend cancel. Stale verdict history labeled, not recomputed silently.

| Additional event | Outcome | Guard |
|---|---|---|
| Bind while AI op pending/unknown | Stay existing context, reconcile/wait | No implicit cancellation/reinterpretation; explicit reconfirm with fresh version |
| Remote FREE→ITEM bind | Same thread projection ITEM | Preserve transcript/draft; discard old query generation, no auto resend |
| Freshness24h expires offline | Same readable cached content+STALE label | No expiry purge/read denial; no writes/AI |
| Known delete after reconnect | Unavailable+purged chat cache | Tombstone defeats stale responses; official item records unaffected |
