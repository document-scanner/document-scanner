# Contributions to document-scanner
Contributions under the license of the project (see `COPYING`) are welcome. They're managed on github.com through [pull requests (PR)](https://github.com/document-scanner/document-scanner/pulls). Continuous Integration is provided by [travis-ci.org](https://travis-ci.org/document-scanner/document-scanner) and contributions are expected to pass the tests, provide test cases and documentation.

If you have an idea for an improvement or want to work around an issue, but don't understand the source or its comments, please [open an issue](https://github.com/document-scanner/document-scanner/issues/new). The project values documentation as important and necessary part of quality code, so even the smallest unclearness can be interesting for an improvement.

## IDE specifics
Unless there's a good reason raised (probably in form of an issue), IDE-specific files ought to be kept out of the version control system, i.e. `git`, (including `.gitignore`, i.e. use `.git/info/exclude` for exclusion).

There're no special build commands, except for `mvn clean package -DskipDeb` which skips the time consuming construction of the `.deb` package with `dpkg-deb` which apparently nobody bothered to parallelize over the many years multi-core processors are widely available.

# OS specifics
Some management operations like starting of database servers and downloading a separate MySQL server are OS specific. That requires that a list of supported operating systems needs to be specified. It includes:

  * Debian-based Linux'
  * Mac OSX
  * Microsoft Windows

In case you notice that other systems are supported, please add them to the list, in case you want another system to be supported, please open an issue or a PR.

Building `.deb` and `.dmg` packages for Debian-based systems and Mac OSX requires tools, like `genisoimage`, see `.travis.yml` for details.
