<?xml version="1.0" encoding="utf-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">

    <parent>
        <groupId>edu.gemini.aspen.giapi-osgi.build</groupId>
        <artifactId>shared-plugin-settings</artifactId>
        <version>0.2.6</version>
    </parent>

    <modelVersion>4.0.0</modelVersion>
    <artifactId>compiled-bundle-settings</artifactId>
    <version>0.2.6</version>

    <name>giapi-osgi - bundle instructions</name>

    <packaging>pom</packaging>

    <properties>
        <!-- Matches the date format for the /sbin/date command -->
        <maven.build.timestamp.format>E MMM dd k:mm:ss z yyyy</maven.build.timestamp.format>
    </properties>

    <build>
        <resources>
            <resource>
                <directory>src/main/resources</directory>
            </resource>
        </resources>
        <plugins>
            <plugin>
                <groupId>org.apache.felix</groupId>
                <artifactId>maven-bundle-plugin</artifactId>
                <version>3.5.0</version>
                <extensions>true</extensions>
                <configuration>
                    <supportedProjectTypes>
                        <supportedProjectType>jar</supportedProjectType>
                        <supportedProjectType>bundle</supportedProjectType>
                        <supportedProjectType>war</supportedProjectType>
                    </supportedProjectTypes>
                    <instructions>
                        <Bundle-SymbolicName>${bundle.symbolicName}</Bundle-SymbolicName>
                        <Bundle-Version>${project.version}</Bundle-Version>
                        <Build-Date>${maven.build.timestamp}</Build-Date>
                        <Build-Host>${maven.build.hostname}</Build-Host>
                        <Bundle-Activator>${bundle.namespace}.osgi.Activator</Bundle-Activator>
                        <Export-Package>${bundle.namespace}.*;version="${project.version}"</Export-Package>
                        <_include>-osgi.bnd</_include>
                        <_debug>true</_debug>
                    </instructions>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-source-plugin</artifactId>
                <version>2.1.2</version>
                <configuration>
                    <attach>true</attach>
                </configuration>
                <executions>
                    <execution>
                        <id>attach-sources</id>
                        <phase>generate-resources</phase>
                        <goals>
                            <goal>jar-no-fork</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
