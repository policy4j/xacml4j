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
    guard fd >= 0, flock(fd, LOCK_EX | LOCK_NB) == 0 else { exit(2) }; locks.append(fd)
}
lock("ledger.lock")
let ledgerURL = dir.appendingPathComponent("ledger.json")
var ledger = try JSONSerialization.jsonObject(with: Data(contentsOf: ledgerURL)) as! [String: Any]
let id = (ledger["requests"] as! [String: String])["characterization-20261004-claude"]!
lock(id + ".lock")
var jobs = ledger["jobs"] as! [String: [String: Any]]; var job = jobs[id]!
guard job["draft_creation"] != nil, job["prompt_possibly_sent"] as? Bool == false, job["provider_state"] as? String == "DRAFTING" else { print("JOB_GUARD"); exit(2) }
lock("desktop.lock")
func attr(_ e: AXUIElement, _ key: String) -> CFTypeRef? { var v: CFTypeRef?; return AXUIElementCopyAttributeValue(e, key as CFString, &v) == .success ? v : nil }
func idle() -> Double { CGEventSource.secondsSinceLastEventType(.combinedSessionState, eventType: CGEventType(rawValue: UInt32.max)!) }
guard let app = NSRunningApplication.runningApplications(withBundleIdentifier: "com.anthropic.claudefordesktop").first, idle() >= 2 else { print("INPUT_GUARD"); exit(2) }
_ = NSApplication.shared
if NSWorkspace.shared.frontmostApplication?.processIdentifier != app.processIdentifier {
    guard app.activate(options: [.activateAllWindows]) else { exit(2) }; let d = Date().addingTimeInterval(2)
    while NSWorkspace.shared.frontmostApplication?.processIdentifier != app.processIdentifier && Date() < d { RunLoop.current.run(until: Date().addingTimeInterval(0.05)) }
}
let root = AXUIElementCreateApplication(app.processIdentifier)
func scan() -> [AXUIElement] {
    var q = [root]; var all: [AXUIElement] = []
    while !q.isEmpty && all.count < 6000 { let e = q.removeFirst(); all.append(e); q += attr(e, kAXChildrenAttribute) as? [AXUIElement] ?? [] }
    guard q.isEmpty else { exit(2) }; return all
}
func labels(_ e: AXUIElement) -> [String] { [kAXTitleAttribute, kAXDescriptionAttribute, kAXHelpAttribute].compactMap { attr(e, $0) as? String }.map { $0.trimmingCharacters(in: .whitespacesAndNewlines).lowercased() } }
func guardFocus() { guard idle() >= 2, NSWorkspace.shared.frontmostApplication?.processIdentifier == app.processIdentifier else { print("FOCUS_OR_INPUT_CHANGED"); exit(2) } }
func save() throws {
    jobs[id] = job; ledger["jobs"] = jobs
    for (value, url) in [(job, dir.appendingPathComponent("reports/" + id + "/job.json")), (ledger, ledgerURL)] {
        let tmp = url.appendingPathExtension("pending"); let fd = open(tmp.path, O_CREAT | O_EXCL | O_WRONLY | O_NOFOLLOW, mode_t(0o600)); guard fd >= 0 else { exit(2) }
        let h = FileHandle(fileDescriptor: fd, closeOnDealloc: false); try h.write(contentsOf: JSONSerialization.data(withJSONObject: value, options: [.prettyPrinted, .sortedKeys])); try h.synchronize(); try h.close()
        guard rename(tmp.path, url.path) == 0 else { exit(2) }; let p = open(url.deletingLastPathComponent().path, O_RDONLY); fsync(p); close(p)
    }
}


let all = scan()
let drafts = all.filter { attr($0, kAXRoleAttribute) as? String == "AXTextArea" }
let prompt = try String(contentsOf: dir.appendingPathComponent("reports/" + id + "/prompt.txt"), encoding: .utf8)
guard drafts.count == 1, let actual = attr(drafts[0], kAXValueAttribute) as? String else { exit(2) }
let exact = actual == prompt
let appendedNewline = actual == prompt + "\n"
let removedFinalNewline = actual + "\n" == prompt
print("full_prompt_exact=\(exact) full_prompt_plus_ui_newline=\(appendedNewline) removed_final_newline=\(removedFinalNewline) expected_length=\(prompt.count) observed_length=\(actual.count)")
guard exact || appendedNewline || removedFinalNewline else { print("OWNED_DRAFT_NOT_VERIFIED"); exit(2) }
job["owned_composer_hash"] = SHA256.hash(data: Data(actual.utf8)).map { String(format: "%02x", $0) }.joined()
job["owned_composer_end_markers"] = true
job["input_normalization"] = removedFinalNewline ? "single-removed-final-newline" : (appendedNewline ? "single-appended-UI-newline" : "exact")
try save()
let add = all.filter { attr($0, kAXRoleAttribute) as? String == "AXPopUpButton" && labels($0).contains { $0 == "add" || $0.hasPrefix("add ") } }
guard add.count == 1 else { exit(2) }
guardFocus()
guard AXUIElementPerformAction(add[0], kAXPressAction as CFString) == .success else { exit(2) }
print("owned_draft_verified=true source_menu_open_requested=true send_performed=false")
