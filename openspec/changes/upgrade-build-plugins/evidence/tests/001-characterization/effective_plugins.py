"""List the plugin versions that an effective POM binds in build/plugins of every project.

Usage: python3 -I effective_plugins.py <effective-pom.xml>
Prints "artifactId version[,version...]" sorted by artifactId.
"""
import sys
import xml.etree.ElementTree as ET

NS = "{http://maven.apache.org/POM/4.0.0}"


def main():
    root = ET.parse(sys.argv[1]).getroot()
    projects = [root] if root.tag == NS + "project" else root.findall(NS + "project")
    found = {}
    for project in projects:
        for plugin in project.findall(f"{NS}build/{NS}plugins/{NS}plugin"):
            artifact = plugin.findtext(NS + "artifactId")
            version = plugin.findtext(NS + "version") or "(none)"
            found.setdefault(artifact, set()).add(version)
    for artifact in sorted(found):
        print(artifact, ",".join(sorted(found[artifact])))


if __name__ == "__main__":
    main()
