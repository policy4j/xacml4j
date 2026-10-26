import AppKit
import Foundation
import ScreenCaptureKit
import Vision
_ = NSApplication.shared
_ = CGMainDisplayID()
Task { @MainActor in
    do {
        guard let app = NSRunningApplication.runningApplications(withBundleIdentifier: "com.anthropic.claudefordesktop").first else { exit(2) }
        let content = try await SCShareableContent.excludingDesktopWindows(true, onScreenWindowsOnly: true)
        let windows = content.windows.filter { $0.owningApplication?.processID == app.processIdentifier && $0.frame.width > 500 && $0.frame.height > 400 }
        guard windows.count == 1, let window = windows.first else { print("WINDOW_AMBIGUOUS"); exit(2) }
        let config = SCStreamConfiguration(); config.width = Int(window.frame.width); config.height = Int(window.frame.height); config.showsCursor = false
        let image = try await SCScreenshotManager.captureImage(contentFilter: SCContentFilter(desktopIndependentWindow: window), configuration: config)
        let request = VNRecognizeTextRequest(); request.recognitionLevel = .accurate
        try VNImageRequestHandler(cgImage: image).perform([request])
        let allowed = ["Web search", "Search the web", "Extended thinking", "Thinking", "Research", "Search and reference chats", "Use style", "Connectors", "New chat", "Send", "Search", "Web", "Memory", "Tools", "Skills", "Capabilities", "Preferences", "Settings", "Incognito", "All tools", "Writing style"]
        var matches: [[String: Any]] = []
        for r in request.results ?? [] {
            guard let t = r.topCandidates(1).first, t.confidence > 0.65 else { continue }
            for label in allowed where t.string.range(of: "\\b" + NSRegularExpression.escapedPattern(for: label) + "\\b", options: [.caseInsensitive, .regularExpression]) != nil {
                let b = r.boundingBox
                matches.append(["label": label, "confidence": t.confidence, "x": window.frame.minX + b.minX * window.frame.width, "y": window.frame.minY + (1-b.maxY)*window.frame.height, "w": b.width*window.frame.width, "h": b.height*window.frame.height])
            }
        }
        print(String(data: try JSONSerialization.data(withJSONObject: ["coverage": "OCR_STATIC_LABELS_PARTIAL", "controls": matches], options: [.sortedKeys]), encoding: .utf8)!)
        exit(0)
    } catch { print("CAPTURE_FAILED type=\(type(of: error))"); exit(2) }
}
RunLoop.current.run()
