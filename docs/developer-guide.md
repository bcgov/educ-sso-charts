# The sso helm charts

The helm charts for the sso-project are hosted here.  The actions on the repos will automatically build new releases when the charts' `Chart.yaml` file is changed.  Creating a new tagged version hosted by the repos.

## Versioning

Any change to a helm chart must have a new version, chart versions are expected to follow Semantic Versioning (https://semver.org/)

## Development guide

This repos hosts both charts and dependencies of those charts.  If a dependency and the dependant are updated in the same pull request, the build will fail for both. Meaning separate PRs are needed.
