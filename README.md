cityLibProject
==============

mvn -Prelease --batch-mode release:prepare release:perform -DreleaseVersion=3.4.5-M11 -DdevelopmentVersion=3.4.5-M12-SNAPSHOT



mvn deploy:deploy-file \
-DrepositoryId=ttbox-repository-release \
-Durl=https://raw.github.com/gabuzomeu/maven-repo/master/releases/ \
-DgroupId=eu.ttbox.citylib \
-DartifactId=test-artifact  \
-Dversion=1.0.0 \
-Dpackaging=wsdl \
-Dfile=./settings.gradle

