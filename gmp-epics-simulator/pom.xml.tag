<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">

    <parent>
        <relativePath>../poms/compiled/</relativePath>
        <groupId>edu.gemini.aspen.giapi-osgi.build</groupId>
        <artifactId>compiled-bundle-settings</artifactId>
        <version>0.2.6</version>
    </parent>

    <properties>
        <bundle.symbolicName>edu.gemini.aspen.gmp.epics-simulator</bundle.symbolicName>
        <bundle.namespace>edu.gemini.aspen.gmp.epics.simulator</bundle.namespace>
    </properties>

    <modelVersion>4.0.0</modelVersion>
    <groupId>edu.gemini.aspen.gmp</groupId>
    <artifactId>gmp-epics-simulator</artifactId>
    <version>0.4.17</version>

    <name>GMP Epics Simulator</name>
    <description>Simple simulator of EPICS channels for testing</description>

    <packaging>bundle</packaging>

    <dependencies>
        <dependency>
            <groupId>edu.gemini.aspen.gmp</groupId>
            <artifactId>gmp-epics-access</artifactId>
        </dependency>
    </dependencies>

</project>
