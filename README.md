# document-scanner

## Synopsis
`document-scanner` is a graphical user interface (GUI) program which assists in scanning paper and digital (PDF, etc.) documents, extracting metadata from them and storing the image data and this metadata in a database or similar storage backend. A common workflow for a paper document looks like this:

  * Only once: Choose your scanner (if multiple are connected) and check that the settings make sense (`document-scanner` tries its best to guess the ideal ones).
  * Scan a batch of documents (ideally with an [automatic document feed (ADF)](https://en.wikipedia.org/wiki/Automatic_document_feeder), but it's not required), sort their pages into logical documents (because the scanner doesn't know where one document start and end) and turn upside down images or images in landscape format in the right sense.
  * Create entities for metadata of the document (usually has a company as a sender and a person as recipient (probably you)), select a reception date, etc. Note that `document-scanner` automatically stores the image data of the document as well as an [Optical character recognition (OCR)](https://en.wikipedia.org/wiki/Optical_character_recognition) result. You can click and drag the mouse over the image and get an OCR result for a part of the document (those partial results aren't as good as the OCR result of the complete image (yet)). Also note that an auto-value-detection function has been put partially in place currently recognizing date values (proposition for values are above each data control).
  * Save and explore features like editing entities and exporting PDFs from scan images.
  * Advanced: After you scanned some documents (not too many), you might consider using a more sophisticated storage setup with a database running in its own process started, watched and shutdown automatically by `document-scanner`. There's no feature to migrate records from one storage to another (yet), so make your storage choice as soon as you're ready to deal with advanced usage.

## Installation
### Binaries
#### Java Runtime Environment
Running and eventually installing `document-scanner` requires a Java Runtime Environment (JRE) version 8 which is free of charge and installed

  * on Debian-based systems (Debian, Ubuntu, Linux Mint, etc.) by running `sudo apt-get update && sudo apt-get install openjdk-8-jre` in a terminal
  * on Mac OSX by installing it through the AppStore (serach for `Java Runtime Environment`)
  * on other systems (including Windows) by downloading an installer from the download page ("Downloads" tab) referenced at http://www.oracle.com/technetwork/java/javase/overview/index.html. You only need the Java _Runtime Environment_ (J_RE_), and not the Java _Development Kit_ (J_DK_), but it's not a problem to install the latter. Choose the highest version starting with 8. You'll need to accept the use of cookies by Oracle depending on which country you're in/from and the license agreement for download and usage by Oracle. The download should also provide alternative installation routines for the aforementioned operating systems which you might want to consider in case the described ways didn't work.

#### document-scanner
If you have a Debian-based operating system, checkout the [.deb package download](https://richtercloud.de:451/index.php/s/WZQNbSSMeJ5SjMi), if you have Mac OSX, the [.dmg download](https://richtercloud.de:451/index.php/s/nNCw5nDnEmi7ijb) and otherwise the [cross-platform Java installer](https://richtercloud.de:451/index.php/s/lZrdswA1zzH6CzG) (also in case you experience trouble with the former).

### From source
Build from source by

  * installing [Apache Maven 3.1.1](https://www.apache.org/dist/maven/binaries/apache-maven-3.1.1-bin.zip) and [setting it up](https://maven.apache.org/install.html) for your operating system,
  * cloning [document-scanner-aggregator](https://github.com/document-scanner/document-scanner-aggregator) which lets you build all necessary dependencies and `document-scanner` itself without trouble with `git clone https://github.com/document-scanner/document-scanner-aggregator` and
  * running `mvn clean install` in the just cloned source root.

You can then start the program with `java -cp "target/staging/lib/*:target/document-scanner-1.0-beta.jar" richtercloud.document.scanner.gui.DocumentScanner` or use the built packages for Debian-based systems, Mac OSX or the cross-platform installer which are in `target/` and `target/izpack-installer/` respectively after a successful build.

You should be able to open the project in any IDE that supports Maven, including NetBeans, which lets you build and run the application from source maybe more conveniently. The project uses Maven aggregator and parent projects.

## License
`document-scanner` is free software - [free as in "free speech" - not as in "free beer"](https://www.gnu.org/philosophy/free-sw.html) licensed under the [GNU General Public License version 3 (GPLv3)](https://en.wikipedia.org/wiki/GNU_General_Public_License#Version_3).

## Troubleshooting
You might suffer from [Ubuntu bug #1480919](https://bugs.launchpad.net/ubuntu/+source/sane-backends/+bug/1480919) which prevents `saned` to be started with `systemd`. See the issue report for help.
