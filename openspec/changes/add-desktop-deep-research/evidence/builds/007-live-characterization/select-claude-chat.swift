import AppKit
import ApplicationServices
import CoreGraphics
import Darwin
import Foundation

let lockPath = FileManager.default.homeDirectoryForCurrentUser
    .appendingPathComponent("Library/Application Support/desktop-deep-research/desktop.lock").path
let descriptor = open(lockPath, O_CREAT | O_RDWR | O_NOFOLLOW, mode_t(0o600))
guard descriptor >= 0, flock(descriptor, LOCK_EX | LOCK_NB) == 0 else { print("DESKTOP_BUSY"); exit(2) }
defer { flock(descriptor, LOCK_UN); close(descriptor) }
let idle = CGEventSource.secondsSinceLastEventType(.combinedSessionState, eventType: CGEventType(rawValue: UInt32.max)!)
guard idle >= 2 else { print("USER_INPUT_ACTIVE"); exit(2) }
let id = "com.anthropic.claudefordesktop"
let candidates = NSRunningApplication.runningApplications(withBundleIdentifier: id)
guard candidates.count == 1, let app = candidates.first else {
    print("TARGET_AMBIGUOUS"); exit(2)
}
_ = NSApplication.shared
if NSWorkspace.shared.frontmostApplication?.processIdentifier != app.processIdentifier {
    print("activate_request=\(app.activate(options: [.activateAllWindows]))")
    let deadline = Date().addingTimeInterval(2)
    while NSWorkspace.shared.frontmostApplication?.processIdentifier != app.processIdentifier && Date() < deadline {
        RunLoop.current.run(until: Date().addingTimeInterval(0.05))
    }
    guard NSWorkspace.shared.frontmostApplication?.processIdentifier == app.processIdentifier else {
        print("FOREGROUND_NOT_ESTABLISHED"); exit(2)
    }
}
func attr(_ e: AXUIElement, _ key: String) -> CFTypeRef? {
    var value: CFTypeRef?
    return AXUIElementCopyAttributeValue(e, key as CFString, &value) == .success ? value : nil
}
let root = AXUIElementCreateApplication(app.processIdentifier)
let action = CommandLine.arguments.dropFirst().first ?? "select-chat"
guard ["select-chat", "new-chat", "open-tools", "select-research", "open-connectors"].contains(action) else { print("ACTION_UNSUPPORTED"); exit(2) }
var queue = [root]
if action == "new-chat" {
    guard let bar = attr(root, kAXMenuBarAttribute), CFGetTypeID(bar) == AXUIElementGetTypeID() else {
        print("MENU_UNAVAILABLE"); exit(2)
    }
    queue = [unsafeBitCast(bar, to: AXUIElement.self)]
}
var selected: [AXUIElement] = []
var nonemptyDraft = false
var count = 0
while !queue.isEmpty && count < 6000 {
    let element = queue.removeFirst(); count += 1
    let role = attr(element, kAXRoleAttribute) as? String
    if role == "AXTextArea", let value = attr(element, kAXValueAttribute) as? String,
       !value.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty { nonemptyDraft = true }
    if (action == "select-chat" && role == "AXRadioButton") ||
       (action == "new-chat" && role == "AXMenuItem") ||
       (action == "open-tools" && role == "AXPopUpButton") ||
       (["select-research", "open-connectors"].contains(action) && role == "AXMenuItem") {
        let labels = [kAXTitleAttribute, kAXDescriptionAttribute, kAXHelpAttribute, kAXValueAttribute]
            .compactMap { attr(element, $0) as? String }
            .map { $0.trimmingCharacters(in: .whitespacesAndNewlines).lowercased() }
        if (action == "select-chat" && labels.contains("claude")) ||
           (action == "new-chat" && labels.contains("new chat")) ||
           (action == "open-tools" && labels.contains(where: { $0 == "add" || $0.hasPrefix("add ") })) ||
           (action == "select-research" && labels.contains("research")) ||
           (action == "open-connectors" && labels.contains("connectors")) {
            selected.append(element)
        }
    }
    if let children = attr(element, kAXChildrenAttribute) as? [AXUIElement] { queue += children }
}
guard queue.isEmpty, !nonemptyDraft, selected.count == 1 else {
    print("TARGET_OR_DRAFT_GUARD_FAILED nodes=\(count) truncated=\(!queue.isEmpty) matches=\(selected.count) nonempty_draft=\(nonemptyDraft)"); exit(2)
}
guard NSWorkspace.shared.frontmostApplication?.processIdentifier == app.processIdentifier,
      CGEventSource.secondsSinceLastEventType(.combinedSessionState, eventType: CGEventType(rawValue: UInt32.max)!) >= 2 else {
    print("FOCUS_OR_INPUT_CHANGED"); exit(2)
}
let nativeAction = action == "open-connectors" ? kAXShowMenuAction : kAXPressAction
print("\(action)=\(AXUIElementPerformAction(selected[0], nativeAction as CFString).rawValue)")
