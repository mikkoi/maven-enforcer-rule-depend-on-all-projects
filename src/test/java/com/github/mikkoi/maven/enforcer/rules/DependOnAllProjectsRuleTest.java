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

import java.util.Collection;
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

    public static Dependency createDependency(String groupId, String artifactId, String version, String type, String scope) {
        final Dependency dependency = new Dependency();
        dependency.setGroupId(groupId);
        dependency.setArtifactId(artifactId);
        dependency.setVersion(version);
        dependency.setType(type);
        dependency.setScope(scope);
        return dependency;
    }

    public static MavenProject createTestMavenProjectWithDependencies(String groupId, String artifactId, String version, String packaging, Collection<Dependency> dependencies) {
        final Model model = new Model();
        model.setGroupId(groupId);
        model.setArtifactId(artifactId);
        model.setVersion(version);
        model.setPackaging(packaging);
        for (Dependency dependency : dependencies) {
            model.addDependency(dependency);
        }
        return new MavenProject(model);
    }

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

    public static ProjectDependencyGraph createTestProjectDependencyGraph(List<MavenProject> sortedProjects) {
        return new ProjectDependencyGraph() {
            @Override
            public List<MavenProject> getAllProjects() {
                return new ArrayList<>();
            }

            @Override
            public List<MavenProject> getSortedProjects() {
                return sortedProjects;
            }

            @Override
            public List<MavenProject> getDownstreamProjects(MavenProject project,
                                                            boolean transitive) {
                return new ArrayList<>();
            }

            @Override
            public List<MavenProject> getUpstreamProjects(MavenProject project,
                                                          boolean transitive) {
                return new ArrayList<>();
            }
        };
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

        rule.setExcludes(null);
        rule.setIncludes(null);
        rule.setErrorIfUnknownProject(null);
        rule.setIncludeRootProject(null);
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

        // We create our own ProjectDependencyGraph...
        // In this case, the reactor contains proj-a, proj-b and proj-z (aggregate)
        // The root project depends on proj-a, proj-b and proj-z
        // The current project is proj-z, and it depends on all projects, including the root project
        // (which setup is a bit odd, but should be supported)
        //
        final Dependency dependencyProjA = createDependency(
            "com.github.mikkoi", "proj-a", "1.0.0", "jar", "compile"
        );
        final Dependency dependencyProjB = createDependency(
            "com.github.mikkoi", "proj-b", "1.0.0", "jar", "compile"
        );
        final Dependency dependencyProjZ = createDependency(
            "com.github.mikkoi", "proj-z", "1.0.0", "pom", "compile"
        );
        final Dependency dependencyProjRoot = createDependency(
            "com.github.mikkoi", "proj-root", "1.0.0", "pom", "compile"
        );

        final List<Dependency> projRootDependencies = new ArrayList<>();
        projRootDependencies.add(dependencyProjA);
        projRootDependencies.add(dependencyProjB);
        projRootDependencies.add(dependencyProjZ);
        final List<Dependency> projZDependencies = new ArrayList<>();
        projZDependencies.add(dependencyProjA);
        projZDependencies.add(dependencyProjB);
        projZDependencies.add(dependencyProjRoot);

        MavenProject mavenProjectA = createTestMavenProjectWithDependencies(
            "com.github.mikkoi", "proj-a", "1.0.0", "jar", new ArrayList<>()
        );
        MavenProject mavenProjectB = createTestMavenProjectWithDependencies(
            "com.github.mikkoi", "proj-b", "1.0.0", "jar", new ArrayList<>()
        );
        MavenProject mavenProjectZ = createTestMavenProjectWithDependencies(
            "com.github.mikkoi", "proj-z", "1.0.0", "pom", projZDependencies
        );
        MavenProject mavenProjectRoot = createTestMavenProjectWithDependencies(
            "com.github.mikkoi", "proj-root", "1.0.0", "pom", projRootDependencies
        );
        // List of projects in the reactor build in topological (sorted) order
        // in which they should be built.
        final List<MavenProject> projects = new ArrayList<>();
        projects.add(mavenProjectA);
        projects.add(mavenProjectB);
        projects.add(mavenProjectZ);
        // The root project depends on proj-a, proj-b and proj-z
        projects.add(mavenProjectRoot);

        ProjectDependencyGraph projectDependencyGraph = createTestProjectDependencyGraph(projects);
        mavenSession.setProjectDependencyGraph(projectDependencyGraph);
        mavenSession.setCurrentProject(mavenProjectZ);
        assertThatNoException().isThrownBy(rule::execute);

        // Now, we remove proj-root from proj-z dependencies
        projZDependencies.clear();
        projZDependencies.add(dependencyProjA);
        projZDependencies.add(dependencyProjB);
        projects.set(2, createTestMavenProjectWithDependencies(
            "com.github.mikkoi", "proj-z", "1.0.0", "pom", projZDependencies
        ));
        mavenSession.setProjectDependencyGraph(createTestProjectDependencyGraph(projects));
        mavenSession.setCurrentProject(mavenProjectZ);
        rule.setIncludeRootProject("false");
        assertThatNoException().isThrownBy(rule::execute);

    }
}
