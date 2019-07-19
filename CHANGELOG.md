# Change Log
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/).

For more details on a given release, please check also the [Milestone planning](https://gitlab.cern.ch/c2mon/c2mon-web-ui/milestones?state=all).

## [Unreleased]
### Added

### Changed

### Fixed

## 0.1.15 - 2019-07-19
### Added
-  Added possibility to protect Process configuration view (#23)
-  Add direct link to commfault tag and alive tag from Equipment view

### Changed
- Alarm help link gets now opened in separate tab (CERN related)


## 0.1.14 - 2019-06-28
### Added
- Added source timestamp column to alarm history and single alarm page (#30)

### Changed
- Tag history is now displayed in descending order
- Tag history view: correlate tag and alarm history by source timestamp (#31)
- CERN internal: Change Helpalarm link to point to the new URL (#32)

### Fixed
- Fixed problem of not always refreshing command view (#24)
- Fixed problems with timestamp in alarm history, which is not correctly converted to actual timezone (#12)
- Fixed issue that C2MON was activating alarms several times with the same timestamp (#22)


## 0.1.8 
### Fixed
- Listing of last N alarms (#10)

### Removed
- Support for Datatag XML serialisation. Please use [c2mon-web-restapi](https://github.com/c2mon/c2mon-web-restapi) instead (#6)



[Unreleased]: https://gitlab.cern.ch/c2mon/c2mon-web-ui/milestones/3
[0.1.9]: https://gitlab.cern.ch/c2mon/c2mon-web-ui/milestones/2
[0.1.8]: https://gitlab.cern.ch/c2mon/c2mon-web-ui/milestones/1