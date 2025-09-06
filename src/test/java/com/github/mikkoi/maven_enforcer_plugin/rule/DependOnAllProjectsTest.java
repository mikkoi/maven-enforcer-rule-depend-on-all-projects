package com.github.mikkoi.maven_enforcer_plugin.rule;

import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class DependOnAllProjectsTest {

    @Test
    public void testIsProjectIncluded() {
        final MavenProject project = new MavenProject();

        List<String> includes = new ArrayList<>();
        List<String> excludes = new ArrayList<>();

        includes.add("*");
        project.setGroupId("com.github.mikkoi");
        project.setArtifactId("test-artifact");
        assertThat(DependOnAllProjects.isProjectIncluded(includes, excludes, project)).isTrue();

        includes.add("com.github.mikkoi:*");
        excludes.add("com.github.mikkoi:test-artifact");

        project.setGroupId("com.github.mikkoi");
        project.setArtifactId("test-artifact");
        assertThat(DependOnAllProjects.isProjectIncluded(includes, excludes, project)).isFalse();

        project.setArtifactId("test-artifact-2");
        assertThat(DependOnAllProjects.isProjectIncluded(includes, excludes, project)).isTrue();

        excludes.add("com.gitlab.other:other-artifact");
        project.setArtifactId("other-artifact");
        assertThat(DependOnAllProjects.isProjectIncluded(includes, excludes, project)).isTrue();

        project.setGroupId("com.gitlab.other");
        assertThat(DependOnAllProjects.isProjectIncluded(includes, excludes, project)).isFalse();

        excludes.add("com.gitlab.second:*-other-artifact");
        project.setGroupId("com.gitlab.second");
        project.setArtifactId("diff-other-artifact");
        assertThat(DependOnAllProjects.isProjectIncluded(includes, excludes, project)).isFalse();

        project.setArtifactId("artifact-something");
        assertThat(DependOnAllProjects.isProjectIncluded(includes, excludes, project)).isTrue();
    }

}
