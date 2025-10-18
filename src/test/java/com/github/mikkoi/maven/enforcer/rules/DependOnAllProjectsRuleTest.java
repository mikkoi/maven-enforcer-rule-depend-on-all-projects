package com.github.mikkoi.maven.enforcer.rules;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;

import org.apache.maven.enforcer.rule.api.EnforcerLogger;
import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
import org.apache.maven.execution.ProjectDependencyGraph;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.PlexusContainer;
import org.mockito.Mockito;

import java.util.Collections;

import org.apache.maven.execution.DefaultMavenExecutionRequest;
import org.apache.maven.execution.DefaultMavenExecutionResult;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionResult;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for DependOnAllProjects rule.
 * In this test class, we create a complete dependOnAllProjects rule instance.
 */
class DependOnAllProjectsRuleTest {

    public static MavenProject createTestMavenProject() {
        final Model model = new Model();
        model.setGroupId("com.github.mikkoi");
        model.setArtifactId("test-artifact");
        model.setVersion("1.0.0-SNAPSHOT");
        model.setPackaging("jar");
        final Dependency dependency = new Dependency();
        dependency.setGroupId("junit");
        dependency.setArtifactId("junit");
        dependency.setVersion("4.12");
        dependency.setScope("test");
        model.addDependency(dependency);
        model.addDependency(new Dependency() {{
            setGroupId("org.apache.maven");
            setArtifactId("maven-core");
            setVersion("3.6.3");
        }});
        return new MavenProject(model);
    }

    public static MavenSession createTestMavenSession() {
        final MavenProject project = createTestMavenProject();
        MavenExecutionRequest request = new DefaultMavenExecutionRequest();
        MavenExecutionResult result = new DefaultMavenExecutionResult();
        PlexusContainer container = Mockito.mock(PlexusContainer.class);
        ProjectDependencyGraph projectDependencyGraph = Mockito.mock(ProjectDependencyGraph.class);
        List<MavenProject> reactorProjects = new ArrayList<>();
        reactorProjects.add(project);
        reactorProjects.add(new MavenProject(new Model() {{
            setGroupId("com.github.mikkoi");
            setArtifactId("test-artifact-a");
            setVersion("0.1.0");
            setPackaging("jar");
        }}));
        reactorProjects.add(new MavenProject(new Model() {{
            setGroupId("com.github.mikkoi");
            setArtifactId("test-artifact-b");
            setVersion("0.1.0");
            setPackaging("jar");
        }}));
        Mockito.when(projectDependencyGraph.getSortedProjects())
            .thenReturn(reactorProjects);

        @SuppressWarnings("deprecation")
        MavenSession mavenSession = new MavenSession(
            container,
            request,
            result,
            Collections.singletonList(project)
        );
        mavenSession.setProjectDependencyGraph(projectDependencyGraph);
        return mavenSession;
    }

    public static EnforcerLogger createTestLogger() {
        return Mockito.mock(EnforcerLogger.class);
    }
    @Test
    void testProjectsContains2() throws EnforcerRuleException {
        final MavenSession mavenSession = createTestMavenSession();
        DependOnAllProjects rule = new DependOnAllProjects(mavenSession);
        rule.setLog(createTestLogger());
        rule.validateAndPrepareParameters();
        assertThat(rule).hasToString("DependOnAllProjects["
            + "includes=[*];"
            + "excludes=[];"
            + "includeRootProject=false;"
            + "errorIfUnknownProject=false"
            + "]");

        final List<String> includes = new ArrayList<>();
        includes.add("*");
        includes.add("com.github.mikkoi:*");
        final List<String> excludes = new ArrayList<>();
        excludes.add("com.github.mikkoi:test-artifact-b");
        rule.setIncludes(includes);
        rule.setExcludes(excludes);
        rule.setErrorIfUnknownProject("true");
        rule.setIncludeRootProject("true");
        rule.validateAndPrepareParameters();
        assertThat(rule).hasToString("DependOnAllProjects["
            + "includes=[*, com.github.mikkoi:*];"
            + "excludes=[com.github.mikkoi:test-artifact-b];"
            + "includeRootProject=true;"
            + "errorIfUnknownProject=true"
            + "]");

        MavenProject projectA = new MavenProject();
        projectA.setGroupId("com.github.mikkoi");
        projectA.setArtifactId("test-artifact-a");
        projectA.setVersion("0.1.0");
        MavenProject projectB = new MavenProject();
        projectB.setGroupId("com.github.mikkoi");
        projectB.setArtifactId("test-artifact-b");
        projectB.setVersion("0.1.2");

        assertThat(rule.isIncluded(projectA)).isTrue();
        assertThat(rule.isIncluded(projectB)).isFalse();

        includes.clear();
        includes.add("com.github:*");
        rule.setIncludes(includes);
        rule.setExcludes(excludes);
        assertThat(rule.isIncluded(projectA)).isFalse();
        assertThat(rule.isIncluded(projectB)).isFalse();

        includes.clear();
        includes.add("com.github:*");
        excludes.clear();
        excludes.add("com.github.mikkoi:test-artifact-a");
        excludes.add("com.github.mikkoi:test-artifact-b");
        rule.setIncludes(includes);
        rule.setExcludes(excludes);
        assertThatNoException().isThrownBy(rule::execute);

        includes.clear();
        excludes.clear();
        rule.setIncludes(includes);
        rule.setExcludes(excludes);
        assertThatExceptionOfType(EnforcerRuleException.class)
            .isThrownBy(rule::execute)
            .withMessageContaining("Project 'com.github.mikkoi:test-artifact' is missing dependency 'com.github.mikkoi:test-artifact-a:jar'.\n"
                + "Project 'com.github.mikkoi:test-artifact' is missing dependency 'com.github.mikkoi:test-artifact-b:jar'.");
    }

}
