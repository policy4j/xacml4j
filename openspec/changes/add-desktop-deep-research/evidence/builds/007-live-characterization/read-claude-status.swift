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


guard job["submitted_binding"] != nil else {exit(2)}
let all=scan()
let marker=job["marker"] as! String
let users=all.filter{e in attr(e,kAXRoleAttribute) as? String=="AXHeading" && [kAXTitleAttribute,kAXDescriptionAttribute,kAXValueAttribute].compactMap{attr(e,$0) as? String}.contains{$0.lowercased().hasPrefix("you") && $0.contains(marker)}}
guard users.count==1 else{exit(2)}
let heads=all.filter{attr($0,kAXRoleAttribute) as? String=="AXHeading" && labels($0).contains{$0=="claude" || $0.hasPrefix("claude ")}}
guard heads.count==1,let par=attr(heads[0],kAXParentAttribute),CFGetTypeID(par)==AXUIElementGetTypeID() else{print("ASSISTANT_AMBIGUOUS");exit(2)}
let parent=unsafeBitCast(par,to:AXUIElement.self)
var q=[parent];var textNodes:[String]=[];var buttons:[String]=[];var nativeLinks:[String]=[]
while !q.isEmpty{
    let e=q.removeFirst();let r=attr(e,kAXRoleAttribute) as? String ?? ""
    if r=="AXStaticText",let s=attr(e,kAXValueAttribute) as? String ?? attr(e,kAXTitleAttribute) as? String,!s.isEmpty{textNodes.append(s)}
    if r=="AXButton"{buttons += labels(e)}
    if r=="AXLink",let v=attr(e,kAXURLAttribute){if let u=v as? URL{nativeLinks.append(u.absoluteString)}else if let s=v as? String{nativeLinks.append(s)}}
    q.insert(contentsOf:attr(e,kAXChildrenAttribute) as? [AXUIElement] ?? [],at:0)
}
let text=textNodes.joined(separator:"\n")
let output=dir.appendingPathComponent("reports/"+id+"/observed-assistant.txt")
let fd=open(output.path,O_CREAT|O_TRUNC|O_WRONLY|O_NOFOLLOW,mode_t(0o600));guard fd>=0 else{exit(2)}
let h=FileHandle(fileDescriptor:fd,closeOnDealloc:false);try h.write(contentsOf:Data(text.utf8));try h.synchronize();try h.close()
let safeButtons=buttons.map{s in ["research","sources","complete","finished","start","stop","response","copy","report","planning","searching","thinking"].filter{w in s.range(of:"\\b"+w+"\\b",options:.regularExpression) != nil}}
let stop=all.contains{attr($0,kAXRoleAttribute) as? String=="AXButton" && labels($0).contains{$0.hasPrefix("stop response")}}
let counters = text.components(separatedBy:"\n").filter { $0.range(of:"^[0-9]+ sources and counting",options:.regularExpression) != nil }
print(String(data:try JSONSerialization.data(withJSONObject:["body_characters":text.count,"native_link_count":nativeLinks.count,"stop_response_present":stop,"button_flags":safeButtons,"research_source_counters":counters],options:[.sortedKeys]),encoding:.utf8)!)
