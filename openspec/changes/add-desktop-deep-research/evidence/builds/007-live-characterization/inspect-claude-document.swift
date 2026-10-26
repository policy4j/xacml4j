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


guard job["submitted_binding"] != nil else{exit(2)}
var q:[(AXUIElement,String)]=[(root,"app")];var headings:[(AXUIElement,String)]=[];var count=0
while !q.isEmpty && count<12000{
 let(e,p)=q.removeFirst();count += 1
 let text=[kAXTitleAttribute,kAXDescriptionAttribute,kAXValueAttribute].compactMap{attr(e,$0) as? String}
 if attr(e,kAXRoleAttribute) as? String == "AXHeading" || text.contains(where: {$0.lowercased().hasPrefix("claude research mode")}) {headings.append((e,p))}
 for(i,c) in (attr(e,kAXChildrenAttribute) as? [AXUIElement] ?? []).enumerated(){q.append((c,p+"/"+String(i)))}
}
var out:[[String:Any]]=[]
for(e,p) in headings{
 var a:AXUIElement?=e;var ancestry:[[String:Any]]=[]
 for _ in 0..<10{
  guard let current=a else{break}
  let cs=attr(current,kAXChildrenAttribute) as? [AXUIElement] ?? []
  ancestry.append(["role":attr(current,kAXRoleAttribute) as? String ?? "","children":cs.count,"child_roles":cs.map{attr($0,kAXRoleAttribute) as? String ?? ""}])
  guard let v=attr(current,kAXParentAttribute),CFGetTypeID(v)==AXUIElementGetTypeID() else{break};a=unsafeBitCast(v,to:AXUIElement.self)
 }
 out.append(["path":p,"ancestry":ancestry])
}
print(String(data:try JSONSerialization.data(withJSONObject:["matches":out,"nodes":count,"truncated":!q.isEmpty],options:[.sortedKeys]),encoding:.utf8)!)
