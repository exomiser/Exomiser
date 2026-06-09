Exomiser Release Checklist
=

Releases can include either binary or data releases or both. Just follow the relevant checklists below.

Binary Release
---
- Update exomiser-cli/CHANGELOG.md
- Update exomiser-core/CHANGELOG.md
- Update version in all pom.xml
- Build local release
  - Run final end-to-end tests
- Build docker image
  - Run final end-to-end tests
- Merge changes into `master` branch
- Push release to GitHub
- Create GitHub release where tag equals latest version
- Push release to Docker Hub

Data Release
---
- Prepare data release notes
- Move data into Globus area
- Create GitHub discussion
