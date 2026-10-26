VERDICT: REQUEST_CHANGES

1. **HIGH — design.md D12; spec.md “Verify the target…”**  
   A single fill operation can trigger an untrusted input handler that submits asynchronously. If the submitted turn has not appeared when the explicit Send runs, the workflow can submit twice; checking for a visible turn does not establish that entry was unsent. Replace the “cannot submit” guarantee with an explicit, achievable entry contract, and add delayed self-submission fixtures. Block automatic Send where non-submitting entry cannot be established.

2. **HIGH — design.md D10–D13; spec.md “Keep page content out…”; proposal.md “What Changes”**  
   Removing MCP logging does not establish that unrelated page text is never written: the persistent browser can retain page content in cache, history and site storage before job binding. The absolute storage claims therefore exceed the described controls. Enumerate browser-managed storage, define its permitted contents and retention, and test it separately from driver output. If the prohibition includes browser storage, revise the persistent-profile design.

3. **HIGH — design.md D12 action preconditions; spec.md “Apply the execution rules…”**  
   `answer` rechecks neither model nor private-source settings, while `begin` omits model verification. Changes between commands can therefore admit a clarification answer under private-source settings despite the job’s public-only scope. Apply current model and source checks, including memory and earlier-chat access, to every content-submitting or research-start action. Add fixtures that change these settings after preparation.

4. **MEDIUM — design.md D12; spec.md navigation and activation scenarios; tasks.md 5.6**  
   D12 accepts a gap between the last check and input, but the spec promises activation-time validity and no submission when navigation occurs between observation and Send. Observation numbers and fresh locators do not establish that guarantee during the admitted gap or locator waiting. Bound the requirement to what the driver can enforce, specify cancellation on navigation or target invalidation during pending actions, and test those timings without claiming atomicity.

5. **MEDIUM — design.md D11; spec.md “Run headed or headless…”; tasks.md 5.10**  
   The first provider headless characterization is circular: headless use requires an already-characterized headless profile, but task 5.10 must perform that first observation. Define a separately authorized, non-submitting headless characterization operation after headed characterization. It should promote headless eligibility only after its checks pass.

6. **MEDIUM — spec.md terms acknowledgement, “An ungated operation runs…”; design.md D11 and D14**  
   The scenario requires sign-in to use no network, while `login` opens the browser for provider authentication. These requirements conflict. Limit the no-network guarantee to local checks and local metadata commands; retain the no-driver and no-automated-input guarantees for sign-in.

7. **MEDIUM — design.md D11; Test Strategy fixture level; tasks.md 5.3 and 5.14**  
   The plan treats headed operation without foreground activation as established, but the required driver fixtures are headless and the synthetic observation test cannot demonstrate operating-system focus behavior. Add a real headed launch-and-observation check while another application is active, including browser restart. Make support conditional on that result.

8. **LOW — design.md Migration and Rollback; D9; tasks.md 5.5**  
   Migration says the only desktop effect is delaying acceptance tasks, but D9 and task 5.5 change shared reservation behavior across both surfaces. Correct that statement and identify the desktop behavior changes and their regression checks.

Checked and found sound:

- Reservations survive local failures and uncertain submission outcomes.
- Request identity includes surface, scope, kind and parent.
- The closed driver protocol, fixed launch environment and dependency lock address the identified MCP-specific execution paths in principle.
- Child-job collection exceptions and the two feature cuts remove earlier scope conflicts.
- The acknowledgement’s unsigned-file limit and lack of provider permission are explicit.
- Synthetic, fixture and live evidence are distinguished. The referenced source inspections and provider claims remain unverified within this packet.
