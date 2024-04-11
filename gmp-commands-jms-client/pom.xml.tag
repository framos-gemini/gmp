<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">

    <parent>
        <relativePath>../poms/compiled/</relativePath>
        <groupId>edu.gemini.aspen.giapi-osgi.build</groupId>
        <artifactId>compiled-bundle-settings</artifactId>
        <version>0.2.6</version>
    </parent>

    <properties>
        <bundle.symbolicName>edu.gemini.aspen.gmp-commands-jms-client</bundle.symbolicName>
        <bundle.namespace>edu.gemini.aspen.gmp.commands.jms.client</bundle.namespace>
    </properties>

    <modelVersion>4.0.0</modelVersion>
    <groupId>edu.gemini.aspen.gmp</groupId>
    <artifactId>gmp-commands-jms-client</artifactId>
    <version>0.2.17</version>

    <name>GMP Commands JMS Client</name>

    <packaging>bundle</packaging>

    <dependencies>
        <dependency>
            <groupId>edu.gemini.aspen.gmp</groupId>
            <artifactId>gmp-commands</artifactId>
        </dependency>
        <dependency>
            <groupId>edu.gemini.jms</groupId>
            <artifactId>jms-api</artifactId>
        </dependency>
        <dependency>
            <groupId>edu.gemini.aspen</groupId>
            <artifactId>giapi-jms-util</artifactId>
        </dependency>
        <dependency>
            <groupId>edu.gemini.aspen.gmp</groupId>
            <artifactId>gmp-commands-jms-bridge</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>edu.gemini.aspen</groupId>
            <artifactId>giapi-test-support</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

</project>