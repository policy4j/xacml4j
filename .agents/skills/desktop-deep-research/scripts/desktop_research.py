#!/usr/bin/env python3
"""Durable, public-web desktop Research jobs (Python 3.10+, stdlib only).

The desktop boundary owns native targeting and fresh in-process input guards.
This module owns authorization, persistence, locks, state and deadlines. Tests
may inject that one boundary into a temporary store; they cannot produce live
acceptance receipts. The native bridge is implemented in a separate task.
"""
from __future__ import annotations

import argparse
import contextlib
import copy
import fcntl
import hashlib
import importlib.util
import json
import math
import os
from pathlib import Path
import re
import secrets
import stat
import sys
import tempfile
import time


PROVIDERS = {"claude": ("com.anthropic.claudefordesktop", "Q6L2SF6YDW"),
             "chatgpt": ("com.openai.codex", "2DC432GLL2")}
STATES = {"PREPARED", "DRAFTING", "SUBMISSION_UNCERTAIN", "AWAITING_CLARIFICATION",
          "AWAITING_PLAN", "RUNNING", "COMPLETED", "BLOCKED", "FAILED", "CANCELLED"}
TERMINAL = {"COMPLETED", "BLOCKED", "FAILED", "CANCELLED"}
PENDING = {"SUBMISSION_UNCERTAIN", "AWAITING_CLARIFICATION", "AWAITING_PLAN", "RUNNING"}
TOKEN = re.compile(r"[A-Za-z0-9][A-Za-z0-9_.:-]{0,127}\Z")
HASH = re.compile(r"[a-f0-9]{64}\Z")
MAX_FILE = 65536
APP_FIELDS = {"bundle_id", "version", "pid", "start_time", "signature_verified",
              "team_id", "profile_id", "account_hash", "caller_excluded"}
BINDING_FIELDS = {"kind", "new", "empty", "window_id", "tab_id", "conversation_id",
                  "caller_excluded", "session", "marker", "role", "request_id"}
SCALAR_FIELDS = {"ok", "complete", "boundary", "reason", "ready", "accessibility",
                 "screen_capture", "research_available", "mode", "idle_seconds",
                 "focus_matches", "unexpected_dialog", "mode_selected",
                 "full_value_sha256", "end_markers", "event_posted", "match_count",
                 "role", "marker", "window_id", "state", "final_result",
                 "pending_interaction", "positive_unsent", "question", "question_hash",
                 "plan", "plan_hash", "plan_source_scope", "restored"}
SCALAR_FIELDS |= {"self_test_passed", "self_test_count", "helper_path"}


class Fault(Exception):
    def __init__(self, reason, **details):
        self.reason, self.details = reason, details
        super().__init__(reason)


def emit(value):
    print(json.dumps(value, sort_keys=True, ensure_ascii=True), flush=True)


def digest(value):
    return hashlib.sha256(value).hexdigest()


def identifier(value):
    if not isinstance(value, str) or not TOKEN.fullmatch(value):
        raise Fault("INVALID_IDENTIFIER")
    return value


def safe_path(value):
    """Reject symlink components, including absent-target symlinks.

    macOS's fixed /var and /tmp aliases are canonicalized before inspection;
    user-controlled links in the remaining path are never followed.
    """
    path = Path(os.path.abspath(os.fspath(value)))
    for alias, target in (("/var", "/private/var"), ("/tmp", "/private/tmp")):
        if sys.platform == "darwin" and (str(path) == alias or str(path).startswith(alias + "/")):
            path = Path(target + str(path)[len(alias):])
    for part in [*reversed(path.parents), path]:
        try:
            if stat.S_ISLNK(part.lstat().st_mode):
                raise Fault("UNSAFE_PATH")
        except FileNotFoundError:
            pass
    return path


def private_directory(path):
    path = safe_path(path)
    missing = []
    ancestor = path
    while not ancestor.exists():
        missing.append(ancestor)
        ancestor = ancestor.parent
    for folder in reversed(missing):
        try:
            folder.mkdir(mode=0o700)
        except FileExistsError:
            pass
    info = path.lstat()
    if not stat.S_ISDIR(info.st_mode) or info.st_uid != os.getuid():
        raise Fault("UNSAFE_DIRECTORY")
    os.chmod(path, 0o700)
    return path


def directory_fd(path):
    return os.open(safe_path(path), os.O_RDONLY | os.O_DIRECTORY | os.O_NOFOLLOW)


def atomic_write(path, data):
    """Owner-only replacement; fsync bytes and parent, never follow links."""
    path = safe_path(path)
    parent = directory_fd(path.parent)
    temporary = "." + path.name + "." + secrets.token_hex(12)
    try:
        try:
            info = os.stat(path.name, dir_fd=parent, follow_symlinks=False)
            if not stat.S_ISREG(info.st_mode) or info.st_uid != os.getuid() or info.st_nlink != 1:
                raise Fault("UNSAFE_FILE")
        except FileNotFoundError:
            pass
        fd = os.open(temporary, os.O_CREAT | os.O_EXCL | os.O_WRONLY | os.O_NOFOLLOW, 0o600, dir_fd=parent)
        try:
            with os.fdopen(fd, "wb") as stream:
                stream.write(data)
                stream.flush()
                os.fsync(stream.fileno())
            os.replace(temporary, path.name, src_dir_fd=parent, dst_dir_fd=parent)
            os.fsync(parent)
        finally:
            try:
                os.unlink(temporary, dir_fd=parent)
            except FileNotFoundError:
                pass
    finally:
        os.close(parent)


def read_bytes(path, limit=MAX_FILE, private=False):
    path = safe_path(path)
    fd = os.open(path, os.O_RDONLY | os.O_NOFOLLOW)
    with os.fdopen(fd, "rb") as stream:
        before = os.fstat(stream.fileno())
        if not stat.S_ISREG(before.st_mode) or before.st_nlink != 1:
            raise Fault("UNSAFE_FILE")
        if private and (before.st_uid != os.getuid() or stat.S_IMODE(before.st_mode) & 0o077):
            raise Fault("UNSAFE_FILE_PERMISSIONS")
        if before.st_size > limit:
            raise Fault("FILE_TOO_LARGE")
        data = stream.read(limit + 1)
        after = os.fstat(stream.fileno())
        if len(data) > limit or (before.st_size, before.st_mtime_ns) != (after.st_size, after.st_mtime_ns):
            raise Fault("FILE_CHANGED")
        return data


def read_json(path):
    value = json.loads(read_bytes(path, 16 * 1024 * 1024, private=True))
    if not isinstance(value, dict):
        raise Fault("INVALID_RECEIPT")
    return value


def write_json(path, value):
    atomic_write(path, (json.dumps(value, sort_keys=True, indent=2) + "\n").encode())


def question_file(path):
    data = read_bytes(path)
    try:
        text = data.decode("utf-8")
    except UnicodeError:
        raise Fault("PROMPT_NOT_UTF8") from None
    if not text.strip() or "\x00" in text:
        raise Fault("INVALID_PROMPT")
    return data


@contextlib.contextmanager
def file_lock(path, timeout=3.0, reason="STORE_BUSY"):
    path = safe_path(path)
    fd = os.open(path, os.O_CREAT | os.O_RDWR | os.O_NOFOLLOW, 0o600)
    try:
        info = os.fstat(fd)
        if info.st_uid != os.getuid() or not stat.S_ISREG(info.st_mode) or info.st_nlink != 1:
            raise Fault("UNSAFE_LOCK")
        os.fchmod(fd, 0o600)
        deadline = time.monotonic() + timeout
        while True:
            try:
                fcntl.flock(fd, fcntl.LOCK_EX | fcntl.LOCK_NB)
                break
            except BlockingIOError:
                if time.monotonic() >= deadline:
                    raise Fault(reason) from None
                time.sleep(0.01)
        yield fd
    finally:
        os.close(fd)


class Store:
    """Ledger lock → job lock → desktop lock. A write-ahead journal covers
    atomic job/ledger mirroring, including a crash between their replacements.
    Unknown legacy fields are preserved; legacy provenance is never promoted.
    """
    def __init__(self, root):
        self.root = private_directory(root)
        self.reports = private_directory(self.root / "reports")
        self.locks = private_directory(self.root / "locks")

    @contextlib.contextmanager
    def ledger(self):
        with file_lock(self.root / "ledger.lock"):
            path = self.root / "ledger.json"
            ledger = read_json(path) if path.exists() else {"schema_version": 1, "jobs": {}, "requests": {}, "batches": {}}
            for key in ("jobs", "requests", "batches"):
                ledger.setdefault(key, {})
                if not isinstance(ledger[key], dict):
                    raise Fault("INVALID_LEDGER")
            journal = self.root / "transaction.json"
            if journal.exists():
                saved = read_json(journal)
                job = saved.get("job")
                if not isinstance(job, dict):
                    raise Fault("INVALID_JOURNAL")
                job_id = identifier(job.get("id", job.get("job_id")))
                if job_id != saved.get("job_id"):
                    raise Fault("INVALID_JOURNAL")
                current = ledger["jobs"].get(job_id, {})
                if job.get("revision", 0) >= current.get("revision", 0):
                    write_json(self.job_path(job_id), job)
                    ledger["jobs"][job_id] = job
                    for key in ("requests", "batches"):
                        for name, value in saved.get(key, {}).items():
                            existing = ledger[key].get(name)
                            if existing is not None and existing != value:
                                raise Fault("JOURNAL_MAPPING_CONFLICT")
                            ledger[key][name] = value
                    write_json(path, ledger)
                self.remove_journal()
            yield ledger

    def remove_journal(self):
        fd = directory_fd(self.root)
        try:
            os.unlink("transaction.json", dir_fd=fd)
            os.fsync(fd)
        finally:
            os.close(fd)

    def job_path(self, job_id):
        return safe_path(self.reports / identifier(job_id) / "job.json")

    @contextlib.contextmanager
    def job(self, job_id):
        job_id = identifier(job_id)
        with self.ledger() as ledger:
            with file_lock(self.locks / (job_id + ".lock")):
                if job_id not in ledger["jobs"]:
                    raise Fault("JOB_NOT_FOUND")
                job = copy.deepcopy(ledger["jobs"][job_id])
                job.setdefault("id", job_id)
                yield ledger, job

    def save(self, ledger, job):
        job_id = identifier(job["id"])
        job["revision"] = ledger["jobs"].get(job_id, {}).get("revision", 0) + 1
        job["updated_at"] = time.time()
        private_directory(self.reports / job_id)
        write_json(self.root / "transaction.json", {"job_id": job_id, "job": job,
                                                  "requests": ledger["requests"], "batches": ledger["batches"]})
        write_json(self.job_path(job_id), job)
        ledger["jobs"][job_id] = copy.deepcopy(job)
        write_json(self.root / "ledger.json", ledger)
        self.remove_journal()

    def desktop(self):
        return file_lock(self.root / "desktop.lock", timeout=0, reason="DESKTOP_BUSY")


class NativeBoundary:
    boundary = "live"

    def __init__(self):
        self._bridge = None

    def bridge(self):
        if self._bridge is None:
            spec = importlib.util.spec_from_file_location("desktop_research_native_bridge", Path(__file__).with_name("native_bridge.py"))
            module = importlib.util.module_from_spec(spec)
            spec.loader.exec_module(module)
            self._bridge = module.Bridge(sys.modules[__name__])
        return self._bridge

    @property
    def helper_identity(self):
        return self.bridge().helper_identity

    def invoke(self, action, job=None, payload=None):
        return self.bridge().invoke(action, job, payload)


def object_fields(value, fields):
    if not isinstance(value, dict):
        return {}
    return {k: copy.deepcopy(v) for k, v in value.items() if k in fields and
            (v is None or isinstance(v, (str, bool, int, float))) and
            (not isinstance(v, str) or len(v) <= 65536)}


def filter_result(value):
    """Closed schema: discard unrelated UI content before logging/persistence."""
    if not isinstance(value, dict):
        raise Fault("INVALID_BOUNDARY_RESULT")
    result = object_fields(value, SCALAR_FIELDS)
    result["app"] = object_fields(value.get("app"), APP_FIELDS)
    if "draft_binding" in value:
        binding = object_fields(value["draft_binding"], BINDING_FIELDS)
        binding["session"] = object_fields(value["draft_binding"].get("session"), APP_FIELDS)
        result["draft_binding"] = binding
    if "source_scope" in value:
        source = object_fields(value["source_scope"], {"selection_complete", "public_web", "private_enabled", "private_excludable"})
        source["restoration"] = object_fields(value["source_scope"].get("restoration"), {"required", "original_hash"})
        result["source_scope"] = source
    if isinstance(value.get("pending"), list):
        result["pending"] = [v for v in value["pending"] if isinstance(v, str) and re.fullmatch(r"[a-zA-Z0-9_-]{1,80}", v)][:40]
    reason = result.get("reason")
    if reason is not None and (not isinstance(reason, str) or not re.fullmatch(r"[A-Z][A-Z0-9_]{0,79}", reason)):
        result["reason"] = "BOUNDARY_ERROR"
    return result


def summary(job):
    result = object_fields(job, {"provider", "provider_state", "collection_state", "prompt_possibly_sent",
                                 "reason", "restoration_pending", "revision", "boundary"})
    result["job_id"] = job.get("id", job.get("job_id"))
    reason = result.get("reason")
    if reason and (not isinstance(reason, str) or not re.fullmatch(r"[A-Z][A-Z0-9_]{0,79}", reason)):
        result["reason"] = "REDACTED_LEGACY_REASON"
    if job.get("blocking_job_id"):
        result["blocking_job_id"] = job["blocking_job_id"]
    if job.get("observation"):
        result["observation"] = object_fields(job["observation"], {"outcome", "timestamp", "reason", "deadline"})
    return result


class Workflow:
    def __init__(self, store, boundary):
        self.store, self.boundary = store, boundary
        self.kind = boundary.boundary
        self.desktop_fd = None

    @contextlib.contextmanager
    def desktop_transaction(self):
        with self.store.desktop() as descriptor:
            self.desktop_fd = descriptor
            try:
                yield
            finally:
                self.desktop_fd = None

    def call(self, action, job=None, payload=None):
        if action not in {"doctor", "inspect", "create", "prepare", "enter", "send", "observe", "rebind", "answer", "begin", "restore", "collect"}:
            raise Fault("UNSUPPORTED_ACTION")
        payload = dict(payload or {})
        if self.kind == "live" and self.desktop_fd is not None:
            payload["transaction_fd"] = self.desktop_fd
        result = filter_result(self.boundary.invoke(action, copy.deepcopy(job), payload))
        if result.get("boundary") != self.kind:
            raise Fault("BOUNDARY_PROVENANCE_MISMATCH")
        return result

    def trusted_app(self, app, provider, prior=None, rebind=False):
        if not app or app.get("signature_verified") is not True or app.get("caller_excluded") is not True:
            raise Fault("TARGET_TRUST_FAILED")
        pid, start_time = app.get("pid"), app.get("start_time")
        if (not app.get("version") or not app.get("profile_id") or not isinstance(pid, int) or isinstance(pid, bool) or pid <= 0
                or not isinstance(start_time, (int, float)) or not math.isfinite(start_time) or start_time <= 0):
            raise Fault("TARGET_IDENTITY_INCOMPLETE")
        if self.kind == "live":
            bundle, team = PROVIDERS[provider]
            if app.get("bundle_id") != bundle or app.get("team_id") != team:
                raise Fault("TARGET_SIGNATURE_MISMATCH")
        elif app.get("bundle_id") != "synthetic." + provider:
            raise Fault("SYNTHETIC_TARGET_MISMATCH")
        if prior:
            keys = ["bundle_id", "version", "profile_id", "account_hash", "team_id"]
            if not rebind:
                keys += ["pid", "start_time"]
            if any(prior.get(k) != app.get(k) for k in keys):
                raise Fault("TARGET_CONTEXT_CHANGED")

    def guard(self, observation, job, focus=False, prior=None):
        if observation.get("ok") is not True or observation.get("complete") is not True:
            raise Fault(observation.get("reason") or "TARGET_UNOBSERVABLE")
        self.trusted_app(observation.get("app"), job["provider"], prior)
        if observation.get("unexpected_dialog"):
            raise Fault("UNEXPECTED_DIALOG")
        idle = observation.get("idle_seconds")
        if not isinstance(idle, (int, float)) or not math.isfinite(idle) or idle < 2:
            raise Fault("USER_INPUT_ACTIVE")
        if focus and observation.get("focus_matches") is not True:
            raise Fault("FOCUS_CHANGED")

    def doctor(self, provider, self_test=False):
        result = self.call("doctor", payload={"provider": provider, "self_test": self_test})
        if sys.version_info < (3, 10):
            result.update(ready=False, reason="PYTHON_VERSION_UNSUPPORTED")
        if result.get("ready"):
            try:
                self.trusted_app(result.get("app"), provider)
            except Fault as error:
                result.update(ready=False, reason=error.reason)
        allowed = {"ready", "accessibility", "screen_capture", "research_available", "mode", "reason", "boundary", "complete", "self_test_passed", "self_test_count", "helper_path"}
        return {**object_fields(result, allowed), "provider": provider,
                "app": object_fields(result.get("app"), {"bundle_id", "version", "signature_verified", "profile_id", "caller_excluded"}),
                "python_version": ".".join(map(str, sys.version_info[:3]))}

    def allocate(self, ledger, provider, request_id, question, scope="public-web"):
        identifier(request_id)
        if provider not in PROVIDERS or scope != "public-web":
            raise Fault("UNSUPPORTED_PROVIDER_OR_SCOPE")
        fingerprint = digest((provider + "\n" + scope + "\n" + digest(question)).encode())
        key = provider + ":" + request_id
        mapped = ledger["requests"].get(key)
        if mapped:
            job_id = mapped if isinstance(mapped, str) else mapped.get("job_id")
            job = ledger["jobs"].get(job_id)
            if not job or job.get("fingerprint") != fingerprint:
                raise Fault("REQUEST_ID_CONFLICT")
            return job_id
        uncertain = [j for j in ledger["jobs"].values() if j.get("provider") == provider and j.get("provider_state") == "SUBMISSION_UNCERTAIN"]
        if not uncertain:
            for job_id, job in ledger["jobs"].items():
                if job.get("fingerprint") == fingerprint and job.get("provider_state") not in TERMINAL:
                    ledger["requests"][key] = job_id
                    write_json(self.store.root / "ledger.json", ledger)
                    return job_id
        alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        seed = "".join(secrets.choice(alphabet) for _ in range(12))
        marker = "DRC-" + seed + alphabet[sum((i + 1) * alphabet.index(c) for i, c in enumerate(seed)) % len(alphabet)]
        job_id = provider + "-" + marker
        private_directory(self.store.reports / job_id)
        atomic_write(self.store.reports / job_id / "question.txt", question)
        prompt = ("[Desktop Research job " + marker + "]\n").encode() + question.rstrip(b"\n") + ("\n[End Desktop Research job " + marker + "]").encode()
        atomic_write(self.store.reports / job_id / "prompt.txt", prompt)
        job = {"id": job_id, "marker": marker, "request_id": request_id, "provider": provider,
               "mode": "Research" if provider == "claude" else "Deep research", "source_scope": scope,
               "fingerprint": fingerprint, "prompt_sha256": digest(question), "prepared_prompt_sha256": digest(prompt),
               "created_at": time.time(), "provider_state": "PREPARED", "collection_state": "NOT_STARTED",
               "prompt_possibly_sent": False, "draft_authority": False, "submitted_binding": None,
               "boundary": self.kind, "helper_identity": copy.deepcopy(self.boundary.helper_identity),
               "proof_kind": "production", "restoration_pending": False, "send_attempts": 0}
        ledger["requests"][key] = job_id
        self.store.save(ledger, job)
        return job_id

    def draft_check(self, result, job, new=False):
        binding = result.get("draft_binding", {})
        if binding.get("kind") != "dedicated-draft" or binding.get("caller_excluded") is not True:
            raise Fault("DRAFT_BINDING_INVALID")
        self.trusted_app(binding.get("session"), job["provider"], job.get("app"))
        if not binding.get("window_id"):
            raise Fault("DRAFT_BINDING_INVALID")
        if new and (binding.get("new") is not True or binding.get("empty") is not True):
            raise Fault("EXISTING_DRAFT")
        prior = job.get("draft_binding")
        if prior and any(prior.get(k) != binding.get(k) for k in ("window_id", "tab_id", "conversation_id")):
            raise Fault("DRAFT_BINDING_CHANGED")
        return binding

    def observe_locked(self, ledger, job, action="observe", rebind=False):
        result = self.call(action, job)
        try:
            if result.get("ok") is not True or result.get("complete") is not True:
                raise Fault(result.get("reason") or "INCOMPLETE_OBSERVATION")
            self.trusted_app(result.get("app"), job["provider"], job.get("app"), rebind=rebind)
            bound = result.get("match_count") == 1 and result.get("role") == "USER" and result.get("marker") == job["marker"]
            if not bound:
                if (not rebind and result.get("positive_unsent") is True and job.get("provider_state") == "SUBMISSION_UNCERTAIN"
                        and job.get("send_intent", {}).get("action") == "send" and not job.get("submitted_binding")):
                    self.draft_check(result, job)
                    if not job.get("entered_sha256") or result.get("full_value_sha256") != job["entered_sha256"]:
                        raise Fault("UNSENT_DRAFT_HASH_MISMATCH")
                    job.update(provider_state="DRAFTING", draft_authority=True, prompt_possibly_sent=False,
                               positive_unsent_evidence={"timestamp": time.time(), "full_value_sha256": result["full_value_sha256"]})
                    self.note(job, "POSITIVELY_UNSENT")
                    self.store.save(ledger, job)
                    return False, result
                raise Fault("SUBMITTED_JOB_UNOBSERVABLE")
            if not job.get("prompt_possibly_sent"):
                raise Fault("NO_SUBMISSION_INTENT")
            prior = job.get("submitted_binding") or job.get("draft_binding", {})
            if not rebind and prior.get("window_id") != result.get("window_id"):
                raise Fault("CONVERSATION_CHANGED")
            binding = {"kind": "submitted-user-turn", "marker": job["marker"], "role": "USER",
                       "window_id": result["window_id"], "session": result["app"], "timestamp": time.time()}
            job.update(submitted_binding=binding, draft_authority=False, app=result["app"])
            if not rebind:
                state = result.get("state")
                if state not in STATES or state in {"PREPARED", "DRAFTING", "SUBMISSION_UNCERTAIN"}:
                    raise Fault("PROVIDER_STATE_UNOBSERVABLE")
                if state == "COMPLETED" and (result.get("final_result") is not True or result.get("pending_interaction") is not False):
                    raise Fault("FINAL_RESULT_UNOBSERVABLE")
                if job.get("provider_state") == "COMPLETED" and state != "COMPLETED":
                    raise Fault("TERMINAL_STATE_CONFLICT")
                intent = job.get("send_intent", {})
                if job.get("provider_state") == "SUBMISSION_UNCERTAIN" and intent.get("action") in {"answer", "begin"}:
                    waiting = "AWAITING_CLARIFICATION" if intent["action"] == "answer" else "AWAITING_PLAN"
                    interaction_key = "question_hash" if intent["action"] == "answer" else "plan_hash"
                    if state == waiting and result.get(interaction_key) == intent.get("pending_hash"):
                        raise Fault("INTERACTION_RECEIPT_UNCERTAIN")
                if job.get("provider_state") != "COMPLETED" or state == "COMPLETED":
                    job["provider_state"] = state
                    job["reason"] = result.get("reason")
                for field in ("question", "question_hash", "plan", "plan_hash", "plan_source_scope"):
                    job[field] = result.get(field)
                job["final_result_verified"] = state == "COMPLETED"
            self.note(job, "SUPPORTED")
            self.store.save(ledger, job)
            return True, result
        except Fault as error:
            self.note(job, "UNOBSERVABLE", error.reason)
            self.store.save(ledger, job)
            return False, result

    @staticmethod
    def note(job, outcome, reason=None, deadline=None):
        job["observation"] = {"outcome": outcome, "timestamp": time.time(), "reason": reason}
        if deadline is not None:
            job["observation"]["deadline"] = deadline

    def start(self, job_id, receipt_timeout=10):
        with self.store.job(job_id) as (ledger, job):
            emit({"job_id": job_id, "allocated": True, "boundary": job.get("boundary", "unverified")})
            if job.get("prompt_possibly_sent") or job["provider_state"] in {"COMPLETED", "CANCELLED"}:
                self.observe_locked(ledger, job)
                return summary(job), 0 if job["provider_state"] == "COMPLETED" else 3
            if job.get("proof_kind") != "production" or job.get("boundary") != self.kind:
                raise Fault("LEGACY_JOB_REQUIRES_REVIEW", job_id=job_id)
            for other_id, other in ledger["jobs"].items():
                if other_id == job_id or other.get("provider") != job["provider"]:
                    continue
                if other.get("provider_state") in PENDING | {"DRAFTING"}:
                    reason = "PROVIDER_UNCERTAIN" if other["provider_state"] == "SUBMISSION_UNCERTAIN" else "PROVIDER_ACTIVE"
                    job.update(reason=reason, blocking_job_id=other_id)
                    self.store.save(ledger, job)
                    return summary(job), 3
            try:
                readiness = self.call("doctor", job)
                if readiness.get("ready") is not True or readiness.get("research_available") is not True or readiness.get("accessibility") is not True:
                    raise Fault(readiness.get("reason") or "RESEARCH_UNAVAILABLE")
                self.guard(readiness, job, prior=job.get("app"))
                with self.desktop_transaction():
                    resuming = job["provider_state"] == "DRAFTING"
                    if resuming:
                        result = self.call("observe", job)
                        if result.get("complete") is not True or not job.get("draft_authority"):
                            raise Fault("DRAFT_RESUME_UNOBSERVABLE")
                        self.draft_check(result, job)
                        if result.get("full_value_sha256") != job.get("entered_sha256"):
                            raise Fault("OWNED_DRAFT_CHANGED")
                    else:
                        created = self.call("create", job)
                        self.guard(created, job, focus=True)
                        job["draft_binding"] = self.draft_check(created, job, new=True)
                        job.update(app=created["app"], draft_authority=True)
                        self.store.save(ledger, job)
                    prepared = self.call("prepare", job)
                    self.guard(prepared, job, focus=True, prior=job["app"])
                    self.draft_check(prepared, job, new=not resuming)
                    if prepared.get("mode_selected") is not True:
                        raise Fault("RESEARCH_MODE_UNVERIFIED")
                    source = prepared.get("source_scope", {})
                    # Retain restoration intent even if subsequent source checks block.
                    job["restoration_pending"] = source.get("restoration", {}).get("required", False)
                    job["source_evidence"] = {**source, "timestamp": time.time()}
                    self.store.save(ledger, job)
                    if source.get("selection_complete") is not True:
                        raise Fault("SOURCE_SCOPE_UNOBSERVABLE")
                    if source.get("public_web") is not True or source.get("private_enabled") != 0 or source.get("private_excludable") is not True:
                        raise Fault("PRIVATE_SOURCE_ENABLED")
                    prompt_path = self.store.reports / job_id / "prompt.txt"
                    prompt = read_bytes(prompt_path, private=True)
                    if digest(prompt) != job["prepared_prompt_sha256"]:
                        raise Fault("PROMPT_TAMPERED")
                    job.update(provider_state="DRAFTING", reason=None,
                               input_intent={"timestamp": time.time(), "kind": "prompt", "sha256": digest(prompt)})
                    self.store.save(ledger, job)
                    entered = self.call("enter", job, {"prompt_file": str(prompt_path), "sha256": digest(prompt), "purpose": "prompt"})
                    job["entered_sha256"] = entered.get("full_value_sha256")
                    self.store.save(ledger, job)
                    self.guard(entered, job, focus=True, prior=job["app"])
                    self.draft_check(entered, job)
                    if entered.get("full_value_sha256") != digest(prompt) or entered.get("end_markers") is not True:
                        raise Fault("PROMPT_ENTRY_UNVERIFIED")
                    self.send_intent(ledger, job, "send", digest(prompt))
                    job["last_event_receipt"] = self.call("send", job, {"sha256": digest(prompt)})
                    self.store.save(ledger, job)
                    if receipt_timeout > 0:
                        self.observe_locked(ledger, job)
            except Fault as error:
                job["reason"] = error.reason
                if not job.get("input_intent") and error.reason != "DESKTOP_BUSY":
                    job["provider_state"] = "BLOCKED"
                self.store.save(ledger, job)
                return summary(job), 3
            except Exception:
                # Never include exception text: adapters may have encountered private data.
                self.note(job, "UNOBSERVABLE", "DESKTOP_BOUNDARY_LOST")
                self.store.save(ledger, job)
                return summary(job), 3
            result = summary(job)
        if receipt_timeout > 0 and result["provider_state"] == "SUBMISSION_UNCERTAIN":
            deadline = time.monotonic() + receipt_timeout
            while time.monotonic() < deadline:
                result, _ = self.status(job_id)
                if result["provider_state"] != "SUBMISSION_UNCERTAIN":
                    break
                time.sleep(min(0.5, max(0, deadline - time.monotonic())))
        return result, 0 if result["provider_state"] in {"RUNNING", "COMPLETED"} else 3

    def send_intent(self, ledger, job, action, payload_hash, pending_hash=None):
        job.update(provider_state="SUBMISSION_UNCERTAIN", prompt_possibly_sent=True, draft_authority=False,
                   send_intent={"timestamp": time.time(), "action": action, "payload_sha256": payload_hash, "pending_hash": pending_hash},
                   send_attempts=job.get("send_attempts", 0) + 1)
        self.store.save(ledger, job)

    def status(self, job_id):
        with self.store.job(job_id) as (ledger, job):
            try:
                supported, _ = self.observe_locked(ledger, job)
            except Exception:
                supported = False
                self.note(job, "UNOBSERVABLE", "DESKTOP_BOUNDARY_LOST")
                self.store.save(ledger, job)
            return summary(job), 0 if supported else 3

    def wait(self, job_id, timeout):
        deadline = time.monotonic() + timeout
        wall_deadline = time.time() + timeout
        while True:
            result, code = self.status(job_id)
            if result["provider_state"] in TERMINAL | {"AWAITING_PLAN", "AWAITING_CLARIFICATION"}:
                return result, code
            if time.monotonic() >= deadline:
                with self.store.job(job_id) as (ledger, job):
                    self.note(job, "TIMEOUT", deadline=wall_deadline)
                    self.store.save(ledger, job)
                    return summary(job), 3
            time.sleep(min(2, max(0, deadline - time.monotonic())))

    def rebind(self, job_id):
        with self.store.job(job_id) as (ledger, job):
            if not job.get("prompt_possibly_sent"):
                raise Fault("SUBMITTED_BINDING_REQUIRED", job_id=job_id)
            readiness = self.call("doctor", job)
            self.guard(readiness, job)
            with self.desktop_transaction():
                supported, _ = self.observe_locked(ledger, job, "rebind", rebind=True)
            return summary(job), 0 if supported else 3

    def interaction(self, job_id, action, plan_hash=None, answer=None):
        with self.store.job(job_id) as (ledger, job):
            expected = "AWAITING_PLAN" if action == "begin" else "AWAITING_CLARIFICATION"
            if job["provider_state"] != expected:
                raise Fault("NO_PENDING_INTERACTION", job_id=job_id)
            key = "plan_hash" if action == "begin" else "question_hash"
            old_hash = job.get(key)
            interaction_id = action + ":" + (old_hash or "")
            if interaction_id in job.get("interaction_intents", {}):
                raise Fault("INTERACTION_ALREADY_ATTEMPTED", job_id=job_id)
            if action == "begin" and (not HASH.fullmatch(plan_hash or "") or old_hash != plan_hash):
                raise Fault("PLAN_HASH_MISMATCH", job_id=job_id)
            readiness = self.call("doctor", job)
            self.guard(readiness, job, prior=job.get("app"))
            with self.desktop_transaction():
                supported, observed = self.observe_locked(ledger, job)
                if not supported or job["provider_state"] != expected or not old_hash or job.get(key) != old_hash:
                    raise Fault("PENDING_INTERACTION_CHANGED", job_id=job_id)
                prepared = self.call("prepare", job, {"purpose": "continuation"})
                self.guard(prepared, job, focus=True, prior=job.get("app"))
                source = prepared.get("source_scope", {})
                if (prepared.get("mode_selected") is not True or source.get("selection_complete") is not True
                        or source.get("public_web") is not True or source.get("private_enabled") != 0):
                    raise Fault("CONTINUATION_SCOPE_UNVERIFIED", job_id=job_id)
                job["continuation_source_evidence"] = {**source, "timestamp": time.time()}
                if action == "begin":
                    if job.get("source_scope") != "public-web" or observed.get("plan_source_scope") != "public-web":
                        raise Fault("PLAN_SCOPE_EXPANSION", job_id=job_id)
                    job["approved_plan_hash"] = plan_hash
                    payload = {"plan_hash": plan_hash, "source_scope": "public-web"}
                    payload_hash = plan_hash
                else:
                    if answer is None:
                        raise Fault("ANSWER_REQUIRED", job_id=job_id)
                    path = self.store.reports / job_id / ("answer-" + secrets.token_hex(8) + ".txt")
                    atomic_write(path, answer)
                    payload_hash = digest(answer)
                    job["input_intent"] = {"timestamp": time.time(), "kind": "answer", "sha256": payload_hash, "question_hash": old_hash}
                    self.store.save(ledger, job)
                    payload = {"prompt_file": str(path), "sha256": payload_hash, "question_hash": old_hash, "purpose": "answer"}
                    entered = self.call("enter", job, payload)
                    self.guard(entered, job, focus=True, prior=job["app"])
                    if entered.get("full_value_sha256") != payload_hash or entered.get("end_markers") is not True:
                        raise Fault("ANSWER_ENTRY_UNVERIFIED", job_id=job_id)
                job.setdefault("interaction_intents", {})[interaction_id] = {"timestamp": time.time(), "payload_sha256": payload_hash}
                self.send_intent(ledger, job, action, payload_hash, old_hash)
                try:
                    job["last_event_receipt"] = self.call(action, job, payload)
                    self.store.save(ledger, job)
                    self.observe_locked(ledger, job)
                except Exception:
                    self.note(job, "UNOBSERVABLE", "DESKTOP_BOUNDARY_LOST")
                    self.store.save(ledger, job)
                return summary(job), 0 if job["provider_state"] == "RUNNING" else 3

    def collect(self, job_id):
        with self.store.job(job_id) as (ledger, job):
            # Collection implementation/format validation is the following task group.
            # Never allow its failure to erase an independently observed completion.
            readiness = self.call("doctor", job)
            self.guard(readiness, job, prior=job.get("app"))
            with self.desktop_transaction():
                supported, result = self.observe_locked(ledger, job)
                if not supported or job["provider_state"] != "COMPLETED" or result.get("final_result") is not True or result.get("pending_interaction") is not False:
                    raise Fault("FINAL_BOUND_REPORT_REQUIRED", job_id=job_id)
                if job.get("restoration_pending"):
                    restoration = self.call("restore", job)
                    job["restoration_pending"] = restoration.get("restored") is not True or bool(restoration.get("pending"))
                    job["restoration_result"] = restoration
                    self.store.save(ledger, job)
                attempt = job.get("collection_attempts", 0) + 1
                folder = private_directory(self.store.reports / job_id / "attempts" / f"{attempt:03d}")
                job.update(collection_state="IN_PROGRESS", collection_attempts=attempt)
                self.store.save(ledger, job)
                collected = self.call("collect", job, {"attempt_directory": str(folder)})
                # A native success alone is never a validated collection.
                reason = collected.get("reason") if collected.get("ok") is not True else "COLLECTION_VALIDATOR_NOT_IMPLEMENTED"
                job.update(collection_state="FAILED", collection_reason=reason or "EXPORT_FAILED")
                write_json(folder / "receipt.json", {"boundary": self.kind, "ok": False, "reason": job["collection_reason"]})
                self.store.save(ledger, job)
                return summary(job), 3

    def audit(self, job_id, require_live):
        with self.store.job(job_id) as (_, job):
            if require_live and job.get("boundary") != "live":
                raise Fault("SYNTHETIC_NOT_LIVE_EVIDENCE", job_id=job_id)
            identity = job.get("helper_identity", {})
            valid = (job.get("proof_kind") == "production" and job.get("submitted_binding") and
                     all(HASH.fullmatch(identity.get(k, "")) for k in ("source_sha256", "binary_sha256")))
            if self.kind == "live":
                valid = valid and identity == self.boundary.helper_identity and bool(identity)
            if not valid:
                raise Fault("PRODUCTION_PROVENANCE_INCOMPLETE", job_id=job_id)
            return {**summary(job), "provenance_verified": True, "live": job["boundary"] == "live"}, 0

    def batch(self, request_file, timeout):
        # Validate the entire request and every prompt before allocating or input.
        manifest = json.loads(read_bytes(request_file))
        if not isinstance(manifest, dict) or set(manifest) != {"batch_id", "entries"}:
            raise Fault("INVALID_BATCH")
        batch_id = identifier(manifest["batch_id"])
        entries = manifest["entries"]
        if not isinstance(entries, list) or not 1 <= len(entries) <= 20:
            raise Fault("INVALID_BATCH")
        validated = []
        for entry in entries:
            if not isinstance(entry, dict) or set(entry) != {"entry_id", "provider", "prompt_file", "source_scope"}:
                raise Fault("INVALID_BATCH_ENTRY")
            entry_id = identifier(entry["entry_id"])
            if entry["provider"] not in PROVIDERS or entry["source_scope"] != "public-web" or entry_id in [v[0] for v in validated]:
                raise Fault("INVALID_BATCH_ENTRY")
            validated.append((entry_id, entry["provider"], question_file(entry["prompt_file"])))
        batch_fingerprint = digest(json.dumps([(e, p, digest(q)) for e, p, q in validated]).encode())
        with self.store.ledger() as ledger:
            prior = ledger["batches"].get(batch_id)
            if prior and prior.get("fingerprint") != batch_fingerprint:
                raise Fault("BATCH_ID_CONFLICT")
            if not prior:
                mapping = {}
                for entry_id, provider, question in validated:
                    request_id = "batch-" + digest((batch_id + "\n" + entry_id).encode())
                    mapping[entry_id] = self.allocate(ledger, provider, request_id, question)
                prior = {"fingerprint": batch_fingerprint, "entries": mapping}
                ledger["batches"][batch_id] = prior
                write_json(self.store.root / "ledger.json", ledger)
            mapping = copy.deepcopy(prior["entries"])
        emit({"batch_id": batch_id, "allocated": mapping, "boundary": self.kind})
        results = []
        for entry_id, _, _ in validated:
            job_id = mapping[entry_id]
            try:
                result, _ = self.start(job_id)
                if result["provider_state"] in PENDING:
                    result, _ = self.wait(job_id, timeout)
                if result["provider_state"] == "COMPLETED":
                    result, _ = self.collect(job_id)
            except Fault as error:
                with self.store.job(job_id) as (_, job):
                    result = {**summary(job), "reason": error.reason}
            results.append({**result, "entry_id": entry_id})
        fully = all(r["collection_state"] == "COLLECTED" for r in results)
        return {"batch_id": batch_id, "entries": results, "fully_collected": fully, "boundary": self.kind}, 0 if fully else 3


class Parser(argparse.ArgumentParser):
    def error(self, message):
        raise Fault("INVALID_COMMAND")


def parser():
    p = Parser(description=__doc__)
    p.add_argument("--synthetic-root", help=argparse.SUPPRESS)
    sub = p.add_subparsers(dest="command", required=True)
    for command in ("doctor", "inspect"):
        c = sub.add_parser(command)
        c.add_argument("--provider", choices=[*PROVIDERS, "all"])
        if command == "doctor":
            c.add_argument("--self-test", action="store_true")
        else:
            c.add_argument("--job")
            c.add_argument("--job-content", action="store_true")
    c = sub.add_parser("start")
    c.add_argument("--provider", choices=list(PROVIDERS))
    c.add_argument("--request-id")
    c.add_argument("--prompt-file")
    c.add_argument("--source-scope", choices=["public-web"], default="public-web")
    c.add_argument("--job")
    c.add_argument("--receipt-timeout", type=float, default=10)
    sub.add_parser("list")
    for command in ("status", "wait", "rebind", "respond", "approve-plan", "collect", "audit"):
        c = sub.add_parser(command)
        c.add_argument("--job", required=True)
        if command == "wait":
            c.add_argument("--timeout", type=float, default=60)
        if command == "respond":
            c.add_argument("--answer-file", required=True)
        if command == "approve-plan":
            c.add_argument("--plan-hash", required=True)
        if command == "audit":
            c.add_argument("--require-live", action="store_true")
    c = sub.add_parser("batch")
    c.add_argument("--request-file", required=True)
    c.add_argument("--timeout", type=float, default=60)
    return p


def main(argv=None, *, boundary=None):
    try:
        args = parser().parse_args(argv)
        if any(not 0 <= getattr(args, name, 0) <= 3600 for name in ("timeout", "receipt_timeout")):
            raise Fault("INVALID_TIMEOUT")
        injected = boundary is not None
        boundary = boundary or NativeBoundary()
        if args.synthetic_root:
            root = safe_path(args.synthetic_root)
            temporary = Path(tempfile.gettempdir()).resolve()
            platform_roots = (Path("/private/var/folders"), Path("/private/tmp")) if sys.platform == "darwin" else (Path("/tmp"), Path("/var/tmp"))
            if (not injected or boundary.boundary != "synthetic" or not root.is_relative_to(temporary)
                    or not any(root.is_relative_to(p) for p in platform_roots)):
                raise Fault("SYNTHETIC_REQUIRES_TEMPORARY_INJECTED_BOUNDARY")
        else:
            if injected or sys.platform != "darwin":
                raise Fault("LIVE_REQUIRES_NATIVE_MACOS_BOUNDARY")
            root = Path.home() / "Library/Application Support/desktop-deep-research"
        workflow = Workflow(Store(root), boundary)
        command = args.command
        if command == "doctor":
            providers = list(PROVIDERS) if args.provider in {None, "all"} else [args.provider]
            rows = [workflow.doctor(p, args.self_test) for p in providers]
            result = rows[0] if len(rows) == 1 else {"providers": rows, "ready": all(r.get("ready") for r in rows)}
            code = 0 if all(r.get("ready") for r in rows) else 3
        elif command == "start":
            if args.job:
                if args.request_id or args.prompt_file or args.provider:
                    raise Fault("INVALID_COMMAND")
                job_id = identifier(args.job)
            else:
                if not all((args.provider, args.request_id, args.prompt_file)):
                    raise Fault("INVALID_COMMAND")
                question = question_file(args.prompt_file)
                with workflow.store.ledger() as ledger:
                    job_id = workflow.allocate(ledger, args.provider, args.request_id, question, args.source_scope)
            result, code = workflow.start(job_id, args.receipt_timeout)
        elif command == "list":
            with workflow.store.ledger() as ledger:
                result = {"jobs": [summary(j) for j in ledger["jobs"].values()]}
            code = 0
        elif command == "inspect":
            if args.job:
                with workflow.store.job(args.job) as (ledger, job):
                    supported, _ = workflow.observe_locked(ledger, job)
                    result, code = summary(job), 0 if supported else 3
                    if args.job_content and supported:
                        result["untrusted_job_content"] = {k: job.get(k) for k in ("question", "question_hash", "plan", "plan_hash", "plan_source_scope")}
            else:
                if args.job_content or args.provider == "all" or not args.provider:
                    raise Fault("INVALID_COMMAND")
                inspected = workflow.call("inspect", payload={"provider": args.provider})
                result = {"provider": args.provider, **object_fields(inspected, {"ok", "complete", "reason", "boundary", "mode", "research_available"})}
                code = 0 if result.get("ok") else 3
        elif command == "status":
            result, code = workflow.status(args.job)
        elif command == "wait":
            result, code = workflow.wait(args.job, args.timeout)
        elif command == "rebind":
            result, code = workflow.rebind(args.job)
        elif command == "respond":
            result, code = workflow.interaction(args.job, "answer", answer=question_file(args.answer_file))
        elif command == "approve-plan":
            result, code = workflow.interaction(args.job, "begin", plan_hash=args.plan_hash)
        elif command == "collect":
            result, code = workflow.collect(args.job)
        elif command == "audit":
            result, code = workflow.audit(args.job, args.require_live)
        else:
            result, code = workflow.batch(args.request_file, args.timeout)
        emit(result)
        return code
    except Fault as error:
        emit({"ok": False, "reason": error.reason, **error.details})
        return 3
    except (OSError, ValueError, TypeError, KeyError):
        emit({"ok": False, "reason": "INVALID_OR_UNAVAILABLE_LOCAL_INPUT"})
        return 3
    except Exception:
        emit({"ok": False, "reason": "INTERNAL_ERROR"})
        return 70


if __name__ == "__main__":
    sys.exit(main())
