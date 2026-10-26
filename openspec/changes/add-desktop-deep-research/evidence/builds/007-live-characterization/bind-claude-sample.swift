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


let all=scan()
let marker=job["marker"] as! String
let userHeadings=all.filter{e in
    attr(e,kAXRoleAttribute) as? String == "AXHeading" &&
    [kAXTitleAttribute,kAXDescriptionAttribute,kAXValueAttribute].compactMap{attr(e,$0) as? String}.contains{$0.lowercased().hasPrefix("you") && $0.contains(marker)}
}
guard userHeadings.count==1 else{print("SUBMITTED_USER_TURN_UNOBSERVABLE matches=\(userHeadings.count)");exit(2)}
guard let rawParent=attr(userHeadings[0],kAXParentAttribute),CFGetTypeID(rawParent)==AXUIElementGetTypeID() else{exit(2)}
let userParent=unsafeBitCast(rawParent,to:AXUIElement.self)
var q=[userParent];var markerTextNodes=0
while !q.isEmpty{
    let e=q.removeFirst()
    if attr(e,kAXRoleAttribute) as? String=="AXStaticText",
       (attr(e,kAXValueAttribute) as? String ?? attr(e,kAXTitleAttribute) as? String ?? "").contains(marker){markerTextNodes += 1}
    q += attr(e,kAXChildrenAttribute) as? [AXUIElement] ?? []
}
guard markerTextNodes>=2 else{print("USER_BODY_MARKERS_UNOBSERVABLE");exit(2)}
job["submitted_binding"]=["class":"signed-session-window-and-exact-marker-in-USER-turn","pid":app.processIdentifier,"process_start":app.launchDate?.timeIntervalSince1970 ?? 0,"user_heading_unique":true,"body_marker_nodes":markerTextNodes,"timestamp":Date().timeIntervalSince1970]
job["provider_state"]="RUNNING";try save()
var buttons:[[String:Any]]=[]
let words=["research","start","stop","response","continue","report","complete","finished","sources","search","plan","thinking","researching","quota","limit","retry","cancel","download","export"]
for e in all where ["AXButton","AXHeading","AXStaticText"].contains(attr(e,kAXRoleAttribute) as? String ?? ""){
    let t=labels(e)+[attr(e,kAXValueAttribute) as? String ?? ""].map{$0.lowercased()}
    let flags=words.filter{w in t.contains{$0.range(of:"\\b"+w+"\\b",options:.regularExpression) != nil}}
    if !flags.isEmpty{buttons.append(["role":attr(e,kAXRoleAttribute) as? String ?? "", "flags":flags])}
}
print(String(data:try JSONSerialization.data(withJSONObject:["submitted_user_turn_verified":true,"marker_body_nodes":markerTextNodes,"state":"RUNNING","public_status_words":buttons],options:[.sortedKeys]),encoding:.utf8)!)

