package com.github.mikkoi.maven.enforcer.rules;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Unit tests for DependOnAllProjects.
 */
class DependOnAllProjectsTest {

    @Test
    void testProjectsContains() {
        final List<MavenProject> reactorProjects = new ArrayList<>();
        for (String s : Arrays.asList("mikkoi:proj-a:pom", "mikkoi:proj-b:jar",
            "mikkoi:proj-c:war")) {
            Model model = new Model();
            model.setGroupId(Arrays.asList(s.split(":")).get(0));
            model.setArtifactId(Arrays.asList(s.split(":")).get(1));
            model.setPackaging(Arrays.asList(s.split(":")).get(2));
            reactorProjects.add(new MavenProject(model));
        }
        assertThat(
            DependOnAllProjects.projectsContains(reactorProjects, "mikkoi:proj-a:pom")).isTrue();
        assertThat(DependOnAllProjects.projectsContains(reactorProjects, "mikkoi:proj-a")).isTrue();
        assertThat(DependOnAllProjects.projectsContains(reactorProjects, "proj-a")).isTrue();
        assertThat(DependOnAllProjects.projectsContains(reactorProjects, "*")).isFalse();
        assertThat(DependOnAllProjects.projectsContains(reactorProjects, "mikkoi:*")).isFalse();
        assertThat(DependOnAllProjects.projectsContains(reactorProjects, "mikkoi:*:pom")).isFalse();
        assertThat(DependOnAllProjects.projectsContains(reactorProjects, "*:*:pom")).isFalse();
        assertThat(DependOnAllProjects.projectsContains(reactorProjects, "proj-*")).isFalse();
        assertThat(DependOnAllProjects.projectsContains(reactorProjects, "proj-d")).isFalse();
        assertThat(
            DependOnAllProjects.projectsContains(reactorProjects, "mikkoi:proj-a:war")).isFalse();
    }

    @Test
    void testIsProjectIncluded() {
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

    @Test
    void testConvertStringForMatching() {
        String name1 = DependOnAllProjects.convertStringForMatching("apache");
        assertThat(name1).isEqualTo(".*:apache:.*");

        String name2 = DependOnAllProjects.convertStringForMatching("org.apache.maven:core:jar");
        assertThat(name2).isEqualTo("org\\.apache\\.maven:core:jar");

        String name3 = DependOnAllProjects.convertStringForMatching("org.apache.maven:core");
        assertThat(name3).isEqualTo("org\\.apache\\.maven:core:.*");

        String name4 = DependOnAllProjects.convertStringForMatching("org.apache.*:*");
        assertThat(name4).isEqualTo("org\\.apache\\..*:.*:.*");

        String name5 =
            DependOnAllProjects.convertStringForMatching("com.github.mikkoi:test-artifact");
        assertThat(name5).isEqualTo("com\\.github\\.mikkoi:test-artifact:.*");

    }

    @Test
    void testFormatDependency() {
        Dependency d1 = new Dependency();
        d1.setGroupId("com.github.mikkoi");
        d1.setArtifactId("test-dependency");
        d1.setVersion("1.2.3-TEST");
        d1.setType("bar");
        String e1 =
            "<dependency>\n  <groupId>com.github.mikkoi</groupId>\n  <artifactId>test-dependency</artifactId>\n  <type>bar</type>\n</dependency>";
        assertThat(DependOnAllProjects.formatDependency(d1, "  ")).isEqualTo(e1);

        d1.setType("jar");
        String e2 =
            "<dependency>\n  <groupId>com.github.mikkoi</groupId>\n  <artifactId>test-dependency</artifactId>\n</dependency>";
        assertThat(DependOnAllProjects.formatDependency(d1, "  ")).isEqualTo(e2);
    }

    public static MavenProject createSimpleTestMavenProject(String groupId, String artifactId, String version, String packaging) {
        final Model model = new Model();
        model.setGroupId(groupId);
        model.setArtifactId(artifactId);
        model.setVersion(version);
        model.setPackaging(packaging);
        return new MavenProject(model);
    }

    public static Dependency createSimpleTestDependency(String groupId, String artifactId, String version, String type) {
        final Dependency dependency = new Dependency();
        dependency.setGroupId(groupId);
        dependency.setArtifactId(artifactId);
        dependency.setVersion(version);
        dependency.setType(type);
        return dependency;
    }

    @Test
    void testProjectsAreEquals() {
        MavenProject p1 = createSimpleTestMavenProject("com.example", "my-artifact", "1.0.0", "jar");
        MavenProject p2 = createSimpleTestMavenProject("com.example", "my-artifact", "1.0.0", "jar");
        MavenProject p3 = createSimpleTestMavenProject("com.example", "my-artifact", "1.0.0", "war");
        MavenProject p4 = createSimpleTestMavenProject("com.example", "my-artifact", "1.1.0", "war");
        MavenProject p5 = createSimpleTestMavenProject("com.example", "my-artifact-1", "1.1.0", "war");
        MavenProject p6 = createSimpleTestMavenProject("com.example.one", "my-artifact-1", "1.1.0", "war");

        assertThat(DependOnAllProjects.projectsAreEquals(p1, p2)).isTrue();
        assertThat(DependOnAllProjects.projectsAreEquals(p1, p3)).isFalse();
        assertThat(DependOnAllProjects.projectsAreEquals(p3, p4)).isFalse();
        assertThat(DependOnAllProjects.projectsAreEquals(p4, p5)).isFalse();
        assertThat(DependOnAllProjects.projectsAreEquals(p5, p6)).isFalse();
    }

    @Test
    void testDependenciesContains() {
        Dependency d1 = createSimpleTestDependency("com.example", "my-artifact", "1.0.0", "jar");
        Dependency d2 = createSimpleTestDependency("com.example", "my-artifact", "1.0.0", "jar");
        Dependency d3 = createSimpleTestDependency("com.example", "my-artifact", "1.0.0", "war");
        Dependency d4 = createSimpleTestDependency("com.example", "my-artifact", "1.1.0", "war");

        MavenProject p1 = createSimpleTestMavenProject("com.example", "my-artifact", "1.0.0", "jar");
        MavenProject p2 = createSimpleTestMavenProject("com.example.two", "my-artifact", "1.0.0", "jar");

        List<Dependency> dependencies = new ArrayList<>();
        dependencies.add(d1);
        dependencies.add(d2);
        dependencies.add(d3);
        dependencies.add(d4);
        assertThat(DependOnAllProjects.dependenciesContains(dependencies, p1)).isTrue();
        assertThat(DependOnAllProjects.dependenciesContains(dependencies, p2)).isFalse();
    }
}
