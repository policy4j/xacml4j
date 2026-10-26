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


guard job["provider_state"] as? String=="COMPLETED",job["collection_state"] as? String=="COLLECTED" else{exit(2)}
let deadline=Date().addingTimeInterval(10)
while idle()<2 && Date()<deadline{RunLoop.current.run(until:Date().addingTimeInterval(0.1))}
guard idle()>=2 else{exit(2)}
_ = NSApplication.shared
if NSWorkspace.shared.frontmostApplication?.processIdentifier != app.processIdentifier{
 guard app.activate(options:[.activateAllWindows]) else{exit(2)};let d=Date().addingTimeInterval(2)
 while NSWorkspace.shared.frontmostApplication?.processIdentifier != app.processIdentifier && Date()<d{RunLoop.current.run(until:Date().addingTimeInterval(0.05))}
}
func guardOwned(){
 let all=scan();let marker=job["marker"] as! String
 guard all.filter({e in attr(e,kAXRoleAttribute) as? String=="AXHeading" && [kAXTitleAttribute,kAXDescriptionAttribute,kAXValueAttribute].compactMap{attr(e,$0) as? String}.contains{$0.lowercased().hasPrefix("you") && $0.contains(marker)}}).count==1 else{exit(2)}
 let drafts=all.filter{attr($0,kAXRoleAttribute) as? String=="AXTextArea"}
 guard drafts.count==1,(attr(drafts[0],kAXValueAttribute) as? String ?? "").trimmingCharacters(in:.whitespacesAndNewlines).isEmpty else{print("RESTORE_DEFERRED_NEW_DRAFT");exit(2)}
 guardFocus()
}
func add(){
 let controls=scan().filter{attr($0,kAXRoleAttribute) as? String=="AXPopUpButton" && labels($0).contains{$0=="add" || $0.hasPrefix("add ")}}
 guard controls.count==1 else{exit(2)};guardOwned();guard AXUIElementPerformAction(controls[0],kAXPressAction as CFString) == .success else{exit(2)}
 RunLoop.current.run(until:Date().addingTimeInterval(0.3))
}
func research()->[AXUIElement]{scan().filter{attr($0,kAXRoleAttribute) as? String=="AXMenuItem" && labels($0).contains("research")}}
guardOwned()
if research().isEmpty{add()}
let controls=research()
guard controls.count==1,let value=attr(controls[0],kAXValueAttribute) as? NSNumber else{exit(2)}
if value.intValue==1{
 job["mode_restoration_intent"]=["target":0,"timestamp":Date().timeIntervalSince1970];try save();guardOwned()
 guard AXUIElementPerformAction(controls[0],kAXPressAction as CFString) == .success else{exit(2)}
 RunLoop.current.run(until:Date().addingTimeInterval(0.3))
}
if research().isEmpty{add()}
let restored=research()
guard restored.count==1,(attr(restored[0],kAXValueAttribute) as? NSNumber)?.intValue==0 else{print("MODE_RESTORE_UNVERIFIED");exit(2)}
job["research_mode_restored_off"]=true;try save()
print("research_mode_restored_off=true sources_menu_open=true")

