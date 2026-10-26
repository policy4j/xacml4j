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


let prompt = try String(contentsOf: dir.appendingPathComponent("reports/"+id+"/prompt.txt"),encoding:.utf8).trimmingCharacters(in:.whitespacesAndNewlines)
let marker=job["marker"] as! String
var q:[(AXUIElement,String)]=[(root,"app")];var out:[[String:Any]]=[];var n=0
let fixed=["researching","research complete","start research","begin research","continue","copy","edit","edit message","user","you","assistant","claude","stop","stop response","sources","view report","download","retry","send message","sign in"]
while !q.isEmpty && n<12000 {
    let(e,p)=q.removeFirst();n += 1
    let role=attr(e,kAXRoleAttribute) as? String ?? ""
    let strings=[kAXTitleAttribute,kAXDescriptionAttribute,kAXHelpAttribute,kAXValueAttribute].compactMap{attr(e,$0) as? String}
    let exact=strings.contains{$0.trimmingCharacters(in:.whitespacesAndNewlines)==prompt}
    let hasMarker=strings.contains{$0.contains(marker)}
    let known=fixed.filter{label in strings.contains{$0.trimmingCharacters(in:.whitespacesAndNewlines).lowercased()==label || $0.lowercased().hasPrefix(label+" ")}}
    if exact || hasMarker || !known.isEmpty || role=="AXTextArea" {
        var item:[String:Any]=["path":p,"role":role,"full_prompt_exact":exact,"marker_present":hasMarker,"known_labels":known,"children":(attr(e,kAXChildrenAttribute) as? [AXUIElement])?.count ?? 0]
        if exact || hasMarker {
            var ancestor:AXUIElement?=e;var metadata:[[String:Any]]=[]
            for _ in 0..<6 {
                guard let a=ancestor else{break}
                let t=[kAXTitleAttribute,kAXDescriptionAttribute,kAXHelpAttribute,kAXValueAttribute,"AXDOMIdentifier","AXIdentifier","AXDOMClassList"].compactMap{attr(a,$0) as? String}
                metadata.append(["role":attr(a,kAXRoleAttribute) as? String ?? "", "flags":["user","assistant","message","turn","research","content"].filter{w in t.contains{$0.range(of:"\\b"+w+"\\b",options:[.regularExpression,.caseInsensitive]) != nil}}])
                guard let v=attr(a,kAXParentAttribute),CFGetTypeID(v)==AXUIElementGetTypeID() else{break};ancestor=unsafeBitCast(v,to:AXUIElement.self)
            };item["ancestry"]=metadata
        }
        if role=="AXTextArea",let v=attr(e,kAXValueAttribute) as? String{item["empty"]=v.trimmingCharacters(in:.whitespacesAndNewlines).isEmpty}
        out.append(item)
    }
    for(i,c) in (attr(e,kAXChildrenAttribute) as? [AXUIElement] ?? []).enumerated(){q.append((c,p+"/"+String(i)))}
}
print(String(data:try JSONSerialization.data(withJSONObject:["nodes":n,"truncated":!q.isEmpty,"observations":out],options:[.sortedKeys]),encoding:.utf8)!)

