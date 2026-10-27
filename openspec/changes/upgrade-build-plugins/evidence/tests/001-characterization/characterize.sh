#!/bin/bash
# Characterize the plugin upgrade in scratch copies of the tree (task 1.1).
# Usage: characterize.sh <repo> <workdir>
set -u
repo="$1"; work="$2"
m39="$HOME/Applications/IntelliJ IDEA.app/Contents/plugins/maven-plugin/lib/maven3/bin"
export JAVA_HOME="$(jenv prefix 21)"
mkdir -p "$work"
copy() { rsync -a --delete --exclude=.git --exclude=target --exclude=openspec --exclude=.beads --exclude=.tgrep --exclude=.serena "$repo/" "$1/"; }

echo "== effective plugin versions before"
for mvnbin in "$m39" /opt/homebrew/bin; do
  PATH="$mvnbin:$PATH" mvn -B -q -f "$repo/pom.xml" help:effective-pom -Doutput="$work/effective-$(basename "$(dirname "$mvnbin")").xml" >/dev/null 2>&1
  echo "$mvnbin -> exit $?"
done

echo "== license headers: 1.7 and 2.7.1 in scratch copies"
for v in 1.7 2.7.1; do
  dir="$work/license-$v"; copy "$dir"
  PATH="$m39:$PATH" mvn -B -q -f "$dir/pom.xml" "org.codehaus.mojo:license-maven-plugin:$v:update-file-header" > "$work/license-$v.log" 2>&1
  echo "license $v exit $?"
  diff -rq --exclude=target "$repo" "$dir" -x .git -x openspec -x .beads -x .tgrep -x .serena > "$work/license-$v.diff" 2>&1
  echo "license $v differing files: $(grep -c '^Files ' "$work/license-$v.diff")"
done

echo "== JAXB generated sources: 0.15.1 vs 0.15.3"
for v in 0.15.1 0.15.3; do
  dir="$work/jaxb-$v"; copy "$dir"
  sed -i '' "s#<version>0.15.1</version>#<version>$v</version>#" "$dir/xacml-xml-jaxb/pom.xml"
  PATH="$m39:$PATH" mvn -B -q -f "$dir/pom.xml" -pl xacml-xml-jaxb -Dlicense.skipUpdateLicense=true generate-sources > "$work/jaxb-$v.log" 2>&1
  echo "jaxb $v exit $?"
done
diff -r -I '^// Generated on:' -I 'Generated on:' "$work/jaxb-0.15.1/xacml-xml-jaxb/target/generated-sources" "$work/jaxb-0.15.3/xacml-xml-jaxb/target/generated-sources" > "$work/jaxb.diff" 2>&1
echo "jaxb diff exit $? lines $(wc -l < "$work/jaxb.diff")"
