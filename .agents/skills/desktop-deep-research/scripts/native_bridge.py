"""Build and invoke the owner-only compiled Swift boundary.

No package install, permission grant, shell command string or consumer API.
The orchestrator's real storage helpers are reused through its module object.
"""
from __future__ import annotations

import hashlib
import json
import os
from pathlib import Path
import platform
import secrets
import subprocess


class Bridge:
    boundary = "live"

    def __init__(self, api):
        self.api = api
        self.source = Path(__file__).with_name("DesktopResearchBridge.swift")
        self._identity = None
        self.binary = None

    def command(self, arguments, timeout=30, **options):
        try:
            return subprocess.run(arguments, stdout=subprocess.PIPE, stderr=subprocess.PIPE,
                                  timeout=timeout, check=False, **options)
        except (OSError, subprocess.TimeoutExpired):
            raise self.api.Fault("NATIVE_TOOLCHAIN_OR_HELPER_UNAVAILABLE") from None

    @property
    def helper_identity(self):
        if self._identity is None:
            self.build()
        return dict(self._identity)

    def build(self):
        api = self.api
        source = api.read_bytes(self.source, 1_048_576)
        compiler = self.command(["/usr/bin/xcrun", "--find", "swiftc"])
        sdk = self.command(["/usr/bin/xcrun", "--show-sdk-version"])
        sdk_path_result = self.command(["/usr/bin/xcrun", "--show-sdk-path"])
        if compiler.returncode or sdk.returncode or sdk_path_result.returncode:
            raise api.Fault("SWIFT_TOOLCHAIN_UNAVAILABLE")
        compiler_path = compiler.stdout.decode().strip()
        if not Path(compiler_path).is_absolute():
            raise api.Fault("SWIFT_TOOLCHAIN_UNAVAILABLE")
        version = self.command([compiler_path, "--version"])
        if version.returncode:
            raise api.Fault("SWIFT_TOOLCHAIN_UNAVAILABLE")
        arch = platform.machine()
        if arch not in {"arm64", "x86_64"}:
            raise api.Fault("NATIVE_ARCHITECTURE_UNSUPPORTED")
        sdk_path = sdk_path_result.stdout.decode().strip()
        if not Path(sdk_path).is_absolute():
            raise api.Fault("SWIFT_TOOLCHAIN_UNAVAILABLE")
        flags = ["-swift-version", "5", "-sdk", sdk_path, "-target", arch + "-apple-macosx14.0", "-O"]
        stamp = {"source_sha256": api.digest(source), "compiler_version": version.stdout.decode().strip(),
                 "sdk_version": sdk.stdout.decode().strip(), "flags": flags}
        key = api.digest(json.dumps(stamp, sort_keys=True).encode())
        root = api.private_directory(Path.home() / "Library/Application Support/desktop-deep-research/cache/native")
        with api.file_lock(root / "build.lock", timeout=3):
            folder = api.private_directory(root / ("build-" + key))
            snapshot, binary, metadata = folder / "SourceSnapshot.swift", folder / "bridge", folder / "build.json"
            if metadata.exists():
                stored = api.read_json(metadata)
                if (stored.get("build_key") != key or stored.get("stamp") != stamp
                        or api.digest(api.read_bytes(snapshot, 1_048_576, private=True)) != stamp["source_sha256"]
                        or api.digest(api.read_bytes(binary, 64 * 1024 * 1024, private=True)) != stored.get("binary_sha256")):
                    raise api.Fault("NATIVE_CACHE_INTEGRITY_FAILED")
            else:
                api.atomic_write(snapshot, source)
                pending = folder / ("bridge-" + secrets.token_hex(8) + ".pending")
                compiled = self.command([compiler_path, *flags, str(snapshot), "-o", str(pending)], timeout=120)
                log = (compiled.stdout + compiled.stderr).replace(str(Path.home()).encode(), b"<USER_HOME>")
                api.atomic_write(folder / "compiler.log", log)
                if compiled.returncode:
                    raise api.Fault("NATIVE_HELPER_COMPILE_FAILED", build_key=key)
                os.chmod(api.safe_path(pending), 0o700)
                data = api.read_bytes(pending, 64 * 1024 * 1024, private=True)
                if data[:4] not in {b"\xcf\xfa\xed\xfe", b"\xfe\xed\xfa\xcf", b"\xca\xfe\xba\xbe", b"\xbe\xba\xfe\xca"}:
                    raise api.Fault("NATIVE_HELPER_NOT_MACHO")
                # Binary is a unique build product in this locked private folder.
                os.replace(pending, binary)
                fd = api.directory_fd(folder)
                try:
                    os.fsync(fd)
                finally:
                    os.close(fd)
                stored = {"build_key": key, "stamp": stamp, "binary_sha256": api.digest(data)}
                api.write_json(metadata, stored)
            self.binary = binary
            self._identity = {"source_sha256": stamp["source_sha256"], "binary_sha256": stored["binary_sha256"], "build_key": key}

    def request(self, action, job=None, payload=None):
        self.helper_identity
        # Rehash the actual executable before each launch, including cache hits.
        if self.api.digest(self.api.read_bytes(self.binary, 64 * 1024 * 1024, private=True)) != self._identity["binary_sha256"]:
            raise self.api.Fault("NATIVE_CACHE_INTEGRITY_FAILED")
        payload = dict(payload or {})
        fd = payload.get("transaction_fd")
        descriptors = (fd,) if isinstance(fd, int) and fd >= 3 else ()
        request = json.dumps({"action": action, "job": job or {}, "payload": payload}).encode()
        if len(request) > 1_048_576:
            raise self.api.Fault("NATIVE_REQUEST_TOO_LARGE")
        result = self.command([str(self.binary)], input=request, pass_fds=descriptors, timeout=30)
        # Stderr may contain OS/private data. Never forward it into diagnostics.
        if len(result.stdout) > 1_048_576:
            raise self.api.Fault("NATIVE_RESULT_TOO_LARGE")
        try:
            value = json.loads(result.stdout)
        except (UnicodeError, ValueError):
            raise self.api.Fault("NATIVE_RESULT_UNREADABLE") from None
        if not isinstance(value, dict) or value.get("boundary") != "live":
            raise self.api.Fault("NATIVE_RESULT_UNREADABLE")
        if result.returncode not in {0, 3} or (result.returncode != 0 and value.get("ok") is True):
            raise self.api.Fault("NATIVE_HELPER_FAILED")
        return value

    def invoke(self, action, job=None, payload=None):
        payload = dict(payload or {})
        tests = None
        if action == "doctor" and payload.pop("self_test", False):
            tests = self.request("self-test")
        result = self.request(action, job, payload)
        result["helper_path"] = "~/" + str(self.binary.relative_to(Path.home()))
        if tests is not None:
            result["self_test_passed"] = tests.get("ok") is True
            result["self_test_count"] = len(tests.get("self_tests", {}))
            # Permission flags are from that exact compiled process, even if
            # a provider/profile/window guard fails during doctor.
            result["accessibility"] = tests.get("accessibility", False)
            result["screen_capture"] = tests.get("screen_capture", False)
            if tests.get("ok") is not True:
                result.update(ready=False, reason="NATIVE_SELF_TEST_FAILED")
        return result
