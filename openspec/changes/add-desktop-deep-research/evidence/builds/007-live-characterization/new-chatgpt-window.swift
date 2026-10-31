import AppKit
import ApplicationServices
import CoreGraphics
import CryptoKit
import Darwin
import Foundation

let rootDirectory = FileManager.default.homeDirectoryForCurrentUser.appendingPathComponent("Library/Application Support/desktop-deep-research")
var locks: [Int32] = []
defer { for fd in locks.reversed() { flock(fd, LOCK_UN); close(fd) } }
func lock(_ name: String) {
    let fd = open(rootDirectory.appendingPathComponent(name).path, O_CREAT | O_RDWR | O_NOFOLLOW, mode_t(0o600))
    guard fd >= 0, flock(fd, LOCK_EX | LOCK_NB) == 0 else { print("LOCK_BUSY"); exit(2) }
    locks.append(fd)
}
lock("ledger.lock")
let ledgerURL = rootDirectory.appendingPathComponent("ledger.json")
var ledger = try JSONSerialization.jsonObject(with: Data(contentsOf: ledgerURL)) as! [String: Any]
let requests = ledger["requests"] as! [String: String]
guard let jobID = requests["characterization-20261004-chatgpt"] else { exit(2) }
lock(jobID + ".lock")
var jobs = ledger["jobs"] as! [String: [String: Any]]
guard var job = jobs[jobID], job["provider_state"] as? String == "PREPARED", job["prompt_possibly_sent"] as? Bool == false else { print("JOB_NOT_PREPARED"); exit(2) }
if job["draft_creation"] != nil { print("DRAFT_ALREADY_CREATED"); exit(0) }
lock("desktop.lock")
let candidates = NSRunningApplication.runningApplications(withBundleIdentifier: "com.openai.codex")
guard candidates.count == 1, let app = candidates.first else { print("APP_AMBIGUOUS"); exit(2) }
func idle() -> Double { CGEventSource.secondsSinceLastEventType(.combinedSessionState, eventType: CGEventType(rawValue: UInt32.max)!) }
guard idle() >= 2 else { print("USER_INPUT_ACTIVE"); exit(2) }
_ = NSApplication.shared
if NSWorkspace.shared.frontmostApplication?.processIdentifier != app.processIdentifier {
    guard app.activate(options: [.activateAllWindows]) else { exit(2) }
    let deadline = Date().addingTimeInterval(2)
    while NSWorkspace.shared.frontmostApplication?.processIdentifier != app.processIdentifier && Date() < deadline { RunLoop.current.run(until: Date().addingTimeInterval(0.05)) }
}
func attr(_ e: AXUIElement, _ key: String) -> CFTypeRef? {
    var value: CFTypeRef?
    return AXUIElementCopyAttributeValue(e, key as CFString, &value) == .success ? value : nil
}
let root = AXUIElementCreateApplication(app.processIdentifier)
guard let old = attr(root, kAXWindowsAttribute) as? [AXUIElement],
      let bar = attr(root, kAXMenuBarAttribute), CFGetTypeID(bar) == AXUIElementGetTypeID() else { exit(2) }
var queue = [unsafeBitCast(bar, to: AXUIElement.self)]; var found: [AXUIElement] = []; var count = 0
while !queue.isEmpty && count < 3000 {
    let e = queue.removeFirst(); count += 1
    let labels = [kAXTitleAttribute, kAXDescriptionAttribute, kAXHelpAttribute, kAXValueAttribute].compactMap { attr(e, $0) as? String }.map { $0.trimmingCharacters(in: .whitespacesAndNewlines).lowercased() }
    if attr(e, kAXRoleAttribute) as? String == "AXMenuItem", labels.contains("new window") { found.append(e) }
    if let children = attr(e, kAXChildrenAttribute) as? [AXUIElement] { queue += children }
}
guard queue.isEmpty, found.count == 1, idle() >= 2,
      NSWorkspace.shared.frontmostApplication?.processIdentifier == app.processIdentifier else { print("CREATION_GUARD_FAILED"); exit(2) }
let code = AXUIElementPerformAction(found[0], kAXPressAction as CFString)
guard code == .success else { print("NEW_WINDOW_ACTION_FAILED code=\(code.rawValue)"); exit(2) }
var windows: [AXUIElement] = []
let deadline = Date().addingTimeInterval(3)
repeat {
    RunLoop.current.run(until: Date().addingTimeInterval(0.1))
    windows = attr(root, kAXWindowsAttribute) as? [AXUIElement] ?? []
} while windows.count == old.count && Date() < deadline
let new = windows.filter { window in !old.contains { CFEqual(window, $0) } }
guard windows.count == old.count + 1, new.count == 1,
      old.allSatisfy({ previous in windows.contains { CFEqual(previous, $0) } }) else {
    print("NEW_WINDOW_UNOBSERVABLE before=\(old.count) after=\(windows.count) new=\(new.count)"); exit(2)
}
let window = new[0]
var point = CGPoint.zero; var size = CGSize.zero
if let p = attr(window, kAXPositionAttribute), CFGetTypeID(p) == AXValueGetTypeID() { AXValueGetValue(unsafeBitCast(p, to: AXValue.self), .cgPoint, &point) }
if let s = attr(window, kAXSizeAttribute), CFGetTypeID(s) == AXValueGetTypeID() { AXValueGetValue(unsafeBitCast(s, to: AXValue.self), .cgSize, &size) }
let title = attr(window, kAXTitleAttribute) as? String ?? ""
let receipt: [String: Any] = ["method": "native-new-window-menu", "all_preexisting_windows_excluded": true, "prior_window_count": old.count, "new_window_count": windows.count, "window_index": windows.firstIndex(where: { CFEqual($0, window) })!, "x": point.x, "y": point.y, "width": size.width, "height": size.height, "window_title_hash": SHA256.hash(data: Data(title.utf8)).map { String(format: "%02x", $0) }.joined(), "pid": app.processIdentifier, "process_start": app.launchDate?.timeIntervalSince1970 ?? 0, "timestamp": Date().timeIntervalSince1970]
job["draft_creation"] = receipt
jobs[jobID] = job; ledger["jobs"] = jobs
func save(_ value: Any, _ url: URL) throws {
    let data = try JSONSerialization.data(withJSONObject: value, options: [.prettyPrinted, .sortedKeys])
    let temp = url.appendingPathExtension("pending")
    let fd = open(temp.path, O_CREAT | O_EXCL | O_WRONLY | O_NOFOLLOW, mode_t(0o600))
    guard fd >= 0 else { exit(2) }
    let handle = FileHandle(fileDescriptor: fd, closeOnDealloc: false)
    try handle.write(contentsOf: data); try handle.synchronize(); try handle.close()
    guard rename(temp.path, url.path) == 0 else { exit(2) }
    let dir = open(url.deletingLastPathComponent().path, O_RDONLY); fsync(dir); close(dir)
}
try save(job, rootDirectory.appendingPathComponent("reports/" + jobID + "/job.json"))
try save(ledger, ledgerURL)
print("new_dedicated_window_created=true old_windows_preserved=\(old.count) selected_window_index=\(receipt["window_index"]!)")
