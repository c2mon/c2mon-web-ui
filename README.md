# C2MON : CERN Control and Monitoring Platform
## C2MON web interface application

[![build status](https://gitlab.cern.ch/c2mon/c2mon-web-ui/badges/master/build.svg)](https://gitlab.cern.ch/c2mon/c2mon-web-ui/commits/master)

The CERN Control and Monitoring Platform (C2MON) is a heterogeneous data acquisition and monitoring framework. It contains many useful features
such as historical metric persistence and browsing, command execution and alerting. It can be suitable for building many different types
of monitoring and control system.

## Documentation
See the current [reference docs][].

## Issue Tracking
Please report issues on GitLab via the [issue tracker][].

## Building from Source
C2MON uses a [Maven][]-based build system. The web interface also uses NPM and webpack to build the frontend. 

### Prerequisites

[Git][] and [JDK 8 update 20 or later][JDK8 build]

Be sure that your `JAVA_HOME` environment variable points to the `jdk1.8.0` folder
extracted from the JDK download.

[Node.js v6.9.5 or later][] with NPM

### Check out sources
`git clone git@github.com:c2mon/c2mon-web-ui.git`

### Build the frontend
`npm install && npm run build` (this may take a while)

### Development
Run `cern.c2mon.web.ui.Application` as a Spring Boot application from within your IDE.

If you are modifying the frontend, you will want to run `npm run watch`. This will watch all the TS, CSS and HTML source code for changes and copy them into 
the build directory. You will need to reload the page to see the new changes, but you will not need to restart the application server.

### Compile and test; build all jars, distribution zips, and docs
`mvn package`

## Contributing
[Pull requests][] are welcome; see the [contributor guidelines][] for details.

## License
C2MON is released under the [GNU LGPLv3 License][].

[reference docs]: http://c2mon.web.cern.ch/c2mon/docs/latest/
[issue tracker]: https://gitlab.cern.ch/c2mon/c2mon-web-ui/issues
[Maven]: http://maven.apache.org
[Git]: http://help.github.com/set-up-git-redirect
[JDK8 build]: http://www.oracle.com/technetwork/java/javase/downloads
[NodeJS]: https://nodejs.org
[Pull requests]: http://help.github.com/send-pull-requests
[contributor guidelines]: /CONTRIBUTING.md
[GNU LGPLv3 License]: /LICENSE
