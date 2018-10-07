# subrosa-maven-extension
==================================

Add the extension in your project's .mvn/extension.xml file:

```xml
 <?xml version="1.0" encoding="UTF-8"?>
<extensions>
    <extension>
        <groupId>org.subrosa</groupId>
        <artifactId>subrosa-maven-extension</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </extension>
</extensions>
```

Define a .mvn/subrosa-project.properties file:

version_placeholder=MYPROJECT_VERSION

In your pom.xml files, replace the version with the placeholder:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns = "http://maven.apache.org/POM/4.0.0" xmlns:xsi = "http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation = "http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.acme.stuff</groupId>
    <artifactId>stuff-main-parent</artifactId>
    <packaging>pom</packaging>
    <name>ACME :: Main Parent POM</name>
    <version>MYPROJECT_VERSION</version>
```

The placeholder can be used in pom.xml version or as a parent:

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <parent>
        <groupId>com.acme.stuff</groupId>
        <artifactId>stuff-main-parent</artifactId>
        <version>MYPROJECT_VERSION</version>
    </parent>

	<modelVersion>4.0.0</modelVersion>
	<groupId>com.acceo.stuff.abc</groupId>
	<artifactId>stuff-abc-parent</artifactId>
```

To compile a specific version (in this order of priority):

a) mvn install -DMYPROJECT_VERSION=1.0.0-SNAPSHOT

b) set an environment variable.  
Ex: windows% set MYPROJECT_VERSION=1.0.0-SNAPSHOT  
mvn install

c) Add property version_value in subrosa-project.properties.  
Ex: version_value=5.5.6-SNAPSHOT

You can then open feature branches or any type of branches and rebase or merge whenever you want without fear of merge conflicts.  
Keeping the version of the project outside of the pom.xml file is the key to make sure you do not have useless conflicts.  
With only structural changes in your pom.xml such as adding a module and/or changing the list of dependencies, rebasing and merging becomes much easier.
