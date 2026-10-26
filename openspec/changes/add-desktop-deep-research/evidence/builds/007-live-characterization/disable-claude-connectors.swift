import AppKit
import ApplicationServices
import CoreGraphics
import CryptoKit
import Darwin
import Foundation

let dir = FileManager.default.homeDirectoryForCurrentUser.appendingPathComponent("Library/Application Support/desktop-deep-research")
var locks: [Int32] = []
defer { for fd in locks.reversed() { flock(fd, LOCK_UN); close(fd) } }
func lock(_ name: String) {
    let fd = open(dir.appendingPathComponent(name).path, O_CREAT | O_RDWR | O_NOFOLLOW, mode_t(0o600))
    guard fd >= 0, flock(fd, LOCK_EX | LOCK_NB) == 0 else { print("LOCK_BUSY"); exit(2) }
    locks.append(fd)
}
lock("ledger.lock")
let ledgerURL = dir.appendingPathComponent("ledger.json")
var ledger = try JSONSerialization.jsonObject(with: Data(contentsOf: ledgerURL)) as! [String: Any]
let id = (ledger["requests"] as! [String: String])["characterization-20261004-claude"]!
lock(id + ".lock")
var jobs = ledger["jobs"] as! [String: [String: Any]]
var job = jobs[id]!
guard job["prompt_possibly_sent"] as? Bool == false, job["provider_state"] as? String == "PREPARED" else { exit(2) }
lock("desktop.lock")
func attr(_ e: AXUIElement, _ key: String) -> CFTypeRef? {
    var v: CFTypeRef?
    return AXUIElementCopyAttributeValue(e, key as CFString, &v) == .success ? v : nil
}
func hash(_ s: String) -> String { SHA256.hash(data: Data(s.utf8)).map { String(format: "%02x", $0) }.joined() }
func idle() -> Double { CGEventSource.secondsSinceLastEventType(.combinedSessionState, eventType: CGEventType(rawValue: UInt32.max)!) }
guard let app = NSRunningApplication.runningApplications(withBundleIdentifier: "com.anthropic.claudefordesktop").first, idle() >= 2 else { print("INPUT_GUARD idle=\(idle())"); exit(2) }
_ = NSApplication.shared
if NSWorkspace.shared.frontmostApplication?.processIdentifier != app.processIdentifier {
    guard app.activate(options: [.activateAllWindows]) else { exit(2) }
    let deadline = Date().addingTimeInterval(2)
    while NSWorkspace.shared.frontmostApplication?.processIdentifier != app.processIdentifier && Date() < deadline { RunLoop.current.run(until: Date().addingTimeInterval(0.05)) }
}
guard NSWorkspace.shared.frontmostApplication?.processIdentifier == app.processIdentifier, idle() >= 2 else { print("FOCUS_OR_INPUT_GUARD"); exit(2) }
let root = AXUIElementCreateApplication(app.processIdentifier)
func scan() -> [(AXUIElement, String)] {
    var q = [(root, "app")]; var all: [(AXUIElement, String)] = []
    while !q.isEmpty && all.count < 6000 {
        let (e, path) = q.removeFirst(); all.append((e, path))
        for (i, c) in (attr(e, kAXChildrenAttribute) as? [AXUIElement] ?? []).enumerated() { q.append((c, path + "/" + String(i))) }
    }
    guard q.isEmpty else { print("AX_TRUNCATED"); exit(2) }; return all
}
func save() throws {
    jobs[id] = job; ledger["jobs"] = jobs
    for (value, url) in [(job, dir.appendingPathComponent("reports/" + id + "/job.json")), (ledger, ledgerURL)] {
        let data = try JSONSerialization.data(withJSONObject: value, options: [.prettyPrinted, .sortedKeys])
        let tmp = url.appendingPathExtension("pending")
        let fd = open(tmp.path, O_CREAT | O_EXCL | O_WRONLY | O_NOFOLLOW, mode_t(0o600)); guard fd >= 0 else { exit(2) }
        let h = FileHandle(fileDescriptor: fd, closeOnDealloc: false); try h.write(contentsOf: data); try h.synchronize(); try h.close()
        guard rename(tmp.path, url.path) == 0 else { exit(2) }
        let p = open(url.deletingLastPathComponent().path, O_RDONLY); fsync(p); close(p)
    }
}
let all = scan()
let drafts = all.filter { attr($0.0, kAXRoleAttribute) as? String == "AXTextArea" }
guard drafts.count == 1, let draft = attr(drafts[0].0, kAXValueAttribute) as? String,
      draft.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty else { print("DRAFT_NOT_EMPTY"); exit(2) }
let sourcePrefix = "app/0/0/0/0/0/0/1/0/0/0/0/0/0/1/0/0/1/0/3/0/2/0/0/0/1/0/1/0/0/0/6/0/0/0/3/"
let controls = all.filter { $0.1.hasPrefix(sourcePrefix) && !$0.1.dropFirst(sourcePrefix.count).contains("/") && attr($0.0, kAXRoleAttribute) as? String == "AXMenuItem" }
guard controls.count == 12 else { print("CONNECTOR_LIST_CHANGED count=\(controls.count)"); exit(2) }
let research = all.filter { attr($0.0, kAXRoleAttribute) as? String == "AXMenuItem" && (attr($0.0, kAXTitleAttribute) as? String)?.lowercased() == "research" }
guard research.count == 1, (attr(research[0].0, kAXValueAttribute) as? NSNumber)?.intValue == 1 else { print("RESEARCH_NOT_SELECTED"); exit(2) }
if job["draft_creation"] == nil {
    job["draft_creation"] = ["method": "observed-native-new-chat-menu-and-empty-composer", "pid": app.processIdentifier, "process_start": app.launchDate?.timeIntervalSince1970 ?? 0, "composer_path": drafts[0].1, "empty_value_hash": hash(draft), "timestamp": Date().timeIntervalSince1970]
}
if job["connector_original_states"] == nil {
    job["connector_original_states"] = controls.map { ["path": $0.1, "label_hash": hash(attr($0.0, kAXTitleAttribute) as? String ?? ""), "original_value": (attr($0.0, kAXValueAttribute) as? NSNumber)?.intValue ?? -1] as [String: Any] }
    job["connector_scope_documentation"] = "https://support.claude.com/en/articles/11175166-get-started-with-custom-connectors-using-remote-mcp"
    try save()
}
var disabled = 0
for (control, path) in controls {
    guard (attr(control, kAXValueAttribute) as? NSNumber)?.intValue == 1 else { continue }
    guard idle() >= 2, NSWorkspace.shared.frontmostApplication?.processIdentifier == app.processIdentifier else { print("INPUT_CHANGED"); exit(2) }
    job["connector_mutation_intent"] = ["path": path, "target_value": 0, "timestamp": Date().timeIntervalSince1970]; try save()
    let action = AXUIElementPerformAction(control, kAXPressAction as CFString)
    RunLoop.current.run(until: Date().addingTimeInterval(0.3))
    guard action == .success, (attr(control, kAXValueAttribute) as? NSNumber)?.intValue == 0 else {
        print("CONNECTOR_CHANGE_UNVERIFIED changed_before_failure=\(disabled) action=\(action.rawValue)"); exit(2)
    }
    disabled += 1
    job["connector_disabled_count"] = disabled; try save()
}
let fresh = scan().filter { $0.1.hasPrefix(sourcePrefix) && !$0.1.dropFirst(sourcePrefix.count).contains("/") && attr($0.0, kAXRoleAttribute) as? String == "AXMenuItem" }
let enabled = fresh.filter { (attr($0.0, kAXValueAttribute) as? NSNumber)?.intValue == 1 }.count
job["connector_observation"] = ["count": fresh.count, "enabled": enabled, "changed": disabled, "timestamp": Date().timeIntervalSince1970, "coverage": "AX_LIST_PARTIAL_PENDING_SCROLL_COMPLETENESS"]
try save()
print("conversation_connectors_changed=\(disabled) list_count=\(fresh.count) enabled_remaining=\(enabled)")
