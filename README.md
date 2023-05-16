# ronin-product-common

## Warnings:

Versions >= 2.2.0 of this library update the authentication configuration to utilize [product-spring-auth](product-spring-modules/product-spring-auth) instead of 
[product-spring-seki-auth](product-spring-modules/product-spring-auth/product-spring-seki-auth).  See the readme in [product-spring-auth](product-spring-modules/product-spring-auth)
for more information.

## Releasing:

### Locally

If you run `./gradlew assemble publishToMavenLocal` on your local environment, this project uses the branch name and latest version tags in the repository to determine a version,
and will publish a snapshot version to your local maven repository.  Versions will look like: `MAJOR.MINOR.PATCH-TRUNCATED_BRANCH-SNAPSHOT`, where the patch will be _incremented 1_
from the latest tag in the format `vMAJOR.MINOR.PATCH`.  You can run `./gradlew printVersion` to see what that version will be.

### On GitHub

When you create a pull request, the [snapshot_publish.yml](.github/workflows/snapshot_publish.yml) workflow will publish a snapshot based on your branch which should be exactly the same
as the local one from your branch that you can see with `./gradlew printVersion`.

When you _merge_ a pull request to main, the same workflow will publish a snapshot that should be the same but without the branch tag, so like: `MAJOR.MINOR.PATCH-SNAPSHOT`.

To create a _release version_ of the library, use the [releases](https://github.com/projectronin/ronin-product-common/releases) functionality on github, draft a new release with the tag format
`vMAJOR.MINOR.PATCH` (MAJOR, MINOR, and PATCH must be integers, and it must be prefixed with `v`), and publish that release.  That should trigger the workflow to publish a release version
of the library.
