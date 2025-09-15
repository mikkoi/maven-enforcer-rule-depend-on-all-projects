[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.mikkoi/maven-enforcer-rule-depend-on-all-projects.svg)](https://mvnrepository.com/artifact/com.github.mikkoi/maven-enforcer-rule-depend-on-all-projects)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=mikkoi_maven-enforcer-rule-depend-on-all-projects&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=mikkoi_maven-enforcer-rule-depend-on-all-projects)

# Maven Enforcer Plugin Rule dependOnAllProjects

# Introduction

This is a custom rule to [Maven Enforcer](https://maven.apache.org/enforcer/index.html).

Do you have a Maven project with a special subproject which needs to be executed last?
For example, collecting test results and aggregating them.
**Such a project should be dependent upon all the other projects.**

This is difficult to maintain because dependencies are not dynamically added
during Maven build. They must be hardcoded so every time a new project is added
or old is removed, a change must also be made in the subproject.
Human error makes it so that this subproject is often behind.

This custom rule will emit an error and break build (unless suppressed)
if all other subprojects of the build are not defined as dependencies
to this subproject.

Example usage:

    <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-enforcer-plugin</artifactId>
        <dependencies>
            <dependency>
                <groupId>com.github.mikkoi</groupId>
                <artifactId>maven-enforcer-rule-depend-on-all-projects</artifactId>
            </dependency>
        </dependencies>
        <executions>
            <execution>
                <id>depend-on-all-projects</id>
                <phase>validate</phase>
                <goals>
                    <goal>enforce</goal>
                </goals>
                <configuration>
                    <rules>
                        <dependOnAllProjects>
                            <includes>
                                <include>main-*</include>
                            </includes>
                            <excludes>
                                <exclude>main-project-excluded</exclude>
                                <exclude>main-project-excluded-*</exclude>
                            </excludes>
                        </dependOnAllProjects>
                    </rules>
                </configuration>
            </execution>
        </executions>
    </plugin>


## License

Apache License Version 2.0, January 2004
