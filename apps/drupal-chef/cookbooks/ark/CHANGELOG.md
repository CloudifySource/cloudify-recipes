## v0.3.0:

### Improvement

- [COOK-3087]: Can't use ark with chef < 11

### Bug

- [COOK-3064]: `only_if` statements in ark's `install_with_make` and
  configure actions are not testing for file existence correctly.
- [COOK-3067]: ark kitchen test for `cherry_pick` is expecting the
  binary to be in the same parent folder as in the archive.

## v0.2.4:

### Bug

- [COOK-3048]: Ark provider contains a `ruby_block` resource without a block attribute
- [COOK-3063]: Ark cookbook `cherry_pick` action's unzip command does not close if statement
- [COOK-3065]: Ark install action does not symlink binaries correctly

## 0.2.2:

Update the README to reflect the requirement for Chef 11 to use the
ark resource (`use_inline_resources`).

Making this a release so it will also appear on the community site
page.

## 0.2.0:

### Bug

- [COOK-2772]: Ark cookbook has foodcritic failures in provides/default.rb

### Improvement

- [COOK-2520]: Refactor ark providers to use the '`use_inline_resources`' LWRP DSL feature

## 0.1.0:

* [COOK-2335] - ark resource broken on Chef 11

## 0.0.17

* [COOK-2026] - Allow `cherry_pick` action to be used for directories as
  well as files

## 0.0.16

* [COOK-1593] - README formatting updates for better display on
  Community Site

## 0.0.15

New features
* add `setup_py_*` actions
* add vagrantfile
* add foodcritic test
* travis.ci support

Bug fixes
* dangling "unless"

## 0.0.10 (May 23, 2012)

New features
* use autogen.sh to generate configure script for configure action
  https://github.com/bryanwb/chef-ark/issues/16
* support more file extensions https://github.com/bryanwb/chef-ark/pull/18
* add extension attribute which allows you to download files which do
  not have the file extension as part of the URL

Bug fixes
* `strip_leading_dir` not working for zip files
  https://github.com/bryanwb/chef-ark/issues/19
