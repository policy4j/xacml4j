// Native desktop boundary. JSON on stdin/stdout; no prompt text in argv.
// Observation never activates, navigates, scrolls, opens menus or reads the
// clipboard. Provider mutation adapters are developed separately below.
import AppKit
import ApplicationServices
import CoreGraphics
import CoreServices
import CryptoKit
import Darwin
import Foundation
import ScreenCaptureKit
import Security
import Vision

typealias Object = [String: Any]
let allowedActions: Set<String> = ["self-test", "doctor", "inspect", "observe", "create", "prepare", "enter", "send", "rebind", "answer", "begin", "restore", "collect"]
let mutations: Set<String> = ["create", "prepare", "enter", "send", "rebind", "answer", "begin", "restore", "collect"]
let identities: [String: (String, String, String)] = [
    "claude": ("com.anthropic.claudefordesktop", "Q6L2SF6YDW", "2.19675.0"),
    "chatgpt": ("com.openai.codex", "2DC432GLL2", "26.928.31416")
]
let publicLabels: Set<String> = ["Research", "Deep research", "Add", "New chat", "New window", "Claude", "Code", "Cowork", "Connectors", "Search and reference chats", "Research complete", "Stop response", "Send message", "Start research", "Begin research", "Sign in", "Copy", "Download", "Sources", "Public web", "Work", "Plugins", "Fable 5.1", "GPT-6 Astra", "Astra", "Max", "Ultra", "High"]

struct Failure: Error { let reason: String }
func fail(_ reason: String) throws -> Never { throw Failure(reason: reason) }
func sha(_ bytes: Data) -> String { SHA256.hash(data: bytes).map { String(format: "%02x", $0) }.joined() }
func attribute(_ element: AXUIElement, _ name: String) -> CFTypeRef? {
    var value: CFTypeRef?
    return AXUIElementCopyAttributeValue(element, name as CFString, &value) == .success ? value : nil
}
func text(_ element: AXUIElement, _ name: String) -> String? { attribute(element, name) as? String }
func labels(_ element: AXUIElement) -> [String] {
    [kAXTitleAttribute, kAXDescriptionAttribute, kAXHelpAttribute].compactMap { text(element, $0) }
        .map { $0.trimmingCharacters(in: .whitespacesAndNewlines) }
}
func exactLabel(_ element: AXUIElement, _ label: String) -> Bool {
    labels(element).contains { $0.caseInsensitiveCompare(label) == .orderedSame }
}
func idle() -> Double {
    CGEventSource.secondsSinceLastEventType(.combinedSessionState, eventType: CGEventType(rawValue: UInt32.max)!)
}
func bsd(_ pid: pid_t) -> proc_bsdinfo? {
    var info = proc_bsdinfo()
    let count = proc_pidinfo(pid, PROC_PIDTBSDINFO, 0, &info, Int32(MemoryLayout<proc_bsdinfo>.size))
    return count == MemoryLayout<proc_bsdinfo>.size ? info : nil
}
func callerAncestors() -> Set<pid_t> {
    var result = Set<pid_t>()
    var pid = getppid()
    for _ in 0..<64 {
        guard pid > 1, result.insert(pid).inserted, let info = bsd(pid) else { break }
        pid = pid_t(info.pbi_ppid)
    }
    return result
}
func bounds(_ element: AXUIElement) -> CGRect? {
    guard let p = attribute(element, kAXPositionAttribute), let s = attribute(element, kAXSizeAttribute),
          CFGetTypeID(p) == AXValueGetTypeID(), CFGetTypeID(s) == AXValueGetTypeID() else { return nil }
    var point = CGPoint.zero; var size = CGSize.zero
    guard AXValueGetValue(unsafeBitCast(p, to: AXValue.self), .cgPoint, &point),
          AXValueGetValue(unsafeBitCast(s, to: AXValue.self), .cgSize, &size) else { return nil }
    return CGRect(origin: point, size: size)
}
func windowNumber(_ window: AXUIElement, _ pid: pid_t) -> String? {
    // Do not request or read CGWindowName; only PID, bounds and numeric id.
    guard let frame = bounds(window),
          let windows = CGWindowListCopyWindowInfo(.optionAll, kCGNullWindowID) as? [Object] else { return nil }
    let candidates = windows.filter { item in
        guard (item[kCGWindowOwnerPID as String] as? NSNumber)?.int32Value == pid,
              let raw = item[kCGWindowBounds as String] as? [String: Any],
              let other = CGRect(dictionaryRepresentation: raw as CFDictionary) else { return false }
        return abs(other.minX - frame.minX) < 2 && abs(other.minY - frame.minY) < 2 &&
            abs(other.width - frame.width) < 2 && abs(other.height - frame.height) < 2
    }
    guard candidates.count == 1, let id = candidates[0][kCGWindowNumber as String] as? NSNumber else { return nil }
    return id.stringValue
}
func validToken(_ value: String) -> Bool {
    value.range(of: "^[A-Za-z0-9][A-Za-z0-9_.:-]{0,127}$", options: .regularExpression) != nil
}
func userHeading(_ value: String, _ marker: String) -> Bool {
    let normalized = value.trimmingCharacters(in: .whitespacesAndNewlines).lowercased()
    return (normalized.hasPrefix("you ") || normalized.hasPrefix("you\n")) && value.contains(marker)
}
func completeResult(_ complete: Bool, _ userCount: Int, _ finalCard: Bool, _ stopAbsent: Bool, _ pending: Bool) -> Bool {
    complete && userCount == 1 && finalCard && stopAbsent && !pending
}

struct Tree {
    var elements: [AXUIElement] = []
    var complete = true
    init(_ root: AXUIElement, limit: Int = 12000) {
        var queue = [root]; var index = 0
        let leaves: Set<String> = ["AXStaticText", "AXButton", "AXCheckBox", "AXRadioButton", "AXTextArea", "AXTextField", "AXImage", "AXHeading", "AXLink", "AXScrollBar", "AXSplitter", "AXMenuItem", "AXPopUpButton"]
        while index < queue.count && elements.count < limit {
            let element = queue[index]; index += 1; elements.append(element)
            var children: CFTypeRef?
            let result = AXUIElementCopyAttributeValue(element, kAXChildrenAttribute as CFString, &children)
            if result == .success, let values = children as? [AXUIElement] { queue += values }
            else if result != .success && !leaves.contains(text(element, kAXRoleAttribute) ?? "") { complete = false }
        }
        if index < queue.count { complete = false }
    }
    func controls() -> [String: Int] {
        var counts: [String: Int] = [:]
        for element in elements where ["AXButton", "AXRadioButton", "AXMenuItem", "AXPopUpButton", "AXHeading"].contains(text(element, kAXRoleAttribute) ?? "") {
            for label in publicLabels where exactLabel(element, label) { counts[label, default: 0] += 1 }
        }
        return counts
    }
    func modelCandidates() -> [Object] {
        let names = ["Fable 5.1", "GPT-6 Astra", "Astra"]
        let suffixes = ["", ",", " (", " ·", " —", " max", " high", " ultra"]
        var output: [Object] = []
        for element in elements {
            let role = text(element, kAXRoleAttribute) ?? ""
            guard ["AXButton", "AXPopUpButton", "AXMenuItem"].contains(role) else { continue }
            let values = labels(element).map { $0.lowercased() }
            for name in names {
                let lower = name.lowercased()
                if values.contains(where: { value in suffixes.contains { suffix in
                    suffix.isEmpty ? value == lower : value.hasPrefix(lower + suffix)
                } }) {
                    output.append(["name": name, "role": role, "coverage": "label-candidate-only"])
                }
            }
        }
        return output
    }
}

func signedApp(_ provider: String) throws -> (NSRunningApplication, Object) {
    guard let identity = identities[provider] else { try fail("UNSUPPORTED_PROVIDER") }
    guard let installed = LSCopyApplicationURLsForBundleIdentifier(identity.0 as CFString, nil)?.takeRetainedValue() as? [URL] else { try fail("APP_NOT_INSTALLED") }
    let urls = Set(installed.map { $0.standardizedFileURL })
    guard urls.count == 1, let url = urls.first, let bundle = Bundle(url: url),
          bundle.bundleIdentifier == identity.0 else { try fail("APP_CANDIDATE_AMBIGUOUS") }
    let apps = NSRunningApplication.runningApplications(withBundleIdentifier: identity.0)
    guard apps.count == 1, let app = apps.first, app.bundleURL?.standardizedFileURL == url else { try fail("RUNNING_APP_AMBIGUOUS_OR_MISSING") }
    var code: SecStaticCode?
    guard SecStaticCodeCreateWithPath(url as CFURL, SecCSFlags(rawValue: 0), &code) == errSecSuccess, let code else { try fail("SIGNATURE_UNOBSERVABLE") }
    var requirement: SecRequirement?
    let expression = "anchor apple generic and identifier \"\(identity.0)\" and certificate leaf[subject.OU] = \"\(identity.1)\""
    guard SecRequirementCreateWithString(expression as CFString, SecCSFlags(rawValue: 0), &requirement) == errSecSuccess,
          let requirement,
          SecStaticCodeCheckValidity(code, SecCSFlags(rawValue: kSecCSStrictValidate | kSecCSCheckAllArchitectures | kSecCSCheckNestedCode), requirement) == errSecSuccess else { try fail("TARGET_SIGNATURE_MISMATCH") }
    guard let version = bundle.object(forInfoDictionaryKey: "CFBundleShortVersionString") as? String else { try fail("VERSION_UNOBSERVABLE") }
    guard version == identity.2 else { try fail("VERSION_PROFILE_UNCHARACTERIZED") }
    let start = app.launchDate?.timeIntervalSince1970 ?? bsd(app.processIdentifier).map { Double($0.pbi_start_tvsec) + Double($0.pbi_start_tvusec) / 1_000_000 } ?? 0
    guard start > 0 else { try fail("PROCESS_START_UNOBSERVABLE") }
    let receipt: Object = ["bundle_id": identity.0, "version": version, "team_id": identity.1,
                           "pid": app.processIdentifier, "start_time": start, "signature_verified": true,
                           "profile_id": provider + "-" + version + "-char007-development",
                           "account_hash": NSNull(), "caller_excluded": !callerAncestors().contains(app.processIdentifier)]
    return (app, receipt)
}

func scopedWindow(_ app: NSRunningApplication, _ job: Object) throws -> (AXUIElement, String) {
    let root = AXUIElementCreateApplication(app.processIdentifier)
    guard let windows = attribute(root, kAXWindowsAttribute) as? [AXUIElement], !windows.isEmpty else { try fail("WINDOW_UNOBSERVABLE") }
    let submitted = job["submitted_binding"] as? Object
    let draft = job["draft_binding"] as? Object
    let expected = submitted?["window_id"] as? String ?? draft?["window_id"] as? String
    let caller = callerAncestors().contains(app.processIdentifier)
    if caller && expected == nil { try fail("CALLER_WINDOW_ISOLATION_REQUIRED") }
    let matches: [(AXUIElement, String)] = windows.compactMap { window in
        guard let id = windowNumber(window, app.processIdentifier), expected == nil || id == expected else { return nil }
        return (window, id)
    }
    guard matches.count == 1 else { try fail("WINDOW_MATCH_AMBIGUOUS_OR_MISSING") }
    if caller {
        let excluded = job["caller_window_ids"] as? [String] ?? []
        guard !excluded.isEmpty, !excluded.contains(matches[0].1) else { try fail("CALLER_WINDOW_ISOLATION_REQUIRED") }
    }
    guard (attribute(matches[0].0, kAXMinimizedAttribute) as? NSNumber)?.boolValue == false else { try fail("WINDOW_MINIMIZED_OR_UNOBSERVABLE") }
    return matches[0]
}

func transactionOwned(_ payload: Object) throws {
    guard let number = payload["transaction_fd"] as? NSNumber else { try fail("TRANSACTION_LOCK_REQUIRED") }
    let fd = number.int32Value
    var info = stat()
    guard fd >= 3, fstat(fd, &info) == 0, info.st_uid == getuid(), (info.st_mode & S_IFMT) == S_IFREG, info.st_nlink == 1 else { try fail("TRANSACTION_LOCK_INVALID") }
    let path = FileManager.default.homeDirectoryForCurrentUser.appendingPathComponent("Library/Application Support/desktop-deep-research/desktop.lock").path
    let probe = open(path, O_RDWR | O_NOFOLLOW)
    guard probe >= 0 else { try fail("TRANSACTION_LOCK_INVALID") }
    defer { close(probe) }
    var other = stat()
    guard fstat(probe, &other) == 0, info.st_dev == other.st_dev, info.st_ino == other.st_ino else { try fail("TRANSACTION_LOCK_INVALID") }
    if flock(probe, LOCK_EX | LOCK_NB) == 0 {
        flock(probe, LOCK_UN)
        try fail("TRANSACTION_LOCK_NOT_HELD")
    }
    guard errno == EWOULDBLOCK, flock(fd, LOCK_EX | LOCK_NB) == 0 else { try fail("TRANSACTION_LOCK_NOT_OWNED") }
}

func selfTests() -> Object {
    let marker = "DRC-ABCDEFGH23456"
    let checks = [
        "closed_actions": !allowedActions.contains("arbitrary-key-command"),
        "assistant_echo_rejected": !userHeading("Claude " + marker, marker),
        "submitted_user_marker": userHeading("You [Desktop Research job " + marker + "]", marker),
        "incomplete_not_final": !completeResult(false, 1, true, true, false),
        "ambiguous_not_final": !completeResult(true, 2, true, true, false),
        "active_not_final": !completeResult(true, 1, true, false, false),
        "pending_not_final": !completeResult(true, 1, true, true, true),
        "positive_final": completeResult(true, 1, true, true, false),
        "traversal_token_rejected": !validToken("../report"),
        "source_urls_never_ocr": !publicLabels.contains("https://example.org"),
        "sha256_bytes": sha(Data("abc".utf8)) == "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad"
    ]
    return ["ok": !checks.values.contains(false), "complete": true, "boundary": "live",
            "self_tests": checks, "accessibility": AXIsProcessTrusted(), "screen_capture": CGPreflightScreenCaptureAccess(),
            "scope": "pure native guard tests and exact-binary permission preflight; no UI mutation or Research acceptance"]
}

func perform(_ request: Object) throws -> Object {
    guard Set(request.keys) == ["action", "job", "payload"], let action = request["action"] as? String,
          allowedActions.contains(action), let job = request["job"] as? Object,
          let payload = request["payload"] as? Object else { try fail("INVALID_NATIVE_COMMAND") }
    if action == "self-test" { return selfTests() }
    let provider = job["provider"] as? String ?? payload["provider"] as? String ?? ""
    let (app, appReceipt) = try signedApp(provider)
    let accessibility = AXIsProcessTrusted()
    let capture = CGPreflightScreenCaptureAccess()
    var result: Object = ["ok": true, "complete": false, "boundary": "live", "app": appReceipt,
                          "accessibility": accessibility, "screen_capture": capture,
                          "idle_seconds": idle(), "ready": false, "research_available": false]
    guard accessibility else { result["reason"] = "ACCESSIBILITY_PERMISSION_REQUIRED"; return result }
    if mutations.contains(action) {
        try transactionOwned(payload)
        guard idle().isFinite, idle() >= 2 else { try fail("USER_INPUT_ACTIVE") }
        // No mutation until each characterized provider adapter is implemented
        // and gated. A valid lock alone is never input authority.
        result["reason"] = "NATIVE_ADAPTER_IN_PROGRESS"
        return result
    }
    let (window, windowID) = try scopedWindow(app, job)
    let tree = Tree(window)
    result["complete"] = tree.complete
    result["window_id"] = windowID
    result["controls"] = tree.controls()
    result["model_candidates"] = tree.modelCandidates()
    result["nodes"] = tree.elements.count
    result["reason"] = provider == "chatgpt" ? "CHATGPT_PROFILE_NOT_CHARACTERIZED" : "NATIVE_ADAPTER_IN_PROGRESS"
    // No state/completion claim from generic labels or incomplete trees.
    return result
}

_ = NSApplication.shared
NSApplication.shared.setActivationPolicy(.prohibited)
_ = CGMainDisplayID()
do {
    var data = Data()
    while let chunk = try FileHandle.standardInput.read(upToCount: 4096), !chunk.isEmpty {
        data.append(chunk)
        guard data.count <= 1_048_576 else { try fail("NATIVE_REQUEST_TOO_LARGE") }
    }
    guard let request = try JSONSerialization.jsonObject(with: data) as? Object else { try fail("INVALID_NATIVE_COMMAND") }
    let result = try perform(request)
    print(String(data: try JSONSerialization.data(withJSONObject: result, options: [.sortedKeys]), encoding: .utf8)!)
    exit((result["ok"] as? Bool == true) ? 0 : 3)
} catch let failure as Failure {
    print("{\"ok\":false,\"complete\":false,\"boundary\":\"live\",\"ready\":false,\"reason\":\"\(failure.reason)\"}")
    exit(3)
} catch {
    // Never include arbitrary exception data or unrelated UI content.
    print("{\"ok\":false,\"complete\":false,\"boundary\":\"live\",\"ready\":false,\"reason\":\"NATIVE_INTERNAL_ERROR\"}")
    exit(3)
}
