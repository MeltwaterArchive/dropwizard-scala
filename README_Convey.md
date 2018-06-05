Building and deploying to S3
============================

There are SBT plugins to deploy to S3, but unfortunately, the ones
I tried do not generate and update `maven-metadata.xml` files, which
results in errors when Maven attempts to resolve the artifacts.  Because
of that, we use SBT to build the artifacts and Maven to deploy them.

Building
--------

```$bash
$ sbt
[info] Loading project definition from /Users/david/dev/dropwizard-scala/project
(etc.)

> ++ 2.11.8
[info] Setting version to 2.11.8
[info] Reapplying settings...
(etc.)

> package
(various output)
[info] Done packaging.
[success] Total time: 12 s, completed Jun 5, 2018 5:40:12 PM
```

Deploying
---------

To deploy a snapshot version, pass the version to the deploy script:

```$bash
$ ./deploy v1.3.2-SNAPSHOT
Snapshot version:  1.3.0-SNAPSHOT
Deploying dropwizard-scala-core_2.11 snapshot....
(etc.)
```

To deploy a release version, check out the release tag and run the release 
script without arguments:

```$bash
$ git checkout v1.3.2
$ ./deploy
Getting release version from tag
Release version:  1.3.2
Deploying dropwizard-scala-core_2.11 release....
``` 
