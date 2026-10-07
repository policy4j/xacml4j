# Vendored fixture history and restoration

Branch: `integration/modernize-core-jdk28-valhalla`  
Baseline: `a8a90d46`  
Inspection head: `befdd116bd11385ac8d007732fa8e4d347af3143`

## Path results

Blob ids for 1.4 are the requested `HEAD:<path>` ids before local edits. Those paths each have only their add commit in `git log --oneline HEAD -- <path>` and show no changes in `a8a90d46..HEAD`; they were left untouched. For 1.5, blob ids are the restored working-tree hashes, matching the add-commit blobs.

| Path | Add commit | Blob id | Action taken | 1.5 edit classification |
| --- | --- | --- | --- | --- |
| `xacml-conformance-tests/src/test/resources/Example_2/record.xsd` | `b09e94e510fbfad7fff59bb23af1a6829817efcb` | `e960681d8cdbded7093fbcf074a450286e0c82ac` | Kept; only add commit touched it | — |
| `xacml-conformance-tests/src/test/resources/oasis-xacml20-compat-test/ConformanceTests.html` | `451e0b44659dad4bceb1604d7ae35dd0d268156b` | `bfdefa095574cd2c0760b7a6a07024f2ed5e24e1` | Kept; only add commit touched it | — |
| `xacml-conformance-tests/src/test/resources/oasis-xacml20-compat-test/IIA002Special.txt` | `451e0b44659dad4bceb1604d7ae35dd0d268156b` | `d9d28ca63d482bb5c131bad570055a55e0f64212` | Kept; only add commit touched it | — |
| `xacml-conformance-tests/src/test/resources/rsa2008-interop/XacmlPolicySet-04-N-PPS-PRD-004.xml` | `451e0b44659dad4bceb1604d7ae35dd0d268156b` | `b7dc431d1c35baf402e6a9cb4c03e32cc43d4d04` | Kept; only add commit touched it | — |
| `xacml-conformance-tests/src/test/resources/xacml3.0-ct-v.0.4/ConformanceTests.html` | `c92be7b7c00a04cee07fc776cc56b02062a3bfdb` | `302245391870083ab47ecdade505230f29cdaffc` | Kept; only add commit touched it | — |
| `xacml-conformance-tests/src/test/resources/xacml3.0-ct-v.0.4/IIA023Policy.xml` | `c92be7b7c00a04cee07fc776cc56b02062a3bfdb` | `18f5f613f5762882aec8b3c1b72b6d6d18c18a3f` | Kept; only add commit touched it | — |
| `xacml-conformance-tests/src/test/resources/xacml3.0-ct-v.0.4/IIIA301Policy.xml` | `c92be7b7c00a04cee07fc776cc56b02062a3bfdb` | `287263cd3bcf557a1b562f5b312e11fd3181c614` | Kept; only add commit touched it | — |
| `xacml-conformance-tests/src/test/resources/xacml3.0-ct-v.0.4/IIIA340Policy.xml` | `c92be7b7c00a04cee07fc776cc56b02062a3bfdb` | `fe6c1f51204f03eb65190cda6d19f96e93359c42` | Kept; only add commit touched it | — |
| `xacml-conformance-tests/src/test/resources/xacml3.0-ct-v.0.4/IIIG002Policy.xml` | `c92be7b7c00a04cee07fc776cc56b02062a3bfdb` | `f4d10a82fdcd75b4454e691fccb787b964a39f26` | Kept; only add commit touched it | — |
| `xacml-conformance-tests/src/test/resources/xacml3.0-ct-v.0.4/xacml3.0-deprecated/IIIG002dPolicy.xml` | `c92be7b7c00a04cee07fc776cc56b02062a3bfdb` | `b919ab0844e1e2fa0a5a46e1613f6637c5bf75c7` | Kept; only add commit touched it | — |
| `xacml-xml-jaxb/src/main/resources/xsd/xml.xsd` | `cc8fb2b62a5305f1579e3b02e49198bbe34eeaf5` | `20555e269f17b87a41cb360cf8bc225a5debede3` | Kept; only add commit touched it | — |
| `xacml-conformance-tests/src/test/resources/oasis-xacml20-compat-test/IIIF002Request.xml` | `451e0b44659dad4bceb1604d7ae35dd0d268156b` | `73b9fc0614e84f05092292fec1a11134c8acaa72` | Restored from add commit | Whitespace plus removal of unused `md` namespace; request attributes and values are unchanged. |
| `xacml-conformance-tests/src/test/resources/oasis-xacml20-compat-test/IIIF007Policy.xml` | `451e0b44659dad4bceb1604d7ae35dd0d268156b` | `5344cd2146393ff2dab3d90bc8c9ee6f7ab4bd7b` | Restored from add commit | Semantic: the edit removed `xacml-context`, used in `RequestContextPath` selectors, changing namespace resolution for this conformance case. |
| `xacml-conformance-tests/src/test/resources/oasis-xacml20-compat-test/IIIG006Policy.xml` | `451e0b44659dad4bceb1604d7ae35dd0d268156b` | `728d89003e9a27996d14c01229dcb4769c1bbd80` | Restored from add commit | Semantic: the edit removed `xacml-context`, used in the case's XPath expressions, changing selector path namespace resolution. |
| `xacml-conformance-tests/src/test/resources/Example_2/Rule_1.xml` | `b09e94e510fbfad7fff59bb23af1a6829817efcb` | `92fc4b8baac9ca46b99140e4ea6529190042f4d5` | Restored from add commit | Semantic: the edit removed the `xacml-context` binding used by an `AttributeSelector` path into `ResourceContent`. |
| `xacml-conformance-tests/src/test/resources/Example_2/Rule_2.xml` | `b09e94e510fbfad7fff59bb23af1a6829817efcb` | `af9d2f817a132d67d03c37d82f620278a32be2f2` | Restored from add commit | Semantic: the edit removed the `xacml-context` binding used by an `AttributeSelector` path into `ResourceContent`. |
| `xacml-conformance-tests/src/test/resources/Example_2/Rule_3.xml` | `b09e94e510fbfad7fff59bb23af1a6829817efcb` | `131ac04b8a99545e1e4a6d80fb777bac08bdf165` | Restored from add commit | Semantic: the edit removed the `xacml-context` binding used by an `AttributeSelector` path into `ResourceContent`. |
| `xacml-json/src/test/resources/policy.json` | — | — | Deleted after the scoped reference search returned no matches | — |

## `policy.json`

`rtk rg -n 'policy\\.json' xacml-json/src -g '!**/xacml30-policy.json'` returned no references. `rtk git ls-files --error-unmatch` confirmed the donor file was tracked before deletion. It was removed with `rtk proxy rm`, not `git rm`.

## Notes

The 1.5 edit history is retained in `edits-1.5.diff`. The full-history review shows the six fixture edits came from `ea3fc607d7493530ad90a77aee221d77464cb0b3`; namespace binding removals affect selector paths, so five edits are semantic and the request-only edit is formatting/unused namespace cleanup. The task 1.5 Beads claim was rejected because issue 1.4 is still in progress and blocks it; task 1.4 remains claimed by this lane. No Maven command was run, as instructed.
