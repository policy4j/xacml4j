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
guard job["draft_creation"] != nil, job["prompt_possibly_sent"] as? Bool == true else { print("JOB_GUARD"); exit(2) }
lock("desktop.lock")
func attr(_ e: AXUIElement, _ key: String) -> CFTypeRef? { var v: CFTypeRef?; return AXUIElementCopyAttributeValue(e, key as CFString, &v) == .success ? v : nil }
func idle() -> Double { CGEventSource.secondsSinceLastEventType(.combinedSessionState, eventType: CGEventType(rawValue: UInt32.max)!) }
guard let app = NSRunningApplication.runningApplications(withBundleIdentifier: "com.anthropic.claudefordesktop").first else { exit(2) }
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


guard let binding=job["submitted_binding"] as? [String:Any],binding["pid"] as? Int==Int(app.processIdentifier) else{print("BINDING_CHANGED");exit(2)}
let idleDeadline=Date().addingTimeInterval(10)
while idle()<2 && Date()<idleDeadline{RunLoop.current.run(until:Date().addingTimeInterval(0.1))}
guard idle()>=2 else{exit(2)}
_ = NSApplication.shared
if NSWorkspace.shared.frontmostApplication?.processIdentifier != app.processIdentifier{
 guard app.activate(options:[.activateAllWindows]) else{exit(2)};let d=Date().addingTimeInterval(2)
 while NSWorkspace.shared.frontmostApplication?.processIdentifier != app.processIdentifier && Date()<d{RunLoop.current.run(until:Date().addingTimeInterval(0.05))}
}
let all=scan();let marker=job["marker"] as! String
let users=all.filter{e in attr(e,kAXRoleAttribute) as? String=="AXHeading" && [kAXTitleAttribute,kAXDescriptionAttribute,kAXValueAttribute].compactMap{attr(e,$0) as? String}.contains{$0.lowercased().hasPrefix("you") && $0.contains(marker)}}
let done=all.filter{attr($0,kAXRoleAttribute) as? String=="AXStaticText" && (attr($0,kAXValueAttribute) as? String ?? attr($0,kAXTitleAttribute) as? String ?? "").lowercased()=="research complete"}
let stop=all.filter{attr($0,kAXRoleAttribute) as? String=="AXButton" && labels($0).contains{$0.hasPrefix("stop response")}}
let titles=all.filter{e in attr(e,kAXRoleAttribute) as? String=="AXStaticText" && [kAXTitleAttribute,kAXValueAttribute].compactMap{attr(e,$0) as? String}.contains{$0.hasPrefix("Claude Research Mode on Desktop: How to Enable It and How Reports Are Collected")}}
guard users.count==1,done.count==1,stop.isEmpty,titles.count==1 else{print("COMPLETION_UNVERIFIED users=\(users.count) done=\(done.count) stop=\(stop.count) titles=\(titles.count)");exit(2)}
job["provider_state"]="COMPLETED";job["completion_observation"]=["predicate":"unique-bound-USER-turn+Research-complete+no-Stop+unique-document-card","timestamp":Date().timeIntervalSince1970];try save()
var e=titles[0]
for _ in 0..<2{guard let p=attr(e,kAXParentAttribute),CFGetTypeID(p)==AXUIElementGetTypeID() else{exit(2)};e=unsafeBitCast(p,to:AXUIElement.self)}
let cs=(attr(e,kAXChildrenAttribute) as? [AXUIElement] ?? []).filter{attr($0,kAXRoleAttribute) as? String=="AXCheckBox"}
guard cs.count==1 else{print("DOCUMENT_CONTROL_AMBIGUOUS");exit(2)}
guard let selected=attr(cs[0],kAXValueAttribute) as? NSNumber else{print("DOCUMENT_SELECTION_UNOBSERVABLE");exit(2)}
if selected.intValue==1{print("completion_observed=true document_already_open=true");exit(0)}
guard selected.intValue==0 else{exit(2)}
guardFocus()
let result=AXUIElementPerformAction(cs[0],kAXPressAction as CFString)
RunLoop.current.run(until:Date().addingTimeInterval(0.5))
print("completion_observed=true document_open_action=\(result.rawValue)")
