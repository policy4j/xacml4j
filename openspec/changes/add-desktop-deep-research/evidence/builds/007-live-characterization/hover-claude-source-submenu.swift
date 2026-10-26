import AppKit
import ApplicationServices
import CoreGraphics
import CryptoKit
import Darwin
import Foundation
import ScreenCaptureKit
import Vision

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
let idleDeadline = Date().addingTimeInterval(10)
while idle() < 2 && Date() < idleDeadline { RunLoop.current.run(until: Date().addingTimeInterval(0.1)) }
guard let app = NSRunningApplication.runningApplications(withBundleIdentifier: "com.anthropic.claudefordesktop").first, idle() >= 2 else { print("INPUT_GUARD idle=\(idle())"); exit(2) }
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


let row = CommandLine.arguments.dropFirst().first ?? "10"
guard ["10","11"].contains(row) else { exit(2) }
let prefix = "app/0/0/0/0/0/0/1/0/0/0/0/0/0/1/0/0/1/0/3/0/2/0/0/0/1/0/1/0/0/0/6/0/0/0/3/"
var e = root
for component in (prefix+row).split(separator:"/").dropFirst() {
    guard let i=Int(component), let cs=attr(e,kAXChildrenAttribute) as? [AXUIElement], i<cs.count else { print("SOURCE_PATH_CHANGED"); exit(2) }; e=cs[i]
}
guard attr(e,kAXRoleAttribute) as? String == "AXMenuItem", (attr(e,"AXHasPopup") as? NSNumber)?.boolValue == true else { exit(2) }
var p=CGPoint.zero; var s=CGSize.zero
guard let pv=attr(e,kAXPositionAttribute), let sv=attr(e,kAXSizeAttribute) else { exit(2) }
AXValueGetValue(unsafeBitCast(pv,to:AXValue.self),.cgPoint,&p); AXValueGetValue(unsafeBitCast(sv,to:AXValue.self),.cgSize,&s)
guard s.height>20 else { print("SOURCE_NOT_VISIBLE"); exit(2) }
let rect=CGRect(origin:p,size:s); let center=CGPoint(x:p.x+s.width*0.25,y:p.y+s.height*0.5)
var targetNames=labels(e)
for c in attr(e,kAXChildrenAttribute) as? [AXUIElement] ?? [] {
    targetNames += [kAXTitleAttribute,kAXDescriptionAttribute,kAXValueAttribute].compactMap { attr(c,$0) as? String }.map{$0.lowercased().trimmingCharacters(in:.whitespacesAndNewlines)}
}
targetNames=targetNames.filter{$0.count>=4}
guardFocus()
var hit:AXUIElement?; var hp:pid_t=0
guard AXUIElementCopyElementAtPosition(AXUIElementCreateSystemWide(),Float(center.x),Float(center.y),&hit) == .success, let hit=hit,
      AXUIElementGetPid(hit,&hp) == .success,hp==app.processIdentifier,
      let tw=attr(e,kAXWindowAttribute),let hw=attr(hit,kAXWindowAttribute),CFEqual(tw,hw) else { print("TARGET_OCCLUDED"); exit(2) }
_ = CGMainDisplayID()
Task { @MainActor in
    do {
        let content=try await SCShareableContent.excludingDesktopWindows(true,onScreenWindowsOnly:true)
        let ws=content.windows.filter{$0.owningApplication?.processID==app.processIdentifier && $0.frame.width>500 && $0.frame.height>400}
        guard ws.count==1,let w=ws.first else { exit(2) }
        let config=SCStreamConfiguration();config.width=Int(w.frame.width);config.height=Int(w.frame.height);config.showsCursor=false
        let image=try await SCScreenshotManager.captureImage(contentFilter:SCContentFilter(desktopIndependentWindow:w),configuration:config)
        let request=VNRecognizeTextRequest();request.recognitionLevel = .accurate
        try VNImageRequestHandler(cgImage:image).perform([request])
        let matches=(request.results ?? []).filter{r in
            guard let t=r.topCandidates(1).first,t.confidence>0.8,targetNames.contains(where:{t.string.lowercased().contains($0)}) else{return false}
            let b=r.boundingBox;return rect.contains(CGPoint(x:w.frame.minX+b.midX*w.frame.width,y:w.frame.minY+(1-b.midY)*w.frame.height))
        }
        guard matches.count==1 else{print("SOURCE_VISUAL_UNVERIFIED matches=\(matches.count)");exit(2)}
        guardFocus()
        guard let event=CGEvent(mouseEventSource:nil,mouseType:.mouseMoved,mouseCursorPosition:center,mouseButton:.left) else {exit(2)}
        event.setIntegerValueField(.eventSourceUserData,value:0x445243);event.post(tap:.cghidEventTap)
        try await Task.sleep(nanoseconds:400_000_000)
        print("connector_submenu_hovered=\(row) coverage=AX_WINDOW_AND_FRESH_OCR_PARTIAL")
        exit(0)
    } catch {print("CAPTURE_FAILED");exit(2)}
}
RunLoop.current.run()
