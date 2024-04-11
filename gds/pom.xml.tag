<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">

    <parent>
        <relativePath>../poms/scala/</relativePath>
        <groupId>edu.gemini.aspen.giapi-osgi.build</groupId>
        <artifactId>scala-compiled-bundle-settings</artifactId>
        <version>0.2.6</version>

    </parent>

    <properties>
        <bundle.symbolicName>edu.gemini.aspen.gds</bundle.symbolicName>
        <bundle.namespace>edu.gemini.aspen.gds</bundle.namespace>
        <scala.lib.suffix>2.13</scala.lib.suffix>
    </properties>

    <modelVersion>4.0.0</modelVersion>
    <groupId>edu.gemini.aspen.gds</groupId>
    <artifactId>gds</artifactId>
    <version>0.0.10</version>

    <name>GDS</name>
    <description>The module contains the new GDS</description>

    <packaging>bundle</packaging>

    <build>
        <plugins>
            <plugin>
                <groupId>net.alchim31.maven</groupId>
                <artifactId>scala-maven-plugin</artifactId>
                <version>${scala-plugin.version}</version>
                <configuration>
                    <!-- values obtained from the sbt-tpolecat plugin on github. May need to be adjusted with version changes. -->
                    <args combine.children="append">
                        <arg>-language:implicitConversions</arg>
                        <arg>-Xfatal-warnings</arg>
                        <arg>-Xlint:adapted-args</arg>
                        <arg>-Xlint:constant</arg>
                        <arg>-Xlint:deprecation</arg>
                        <arg>-Xlint:inaccessible</arg>
                        <arg>-Xlint:infer-any</arg>
                        <arg>-Xlint:missing-interpolator</arg>
                        <arg>-Xlint:nullary-unit</arg>
                        <arg>-Xlint:option-implicit</arg>
                        <arg>-Xlint:package-object-classes</arg>
                        <arg>-Xlint:poly-implicit-overload</arg>
                        <arg>-Xlint:private-shadow</arg>
                        <arg>-Xlint:stars-align</arg>
                        <arg>-Xlint:strict-unsealed-patmat</arg>
                        <arg>-Xlint:type-parameter-shadow</arg>
                        <arg>-Xlint:byname-implicit</arg>
                        <arg>-Wunused:nowarn</arg>
                        <arg>-Wdead-code</arg>
                        <arg>-Wextra-implicit</arg>
                        <arg>-Wnumeric-widen</arg>
                        <arg>-Wunused:implicits</arg>
                        <arg>-Wunused:explicits</arg>
                        <arg>-Wunused:imports</arg>
                        <arg>-Wunused:locals</arg>
                        <arg>-Wunused:params</arg>
                        <arg>-Wunused:patvars</arg>
                        <arg>-Wunused:privates</arg>
                        <arg>-Wvalue-discard</arg>
                        <arg>-Vimplicits</arg>
                        <arg>-Vtype-diffs</arg>
                    </args>
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
            <groupId>edu.gemini.epics</groupId>
            <artifactId>epics-service</artifactId>
        </dependency>
        <dependency>
            <groupId>edu.gemini.aspen.gmp</groupId>
            <artifactId>gmp-services</artifactId>
        </dependency>
        <dependency>
            <groupId>edu.gemini.ocs</groupId>
            <artifactId>edu-gemini-util-osgi_${scala.lib.suffix}</artifactId>
        </dependency>
        <dependency>
            <groupId>edu.gemini.gds</groupId>
            <artifactId>gds-fp</artifactId>
        </dependency>

        <!-- for the configuration file -->
        <dependency>
            <groupId>org.scala-lang.modules</groupId>
            <artifactId>scala-parser-combinators_${scala.lib.suffix}</artifactId>
        </dependency>

        <!-- I think something in gds-fs2 needs this, not sure -->
        <dependency>
            <groupId>org.scala-lang</groupId>
            <artifactId>scala-reflect</artifactId>
            <version>${scala-lang.version}</version>
        </dependency>
    </dependencies>

</project>
