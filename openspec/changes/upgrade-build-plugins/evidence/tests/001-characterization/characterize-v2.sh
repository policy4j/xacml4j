#!/bin/bash
# Revised characterization helper (review rv001-plugins-code, finding B2).
# characterize.sh in this directory is the script that ran for run 001, on the tree
# before the POM change (license 1.7 with no skip default, maven-jaxb2-plugin 0.15.1).
# This version gives the same comparison on any tree: it forces each requested plugin
# version, forces the header update on, stops on a failed command, and records which
# versions ran.
# Usage: characterize-v2.sh <repo> <workdir>
set -euo pipefail
repo="$1"; work="$2"
m39="$HOME/Applications/IntelliJ IDEA.app/Contents/plugins/maven-plugin/lib/maven3/bin"
export JAVA_HOME="$(jenv prefix 21)"
mkdir -p "$work"
copy() { rsync -a --delete --exclude=.git --exclude=target --exclude=openspec --exclude=.beads --exclude=.tgrep --exclude=.serena "$repo/" "$1/"; }

echo "== license headers: forced update with 1.7 and 2.7.1"
for v in 1.7 2.7.1; do
  dir="$work/license-$v"; copy "$dir"
  PATH="$m39:$PATH" mvn -B -q -f "$dir/pom.xml" -Dlicense.skipUpdateLicense=false \
    "org.codehaus.mojo:license-maven-plugin:$v:update-file-header" > "$work/license-$v.log" 2>&1
  diff -rq --exclude=target -x .git -x openspec -x .beads -x .tgrep -x .serena "$repo" "$dir" > "$work/license-$v.diff" 2>&1 || true
  echo "license $v: differing files $(grep -c '^Files ' "$work/license-$v.diff" || true)"
done

echo "== JAXB generated sources: 0.15.1 and 0.15.3, version set explicitly"
for v in 0.15.1 0.15.3; do
  dir="$work/jaxb-$v"; copy "$dir"
  perl -0pi -e "s#(<artifactId>maven-jaxb2-plugin</artifactId>\s*<version>)[^<]+(</version>)#\${1}$v\${2}#" "$dir/xacml-xml-jaxb/pom.xml"
  grep -A1 'maven-jaxb2-plugin' "$dir/xacml-xml-jaxb/pom.xml" | grep -q "<version>$v</version>"
  PATH="$m39:$PATH" mvn -B -q -f "$dir/pom.xml" -pl xacml-xml-jaxb generate-sources > "$work/jaxb-$v.log" 2>&1
  grep -rhq "Eclipse Implementation of JAXB" "$dir/xacml-xml-jaxb/target/generated-sources" && echo "jaxb $v: generated"
done
# Same file list, no failed diff, and only the two XJC header comment lines may differ
# (review rv010, finding N1; cases in compare-generated-test.sh).
bash "$(dirname "$0")/compare-generated.sh" \
  "$work/jaxb-0.15.1/xacml-xml-jaxb/target/generated-sources" \
  "$work/jaxb-0.15.3/xacml-xml-jaxb/target/generated-sources" "$work/jaxb.diff"
