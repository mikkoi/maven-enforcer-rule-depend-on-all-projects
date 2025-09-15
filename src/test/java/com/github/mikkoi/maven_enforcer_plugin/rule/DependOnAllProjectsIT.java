package com.github.mikkoi.maven_enforcer_plugin.rule;

import com.soebes.itf.jupiter.extension.MavenCLIOptions;
import com.soebes.itf.jupiter.extension.MavenGoal;
import com.soebes.itf.jupiter.extension.MavenJupiterExtension;
import com.soebes.itf.jupiter.extension.MavenOption;
import com.soebes.itf.jupiter.extension.MavenProject;
import com.soebes.itf.jupiter.extension.MavenRepository;
import com.soebes.itf.jupiter.extension.MavenTest;
import com.soebes.itf.jupiter.maven.MavenExecutionResult;
import org.apache.maven.model.Dependency;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;

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

        @MavenTest
        @Order(1)
        void the_first_test_case(MavenExecutionResult result) {
            assertThat(result).isSuccessful();
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
    class set_002 {

        @MavenTest
        @Order(1)
        void the_first_test_case(MavenExecutionResult result) {
            final String groupId = result.getMavenProjectResult().getModel().getGroupId();
            final String version = result.getMavenProjectResult().getModel().getVersion();
            assertThat(result).isFailure().out().error()
                    .contains("Rule 0: com.github.mikkoi.maven_enforcer_plugin.rule.DependOnAllProjects failed with message:")
                    .contains(String.format("Project '%s:%s' is missing dependency '%s:%s:%s'.", groupId, "z-aggregation", groupId, "subproject", "jar"))
                    .contains(String.format("Project '%s:%s' is missing dependency '%s:%s:%s'.", groupId, "z-aggregation", groupId, "other-second-fourth", "jar"))
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

    @Nested
    @MavenProject      // Use same Maven project for all tests in this set.
    @MavenGoal("validate")
    @MavenOption(MavenCLIOptions.BATCH_MODE)
    @MavenOption(MavenCLIOptions.QUIET)
    //@MavenOption(MavenCLIOptions.VERBOSE)
    @MavenRepository   // We can share the local repository because this plugin does not use it.
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class set_004 {

        @MavenTest
        @Order(1)
        void the_first_test_case(MavenExecutionResult result) {
            assertThat(result).isSuccessful();
        }

    }

    @Nested
    @MavenProject      // Use same Maven project for all tests in this set.
    @MavenGoal("validate")
    @MavenOption(MavenCLIOptions.BATCH_MODE)
    @MavenOption(MavenCLIOptions.QUIET)
    @MavenOption(MavenCLIOptions.VERBOSE)
    @MavenRepository   // We can share the local repository because this plugin does not use it.
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class set_005 {

        @MavenTest
        @Order(1)
        void the_first_test_case(MavenExecutionResult result) {
            assertThat(result).isFailure().out().error().contains("Failure in parameter 'excludes'. Project 'com.github.mikkoi:non-existing-project' not found in build");
        }

    }

    @Nested
    @MavenProject      // Use same Maven project for all tests in this set.
    @MavenGoal("validate")
    @MavenOption(MavenCLIOptions.BATCH_MODE)
    @MavenOption(MavenCLIOptions.QUIET)
    @MavenOption(MavenCLIOptions.VERBOSE)
    @MavenRepository   // We can share the local repository because this plugin does not use it.
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class set_006 {

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
