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
guard job["draft_creation"] != nil, job["prompt_possibly_sent"] as? Bool == false, job["provider_state"] as? String == "PREPARED" else { print("JOB_GUARD"); exit(2) }
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
var all = scan()
let drafts = all.filter { attr($0, kAXRoleAttribute) as? String == "AXTextArea" }
guard drafts.count == 1, let empty = attr(drafts[0], kAXValueAttribute) as? String, empty.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty else { print("DRAFT_NOT_EMPTY"); exit(2) }
let memory = all.filter { attr($0, kAXRoleAttribute) as? String == "AXMenuItem" && labels($0).contains { $0.range(of: "\\bmemory\\b", options: .regularExpression) != nil } }
guard memory.count == 1, let memoryValue = attr(memory[0], kAXValueAttribute) as? NSNumber else { print("MEMORY_UNOBSERVABLE"); exit(2) }
if job["memory_original_state"] == nil { job["memory_original_state"] = memoryValue.intValue; try save() }
if memoryValue.intValue == 1 {
    guardFocus(); job["memory_mutation_intent"] = ["target_value": 0, "timestamp": Date().timeIntervalSince1970]; try save()
    guard AXUIElementPerformAction(memory[0], kAXPressAction as CFString) == .success else { exit(2) }
    RunLoop.current.run(until: Date().addingTimeInterval(0.3))
}
guard (attr(memory[0], kAXValueAttribute) as? NSNumber)?.intValue == 0 else { print("MEMORY_CHANGE_UNVERIFIED"); exit(2) }
job["memory_disabled_observed"] = true; try save()
all = scan()
let research = all.filter { attr($0, kAXRoleAttribute) as? String == "AXMenuItem" && labels($0).contains("research") }
guard research.count == 1, (attr(research[0], kAXValueAttribute) as? NSNumber)?.intValue == 1,
      let connectors = job["connector_observation"] as? [String: Any], connectors["enabled"] as? Int == 0, connectors["count"] as? Int == 12 else { print("SOURCE_OR_MODE_GUARD"); exit(2) }
let add = all.filter { attr($0, kAXRoleAttribute) as? String == "AXPopUpButton" && labels($0).contains { $0 == "add" || $0.hasPrefix("add ") } }
guard add.count == 1 else { exit(2) }; guardFocus()
guard AXUIElementPerformAction(add[0], kAXPressAction as CFString) == .success else { exit(2) }
RunLoop.current.run(until: Date().addingTimeInterval(0.3))
let prompt = try String(contentsOf: dir.appendingPathComponent("reports/" + id + "/prompt.txt"), encoding: .utf8)
let marker = job["marker"] as! String
guard prompt.hasPrefix(marker), prompt.trimmingCharacters(in: .whitespacesAndNewlines).hasSuffix(marker) else { print("PROMPT_MARKER_INVALID"); exit(2) }
guardFocus()
job["provider_state"] = "DRAFTING"; job["submission_intent"] = ["phase": "before-input", "timestamp": Date().timeIntervalSince1970, "memory_off": true, "connectors_enabled": 0, "research_selected": true, "web_search_coverage": "RESEARCH_PROVIDER_PREREQUISITE_DOCUMENTED_UI_NOT_EXPOSED"]
try save()
let set = AXUIElementSetAttributeValue(drafts[0], kAXValueAttribute as CFString, prompt as CFString)
RunLoop.current.run(until: Date().addingTimeInterval(0.5))
guard set == .success, let actual = attr(drafts[0], kAXValueAttribute) as? String, actual == prompt else { print("INPUT_NOT_VERIFIED native=\(set.rawValue)"); exit(2) }
job["owned_composer_hash"] = SHA256.hash(data: Data(actual.utf8)).map { String(format: "%02x", $0) }.joined(); job["owned_composer_end_markers"] = true; try save()
print("public_sample_entered=true full_value_verified=true send_performed=false memory_off=true connectors_enabled=0 research_selected=true")
