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


guard job["provider_state"] as? String=="COMPLETED",job["submitted_binding"] != nil else{print("PROVIDER_NOT_COMPLETE");exit(2)}
let all=scan();let marker=job["marker"] as! String
let users=all.filter{e in attr(e,kAXRoleAttribute) as? String=="AXHeading" && [kAXTitleAttribute,kAXDescriptionAttribute,kAXValueAttribute].compactMap{attr(e,$0) as? String}.contains{$0.lowercased().hasPrefix("you") && $0.contains(marker)}}
let done=all.filter{attr($0,kAXRoleAttribute) as? String=="AXStaticText" && (attr($0,kAXValueAttribute) as? String ?? attr($0,kAXTitleAttribute) as? String ?? "").lowercased()=="research complete"}
guard users.count==1,done.count==1 else{print("COMPLETION_BINDING_CHANGED");exit(2)}
let documentPath="app/0/0/0/0/0/0/1/0/0/0/0/0/0/1/0/0/0/0/1/0/4/0/0/2/0/0"
var document=root
for part in documentPath.split(separator:"/").dropFirst(){
 guard let i=Int(part),let cs=attr(document,kAXChildrenAttribute) as? [AXUIElement],i<cs.count else{print("DOCUMENT_PATH_CHANGED");exit(2)}
 document=cs[i]
}
let blocks=attr(document,kAXChildrenAttribute) as? [AXUIElement] ?? []
guard blocks.count>=30,blocks.contains(where:{attr($0,kAXRoleAttribute) as? String=="AXTable"}) else{print("DOCUMENT_STRUCTURE_CHANGED");exit(2)}
var q=[document];var texts:[String]=[];var urls:[String]=[];var n=0;var headingCount=0
while !q.isEmpty && n<12000{
 let e=q.removeFirst();n += 1;let r=attr(e,kAXRoleAttribute) as? String ?? ""
 if r=="AXHeading"{headingCount += 1}
 if r=="AXStaticText",let s=attr(e,kAXValueAttribute) as? String ?? attr(e,kAXTitleAttribute) as? String,!s.isEmpty{texts.append(s)}
 if r=="AXLink",let v=attr(e,kAXURLAttribute){
  if let u=v as? URL{urls.append(u.absoluteString)}else if let s=v as? String{urls.append(s)}
 }
 q.insert(contentsOf:attr(e,kAXChildrenAttribute) as? [AXUIElement] ?? [],at:0)
}
guard q.isEmpty else{print("DOCUMENT_TRUNCATED");exit(2)}
let report=texts.joined(separator:"\n")+"\n"
let unique=Array(Set(urls)).sorted()
let allowed=Set(["support.claude.com","support.anthropic.com","claude.com","www.claude.com","anthropic.com","www.anthropic.com","docs.anthropic.com","docs.claude.com","platform.claude.com","claude.ai","academy.claude.com","privacy.claude.com"])
let invalid=unique.filter{s in guard let u=URL(string:s),u.scheme=="https",let host=u.host else{return true};return !allowed.contains(host)}
let publicHosts = invalid.compactMap { URL(string:$0)?.host }.filter { $0.range(of:"^[a-zA-Z0-9.-]+\\.(com|org|io|gov|edu|ai|net)$",options:.regularExpression) != nil }
print("additional_public_link_hosts=\(Array(Set(publicHosts)).sorted())")
print("native_document_characters=\(report.count) headings=\(headingCount) blocks=\(blocks.count) native_link_targets=\(unique.count) nonofficial_targets=\(invalid.count) marker_in_document=\(report.contains(marker))")
guard report.count>5000,headingCount>=7,unique.count>=20,invalid.isEmpty else{print("DOCUMENT_OR_SOURCES_NOT_VERIFIED");exit(2)}
let runURL=dir.appendingPathComponent("reports/"+id)
func writePrivate(_ data:Data,_ name:String) throws{
 let url=runURL.appendingPathComponent(name);let tmp=url.appendingPathExtension("pending")
 let fd=open(tmp.path,O_CREAT|O_EXCL|O_WRONLY|O_NOFOLLOW,mode_t(0o600));guard fd>=0 else{exit(2)}
 let h=FileHandle(fileDescriptor:fd,closeOnDealloc:false);try h.write(contentsOf:data);try h.synchronize();try h.close()
 guard rename(tmp.path,url.path)==0 else{exit(2)};let d=open(runURL.path,O_RDONLY);fsync(d);close(d)
}
job["collection_state"]="IN_PROGRESS";try save()
let sourceData=try JSONSerialization.data(withJSONObject:unique.map{["url":$0,"provenance":"native AXLink AXURL in bound Research document"]},options:[.prettyPrinted,.sortedKeys])
try writePrivate(Data(report.utf8),"report.txt");try writePrivate(sourceData,"sources.json")
let manifest:[String:Any]=["job_id":id,"marker":marker,"provider":"claude","provider_state":"COMPLETED","collection_method":"native-accessibility-document-text-and-link-targets","format":"text/plain","report_bytes":Data(report.utf8).count,"report_sha256":SHA256.hash(data:Data(report.utf8)).map{String(format:"%02x",$0)}.joined(),"sources_sha256":SHA256.hash(data:sourceData).map{String(format:"%02x",$0)}.joined(),"native_source_count":unique.count,"heading_count":headingCount,"block_count":blocks.count,"document_marker_present":report.contains(marker),"coverage":"PARTIAL_PENDING_INDEPENDENT_NATIVE_EXPORT_COMPLETENESS_CHECK","timestamp":Date().timeIntervalSince1970]
try writePrivate(try JSONSerialization.data(withJSONObject:manifest,options:[.prettyPrinted,.sortedKeys]),"collection-observation.json")
job["collection_candidate"]=manifest;try save()
print("native_report_candidate_saved=true collection_state=IN_PROGRESS")
