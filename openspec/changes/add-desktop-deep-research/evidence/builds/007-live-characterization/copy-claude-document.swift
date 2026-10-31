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


guard job["provider_state"] as? String=="COMPLETED",job["submitted_binding"] != nil else{exit(2)}
let deadline=Date().addingTimeInterval(10)
while idle()<2 && Date()<deadline{RunLoop.current.run(until:Date().addingTimeInterval(0.1))}
guard idle()>=2 else{exit(2)}
_ = NSApplication.shared
if NSWorkspace.shared.frontmostApplication?.processIdentifier != app.processIdentifier{
 guard app.activate(options:[.activateAllWindows]) else{exit(2)};let d=Date().addingTimeInterval(2)
 while NSWorkspace.shared.frontmostApplication?.processIdentifier != app.processIdentifier && Date()<d{RunLoop.current.run(until:Date().addingTimeInterval(0.05))}
}
var e=root
for p in "app/0/0/0/0/0/0/1/0/0/0/0/0/0/1/0/0/0/0/1/0/4/0/0/1/0/0/0".split(separator:"/").dropFirst(){
 guard let i=Int(p),let cs=attr(e,kAXChildrenAttribute) as? [AXUIElement],i<cs.count else{print("DOCUMENT_MENU_CHANGED");exit(2)};e=cs[i]
}
let cs=(attr(e,kAXChildrenAttribute) as? [AXUIElement] ?? []).filter{
 attr($0,kAXRoleAttribute) as? String=="AXMenuItem" && labels($0).contains("copy")
}
guard cs.count==1 else{print("REPORT_COPY_UNOBSERVABLE matches=\(cs.count)");exit(2)}
let all=scan();let marker=job["marker"] as! String
guard all.filter({e in attr(e,kAXRoleAttribute) as? String=="AXHeading" && [kAXTitleAttribute,kAXDescriptionAttribute,kAXValueAttribute].compactMap{attr(e,$0) as? String}.contains{$0.lowercased().hasPrefix("you") && $0.contains(marker)}}).count==1 else{exit(2)}
let pb=NSPasteboard.general
let before=pb.changeCount
var snapshot:[[NSPasteboard.PasteboardType:Data]]=[]
for item in pb.pasteboardItems ?? []{
 var data:[NSPasteboard.PasteboardType:Data]=[:]
 for type in item.types{guard let d=item.data(forType:type) else{print("CLIPBOARD_SNAPSHOT_INCOMPLETE");exit(2)};data[type]=d}
 snapshot.append(data)
}
guard pb.changeCount==before else{print("CLIPBOARD_CHANGED_BEFORE_ACTION");exit(2)}
guardFocus()
job["collection_state"]="IN_PROGRESS";job["copy_intent"]=["method":"bound-document-Copy","clipboard_change_count_before":before,"timestamp":Date().timeIntervalSince1970];try save()
let action=AXUIElementPerformAction(cs[0],kAXPressAction as CFString)
let end=Date().addingTimeInterval(3)
while pb.changeCount==before && Date()<end{RunLoop.current.run(until:Date().addingTimeInterval(0.05))}
let observed=pb.changeCount
guard action == .success,observed>before,let markdown=pb.string(forType:.string),
      markdown.count>15000,markdown.contains(marker) else{print("COPY_OUTPUT_UNVERIFIED action=\(action.rawValue) clipboard_changed=\(observed>before)");exit(2)}
guard pb.changeCount==observed else{print("FOREIGN_CLIPBOARD_WRITE");exit(2)}
let output=dir.appendingPathComponent("reports/"+id+"/native-copy.txt")
let fd=open(output.path,O_CREAT|O_EXCL|O_WRONLY|O_NOFOLLOW,mode_t(0o600));guard fd>=0 else{print("REPORT_OUTPUT_EXISTS");exit(2)}
let h=FileHandle(fileDescriptor:fd,closeOnDealloc:false);try h.write(contentsOf:Data(markdown.utf8));try h.synchronize();try h.close()
var restored=false
if pb.changeCount==observed{
 pb.clearContents()
 let items=snapshot.map{values -> NSPasteboardItem in let item=NSPasteboardItem();for(type,data) in values{item.setData(data,forType:type)};return item}
 restored=items.isEmpty || pb.writeObjects(items)
}
job["copy_observation"]=["action_return":action.rawValue,"clipboard_change_delta":observed-before,"clipboard_restored":restored,"markdown_characters":markdown.count,"markdown_sha256":SHA256.hash(data:Data(markdown.utf8)).map{String(format:"%02x",$0)}.joined(),"timestamp":Date().timeIntervalSince1970];try save()
print("native_copy_saved=true characters=\(markdown.count) clipboard_restored=\(restored) independent_validation_pending=true")
