# rv091: review of the XML content comparison, run 082 (Codex)

Date: 2026-10-08. Lane: adversarial code reviewer, Codex CLI 0.160.1 with
the default model of the local configuration, one shot per round
(`codex exec -s read-only --skip-git-repo-check`) from a directory that
held only the packet; no repository access. Input per round:
`roundN-prompt.md` followed by the complete diff (`DOMUtil.java`,
`XmlContent.java`, `DOMUtilTest.java`, the new `DOMUtilEqualityTest.java`
and `MultipleResourcesContentOnlyRepeatsTest.java`; from round 3 also the
spec delta), home path removed.

Final verdict: APPROVE (round 3).

| Round | Verdict | Findings | Result |
| --- | --- | --- | --- |
| 1 | REQUEST_CHANGES | HIGH: the attribute lookup matched two attributes with the same namespace and local name to one, so the comparison was not symmetric and disagreed with the hash code. MEDIUM: processing instructions beside the document element were not compared. MEDIUM: the hash code that `XmlContent` kept was stale after a change of the document through `toNode()`. Two missing tests. | Attributes are one multiset of namespace, name and value. A document is compared by its children. `XmlContent` computes its hash code on every call. Tests red first (run 082 red 4); the two named tests added. |
| 2 | REQUEST_CHANGES | MEDIUM: a change of the document through a live node leaves the hash codes that `Entity` and `RequestContext` keep stale (a defect that was there before, not a new one). MEDIUM: a document fragment was compared without its children. LOW: two attribute nodes in different namespaces were equal. | Fragments and attribute nodes fixed, tests red first (red 5). The first finding: no code change; the contract "the nodes are for reading" is written on `XmlContent` and recorded as a limit of the run. |
| 3 | APPROVE | No defect. The reviewer accepts the contract for the first finding of round 2 and corrects the author's reason: a hash code that is computed again would make equal objects agree after a change that happens before the object is used as a key. Missing test: CDATA sections. | The CDATA test is added (`cdataSections`); it passed at once. |

What the prompts got wrong or left out:

- Round 2 said that computing the enclosing hash codes again "would not
  make such a change safe". The reviewer showed that this is too broad
  (round 3, point 2). The decision stands on the contract, not on that
  sentence.
- The prompts give the gates of their rounds (7,877, 7,887 and 7,889
  tests). The tree as committed passes with 7,890 tests (run 082,
  gate 2); the difference to round 3 is the CDATA test.

The first round began with errors of the Codex sandbox on stderr (a
symbolic link in its home directory); the reports came all the same.

Limits: the reviewer saw the diff and the facts in the prompts only. It
ran no build and no test. One model family reviewed this run.
