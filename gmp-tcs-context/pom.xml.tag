<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">

    <parent>
        <relativePath>../poms/scala/</relativePath>
        <groupId>edu.gemini.aspen.giapi-osgi.build</groupId>
        <artifactId>scala-compiled-bundle-settings</artifactId>
        <version>0.2.6</version>
    </parent>

    <properties>
        <bundle.symbolicName>edu.gemini.aspen.gmp.tcs-context</bundle.symbolicName>
        <bundle.namespace>edu.gemini.aspen.gmp.tcs</bundle.namespace>
    </properties>

    <modelVersion>4.0.0</modelVersion>
    <groupId>edu.gemini.aspen.gmp</groupId>
    <artifactId>gmp-tcs-context</artifactId>
    <version>0.3.17</version>

    <name>GMP TCS Context Service</name>
    <description>TCS Context service</description>

    <packaging>bundle</packaging>

    <dependencies>
        <dependency>
            <groupId>edu.gemini.jms</groupId>
            <artifactId>jms-api</artifactId>
        </dependency>
        <dependency>
            <groupId>edu.gemini.epics</groupId>
            <artifactId>epics-service</artifactId>
        </dependency>
        <dependency>
            <groupId>edu.gemini.aspen</groupId>
            <artifactId>giapi-jms-util</artifactId>
        </dependency>
        <dependency>
            <groupId>edu.gemini.ocs</groupId>
            <artifactId>edu-gemini-util-osgi_2.13</artifactId>
        </dependency>
    </dependencies>

</project>
