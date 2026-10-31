import AppKit
import ApplicationServices
import CoreGraphics
import Darwin
import Foundation
import ScreenCaptureKit
import Vision

let lockPath = FileManager.default.homeDirectoryForCurrentUser.appendingPathComponent("Library/Application Support/desktop-deep-research/desktop.lock").path
let fd = open(lockPath, O_CREAT | O_RDWR | O_NOFOLLOW, mode_t(0o600))
guard fd >= 0, flock(fd, LOCK_EX | LOCK_NB) == 0 else { print("DESKTOP_BUSY"); exit(2) }
defer { flock(fd, LOCK_UN); close(fd) }
func idle() -> Double { CGEventSource.secondsSinceLastEventType(.combinedSessionState, eventType: CGEventType(rawValue: UInt32.max)!) }
guard idle() >= 2 else { print("USER_INPUT_ACTIVE"); exit(2) }
guard let app = NSRunningApplication.runningApplications(withBundleIdentifier: "com.anthropic.claudefordesktop").first else { exit(2) }
_ = NSApplication.shared
if NSWorkspace.shared.frontmostApplication?.processIdentifier != app.processIdentifier {
    guard app.activate(options: [.activateAllWindows]) else { exit(2) }
    let deadline = Date().addingTimeInterval(2)
    while NSWorkspace.shared.frontmostApplication?.processIdentifier != app.processIdentifier && Date() < deadline { RunLoop.current.run(until: Date().addingTimeInterval(0.05)) }
}
func attr(_ e: AXUIElement, _ key: String) -> CFTypeRef? {
    var value: CFTypeRef?
    return AXUIElementCopyAttributeValue(e, key as CFString, &value) == .success ? value : nil
}
func labels(_ e: AXUIElement) -> [String] {
    [kAXTitleAttribute, kAXDescriptionAttribute, kAXHelpAttribute, kAXValueAttribute].compactMap { attr(e, $0) as? String }.map { $0.trimmingCharacters(in: .whitespacesAndNewlines).lowercased() }
}
let root = AXUIElementCreateApplication(app.processIdentifier)
var queue = [root]; var controls: [AXUIElement] = []; var count = 0
while !queue.isEmpty && count < 6000 {
    let e = queue.removeFirst(); count += 1
    if attr(e, kAXRoleAttribute) as? String == "AXMenuItem", labels(e).contains("connectors") { controls.append(e) }
    if let children = attr(e, kAXChildrenAttribute) as? [AXUIElement] { queue += children }
}
guard queue.isEmpty, controls.count == 1 else { print("CONNECTORS_UNOBSERVABLE"); exit(2) }
let target = controls[0]
var point = CGPoint.zero; var size = CGSize.zero
guard let p = attr(target, kAXPositionAttribute), CFGetTypeID(p) == AXValueGetTypeID(),
      let s = attr(target, kAXSizeAttribute), CFGetTypeID(s) == AXValueGetTypeID() else { exit(2) }
AXValueGetValue(unsafeBitCast(p, to: AXValue.self), .cgPoint, &point)
AXValueGetValue(unsafeBitCast(s, to: AXValue.self), .cgSize, &size)
let center = CGPoint(x: point.x + size.width * 0.2, y: point.y + size.height * 0.5)
var hit: AXUIElement?
guard NSWorkspace.shared.frontmostApplication?.processIdentifier == app.processIdentifier, idle() >= 2,
      AXUIElementCopyElementAtPosition(AXUIElementCreateSystemWide(), Float(center.x), Float(center.y), &hit) == .success,
      let hit = hit else { print("HIT_TEST_UNAVAILABLE"); exit(2) }
var ancestor: AXUIElement? = hit; var matches = false
var roles: [String] = []
for _ in 0..<8 {
    guard let element = ancestor else { break }
    roles.append(attr(element, kAXRoleAttribute) as? String ?? "unknown")
    if CFEqual(element, target) { matches = true; break }
    var pid: pid_t = 0
    if AXUIElementGetPid(element, &pid) == .success, pid == app.processIdentifier,
       attr(element, kAXRoleAttribute) as? String == "AXMenuItem", labels(element).contains("connectors") {
        var observedPoint = CGPoint.zero; var observedSize = CGSize.zero
        if let p = attr(element, kAXPositionAttribute), let s = attr(element, kAXSizeAttribute),
           CFGetTypeID(p) == AXValueGetTypeID(), CFGetTypeID(s) == AXValueGetTypeID() {
            AXValueGetValue(unsafeBitCast(p, to: AXValue.self), .cgPoint, &observedPoint)
            AXValueGetValue(unsafeBitCast(s, to: AXValue.self), .cgSize, &observedSize)
            if abs(observedPoint.x - point.x) < 1 && abs(observedPoint.y - point.y) < 1 &&
               abs(observedSize.width - size.width) < 1 && abs(observedSize.height - size.height) < 1 { matches = true; break }
        }
    }
    guard let parent = attr(element, kAXParentAttribute), CFGetTypeID(parent) == AXUIElementGetTypeID() else { break }
    ancestor = unsafeBitCast(parent, to: AXUIElement.self)
}
if matches {
    guard let event = CGEvent(mouseEventSource: nil, mouseType: .mouseMoved, mouseCursorPosition: center, mouseButton: .left) else { exit(2) }
    event.setIntegerValueField(.eventSourceUserData, value: 0x445243)
    event.post(tap: .cghidEventTap)
    RunLoop.current.run(until: Date().addingTimeInterval(0.4))
    print("connectors_hover_posted=true target_coverage=AX_CONTROL")
} else {
    var hitPID: pid_t = 0
    guard AXUIElementGetPid(hit, &hitPID) == .success, hitPID == app.processIdentifier,
          let targetWindow = attr(target, kAXWindowAttribute),
          let hitWindow = attr(hit, kAXWindowAttribute), CFEqual(targetWindow, hitWindow) else {
        print("TARGET_OCCLUDED hit_roles=\(roles)"); exit(2)
    }
    _ = CGMainDisplayID()
    Task { @MainActor in
        do {
            let content = try await SCShareableContent.excludingDesktopWindows(true, onScreenWindowsOnly: true)
            guard let window = content.windows.filter({ $0.owningApplication?.processID == app.processIdentifier && $0.frame.width > 500 && $0.frame.height > 400 }).first else { exit(2) }
            let configuration = SCStreamConfiguration()
            configuration.width = Int(window.frame.width); configuration.height = Int(window.frame.height); configuration.showsCursor = false
            let image = try await SCScreenshotManager.captureImage(contentFilter: SCContentFilter(desktopIndependentWindow: window), configuration: configuration)
            let request = VNRecognizeTextRequest(); request.recognitionLevel = .accurate
            try VNImageRequestHandler(cgImage: image).perform([request])
            let targetRect = CGRect(origin: point, size: size)
            let matching = (request.results ?? []).filter { result in
                guard let text = result.topCandidates(1).first, text.confidence > 0.8,
                      text.string.range(of: "\\bconnectors\\b", options: [.regularExpression, .caseInsensitive]) != nil else { return false }
                let b = result.boundingBox
                let frame = CGRect(x: window.frame.minX + b.minX * window.frame.width,
                                   y: window.frame.minY + (1 - b.maxY) * window.frame.height,
                                   width: b.width * window.frame.width, height: b.height * window.frame.height)
                return targetRect.contains(CGPoint(x: frame.midX, y: frame.midY))
            }
            guard matching.count == 1, idle() >= 2,
                  NSWorkspace.shared.frontmostApplication?.processIdentifier == app.processIdentifier else {
                print("VISUAL_TARGET_NOT_CORROBORATED matches=\(matching.count) idle=\(idle()) foreground_matches=\(NSWorkspace.shared.frontmostApplication?.processIdentifier == app.processIdentifier)"); exit(2)
            }
            guard let event = CGEvent(mouseEventSource: nil, mouseType: .mouseMoved, mouseCursorPosition: center, mouseButton: .left) else { exit(2) }
            event.setIntegerValueField(.eventSourceUserData, value: 0x445243)
            event.post(tap: .cghidEventTap)
            try await Task.sleep(nanoseconds: 400_000_000)
            print("connectors_hover_posted=true target_coverage=AX_WINDOW_AND_FRESH_OCR_PARTIAL")
            exit(0)
        } catch { print("VISUAL_CORROBORATION_FAILED type=\(type(of: error))"); exit(2) }
    }
    RunLoop.current.run()
}
