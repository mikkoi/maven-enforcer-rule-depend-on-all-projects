package com.github.mikkoi.maven_enforcer_plugin.rule;

import com.soebes.itf.jupiter.extension.MavenCLIOptions;
import com.soebes.itf.jupiter.extension.MavenGoal;
import com.soebes.itf.jupiter.extension.MavenJupiterExtension;
import com.soebes.itf.jupiter.extension.MavenOption;
import com.soebes.itf.jupiter.extension.MavenProject;
import com.soebes.itf.jupiter.extension.MavenRepository;
import com.soebes.itf.jupiter.extension.MavenTest;
import com.soebes.itf.jupiter.extension.SystemProperty;
import com.soebes.itf.jupiter.maven.MavenExecutionResult;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.DefaultModelReader;
import org.apache.maven.model.io.ModelReader;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.soebes.itf.extension.assertj.MavenITAssertions.assertThat;

@MavenJupiterExtension
public class DependOnAllProjectsIT {

    @Nested
    @MavenProject      // Use same Maven project for all tests in this set.
    @MavenGoal("validate")
    @MavenOption(MavenCLIOptions.BATCH_MODE)
    @MavenOption(MavenCLIOptions.QUIET)
    //@MavenOption(MavenCLIOptions.VERBOSE)
    @MavenRepository   // We can share the local repository because this plugin does not use it.
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class set_001 {

//        private final int NUMBER_OF_PROJECTS_IN_BUILD = 8; // Including root
        @MavenTest
        @Order(1)
        void the_first_test_case(MavenExecutionResult result) {
            assertThat(result).isSuccessful();
        }

//        private final int NUMBER_OF_PROJECTS_IN_BUILD = 8; // Including root
//        @MavenTest
//        @Order(1)
//        void the_first_test_case(MavenExecutionResult result) {
//            assertThat(result).isSuccessful();
//
//
//
//            Path bomPath = Paths.get(result.getMavenProjectResult().getTargetProjectDirectory().toString(), "target", "bom", "pom.xml");
//            assertThat(Files.exists(bomPath)).isTrue();
//
//            final ModelReader modelReader = new DefaultModelReader();
//            Model model;
//            try {
//                model = modelReader.read(bomPath.toFile(), null);
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//            // Number of dependencies in DependencyManagement match number of projects in the build.
//            assertThat(model.getDependencyManagement().getDependencies().size()).isEqualTo(NUMBER_OF_PROJECTS_IN_BUILD);
//
//            // BOM project details match root project equivalents.
//            assertThat(model.getGroupId()).isEqualTo(result.getMavenProjectResult().getModel().getGroupId());
//            assertThat(model.getVersion()).isEqualTo(result.getMavenProjectResult().getModel().getVersion());
//
//            // BOM project specific details (not copied from root project).
//            assertThat(model.getArtifactId()).isEqualTo("bom");
//            assertThat(model.getPackaging()).isEqualTo("pom");
//            assertThat(model.getName()).isEqualTo("Project BOM");
//
//            // Check one dependency from BOM project's DependencyManagement section.
//            // The first dependency is the root project itself.
//            // (unless deactivated with parameter or with different sorting order.)
//            final Dependency rootProject = getRootProjectAsDependency(result);
//            assertThat(model.getDependencyManagement().getDependencies().get(0).getGroupId()).isEqualTo(rootProject.getGroupId());
//            assertThat(model.getDependencyManagement().getDependencies().get(0).getArtifactId()).isEqualTo(rootProject.getArtifactId());
//            assertThat(model.getDependencyManagement().getDependencies().get(0).getVersion()).isEqualTo(rootProject.getVersion());
//            assertThat(model.getDependencyManagement().getDependencies().get(0).getType()).isEqualTo(rootProject.getType());
//        }

//        // Custom bom file location, artifact, group and version id, and exclude root
//        @MavenTest
//        @SystemProperty(value = "projects.createBom.excludes", content = "com.github.mikkoi:projects-maven-plugin-create-bom-set-001")
//        @SystemProperty(value = "projects.createBom.bomFilepath", content = "target/bom-case-002/pom.xml")
//        @SystemProperty(value = "projects.createBom.bomGroupId", content = "this.test.project")
//        @SystemProperty(value = "projects.createBom.bomArtifactId", content = "bom")
//        @SystemProperty(value = "projects.createBom.bomVersion", content = "123")
//        @SystemProperty(value = "projects.createBom.bomName", content = "BOM file for project")
//        @Order(2)
//        void the_second_test_case(MavenExecutionResult result) {
//            assertThat(result).isSuccessful();
//            Path bomPath = Paths.get(result.getMavenProjectResult().getTargetProjectDirectory().toString(), "target", "bom-case-002", "pom.xml");
//            assertThat(Files.exists(bomPath)).isTrue();
//            final ModelReader modelReader = new DefaultModelReader();
//            Model model;
//            try {
//                model = modelReader.read(bomPath.toFile(), null);
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//            assertThat(model.getDependencyManagement().getDependencies().size()).isEqualTo(NUMBER_OF_PROJECTS_IN_BUILD - 1);
//            assertThat(model.getGroupId()).isEqualTo("this.test.project");
//            assertThat(model.getArtifactId()).isEqualTo("bom");
//            assertThat(model.getVersion()).isEqualTo("123");
//            assertThat(model.getName()).isEqualTo("BOM file for project");
//        }

//        @MavenTest
//        @SystemProperty(value = "projects.createBom.includes", content = "com.github.mikkoi:other*") // => 5
//        @SystemProperty(value = "projects.createBom.excludes", content = "com.github.mikkoi:other-second-*") // => 2
//        @SystemProperty(value = "projects.createBom.sortOrder", content = "alphabetical")
//        @SystemProperty(value = "projects.createBom.bomFilepath", content = "target/bom-case-003/pom.xml")
//        @SystemProperty(value = "projects.createBom.bomGroupId", content = "this.test.project")
//        @SystemProperty(value = "projects.createBom.bomArtifactId", content = "bom")
//        @SystemProperty(value = "projects.createBom.bomVersion", content = "TODAY")
//        @Order(3)
//        void the_third_test_case(MavenExecutionResult result) {
//            assertThat(result).isSuccessful();
//            Path bomPath = Paths.get(result.getMavenProjectResult().getTargetProjectDirectory().toString(), "target", "bom-case-003", "pom.xml");
//            assertThat(Files.exists(bomPath)).isTrue();
//            final ModelReader modelReader = new DefaultModelReader();
//            Model model;
//            try {
//                model = modelReader.read(bomPath.toFile(), null);
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//            assertThat(model.getDependencyManagement().getDependencies().size()).isEqualTo(NUMBER_OF_PROJECTS_IN_BUILD - 4);
//            assertThat(model.getGroupId()).isEqualTo("this.test.project");
//            assertThat(model.getArtifactId()).isEqualTo("bom");
//            assertThat(model.getVersion()).isEqualTo("TODAY");
//
//            // Check one dependency from BOM project's DependencyManagement section.
//            final Dependency rootProject = new Dependency();
//            rootProject.setGroupId(result.getMavenProjectResult().getModel().getGroupId());
//            rootProject.setArtifactId("other-second");
//            rootProject.setVersion(result.getMavenProjectResult().getModel().getVersion());
//            rootProject.setType("pom");
//            assertThat(model.getDependencyManagement().getDependencies().get(1).getGroupId()).isEqualTo(rootProject.getGroupId());
//            assertThat(model.getDependencyManagement().getDependencies().get(1).getArtifactId()).isEqualTo(rootProject.getArtifactId());
//            assertThat(model.getDependencyManagement().getDependencies().get(1).getVersion()).isEqualTo(rootProject.getVersion());
//            assertThat(model.getDependencyManagement().getDependencies().get(1).getType()).isEqualTo(rootProject.getType());
//        }

//        @MavenTest
//        @SystemProperty(value = "projects.createBom.includes", content = "com.github.mikkoi:other*")
//        @SystemProperty(value = "projects.createBom.excludes", content = "other,other-second")
//        @SystemProperty(value = "projects.createBom.sortOrder", content = "alphabetical")
//        @SystemProperty(value = "projects.createBom.bomFilepath", content = "target/bom-case-004/pom.xml")
//        @SystemProperty(value = "projects.createBom.bomGroupId", content = "this.test.project")
//        @SystemProperty(value = "projects.createBom.bomArtifactId", content = "bom")
//        @SystemProperty(value = "projects.createBom.bomVersion", content = "TODAY")
//        @SystemProperty(value = "projects.createBom.bomName", content = "BOM TODAY")
//        @Order(4)
//        void the_fourth_test_case(MavenExecutionResult result) {
//            assertThat(result).isSuccessful();
//            Path bomPath = Paths.get(result.getMavenProjectResult().getTargetProjectDirectory().toString(), "target", "bom-case-004", "pom.xml");
//            assertThat(Files.exists(bomPath)).isTrue();
//            final ModelReader modelReader = new DefaultModelReader();
//            Model model;
//            try {
//                model = modelReader.read(bomPath.toFile(), null);
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//            assertThat(model.getDependencyManagement().getDependencies().size()).isEqualTo(NUMBER_OF_PROJECTS_IN_BUILD - 3);
//            assertThat(model.getGroupId()).isEqualTo("this.test.project");
//            assertThat(model.getArtifactId()).isEqualTo("bom");
//            assertThat(model.getVersion()).isEqualTo("TODAY");
//            assertThat(model.getName()).isEqualTo("BOM TODAY");
//
//            // Check one dependency from BOM project's DependencyManagement section.
//            final Dependency rootProject = new Dependency();
//            rootProject.setGroupId(result.getMavenProjectResult().getModel().getGroupId());
//            rootProject.setArtifactId("other-second-third");
//            rootProject.setVersion(result.getMavenProjectResult().getModel().getVersion());
//            rootProject.setType("pom");
//            assertThat(model.getDependencyManagement().getDependencies().get(2).getGroupId()).isEqualTo(rootProject.getGroupId());
//            assertThat(model.getDependencyManagement().getDependencies().get(2).getArtifactId()).isEqualTo(rootProject.getArtifactId());
//            assertThat(model.getDependencyManagement().getDependencies().get(2).getVersion()).isEqualTo(rootProject.getVersion());
//            assertThat(model.getDependencyManagement().getDependencies().get(2).getType()).isEqualTo(rootProject.getType());
//        }

    }

    @Nested
    @MavenProject      // Use same Maven project for all tests in this set.
    @MavenGoal("validate")
    @MavenOption(MavenCLIOptions.BATCH_MODE)
    @MavenOption(MavenCLIOptions.QUIET)
    //@MavenOption(MavenCLIOptions.VERBOSE)
    @MavenRepository   // We can share the local repository because this plugin does not use it.
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class set_002 {

        @MavenTest
        @Order(1)
        void the_first_test_case(MavenExecutionResult result) {
            assertThat(result).isFailure().out().error()
                    .contains("Rule 0: com.github.mikkoi.maven_enforcer_plugin.rule.DependOnAllProjects failed with message:")
                    .contains("Project 'com.github.mikkoi:subproject' missing from dependencies.")
            ;
        }

    }

    @Nested
    @MavenProject      // Use same Maven project for all tests in this set.
    @MavenGoal("validate")
    @MavenOption(MavenCLIOptions.BATCH_MODE)
    @MavenOption(MavenCLIOptions.QUIET)
    //@MavenOption(MavenCLIOptions.VERBOSE)
    @MavenRepository   // We can share the local repository because this plugin does not use it.
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class set_003 {

        @MavenTest
        @Order(1)
        void the_first_test_case(MavenExecutionResult result) {
            assertThat(result).isSuccessful();
        }

    }

    private Dependency getRootProjectAsDependency(MavenExecutionResult result) {
        final Dependency rootProject = new Dependency();
        rootProject.setType(result.getMavenProjectResult().getModel().getPackaging());
        rootProject.setGroupId(result.getMavenProjectResult().getModel().getGroupId());
        rootProject.setArtifactId(result.getMavenProjectResult().getModel().getArtifactId());
        rootProject.setVersion(result.getMavenProjectResult().getModel().getVersion());
        return rootProject;
    }
}
