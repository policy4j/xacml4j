"""Execution contract tests. Only the desktop boundary is synthetic.

The production command parser, filesystem, locks and transitions are real.
These tests never launch apps or establish desktop acceptance.
"""
from __future__ import annotations

import contextlib
import copy
import fcntl
import hashlib
import importlib.util
import io
import json
import multiprocessing
import os
from pathlib import Path
import tempfile
import unittest


SCRIPT = Path(__file__).resolve().parents[1] / "scripts" / "desktop_research.py"
MUTATIONS = {"create", "prepare", "enter", "send", "answer", "begin", "rebind", "restore", "collect"}


class DesktopFixture:
    """Synthetic external UI; never substitutes for orchestration/storage."""

    boundary = "synthetic"
    helper_identity = {"source_sha256": "a" * 64, "binary_sha256": "b" * 64}

    def __init__(self, root):
        self.root = Path(root)
        self.events = []
        self.knobs = {}
        self.on_action = None

    def model_evidence(self, provider, *, before_input=False):
        """Public fake choices and a complete characterized capability order."""
        evidence = {
            "selection_supported": True,
            "choices_complete": True,
            "ranking_complete": True,
            "ranking_basis": "synthetic-contract-v1:characterized-capability-ranking",
            "ranking_provider": provider,
            "ranking_version": "fixture-1",
            "available_choices": [
                {"name": "fixture-low", "enabled": True, "research_compatible": True, "rank": 1},
                {"name": "fixture-disabled", "enabled": False, "research_compatible": True, "rank": 4},
                {"name": "fixture-high", "enabled": True, "research_compatible": True, "rank": 2},
                {"name": "fixture-chat-only", "enabled": True, "research_compatible": False, "rank": 3},
            ],
            "selected_model": "fixture-high",
            "selection_verified": True,
            "verified_after_mode": True,
            "verified_before_input": before_input,
            "engine_kind": "selected-model",
            "worker_model": {"kind": "provider-managed", "selected_model": None, "verified": False},
            "restoration": {"required": True, "original_model": "fixture-low"},
        }
        evidence.update(copy.deepcopy(self.knobs.get("model", {})))
        if before_input:
            evidence.update(copy.deepcopy(self.knobs.get("model_before_input", {})))
        return evidence

    def invoke(self, action, job=None, payload=None):
        self.events.append(action)
        job = copy.deepcopy(job or {})
        payload = payload or {}
        if self.on_action:
            self.on_action(action, job, payload)
        provider = job.get("provider", payload.get("provider", "claude"))
        app = {"bundle_id": "synthetic." + provider, "version": "fixture-1", "pid": 101,
               "start_time": 1000, "signature_verified": True, "profile_id": "synthetic-contract-v1",
               "account_hash": "c" * 64, "caller_excluded": True}
        app.update(self.knobs.get("app", {}))
        binding = {"kind": "dedicated-draft", "new": True, "empty": True,
                   "window_id": "fixture-window", "session": app, "caller_excluded": True}
        binding.update(self.knobs.get("draft", {}))
        default = {"ok": True, "boundary": "synthetic", "complete": True, "app": app}
        if not self.knobs.get("model_evidence_absent"):
            default["model_evidence"] = self.model_evidence(provider, before_input=action == "inspect" and payload.get("purpose") == "model-pre-input")
        if action == "doctor":
            ready = self.knobs.get("ready", {}).get(provider, True)
            return {**default, "ready": ready, "accessibility": True, "screen_capture": True,
                    "research_available": ready, "mode": "Research" if provider == "claude" else "Deep research",
                    "idle_seconds": self.knobs.get("idle", 5), "reason": "RESEARCH_UNAVAILABLE" if not ready else None,
                    "unrelated_text": "SYNTHETIC-UNRELATED-DO-NOT-LOG", **self.knobs.get("doctor", {})}
        if action == "create":
            return {**default, "draft_binding": binding, "idle_seconds": self.knobs.get("idle", 5),
                    "focus_matches": not self.knobs.get("focus_changed", False),
                    "unexpected_dialog": self.knobs.get("dialog", False)}
        if action == "prepare":
            scope = {"selection_complete": True, "public_web": True, "private_enabled": 0,
                     "private_excludable": True, "restoration": {"required": True, "original_hash": "d" * 64}}
            scope.update(self.knobs.get("sources", {}))
            return {**default, "draft_binding": binding, "mode_selected": True,
                    "source_scope": scope, "idle_seconds": self.knobs.get("idle", 5),
                    "focus_matches": not self.knobs.get("focus_changed", False),
                    "unexpected_dialog": self.knobs.get("dialog", False)}
        if action == "enter":
            persisted = json.loads((self.root / "reports" / job["id"] / "job.json").read_text())
            if not persisted.get("input_intent") or not persisted.get("source_evidence"):
                raise AssertionError("Input reached before durable intent and source evidence")
            prompt = Path(payload["prompt_file"]).read_bytes()
            digest = hashlib.sha256(prompt).hexdigest()
            return {**default, "draft_binding": binding, "full_value_sha256": "0" * 64 if self.knobs.get("truncated") else digest,
                    "end_markers": not self.knobs.get("truncated", False),
                    "focus_matches": not self.knobs.get("focus_changed_after_entry", False),
                    "idle_seconds": self.knobs.get("idle", 5)}
        if action in {"send", "answer", "begin"}:
            persisted = json.loads((self.root / "reports" / job["id"] / "job.json").read_text())
            if not persisted.get("prompt_possibly_sent") or not persisted.get("send_intent"):
                raise AssertionError("Send reached before synced possible-send intent")
            oracle = self.root / "synthetic-desktop-sends.json"
            records = json.loads(oracle.read_text()) if oracle.exists() else []
            records.append({"id": job["id"], "marker": job["marker"], "action": action})
            oracle.write_text(json.dumps(records))
            if self.knobs.get("kill_after_send"):
                os._exit(71)
            if self.knobs.get("lose_after_send"):
                raise RuntimeError("Synthetic observer disconnected after event")
            if action in {"answer", "begin"}:
                self.knobs["state"] = "RUNNING"
            return {**default, "event_posted": True}
        if action in {"observe", "rebind", "inspect"}:
            state = self.knobs.get("state", "RUNNING")
            count = self.knobs.get("matches", 1)
            observation = {**default, "complete": self.knobs.get("observation_complete", True),
                           "match_count": count, "role": self.knobs.get("role", "USER"),
                           "marker": "foreign" if self.knobs.get("wrong_marker") else job.get("marker"),
                           "window_id": "fixture-window", "state": state,
                           "final_result": state == "COMPLETED", "pending_interaction": state in {"AWAITING_PLAN", "AWAITING_CLARIFICATION"},
                           "positive_unsent": self.knobs.get("positive_unsent", False),
                           "draft_binding": binding, "full_value_sha256": job.get("entered_sha256"),
                           "question": "Which public format?" if state == "AWAITING_CLARIFICATION" else None,
                           "question_hash": "e" * 64 if state == "AWAITING_CLARIFICATION" else None,
                           "plan": "Search official public help sources." if state == "AWAITING_PLAN" else None,
                           "plan_hash": "f" * 64 if state == "AWAITING_PLAN" else None,
                           "plan_source_scope": self.knobs.get("plan_scope", "public-web"),
                           "reason": "QUOTA" if state == "BLOCKED" else None}
            if action == "inspect" and payload.get("purpose") == "model-pre-input":
                observation.update(mode_selected=True,
                                   idle_seconds=self.knobs.get("idle", 5),
                                   focus_matches=not self.knobs.get("focus_changed", False),
                                   unexpected_dialog=self.knobs.get("dialog", False))
            observation.update(self.knobs.get("observation", {}))
            return observation
        if action == "restore":
            return {**default, "restored": not self.knobs.get("restore_pending", False),
                    "pending": ["source-control-unobservable"] if self.knobs.get("restore_pending") else []}
        if action == "collect":
            return {**default, "ok": False, "reason": "SYNTHETIC_EXPORT_FAILED"}
        raise AssertionError("Unexpected desktop action: " + action)


def load_production():
    if not SCRIPT.exists():
        raise AssertionError("NOT_IMPLEMENTED: production desktop_research.py is absent")
    spec = importlib.util.spec_from_file_location("desktop_research_under_test", SCRIPT)
    module = importlib.util.module_from_spec(spec)
    import sys
    sys.modules[spec.name] = module
    spec.loader.exec_module(module)
    return module


def run_cli(module, root, boundary, arguments):
    out = io.StringIO()
    with contextlib.redirect_stdout(out), contextlib.redirect_stderr(out):
        code = module.main(["--synthetic-root", str(root), *arguments], boundary=boundary)
    lines = [line for line in out.getvalue().splitlines() if line.startswith("{")]
    result = json.loads(lines[-1]) if lines else {}
    return code, result, out.getvalue()


def process_call(root, arguments, knobs=None):
    boundary = DesktopFixture(root)
    boundary.knobs.update(knobs or {})
    run_cli(load_production(), root, boundary, arguments)


class ExecutionContract(unittest.TestCase):
    def setUp(self):
        self.module = load_production()
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.root = Path(self.temp.name) / "store"
        self.root.mkdir(mode=0o700)
        self.prompt = Path(self.temp.name) / "question.txt"
        self.prompt.write_text("Compare public official Research interfaces.\nInclude dated sources.\n")
        self.boundary = DesktopFixture(self.root)

    def call(self, *args, boundary=None):
        return run_cli(self.module, self.root, boundary or self.boundary, list(args))

    def start(self, request="request-1", provider="claude", uncertain=False):
        args = ["start", "--provider", provider, "--request-id", request,
                "--prompt-file", str(self.prompt)]
        if uncertain:
            args += ["--receipt-timeout", "0"]
        code, result, output = self.call(*args)
        self.assertIn("job_id", result, output)
        return result["job_id"]

    def job(self, job_id):
        return json.loads((self.root / "reports" / job_id / "job.json").read_text())

    def seed(self, job_id, **updates):
        """Real on-disk fixture setup, never a fake persistence implementation."""
        job = self.job(job_id)
        job.update(updates)
        (self.root / "reports" / job_id / "job.json").write_text(json.dumps(job))
        ledger = json.loads((self.root / "ledger.json").read_text())
        ledger["jobs"][job_id] = job
        (self.root / "ledger.json").write_text(json.dumps(ledger))

    def test_doctor_reports_observed_capabilities(self):
        code, result, _ = self.call("doctor", "--provider", "claude")
        self.assertEqual(code, 0)
        self.assertTrue(result["ready"])
        self.assertEqual(result["app"]["version"], "fixture-1")
        self.assertEqual(result["boundary"], "synthetic")
        self.assertFalse(set(self.boundary.events) & MUTATIONS)

    def test_start_blocks_without_research_readiness(self):
        self.boundary.knobs["ready"] = {"claude": False}
        job_id = self.start()
        self.assertEqual(self.job(job_id)["provider_state"], "BLOCKED")
        self.assertFalse(self.job(job_id)["prompt_possibly_sent"])
        self.assertFalse(set(self.boundary.events) & {"enter", "send"})
        other = DesktopFixture(self.root)
        other.knobs["doctor"] = {"accessibility": False}
        _, result, _ = self.call("start", "--provider", "claude", "--request-id", "permission", "--prompt-file", str(self.prompt), boundary=other)
        self.assertEqual(self.job(result["job_id"])["provider_state"], "BLOCKED")
        self.assertFalse(set(other.events) & MUTATIONS)

    def test_highest_research_model_verified_before_input(self):
        for provider in ("claude", "chatgpt"):
            with self.subTest(provider=provider):
                boundary = DesktopFixture(self.root)
                boundary.knobs["state"] = "COMPLETED"
                before_input = []
                checked = []

                def observe(action, job, payload):
                    if action == "inspect" and payload.get("purpose") == "model-pre-input":
                        self.assertEqual(payload.get("model_policy"), "highest-available")
                        self.assertEqual(payload.get("selected_model"), "fixture-high")
                        self.assertIn("prepare", boundary.events)
                        self.assertNotIn("enter", boundary.events)
                        checked.append(action)
                    if action == "enter":
                        disk = self.job(job["id"])
                        ledger = json.loads((self.root / "ledger.json").read_text())
                        self.assertEqual(disk.get("model_policy"), "highest-available")
                        self.assertEqual(disk.get("selected_model"), "fixture-high")
                        self.assertEqual(payload.get("model_policy"), "highest-available")
                        self.assertEqual(payload.get("selected_model"), "fixture-high")
                        evidence = disk.get("model_evidence", {})
                        self.assertTrue(evidence.get("selection_verified"))
                        self.assertTrue(evidence.get("verified_after_mode"))
                        self.assertTrue(evidence.get("verified_before_input"))
                        self.assertEqual(evidence.get("ranking_basis"), "synthetic-contract-v1:characterized-capability-ranking")
                        self.assertEqual(evidence.get("ranking_provider"), provider)
                        self.assertEqual(evidence.get("ranking_version"), "fixture-1")
                        self.assertEqual(evidence.get("available_choices"), boundary.model_evidence(provider)["available_choices"])
                        self.assertEqual(evidence.get("engine_kind"), "selected-model")
                        self.assertEqual(evidence.get("worker_model"),
                                         {"kind": "provider-managed", "selected_model": None, "verified": False})
                        self.assertEqual(evidence.get("restoration", {}).get("original_model"), "fixture-low")
                        self.assertLessEqual(evidence["timestamp"], disk["input_intent"]["timestamp"])
                        self.assertEqual(ledger["jobs"][job["id"]]["model_evidence"], evidence)
                        self.assertEqual(checked, ["inspect"])
                        before_input.append(copy.deepcopy(evidence))

                boundary.on_action = observe
                code, result, output = self.call("start", "--provider", provider,
                                                 "--request-id", "highest-" + provider,
                                                 "--prompt-file", str(self.prompt), boundary=boundary)
                self.assertEqual(code, 0, output)
                self.assertEqual(len(before_input), 1)
                job_id = result["job_id"]
                receipt = self.job(job_id)
                self.assertEqual(receipt["provider_state"], "COMPLETED")
                self.assertEqual(receipt["model_evidence"], before_input[0])
                self.assertEqual(receipt["selected_model"], "fixture-high")
                self.assertEqual(receipt["model_policy"], "highest-available")
                self.assertLessEqual(receipt["model_evidence"]["timestamp"], receipt["send_intent"]["timestamp"])
                self.assertEqual(boundary.events.count("send"), 1)
                self.call("status", "--job", job_id, boundary=boundary)
                self.assertEqual(self.job(job_id)["model_evidence"], before_input[0])

    def test_model_unavailable_or_unverifiable_blocks_submission(self):
        invalid = [
            ("missing-evidence", {"model_evidence_absent": True}),
            ("unsupported-selection", {"model": {"selection_supported": False}}),
            ("partial-choices", {"model": {"choices_complete": False}}),
            ("partial-ranking", {"model": {"ranking_complete": False}}),
            ("unknown-ranking", {"model": {"ranking_basis": None}}),
            ("uncharacterized-version", {"model": {"ranking_version": "fixture-uncharacterized"}}),
            ("wrong-provider-ranking", {"model": {"ranking_provider": "other-provider"}}),
            ("unverified-selection", {"model": {"selection_verified": False}}),
            ("unverified-after-mode", {"model": {"verified_after_mode": False}}),
            ("lower-model", {"model": {"selected_model": "fixture-low"}}),
            ("auto-label", {"model": {"selected_model": "Auto"}}),
            ("no-enabled-compatible-model", {"model": {"available_choices": [
                {"name": "fixture-high", "enabled": False, "research_compatible": True, "rank": 2},
                {"name": "fixture-low", "enabled": True, "research_compatible": False, "rank": 1},
            ]}}),
            ("unranked-enabled-model", {"model": {"available_choices": [
                {"name": "fixture-high", "enabled": True, "research_compatible": True, "rank": 2},
                {"name": "fixture-unknown", "enabled": True, "research_compatible": True, "rank": None},
            ]}}),
        ]
        for provider in ("claude", "chatgpt"):
            for name, knobs in invalid:
                with self.subTest(provider=provider, case=name):
                    boundary = DesktopFixture(self.root)
                    # If the missing guard sends anyway, allow the next case
                    # to reach preparation instead of a provider-active gate.
                    boundary.knobs["state"] = "COMPLETED"
                    boundary.knobs.update(copy.deepcopy(knobs))
                    code, result, output = self.call("start", "--provider", provider,
                                                     "--request-id", "model-" + provider + "-" + name,
                                                     "--prompt-file", str(self.prompt), boundary=boundary)
                    self.assertNotEqual(code, 0, output)
                    self.assertIn("job_id", result, output)
                    job = self.job(result["job_id"])
                    self.assertEqual(job["provider_state"], "BLOCKED")
                    self.assertRegex(job.get("reason", ""), r"^MODEL_[A-Z0-9_]+$")
                    self.assertEqual(result["reason"], job["reason"])
                    self.assertFalse(job["prompt_possibly_sent"])
                    self.assertFalse(set(boundary.events) & {"enter", "send"})
                    self.assertFalse((self.root / "synthetic-desktop-sends.json").exists())

    def test_model_change_before_input_blocks_submission(self):
        for provider in ("claude", "chatgpt"):
            for change in ({"selected_model": "fixture-low"}, {"selection_verified": False},
                           {"verified_before_input": False}, {"choices_complete": False}):
                with self.subTest(provider=provider, change=change):
                    boundary = DesktopFixture(self.root)
                    boundary.knobs["state"] = "COMPLETED"
                    boundary.knobs["model_before_input"] = change
                    checks = []

                    def observe(action, job, payload):
                        if action == "inspect" and payload.get("purpose") == "model-pre-input":
                            self.assertEqual(payload.get("selected_model"), "fixture-high")
                            self.assertIn("prepare", boundary.events)
                            self.assertNotIn("enter", boundary.events)
                            checks.append(action)

                    boundary.on_action = observe
                    code, result, output = self.call("start", "--provider", provider,
                                                     "--request-id", "reset-" + provider + "-" + str(len(self.job_ids())),
                                                     "--prompt-file", str(self.prompt), boundary=boundary)
                    self.assertNotEqual(code, 0, output)
                    self.assertEqual(checks, ["inspect"])
                    job = self.job(result["job_id"])
                    self.assertRegex(job.get("reason", ""), r"^MODEL_[A-Z0-9_]+$")
                    self.assertFalse(job["prompt_possibly_sent"])
                    self.assertFalse(set(boundary.events) & {"enter", "send"})
                    self.assertFalse((self.root / "synthetic-desktop-sends.json").exists())

    def test_start_requires_observed_receipt(self):
        job_id = self.start(uncertain=True)
        job = self.job(job_id)
        self.assertEqual(job["provider_state"], "SUBMISSION_UNCERTAIN")
        self.assertTrue(job["prompt_possibly_sent"])
        self.assertIsNone(job.get("submitted_binding"))
        self.call("status", "--job", job_id)
        self.assertEqual(self.job(job_id)["provider_state"], "RUNNING")
        self.assertEqual(self.job(job_id)["submitted_binding"]["marker"], job["marker"])
        self.assertEqual(self.boundary.events.count("send"), 1)

    def test_focus_change_preserves_draft_and_stops_input(self):
        for knob in ({"draft": {"empty": False}}, {"focus_changed": True}):
            with self.subTest(knob=knob):
                b = DesktopFixture(self.root)
                b.knobs.update(knob)
                code, result, _ = self.call("start", "--provider", "claude", "--request-id", "guard-" + str(len(self.job_ids())),
                                             "--prompt-file", str(self.prompt), boundary=b)
                self.assertNotEqual(code, 0)
                self.assertFalse(set(b.events) & {"enter", "send"})
                self.assertFalse(self.job(result["job_id"])["prompt_possibly_sent"])

    def job_ids(self):
        path = self.root / "ledger.json"
        return list(json.loads(path.read_text())["jobs"]) if path.exists() else []

    def test_authorized_plan_can_start_research(self):
        self.boundary.knobs["state"] = "AWAITING_PLAN"
        job_id = self.start()
        before = self.job(job_id)
        self.assertEqual(before["provider_state"], "AWAITING_PLAN")
        code, _, _ = self.call("approve-plan", "--job", job_id, "--plan-hash", "0" * 64)
        self.assertNotEqual(code, 0)
        self.assertNotIn("begin", self.boundary.events)
        code, _, _ = self.call("approve-plan", "--job", job_id, "--plan-hash", "f" * 64)
        self.assertEqual(code, 0)
        self.assertEqual(self.boundary.events.count("begin"), 1)
        self.assertEqual(self.job(job_id)["approved_plan_hash"], "f" * 64)
        self.assertEqual(self.job(job_id)["provider_state"], "RUNNING")

    def test_unresolved_question_or_plan_scope_stops(self):
        self.boundary.knobs["state"] = "AWAITING_CLARIFICATION"
        job_id = self.start()
        self.call("wait", "--job", job_id, "--timeout", "0")
        self.assertEqual(self.job(job_id)["provider_state"], "AWAITING_CLARIFICATION")
        self.assertNotIn("answer", self.boundary.events)
        self.seed(job_id, provider_state="AWAITING_PLAN", plan_hash="f" * 64, plan_source_scope="private")
        self.boundary.knobs["plan_scope"] = "private"
        code, _, _ = self.call("approve-plan", "--job", job_id, "--plan-hash", "f" * 64)
        self.assertNotEqual(code, 0)
        self.assertNotIn("begin", self.boundary.events)

    def test_quota_state_does_not_resubmit(self):
        self.boundary.knobs["state"] = "BLOCKED"
        job_id = self.start()
        self.assertEqual(self.job(job_id)["reason"], "QUOTA")
        for _ in range(2):
            self.call("start", "--job", job_id)
            self.call("status", "--job", job_id)
        self.assertEqual(self.boundary.events.count("send"), 1)
        self.assertTrue(self.job(job_id)["prompt_possibly_sent"])

    def test_wait_timeout_preserves_job_and_resume_does_not_submit(self):
        job_id = self.start()
        self.call("wait", "--job", job_id, "--timeout", "0")
        self.assertEqual(self.job(job_id)["provider_state"], "RUNNING")
        self.assertEqual(self.job(job_id)["observation"]["outcome"], "TIMEOUT")
        self.call("start", "--job", job_id)
        self.call("status", "--job", job_id)
        self.assertEqual(self.boundary.events.count("send"), 1)

    def test_crash_after_send_requires_reconciliation(self):
        self.boundary.knobs["lose_after_send"] = True
        job_id = self.start()
        self.assertEqual(self.job(job_id)["provider_state"], "SUBMISSION_UNCERTAIN")
        self.boundary.knobs.update(lose_after_send=False, matches=0)
        self.call("start", "--job", job_id)
        self.assertEqual(self.job(job_id)["provider_state"], "SUBMISSION_UNCERTAIN")
        self.boundary.knobs["matches"] = 1
        self.call("status", "--job", job_id)
        self.assertEqual(self.job(job_id)["provider_state"], "RUNNING")
        self.assertEqual(self.boundary.events.count("send"), 1)

    def test_desktop_lock_serializes_processes(self):
        fd = os.open(self.root / "desktop.lock", os.O_CREAT | os.O_RDWR, 0o600)
        try:
            fcntl.flock(fd, fcntl.LOCK_EX | fcntl.LOCK_NB)
            code, result, _ = self.call("start", "--provider", "claude", "--request-id", "locked", "--prompt-file", str(self.prompt))
            self.assertNotEqual(code, 0)
            self.assertEqual(result["reason"], "DESKTOP_BUSY")
            self.assertFalse(set(self.boundary.events) & MUTATIONS)
        finally:
            os.close(fd)
        self.start("locked")
        self.assertEqual(self.boundary.events.count("send"), 1)

    def batch_file(self):
        path = Path(self.temp.name) / "batch.json"
        path.write_text(json.dumps({"batch_id": "batch-one", "entries": [
            {"entry_id": "c", "provider": "claude", "prompt_file": str(self.prompt), "source_scope": "public-web"},
            {"entry_id": "g", "provider": "chatgpt", "prompt_file": str(self.prompt), "source_scope": "public-web"}]}))
        return path

    def test_batch_preserves_per_provider_outcomes(self):
        self.boundary.knobs["ready"] = {"chatgpt": False}
        code, result, _ = self.call("batch", "--request-file", str(self.batch_file()), "--timeout", "0")
        self.assertNotEqual(code, 0)
        self.assertEqual(len(result["entries"]), 2)
        states = {e["provider"]: e["provider_state"] for e in result["entries"]}
        self.assertEqual(states, {"claude": "RUNNING", "chatgpt": "BLOCKED"})
        self.assertFalse(result["fully_collected"])

    def test_default_diagnostics_redact_unrelated_data(self):
        _, _, text = self.call("doctor", "--provider", "claude")
        job_id = self.start()
        _, _, listed = self.call("list")
        _, _, inspected = self.call("inspect", "--job", job_id)
        for output in (text, listed, inspected):
            self.assertNotIn("SYNTHETIC-UNRELATED-DO-NOT-LOG", output)
            self.assertNotIn(self.prompt.read_text().strip(), output)
            self.assertNotIn("Which public format?", output)

    def test_public_source_selection_is_in_receipt(self):
        source_seen = []
        def observe(action, job, payload):
            if action == "enter":
                disk = self.job(job["id"])
                source_seen.append(disk["source_evidence"])
                self.assertLessEqual(disk["source_evidence"]["timestamp"], disk["input_intent"]["timestamp"])
        self.boundary.on_action = observe
        job_id = self.start()
        self.assertEqual(len(source_seen), 1)
        self.assertTrue(source_seen[0]["selection_complete"])
        self.assertEqual(self.job(job_id)["source_scope"], "public-web")

    def test_start_blocks_on_unconstrainable_private_source(self):
        self.boundary.knobs["sources"] = {"private_enabled": 1, "private_excludable": False}
        job_id = self.start()
        self.assertEqual(self.job(job_id)["provider_state"], "BLOCKED")
        self.assertNotIn("enter", self.boundary.events)
        self.assertNotIn("send", self.boundary.events)

    def test_start_blocks_when_source_scope_unverifiable(self):
        self.boundary.knobs["sources"] = {"selection_complete": False}
        job_id = self.start()
        self.assertEqual(self.job(job_id)["reason"], "SOURCE_SCOPE_UNOBSERVABLE")
        self.assertNotIn("enter", self.boundary.events)

    def test_source_selection_side_effects_restored(self):
        self.boundary.knobs["state"] = "COMPLETED"
        job_id = self.start()
        self.assertTrue(self.job(job_id)["restoration_pending"])
        self.assertNotIn("restore", self.boundary.events)
        self.boundary.knobs["restore_pending"] = True
        self.call("collect", "--job", job_id)
        self.assertTrue(self.job(job_id)["restoration_pending"])
        self.boundary.knobs["restore_pending"] = False
        self.call("collect", "--job", job_id)
        self.assertFalse(self.job(job_id)["restoration_pending"])
        self.assertEqual(self.job(job_id)["provider_state"], "COMPLETED")

    def test_pre_send_binding_bootstrap_and_upgrade(self):
        seen = []
        def observe(action, job, payload):
            if action == "create":
                import sys
                self.assertIn(job["id"], sys.stdout.getvalue())
                self.assertEqual(self.job(job["id"])["provider_state"], "PREPARED")
            if action == "enter":
                disk = self.job(job["id"])
                self.assertEqual(disk["draft_binding"]["kind"], "dedicated-draft")
                self.assertTrue(disk["draft_binding"]["new"])
                self.assertIsNone(disk.get("submitted_binding"))
                seen.append(action)
            if action == "send":
                disk = self.job(job["id"])
                self.assertFalse(disk["draft_authority"])
                self.assertEqual(disk["provider_state"], "SUBMISSION_UNCERTAIN")
        self.boundary.on_action = observe
        job_id = self.start(uncertain=True)
        self.assertEqual(seen, ["enter"])
        self.boundary.knobs["role"] = "ASSISTANT"
        self.call("status", "--job", job_id)
        self.assertIsNone(self.job(job_id).get("submitted_binding"))
        self.boundary.knobs["role"] = "USER"
        self.call("status", "--job", job_id)
        self.assertEqual(self.job(job_id)["submitted_binding"]["role"], "USER")

    def test_missing_or_ambiguous_rebind_preserves_state(self):
        job_id = self.start()
        for count in (0, 2):
            self.boundary.knobs["matches"] = count
            code, _, _ = self.call("rebind", "--job", job_id)
            self.assertNotEqual(code, 0)
            self.assertEqual(self.job(job_id)["provider_state"], "RUNNING")
        self.assertEqual(self.boundary.events.count("send"), 1)

    def test_wrong_conversation_blocks_mutation_and_collection(self):
        self.boundary.knobs["state"] = "COMPLETED"
        job_id = self.start()
        self.boundary.knobs["wrong_marker"] = True
        for cmd in ("collect", "respond", "approve-plan"):
            args = [cmd, "--job", job_id]
            if cmd == "respond":
                args += ["--answer-file", str(self.prompt)]
            if cmd == "approve-plan":
                args += ["--plan-hash", "f" * 64]
            code, _, _ = self.call(*args)
            self.assertNotEqual(code, 0)
        self.assertFalse(set(self.boundary.events) & {"collect", "answer", "begin"})

    def test_start_retry_after_killed_start_does_not_resubmit(self):
        args = ["start", "--provider", "claude", "--request-id", "killed", "--prompt-file", str(self.prompt)]
        process = multiprocessing.get_context("fork").Process(target=process_call, args=(self.root, args, {"kill_after_send": True}))
        process.start(); process.join(5)
        self.assertEqual(process.exitcode, 71)
        self.assertEqual(len(self.job_ids()), 1)
        job_id = self.job_ids()[0]
        self.assertTrue(self.job(job_id)["prompt_possibly_sent"])
        self.call(*args)
        records = json.loads((self.root / "synthetic-desktop-sends.json").read_text())
        self.assertEqual(len(records), 1)
        self.assertEqual(records[0]["id"], job_id)
        # Recover an interrupted durable mirror update, including request-id
        # mappings. This is on-disk fixture state, not mocked persistence.
        ledger_path = self.root / "ledger.json"
        ledger = json.loads(ledger_path.read_text())
        pending = self.job(job_id)
        pending["revision"] += 1
        recovered_requests = {**ledger["requests"], "claude:journal-alias": job_id}
        (self.root / "transaction.json").write_text(json.dumps({"job_id": job_id, "job": pending,
                                                               "requests": recovered_requests, "batches": ledger["batches"]}))
        (self.root / "transaction.json").chmod(0o600)
        alias_args = ["start", "--provider", "claude", "--request-id", "journal-alias", "--prompt-file", str(self.prompt)]
        _, result, _ = self.call(*alias_args)
        self.assertEqual(result["job_id"], job_id)
        self.assertFalse((self.root / "transaction.json").exists())
        self.assertEqual(len(json.loads((self.root / "synthetic-desktop-sends.json").read_text())), 1)
        changed = Path(self.temp.name) / "changed.txt"
        changed.write_text("A different authorized question.\n")
        before = self.boundary.events.count("send")
        code, result, _ = self.call("start", "--provider", "claude", "--request-id", "killed", "--prompt-file", str(changed))
        self.assertNotEqual(code, 0)
        self.assertEqual(result["reason"], "REQUEST_ID_CONFLICT")
        self.assertEqual(self.boundary.events.count("send"), before)

    def test_provider_uncertainty_blocks_across_checkouts(self):
        job_id = self.start(uncertain=True)
        self.boundary.knobs["matches"] = 0
        alternate = Path(self.temp.name) / "other-checkout"
        alternate.mkdir()
        prior = Path.cwd()
        try:
            os.chdir(alternate)
            code, result, _ = self.call("start", "--provider", "claude", "--request-id", "different", "--prompt-file", str(self.prompt))
        finally:
            os.chdir(prior)
        self.assertNotEqual(code, 0)
        self.assertEqual(result["reason"], "PROVIDER_UNCERTAIN")
        self.assertEqual(result["blocking_job_id"], job_id)
        self.assertEqual(self.boundary.events.count("send"), 1)

    def test_batch_rerun_resumes_without_resubmission(self):
        path = self.batch_file()
        self.call("batch", "--request-file", str(path), "--timeout", "0")
        first = json.loads((self.root / "ledger.json").read_text())["batches"]["batch-one"]
        count = self.boundary.events.count("send")
        self.call("batch", "--request-file", str(path), "--timeout", "0")
        self.assertEqual(json.loads((self.root / "ledger.json").read_text())["batches"]["batch-one"], first)
        self.assertEqual(self.boundary.events.count("send"), count)

    def test_concurrent_job_updates_keep_state(self):
        job_id = self.start()
        ctx = multiprocessing.get_context("fork")
        p1 = ctx.Process(target=process_call, args=(self.root, ["status", "--job", job_id], {"state": "COMPLETED"}))
        p2 = ctx.Process(target=process_call, args=(self.root, ["collect", "--job", job_id], {"state": "COMPLETED"}))
        p1.start(); p2.start(); p1.join(5); p2.join(5)
        self.assertEqual((p1.exitcode, p2.exitcode), (0, 0))
        job = self.job(job_id)
        self.assertEqual(job["provider_state"], "COMPLETED")
        self.assertEqual(job["collection_state"], "FAILED")
        self.assertGreaterEqual(job["revision"], 3)
        self.assertEqual(json.loads((self.root / "ledger.json").read_text())["jobs"][job_id], job)

    def test_transition_table_and_command_ownership(self):
        job_id = self.start(uncertain=True)
        self.boundary.knobs["matches"] = 0
        for state in ("RUNNING", "COMPLETED", "BLOCKED", "FAILED", "CANCELLED", "AWAITING_PLAN", "AWAITING_CLARIFICATION", "SUBMISSION_UNCERTAIN"):
            self.seed(job_id, provider_state=state, prompt_possibly_sent=True)
            before = self.boundary.events.count("send")
            self.call("start", "--job", job_id)
            self.assertEqual(self.boundary.events.count("send"), before, state)
        self.boundary.knobs.update(matches=1, state="AWAITING_CLARIFICATION")
        self.seed(job_id, provider_state="RUNNING")
        self.call("status", "--job", job_id)
        answer = Path(self.temp.name) / "answer.txt"
        answer.write_text("Use public Markdown.\n")
        self.call("respond", "--job", job_id, "--answer-file", str(answer))
        self.assertEqual(self.boundary.events.count("answer"), 1)
        self.assertEqual(self.job(job_id)["provider_state"], "RUNNING")
        self.boundary.knobs["state"] = "FAILED"
        self.call("status", "--job", job_id)
        self.assertEqual(self.job(job_id)["provider_state"], "FAILED")
        self.boundary.knobs["state"] = "CANCELLED"
        self.call("status", "--job", job_id)
        self.assertEqual(self.job(job_id)["provider_state"], "CANCELLED")
        self.assertTrue(self.job(job_id)["prompt_possibly_sent"])
        # Absence does not authorize another Send; a positive unsent receipt
        # must match the same owned draft and full composer hash.
        other = self.start("positive-unsent", uncertain=True)
        self.boundary.knobs.update(matches=0, positive_unsent=False)
        self.call("status", "--job", other)
        self.assertEqual(self.job(other)["provider_state"], "SUBMISSION_UNCERTAIN")
        self.boundary.knobs["positive_unsent"] = True
        self.call("status", "--job", other)
        self.assertEqual(self.job(other)["provider_state"], "DRAFTING")
        self.assertTrue(self.job(other)["draft_authority"])
        self.boundary.knobs.update(matches=1, positive_unsent=False, state="RUNNING")
        self.call("start", "--job", other)
        self.assertEqual(self.job(other)["provider_state"], "RUNNING")
        self.boundary.knobs.update(state="AWAITING_CLARIFICATION", observation={"question_hash": "9" * 64})
        self.call("status", "--job", other)
        self.boundary.knobs["lose_after_send"] = True
        self.call("respond", "--job", other, "--answer-file", str(answer))
        self.assertEqual(self.job(other)["provider_state"], "SUBMISSION_UNCERTAIN")
        count = self.boundary.events.count("answer")
        self.boundary.knobs["lose_after_send"] = False
        self.call("status", "--job", other)
        self.assertEqual(self.job(other)["provider_state"], "SUBMISSION_UNCERTAIN")
        self.call("respond", "--job", other, "--answer-file", str(answer))
        self.assertEqual(self.boundary.events.count("answer"), count)
        self.boundary.knobs.update(state="RUNNING", observation={})
        self.call("status", "--job", other)
        self.assertEqual(self.job(other)["provider_state"], "RUNNING")

    def test_intent_and_multiline_entry_guard_send(self):
        self.boundary.knobs["truncated"] = True
        job_id = self.start()
        self.assertIn("enter", self.boundary.events)
        self.assertNotIn("send", self.boundary.events)
        self.assertEqual(self.job(job_id)["provider_state"], "DRAFTING")
        self.assertFalse(self.job(job_id)["prompt_possibly_sent"])
        self.assertTrue(self.job(job_id)["input_intent"])
        self.boundary.knobs["truncated"] = False
        self.call("start", "--job", job_id)
        self.assertEqual(self.boundary.events.count("send"), 1)
        self.assertEqual(self.job(job_id)["provider_state"], "RUNNING")

    def test_wait_polling_never_activates(self):
        job_id = self.start()
        self.boundary.events.clear()
        self.boundary.knobs["observation_complete"] = False
        self.call("wait", "--job", job_id, "--timeout", "0")
        self.call("status", "--job", job_id)
        self.assertEqual(self.job(job_id)["provider_state"], "RUNNING")
        self.assertFalse(set(self.boundary.events) & MUTATIONS)

    def test_mutation_deferred_while_user_active(self):
        self.boundary.knobs["idle"] = 0.1
        job_id = self.start()
        self.assertEqual(self.job(job_id)["reason"], "USER_INPUT_ACTIVE")
        self.assertFalse(set(self.boundary.events) & MUTATIONS)

    def test_transaction_lock_is_cross_checkout_and_temp_independent(self):
        probes = []
        def observe(action, job, payload):
            if action in {"prepare", "enter", "send"}:
                fd = os.open(self.root / "desktop.lock", os.O_RDWR)
                try:
                    with self.assertRaises(BlockingIOError):
                        fcntl.flock(fd, fcntl.LOCK_EX | fcntl.LOCK_NB)
                    probes.append(action)
                finally:
                    os.close(fd)
        self.boundary.on_action = observe
        prior = Path.cwd()
        other = Path(self.temp.name) / "checkout-b"
        other.mkdir()
        try:
            os.chdir(other)
            self.start()
        finally:
            os.chdir(prior)
        self.assertEqual(probes, ["prepare", "enter", "send"])
        self.assertFalse((other / "desktop.lock").exists())

    def test_bridge_guard_schema_and_modal_handling(self):
        self.boundary.knobs["dialog"] = True
        job_id = self.start()
        self.assertEqual(self.job(job_id)["reason"], "UNEXPECTED_DIALOG")
        self.assertFalse(set(self.boundary.events) & {"enter", "send"})
        code, _, _ = self.call("unsupported-arbitrary-key-command")
        self.assertNotEqual(code, 0)

    def test_synthetic_receipts_are_rejected_by_live_audit(self):
        self.boundary.knobs["state"] = "COMPLETED"
        job_id = self.start()
        self.assertEqual(self.job(job_id)["boundary"], "synthetic")
        self.assertEqual(self.job(job_id)["helper_identity"]["source_sha256"], "a" * 64)
        code, result, _ = self.call("audit", "--job", job_id, "--require-live")
        self.assertNotEqual(code, 0)
        self.assertEqual(result["reason"], "SYNTHETIC_NOT_LIVE_EVIDENCE")

    def test_target_trust_version_and_caller_isolation(self):
        for fields in ({"signature_verified": False}, {"caller_excluded": False}):
            b = DesktopFixture(self.root)
            b.knobs["app"] = fields
            code, result, _ = self.call("start", "--provider", "claude", "--request-id", "trust-" + str(len(self.job_ids())),
                                        "--prompt-file", str(self.prompt), boundary=b)
            self.assertNotEqual(code, 0)
            self.assertFalse(set(b.events) & {"enter", "send"})
        job_id = self.start("valid")
        self.boundary.knobs["app"] = {"version": "changed-profile"}
        self.call("status", "--job", job_id)
        self.assertEqual(self.job(job_id)["provider_state"], "RUNNING")
        self.assertEqual(self.job(job_id)["observation"]["outcome"], "UNOBSERVABLE")
        self.boundary.knobs["app"] = {"account_hash": "changed-account"}
        self.call("status", "--job", job_id)
        self.assertEqual(self.job(job_id)["provider_state"], "RUNNING")
        self.assertEqual(self.job(job_id)["observation"]["outcome"], "UNOBSERVABLE")


if __name__ == "__main__":
    unittest.main()
