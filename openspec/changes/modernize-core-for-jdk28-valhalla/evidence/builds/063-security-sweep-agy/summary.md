# Run 063: security sweep of XML/JSON input paths (Agy)

Date: 2026-10-02. Lane: `policy4j_code_review_agy` (NTM persona), Antigravity;
pane shows Gemini 3.8 Flash (High) (self-label unreliable). Session
`policy4j--g1`, pane 3, 86 tool calls. Read-only. Prompted by rv064 (XXE
regression in BaseJAXBUnmarshaller, excluded here; fix in progress). Raw
report: [agy-report.txt](agy-report.txt).

SAFE (reviewer): DOMUtil document builder and transformer factories,
XmlContent, Xacml20XPathTo30Transformer, PolicyImportTool, the XML 2.0/3.0
request mappers, JSON unmarshalling (fixed type adapters, no reflection or
class loading, Gson depth limits, Long/Double parsing, base64 Content parsed
through DOMUtil's hardened parser), PortRange.readExternal (not reachable
from untrusted input).

UNSAFE (pre-existing, not regressions of this change):
1. HIGH, `MultipleResourcesViaXPathExpressionHandler.java:92-105, 131-160`:
   `Sets.cartesianProduct` over request-supplied content-selector XPath
   matches with no bound (conductor-confirmed at `:92`): `//*` over Content
   in 3-4 categories expands into about 10^6 decisions. Filed
   policy4j-the.4.50.
2. MEDIUM, `RegExpFunctions.java:130-145`: catastrophic backtracking on
   policy/request patterns; unbounded optional pattern cache. Filed
   policy4j-the.4.51.
3. MEDIUM, `XPathProvider.java`: no FEATURE_SECURE_PROCESSING and no
   evaluation limits for request-origin XPath. Filed policy4j-the.4.52.
4. LOW, `xacml-test` Xacml20TestUtility / Xacml30TestUtility parsers (test
   support library, not runtime); noted, not filed.

The bound values (maximum expanded requests, regex limits) are maintainer
decisions.
