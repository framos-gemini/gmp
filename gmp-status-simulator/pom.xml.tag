<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">

    <parent>
        <relativePath>../poms/scala/</relativePath>
        <groupId>edu.gemini.aspen.giapi-osgi.build</groupId>
        <artifactId>scala-compiled-bundle-settings</artifactId>
        <version>0.2.6</version>
    </parent>

    <properties>
        <bundle.symbolicName>edu.gemini.aspen.gmp.status.simulator</bundle.symbolicName>
        <bundle.namespace>edu.gemini.aspen.gmp.status.simulator</bundle.namespace>
    </properties>

    <modelVersion>4.0.0</modelVersion>
    <groupId>edu.gemini.aspen.gmp</groupId>
    <artifactId>gmp-status-simulator</artifactId>
    <version>0.5.17</version>

    <name>GMP Status Simulator</name>
    <description>Simple simulator of Status items for testing</description>

    <packaging>bundle</packaging>

    <build>
        <plugins>
            <plugin>
                <groupId>com.sun.tools.xjc.maven2</groupId>
                <artifactId>maven-jaxb-plugin</artifactId>
                <version>1.1.1</version>
                <executions>
                    <execution>
                        <goals>
                            <goal>generate</goal>
                        </goals>
                    </execution>
                </executions>
                <configuration>
                    <generatePackage>edu.gemini.aspen.gmp.status.simulator.generated</generatePackage>
                    <generateDirectory>src/main/java</generateDirectory>
                    <removeOldOutput>true</removeOldOutput>
                </configuration>
            </plugin>
        </plugins>
    </build>

    <dependencies>
        <dependency>
            <groupId>edu.gemini.aspen</groupId>
            <artifactId>giapi</artifactId>
        </dependency>
        <dependency>
            <groupId>edu.gemini.gmp</groupId>
            <artifactId>gmp-top</artifactId>
        </dependency>
        <dependency>
            <groupId>edu.gemini.aspen.giapi</groupId>
            <artifactId>giapi-status-setter</artifactId>
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
