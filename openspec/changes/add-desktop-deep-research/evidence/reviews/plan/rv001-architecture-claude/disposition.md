# Conductor disposition

The initial review requested changes: 6 HIGH, 12 MEDIUM, 9 LOW. The table below
describes planning corrections, not executed runtime proof. Design/research
changed while that review ran; its original snapshot and qualification remain
retained. A separate independent delta review must accept the revised packet.

| Finding | Disposition | Revised plan and planned proof |
| --- | --- | --- |
| 1 HIGH: identity/re-location | ADDRESSED IN PLAN | D4 distinct dedicated draft bootstrap/upgrade, ordered submitted-job locators, exact submitted-user-turn binding and guarded `rebind`; execution Rebind requirement and live reopen check |
| 2 HIGH: commands/transitions | ADDRESSED IN PLAN | D4 transition table, command ownership, pending-question/plan hashes, invoking-agent scope evaluator; execution interaction tests |
| 3 HIGH: duplicate/retry bypass | ADDRESSED IN PLAN | D5 stable request/batch ids, user-wide uncertainty ledger, fingerprint resume and per-job serialization; four idempotency scenarios |
| 4 HIGH: public sources only design-level | ADDRESSED IN PLAN | Execution Constrain source scope requirement has positive, private, unobservable and restoration scenarios; D5 public-only UI evidence |
| 5 HIGH: no live characterization | ADDRESSED IN PLAN | D7 budget and task 1.4 require one characterization per app after approval and before adapters; one later production acceptance per app. Manual-only narrowing is rejected because the requested outcome is automated scripts |
| 6 HIGH: focus stealing | ADDRESSED IN PLAN | D3/D5 non-activating observation, two-second idle guard and whole-transaction mutations; execution focus/transaction scenarios |
| 7 MEDIUM: provenance | ADDRESSED IN PLAN | D8 live/synthetic stamps with source/binary/profile hashes; execution provenance rejection scenario |
| 8 MEDIUM: live duplicate/batch proof | ADDRESSED IN PLAN | Test Strategy and 4.1/4.2 use production batch, zero receipt-observation budget, resume/rebind and one matching submitted user turn; self-attested counts labeled separately |
| 9 MEDIUM: app trust/promotion | ADDRESSED IN PLAN | D3 signed TeamIdentifier/version profiles, characterization promotion and update/account invalidation; execution trust scenario. Local signing metadata is explicitly weaker than full validation |
| 10 MEDIUM: bridge boundary | ADDRESSED IN PLAN | D8 closed actions/preconditions, in-process recheck/hit-test, default filtering inside helper and unexpected-dialog refusal; helper and production guard tests |
| 11 MEDIUM: input/write-ahead | ADDRESSED IN PLAN | D3 native/AX set-value then characterized paste, intent before first composer input, full hash/end-marker check and separate Send; multiline interruption scenario |
| 12 MEDIUM: Copy/citation provenance | ADDRESSED IN PLAN | D6 exact pasteboard-change count, report corroboration, actual link targets and conditional clear; two collection provenance scenarios |
| 13 MEDIUM: absence under partial view | ADDRESSED IN PLAN | D6 positive completion predicate plus complete-observation flag; collection incomplete-view refusal test |
| 14 MEDIUM: report/capture trust | ADDRESSED IN PLAN | D6 artifacts outside skill/repository, escaped index, attempt directories; D8 opt-in job-region capture; collection storage/capture test |
| 15 MEDIUM: lock semantics | ADDRESSED IN PLAN | D5 fixed user root independent of checkout/TMP, whole transactions, lock order and revision/fsync checks; cross-process and concurrency tests |
| 16 MEDIUM: evaluations touch live desktop | ADDRESSED IN PLAN | Test Strategy and task 3.3/3.4 require six synthetic-only evaluations with GUI access denied |
| 17 MEDIUM: caller is target app | ADDRESSED IN PLAN | D8 ancestry/window exclusion or block, never quit caller host; execution caller-isolation test and window-only live reopen |
| 18 MEDIUM: helper only compiled | ADDRESSED IN PLAN | D8 actual-helper `doctor --self-test`, task 2.3 and gate header cover labels, coordinates, schema, filtering and guards |
| 19 LOW: download details | ADDRESSED IN PLAN | D6 save-panel preference, before/after snapshot, Downloads access and original preservation |
| 20 LOW: prohibitions only in design | ADDRESSED IN PLAN | Execution Rebind requirement prohibits sharing/publication/account export; Observe requirement prohibits whole-desktop capture |
| 21 LOW: per-job/readiness/batch wording | ADDRESSED IN PLAN | Execution per-job outcomes and readiness for both providers; D5 batch submit/resume/wait/collect and same-provider serialization |
| 22 LOW: evidence process | ADDRESSED IN PLAN | Defined `reviews/code/` lane for security concerns, future `<next>` numbering, indexed validation runs and probe authorization in builds/001 summary |
| 23 LOW: discovery/clients | ADDRESSED IN PLAN | D1 relative canonical link and one-inventory-entry check; task 3.3 verifies actual clients. Other clients and unrequested OpenSpec regeneration are outside scope |
| 24 LOW: research policy/docs link | ADDRESSED IN PLAN | D8 desktop reports are secondary synthesis; OpenSpec fetches primary sources through MCP. New docs are linked from the skill; no AGENTS widening is required |
| 25 LOW: approval receipt | PARTLY ACCEPTED | D7/task 1.3 enumerate public prompt templates, two-submission budget, attended windows and operator grants. Fresh confirmation before each send is rejected: existing task/plan authorization covers ordinary actions within budget |
| 26 LOW: rollback residue | ADDRESSED IN PLAN | Rollback documents optional idle-cache cleanup, active lock-path protection, separate ledger/report deletion authority and manual macOS grant revocation |
| 27 LOW: marker/account/tool versions | ADDRESSED IN PLAN | D2 minimum Python/toolchain preflight, D4 OCR-robust marker/check digit and salted account context |

Provider controls, entitlement, runtime scripting, actual input, final-result
predicates, exports and compiled-helper permission identity remain UNVERIFIED.
Task 1.4 resolves feasibility after approval. No fixture or review result can
satisfy the required two-provider live acceptance.

After the first delta review, the conductor and runner confirmed a bootstrap
gap: a new empty conversation cannot yet provide a submitted-user-turn marker.
D4/D8 now distinguish verified dedicated-draft authority from submitted-job
authority and revoke draft input after possible Send. The new
`test_pre_send_binding_bootstrap_and_upgrade` covers the transition. This
correction requires its own final independent check; the earlier delta's
APPROVE is qualified by the subsequently identified gap.
