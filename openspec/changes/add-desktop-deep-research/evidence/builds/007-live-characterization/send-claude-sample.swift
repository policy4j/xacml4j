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
let idleDeadline=Date().addingTimeInterval(10)
while idle()<2 && Date()<idleDeadline{RunLoop.current.run(until:Date().addingTimeInterval(0.1))}
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
let prompt = try String(contentsOf: dir.appendingPathComponent("reports/" + id + "/prompt.txt"), encoding: .utf8)
let draft = all.filter { attr($0,kAXRoleAttribute) as? String == "AXTextArea" }
guard draft.count == 1,let value=attr(draft[0],kAXValueAttribute) as? String,
      value+"\n" == prompt,
      SHA256.hash(data:Data(value.utf8)).map({String(format:"%02x",$0)}).joined() == job["owned_composer_hash"] as? String else {print("OWNED_DRAFT_CHANGED");exit(2)}
let originals=job["connector_original_states"] as! [[String:Any]]
guard originals.count==12 else{exit(2)}
var off=0;var attachments=0
for original in originals {
    let matches=all.filter{e in
        attr(e,kAXRoleAttribute) as? String == "AXMenuItem" &&
        SHA256.hash(data:Data((attr(e,kAXTitleAttribute) as? String ?? "").utf8)).map({String(format:"%02x",$0)}).joined() == original["label_hash"] as? String
    }
    guard matches.count==1 else{print("SOURCE_IDENTITY_AMBIGUOUS");exit(2)}
    if original["original_value"] as? Int == -1 {
        guard (attr(matches[0],"AXHasPopup") as? NSNumber)?.boolValue == true,
              (attr(matches[0],"AXSelected") as? NSNumber)?.boolValue == false,
              attr(matches[0],kAXValueAttribute) as? NSNumber == nil else{print("ATTACHMENT_MENU_CHANGED");exit(2)}
        attachments += 1
    } else {
        guard (attr(matches[0],kAXValueAttribute) as? NSNumber)?.intValue==0 else{print("PRIVATE_CONNECTOR_ENABLED");exit(2)}
        off += 1
    }
}
let memory=all.filter{attr($0,kAXRoleAttribute) as? String == "AXMenuItem" && labels($0).contains{$0.range(of:"\\bmemory\\b",options:.regularExpression) != nil}}
let research=all.filter{attr($0,kAXRoleAttribute) as? String == "AXMenuItem" && labels($0).contains("research")}
guard off==10,attachments==2,memory.count==1,(attr(memory[0],kAXValueAttribute) as? NSNumber)?.intValue==0,
      research.count==1,(attr(research[0],kAXValueAttribute) as? NSNumber)?.intValue==1 else{print("SOURCE_OR_MODE_CHANGED");exit(2)}
job["source_scope_before_send"]=["toggles_observed_off":off,"unselected_attachment_menus":attachments,"memory_off":true,"research_selected":true,"timestamp":Date().timeIntervalSince1970,"coverage":"AX_CONTROLS_AND_DOCUMENTED_LOCAL_MCP_RESEARCH_EXCLUSION","documentation":["https://support.claude.com/en/articles/11175166-get-started-with-custom-connectors-using-remote-mcp","https://modelcontextprotocol.io/docs/2026-07-28/develop/connect-remote-servers"]]
try save()
let add=all.filter{attr($0,kAXRoleAttribute) as? String=="AXPopUpButton" && labels($0).contains{$0 == "add" || $0.hasPrefix("add ")}}
guard add.count==1 else{exit(2)};guardFocus()
guard AXUIElementPerformAction(add[0],kAXPressAction as CFString) == .success else{exit(2)}
RunLoop.current.run(until:Date().addingTimeInterval(0.3))
let fresh=scan()
let send=fresh.filter{attr($0,kAXRoleAttribute) as? String=="AXButton" && labels($0).contains{$0 == "send" || $0.hasPrefix("send ")}}
guard send.count==1,(attr(send[0],kAXEnabledAttribute) as? NSNumber)?.boolValue==true,
      attr(draft[0],kAXValueAttribute) as? String==value else{print("SEND_NOT_VERIFIED");exit(2)}
guardFocus()
job["provider_state"]="SUBMISSION_UNCERTAIN";job["prompt_possibly_sent"]=true;job["attempts"]=1
job["send_intent"]=["timestamp":Date().timeIntervalSince1970,"action":"AXPress","full_owned_prompt_verified":true]
try save()
let code=AXUIElementPerformAction(send[0],kAXPressAction as CFString)
job["send_action_return"]=code.rawValue;job["send_action_timestamp"]=Date().timeIntervalSince1970
try save()
print("send_action_return=\(code.rawValue) prompt_possibly_sent=true attempts=1 state=SUBMISSION_UNCERTAIN")

