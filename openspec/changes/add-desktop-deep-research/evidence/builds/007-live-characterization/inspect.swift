import AppKit
import ApplicationServices
import CoreGraphics
import CryptoKit
import Foundation

let providers = ["claude": "com.anthropic.claudefordesktop", "chatgpt": "com.openai.codex"]
let provider = CommandLine.arguments.dropFirst().first ?? "claude"
guard let bundle = providers[provider] else { exit(2) }
guard let app = NSRunningApplication.runningApplications(withBundleIdentifier: bundle).only else {
    print("APP_CANDIDATE_AMBIGUOUS"); exit(2)
}
let root = AXUIElementCreateApplication(app.processIdentifier)
func attr(_ element: AXUIElement, _ key: String) -> CFTypeRef? {
    var value: CFTypeRef?
    return AXUIElementCopyAttributeValue(element, key as CFString, &value) == .success ? value : nil
}
func string(_ element: AXUIElement, _ key: String) -> String? { attr(element, key) as? String }
let labels = ["Research", "Deep research", "Web search", "Search web", "Use web search", "Enable web search", "New chat", "New conversation", "New session", "New window", "Chat", "Chats", "Code", "Cowork", "Claude", "Send", "Add", "Search", "Start research", "Begin research", "Create new chat", "Search and tools", "Add files or connectors", "Tools", "Connectors", "Sources", "All sources", "Public web", "Search the web", "Copy", "Download", "Export", "File", "No connectors", "No connected sources", "Thinking", "Extended thinking", "Search and reference chats", "Use style"]
var queue: [(AXUIElement, String)] = [(root, "app")]
if let bar = attr(root, kAXMenuBarAttribute), CFGetTypeID(bar) == AXUIElementGetTypeID() {
    queue.append((unsafeBitCast(bar, to: AXUIElement.self), "menubar"))
}
var count = 0
var output: [[String: Any]] = []
while !queue.isEmpty && count < 6000 {
    let (element, path) = queue.removeFirst()
    count += 1
    let role = string(element, kAXRoleAttribute) ?? "unknown"
    let text = [kAXTitleAttribute, kAXDescriptionAttribute, kAXHelpAttribute, kAXValueAttribute].compactMap { string(element, $0) }
    var exact = Set<String>()
    var prefix = Set<String>()
    for value in text {
        let normalized = value.trimmingCharacters(in: .whitespacesAndNewlines).lowercased()
        for label in labels {
            if normalized == label.lowercased() { exact.insert(label) }
            if ["AXRadioButton", "AXMenuItem", "AXButton", "AXPopUpButton"].contains(role),
               normalized.hasPrefix(label.lowercased() + " ") || normalized.hasPrefix(label.lowercased() + "…") || normalized.hasPrefix(label.lowercased() + ",") {
                prefix.insert(label)
            }
        }
    }
    if !exact.isEmpty || !prefix.isEmpty || ["AXRadioButton", "AXTextArea", "AXCheckBox", "AXPopUpButton"].contains(role) ||
       role == "AXMenuItem" {
        var point = CGPoint.zero
        var size = CGSize.zero
        if let value = attr(element, kAXPositionAttribute), CFGetTypeID(value) == AXValueGetTypeID() {
            AXValueGetValue(unsafeBitCast(value, to: AXValue.self), .cgPoint, &point)
        }
        if let value = attr(element, kAXSizeAttribute), CFGetTypeID(value) == AXValueGetTypeID() {
            AXValueGetValue(unsafeBitCast(value, to: AXValue.self), .cgSize, &size)
        }
        var item: [String: Any] = ["path": path, "role": role, "exact_labels": exact.sorted(), "prefix_labels": prefix.sorted(), "x": point.x, "y": point.y, "w": size.width, "h": size.height]
        if role == "AXPopUpButton" { item["public_control_flags"] = ["copy", "export", "download", "more", "menu", "version", "document", "fullscreen", "open", "report"].filter { word in text.contains { $0.range(of:"\\b"+word+"\\b",options:[.regularExpression,.caseInsensitive]) != nil } } }
        if role == "AXMenuItem" && size.height > 0 {
            var childText = text
            var descendants = attr(element, kAXChildrenAttribute) as? [AXUIElement] ?? []
            for _ in 0..<3 {
                var next: [AXUIElement] = []
                for child in descendants {
                    childText += [kAXTitleAttribute, kAXDescriptionAttribute, kAXHelpAttribute, kAXValueAttribute].compactMap { string(child, $0) }
                    next += attr(child, kAXChildrenAttribute) as? [AXUIElement] ?? []
                }
                descendants = next
            }
            item["descendant_public_labels"] = labels.filter { label in childText.contains { $0.trimmingCharacters(in: .whitespacesAndNewlines).lowercased() == label.lowercased() } }
            item["label_hash"] = SHA256.hash(data: Data(childText.joined(separator: "\u{001f}").utf8)).map { String(format: "%02x", $0) }.joined()
            item["enabled"] = (attr(element, kAXEnabledAttribute) as? NSNumber)?.boolValue ?? false
            item["descendant_status_flags"] = ["connect", "connected", "disabled", "disconnected", "unavailable", "not connected", "web search", "search", "tools", "memory"].filter { label in childText.contains { $0.range(of: "\\b" + NSRegularExpression.escapedPattern(for: label) + "\\b", options: [.regularExpression, .caseInsensitive]) != nil } }
            let publicWords = ["web", "search", "internet", "research", "connectors", "none", "connected", "disabled", "manage", "customize", "memory", "tools", "skills"]
            item["public_word_flags"] = publicWords.filter { word in
                text.contains { $0.range(of: "\\b" + word + "\\b", options: [.regularExpression, .caseInsensitive]) != nil }
            }
            item["children_count"] = (attr(element, kAXChildrenAttribute) as? [AXUIElement])?.count ?? 0
            var actions: CFArray?
            if AXUIElementCopyActionNames(element, &actions) == .success {
                item["actions"] = actions as? [String] ?? []
            }
        }
        if let value = attr(element, kAXValueAttribute) as? NSNumber { item["numeric_value"] = value }
        if role == "AXTextArea" {
            if let value = string(element, kAXValueAttribute) {
                item["value_length"] = value.count
                item["empty_after_whitespace_trim"] = value.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty
                item["value_hash"] = SHA256.hash(data: Data(value.utf8)).map { String(format: "%02x", $0) }.joined()
            } else { item["value_observable"] = false }
        }
        if role != "AXMenuItem" || size.height > 0 || !exact.isEmpty || !prefix.isEmpty {
            output.append(item)
        }
    }
    if let children = attr(element, kAXChildrenAttribute) as? [AXUIElement] {
        for (index, child) in children.enumerated() { queue.append((child, path + "/" + String(index))) }
    }
}
let result: [String: Any] = ["provider": provider, "accessibility_trusted": AXIsProcessTrusted(), "any_input_idle_seconds": CGEventSource.secondsSinceLastEventType(.combinedSessionState, eventType: CGEventType(rawValue: UInt32.max)!), "nodes": count, "truncated": !queue.isEmpty, "allowlisted_controls": output]
print(String(data: try JSONSerialization.data(withJSONObject: result, options: [.sortedKeys]), encoding: .utf8)!)

extension Array {
    var only: Element? { count == 1 ? first : nil }
}
