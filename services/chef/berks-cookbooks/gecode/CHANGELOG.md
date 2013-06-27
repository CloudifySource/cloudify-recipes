## v2.0.2:

### Bug

- [COOK-2959]: gecode cookbook has foodcritic failure

## v2.0.0:

* [COOK-1868] - use `node['platform_family']` to handle multiple
  platforms better

## v1.3.0:

* [COOK-1713] - switch to gecode package in Fedora, EPEL >= 6

## v1.2.0:

* [COOK-663] - upgrade libgecode-dev package
* [COOK-778] - update ld.so.conf

## 1.0.2

* split default recipe into source and package recipe (follows pattern of other cookbooks)
* externalize source installation metadata into attributes
* verify mac os x platform support

## 1.0.0

* [COOK-538] fix gecode install on newer ubuntu and debian releases
* [COOK-680] don't rebuild gecode if it is already installed

## 0.99.0

* initial release
