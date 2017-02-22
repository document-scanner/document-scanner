# document-scanner release checklist

  1. change versions in `pom.xml` of affected projects and in `dependencyManagement` in `document-scanner-parent` as well as dependency of `izpack-maven-plugin`
  2. run `mvn package` and publish binaries on download page (currently ownCloud at https://richtercloud.de:451)
  3. change download link in `document-scanner`'s `README`
  4. commit the changes in `pom.xml`'s and `README` with message `version [version]` and tag it with `[version]`
