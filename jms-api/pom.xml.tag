<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">

    <parent>
        <relativePath>../poms/compiled/</relativePath>
        <groupId>edu.gemini.aspen.giapi-osgi.build</groupId>
        <artifactId>compiled-bundle-settings</artifactId>
        <version>0.2.6</version>
    </parent>

    <properties>
        <bundle.symbolicName>edu.gemini.jms-api</bundle.symbolicName>
        <bundle.namespace>edu.gemini.jms.api</bundle.namespace>
    </properties>

    <modelVersion>4.0.0</modelVersion>
    <groupId>edu.gemini.jms</groupId>
    <artifactId>jms-api</artifactId>
    <version>1.6.17</version>

    <name>Gemini JMS API</name>
    <description>The Gemini Broker JMS-based API</description>

    <packaging>bundle</packaging>

    <dependencies>
        <dependency>
            <groupId>org.apache.geronimo.specs</groupId>
            <artifactId>geronimo-jms_1.1_spec</artifactId>
        </dependency>
    </dependencies>

</project>
