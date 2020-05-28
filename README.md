# C2MON Web User Interface (UI)
[![build status](https://gitlab.cern.ch/c2mon/c2mon-web-ui/badges/master/pipeline.svg)](https://gitlab.cern.ch/c2mon/c2mon-web-ui/commits/master)

The C2MON Web UI is based on the [C2MON Client API] and allows:
- to browse through the C2MON Data Acquisition (DAQ) Process-, Equipment- and Tag configuration
- to display the actual Tag value, status and properties.
- to query the Tag history and to display trends of numeric values

Every page of the Web UI has a unique URL based on the Tag id, which makes it simple to point from other applications to the Tag information.

## Documentation
This tool is very simple and intuitive and does not require a detailed documentation. 
To give it a trial you should follow the [Getting Started] guideline that will setup C2MON with some sample data. 

## Issue Tracking
Please report issues on GitLab via the [issue tracker][].

## Building from Source
C2MON uses a [Maven][]-based build system.

### Prerequisites

[Git][] and [JDK 8 update 20 or later][JDK8 build]

Be sure that your `JAVA_HOME` environment variable points to the `jdk1.8.0` folder
extracted from the JDK download.

### Check out sources
`git clone git@github.com:c2mon/c2mon-web-ui.git`

### Compile and test; build all jars, distribution zips, and docs
`mvn package -DskipDockerBuild -DskipDockerTag`

## Contributing
[Pull requests][] are welcome; see the [contributor guidelines][] for details.

## License
C2MON is released under the [GNU LGPLv3 License][].

[C2MON Client API]: http://c2mon.web.cern.ch/c2mon/docs/latest/user-guide/client-api/
[Getting started]: http://c2mon.web.cern.ch/c2mon/docs/latest/getting-started/
[reference docs]: http://c2mon.web.cern.ch/c2mon/docs/latest/
[issue tracker]: https://gitlab.cern.ch/c2mon/c2mon-web-ui/issues
[Maven]: http://maven.apache.org
[Git]: http://help.github.com/set-up-git-redirect
[JDK8 build]: http://www.oracle.com/technetwork/java/javase/downloads
[Pull requests]: http://help.github.com/send-pull-requests
[contributor guidelines]: /CONTRIBUTING.md
[GNU LGPLv3 License]: /LICENSE
