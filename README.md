cityLibProject
==============

mvn -Prelease --batch-mode release:prepare release:perform -DreleaseVersion=0.3.2 -DdevelopmentVersion=0.3.3-SNAPSHOT

mvn --batch-mode release:update-versions -DdevelopmentVersion=0.3.2-SNAPSHOT




