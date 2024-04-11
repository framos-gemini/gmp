<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">

    <parent>
        <relativePath>../poms/scala/</relativePath>
        <groupId>edu.gemini.aspen.giapi-osgi.build</groupId>
        <artifactId>scala-compiled-bundle-settings</artifactId>
        <version>0.2.6</version>
    </parent>

    <properties>
        <bundle.symbolicName>edu.gemini.giapi.clients.lib</bundle.symbolicName>
        <bundle.namespace>edu.gemini.giapi.clients.lib</bundle.namespace>
    </properties>

    <modelVersion>4.0.0</modelVersion>
    <groupId>edu.gemini.giapi.clients</groupId>
    <artifactId>giapi-clients-lib</artifactId>
    <version>1.1.17</version>

    <name>GIAPI Clients library</name>
    <description>The module contains common classes and interfaces for giapi clients</description>

    <packaging>bundle</packaging>

    <dependencies>
        <dependency>
            <groupId>edu.gemini.aspen</groupId>
            <artifactId>giapi</artifactId>
        </dependency>
        <dependency>
            <groupId>edu.gemini.aspen.gmp</groupId>
            <artifactId>gmp-commands-jms-client</artifactId>
        </dependency>
        <dependency>
            <groupId>edu.gemini.external.osgi.com.jcraft</groupId>
            <artifactId>jsch</artifactId>
        </dependency>
        <dependency>
            <groupId>org.scala-lang</groupId>
            <artifactId>scala-library</artifactId>
        </dependency>
        <dependency>
            <groupId>edu.gemini.external.osgi.com.jcraft</groupId>
            <artifactId>jzlib</artifactId>
        </dependency>
    </dependencies>

</project>
