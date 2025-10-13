package com.github.mikkoi.maven.enforcer.rules;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.maven.enforcer.rule.api.AbstractEnforcerRule;
import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Maven Enforcer Custom Rule.
 */
@Named("dependOnAllProjects")
public class DependOnAllProjects extends AbstractEnforcerRule {

    /**
     * Constant value
     * Size of indentation inside a &lt;dependency>&gt; definition.
     */
    private static final String INDENT_DEPENDENCY = "    ";
    /**
     * Constant values for faking boolean parameters.
     */
    private static final String FALSE = "false";
    private static final String TRUE = "true";
    /**
     * Inject needed Maven component.
      */
    private final MavenSession mavenSession;
    /**
     * Include by project [groupId:]artifactId[:packagingType].
     * Default value: all projects included.
     */
    private List<String> includes;
    /**
     * Exclude by project [groupId:]artifactId[:packagingType].
     * Default value: No projects excluded.
     * If includes list contains any items, they are evaluated first.
     * Then excludes are excluded from them.
     */
    private List<String> excludes;
    /**
     * Error if unknown project in includes/excludes.
     * If a wildcard (*) is used in the name, this parameter has no effect.
     */
    @SuppressWarnings("unused") // Not actually unused. Set via Plexus/Sisu Container.
    private String errorIfUnknownProject;
    /**
     * Include Maven root project.
     */
    @SuppressWarnings("unused") // Not actually unused. Set via Plexus/Sisu Container.
    private String includeRootProject;

    /**
     * Constructor.
     *
     * @param session            Initialized MavenSession object.
     */
    @Inject
    @SuppressFBWarnings
    public DependOnAllProjects(MavenSession session) {
        this.mavenSession = Objects.requireNonNull(session);
    }

    /**
     * Set includes.
     * @param includes the includes
     */
    @Inject
    public void setIncludes(@Nullable List<String> includes) {
        if ( includes == null ) {
            this.includes = new ArrayList<>();
        } else {
            this.includes = new ArrayList<>(includes);
        }
    }

    /**
     * Set excludes.
     * @param excludes the excludes
     */
    @Inject
    public void setExcludes(@Nullable List<String> excludes) {
        if ( excludes == null ) {
            this.excludes = new ArrayList<>();
        } else {
            this.excludes = new ArrayList<>(excludes);
        }
    }

    /**
     * Set errorIfUnknownProject.
     * @param errorIfUnknownProject the errorIfUnknownProject
     */
    @Inject
    public void setErrorIfUnknownProject(String errorIfUnknownProject) {
        if ( errorIfUnknownProject != null ) {
            this.errorIfUnknownProject = errorIfUnknownProject;
        } else {
            this.errorIfUnknownProject = FALSE;
        }
    }

    /**
     * Set includeRootProject.
     * @param includeRootProject the includeRootProject
     */
    @Inject
    public void setIncludeRootProject(String includeRootProject) {
        if ( includeRootProject != null ) {
            this.includeRootProject = includeRootProject;
        } else {
            this.includeRootProject = FALSE;
        }
    }

    /**
     * Format Dependency object to XML snippet.
     * User can simply copy-paste this to the project.
     * @param dependency Dependency object
     * @param indent     Indentation string, e.g. "    "
     * @return Formatted XML snippet
     */
    public static String formatDependency(Dependency dependency, String indent) {
        final String newLine = System.lineSeparator();
        final StringBuilder sb = new StringBuilder();
        sb.append("<dependency>").append(newLine);
        sb.append(indent).append(String.format("<groupId>%s</groupId>", dependency.getGroupId()))
            .append(newLine);
        sb.append(indent)
            .append(String.format("<artifactId>%s</artifactId>", dependency.getArtifactId()))
            .append(newLine);
        if (!"jar".equals(dependency.getType())) {
            sb.append(indent).append(String.format("<type>%s</type>", dependency.getType()))
                .append(newLine);
        }
        sb.append("</dependency>");
        return sb.toString();
    }

    /**
     * Convert string for matching.
     *
     * @param s String
     * @return converted string
     */
    public static String convertStringForMatching(String s) {
        String t = s.replace(".", "\\.");
        t = t.replace("*", ".*");
        if (!t.contains(":")) {
            t = ".*:" + t + ":.*";
        }
        if (Arrays.stream(t.split(":")).count() < 3) {
            t = t + ":.*";
        }
        return t;
    }

    /**
     * Compare two MavenProject objects.
     * GroupId, ArtifactId, Version and Packaging must match.
     *
     * @param a MavenProject a
     * @param b MavenProject b
     * @return true if projects are equal
     */
    public static boolean projectsAreEquals(MavenProject a, MavenProject b) {
        return a.getGroupId().equals(b.getGroupId())
            && a.getArtifactId().equals(b.getArtifactId()) && a.getVersion().equals(b.getVersion())
            && a.getPackaging().equals(b.getPackaging());
    }

    /**
     * Compare two Dependency objects.
     * GroupId, ArtifactId, Version and Type must match.
     *
     * @param a Dependency a
     * @param b Dependency b
     * @return true if dependencies are equal
     */
    public static boolean dependenciesAreEquals(Dependency a, Dependency b) {
        return a.getGroupId().equals(b.getGroupId())
            && a.getArtifactId().equals(b.getArtifactId()) && a.getVersion().equals(b.getVersion())
            && a.getType().equals(b.getType());
    }

    /**
     * Does the list (Iterable) of Dependency objects contain the MavenProject?
     *
     * @param projects Iterable of Dependency objects
     * @param project  a Maven project object
     * @return true if project is found
     */
    public static boolean dependenciesContains(Iterable<Dependency> projects,
                                               MavenProject project) {
        for (Dependency p : projects) {
            if (dependenciesAreEquals(p, projectToDependency(project))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Does any of the projects in Maven Reactor build match with this project name?
     * Name must not have a wildcard!
     *
     * @param projects    Iterable of MavenProject objects
     * @param projectName Project name, e.g. "artifactId", "groupId:artifactId", "groupId:artifactId:packingType".
     * @return Boolean
     */
    public static boolean projectsContains(Iterable<MavenProject> projects, String projectName) {
        List<String> ids = Arrays.asList(projectName.split(":"));
        for (MavenProject project : projects) {
            if (ids.size() == 1) {
                if (project.getArtifactId().equals(ids.get(0))) {
                    return true;
                }
            } else if (ids.size() == 2) {
                if (project.getGroupId().equals(ids.get(0))
                    && project.getArtifactId().equals(ids.get(1))) {
                    return true;
                }
            } else {
                assert ids.size() == 3;
                if (project.getGroupId().equals(ids.get(0))
                    && project.getArtifactId().equals(ids.get(1))
                    && project.getPackaging().equals(ids.get(2))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Convert MavenProject to Dependency object.
     *
     * @param mavenProject MavenProject object
     * @return Dependency object
     */
    public static Dependency projectToDependency(MavenProject mavenProject) {
        Dependency d = new Dependency();
        d.setGroupId(mavenProject.getGroupId());
        d.setArtifactId(mavenProject.getArtifactId());
        d.setVersion(mavenProject.getVersion());
        d.setType(mavenProject.getPackaging());
        return d;
    }

    /**
     * Match projects with includes and excludes.
     * Includes are * by default. Then excludes are excluded from the includes.
     *
     * @param includes     Included projects. Default: *
     * @param excludes     Excluded projects. Default: none
     * @param mavenProject Initialized MavenProject object.
     * @return True or false.
     */
    public static boolean isProjectIncluded(List<String> includes, List<String> excludes,
                                            MavenProject mavenProject) {
        String projectId =
            String.format("%s:%s:%s", mavenProject.getGroupId(), mavenProject.getArtifactId(),
                mavenProject.getPackaging());

        // Match from the end of the id, artifactId alone is enough.
        Predicate<String> predicateForProjectId =
            s -> projectId.matches(convertStringForMatching(s));
        return includes.stream().anyMatch(predicateForProjectId)
            && excludes.stream().noneMatch(predicateForProjectId);
    }

    /**
     * Validate parameters provided via properties
     * either on the command line or using configuration element in pom.
     *
     * @throws EnforcerRuleException if parameter validation fails.
     */
    void validateAndPrepareParameters() throws EnforcerRuleException {
        getLog().debug("includes=" + includes);
        getLog().debug("excludes=" + excludes);
        getLog().debug("errorIfUnknownProject=" + errorIfUnknownProject);
        getLog().debug("includeRootProject=" + includeRootProject);

        final List<MavenProject> reactorProjects =
            mavenSession.getProjectDependencyGraph().getSortedProjects();
        getLog().debug("reactorProjects=" + reactorProjects);

        /* There is a bug in Maven/Sisu/Plexus container, which sets includes to a list with one empty string,
         * if the parameter is not set. So we need to check for this case and convert it to an empty list.
         * Then we can add the default value of "*".
         */
        if ( (includes == null) || (includes.size() == 1 && "".equals(includes.get(0))) ) {
            includes = new ArrayList<>();
        }
        if (excludes == null) {
            excludes = new ArrayList<>();
        } else if (excludes.size() == 1 && "".equals(excludes.get(0))) {
            excludes = new ArrayList<>();
        }
        getLog().debug(String.format("Parameter includes.size: %d", includes.size()));
        for (String a : includes) {
            getLog().debug(String.format("Check include '%s'", a));
            if (a == null) {
                throw new EnforcerRuleException(
                    "Failure in parameter 'includes'. String is null");
            }
            if (a.matches("^[\t\n ]+$")) {
                throw new EnforcerRuleException(
                    String.format("Failure in parameter 'includes'. String contains only whitespace: '%s'", a));
            }
            List<String> ids = Arrays.asList(a.split(":"));
            if (ids.size() > 3) {
                throw new EnforcerRuleException(
                    "Failure in parameter 'includes'. String is invalid");
            }
            /* If there is a wildcard, we cannot check if the project exists in the build.
             * So we skip the check in this case.
             */
            if (TRUE.equals(errorIfUnknownProject) && !a.contains("*")
                && !projectsContains(reactorProjects, a)) {
                throw new EnforcerRuleException(String.format(
                    "Failure in parameter 'includes'. Project '%s' not found in build", a));
            }
        }
        if (includes.isEmpty()) {
            includes.add("*");
        }

        getLog().debug(String.format("Parameter excludes.size: %d", excludes.size()));
        for (String a : excludes) {
            getLog().debug(String.format("Check exclude '%s'", a));
            if (a == null) {
                throw new EnforcerRuleException(
                    "Failure in parameter 'excludes'. String is null");
            }
            if (a.matches("^[\t\n ]+$")) {
                throw new EnforcerRuleException(
                    String.format("Failure in parameter 'excludes'. String contains only whitespace: '%s'", a));
            }
            List<String> ids = Arrays.asList(a.split(":"));
            if (ids.size() > 3) {
                throw new EnforcerRuleException(
                    "Failure in parameter 'excludes'. String is invalid");
            }
            /* If there is a wildcard, we cannot check if the project exists in the build.
             * So we skip the check in this case.
             */
            if (TRUE.equals(errorIfUnknownProject) && !a.contains("*")
                && !projectsContains(reactorProjects, a)) {
                throw new EnforcerRuleException(String.format(
                    "Failure in parameter 'excludes'. Project '%s' not found in build", a));
            }
        }

        if (errorIfUnknownProject == null || errorIfUnknownProject.isEmpty()) {
            errorIfUnknownProject = FALSE;
        }
        if (includeRootProject == null || includeRootProject.isEmpty()) {
            includeRootProject = FALSE;
        }
        if (!TRUE.equals(errorIfUnknownProject) && !FALSE.equals(errorIfUnknownProject)) {
            throw new EnforcerRuleException(
                String.format("Failure in parameter 'errorIfUnknownProject'. Must be 'true' or 'false': '%s'", errorIfUnknownProject));
        }
        if (!TRUE.equals(includeRootProject) && !FALSE.equals(includeRootProject)) {
            throw new EnforcerRuleException(
                String.format("Failure in parameter 'includeRootProject'. Must be 'true' or 'false': '%s'", includeRootProject));
        }

        getLog().debug("includes(resolved)=" + includes);
        getLog().debug("excludes(resolved)=" + excludes);
        getLog().debug("errorIfUnknownProject(resolved)=" + errorIfUnknownProject);
        getLog().debug("includeRootProject(resolved)=" + includeRootProject);
    }

    /**
     * The rule logic.
     * Collect all projects in the build and filter according to includes/excludes.
     * Match the list with the dependencies of the current project.
     * If the two lists do not match, Raise EnforcerRuleException
     *
     * @throws EnforcerRuleException if rule fails.
     */
    public void dependOnAllProjects() throws EnforcerRuleException {
        List<MavenProject> includedProjects = new ArrayList<>();
        MavenProject currentProject = mavenSession.getCurrentProject();
        getLog().debug(String.format("Current Project: %s:%s", currentProject.getGroupId(),
            currentProject.getArtifactId()));

        getLog().debug("Iterate through all projects in Maven Dependency Graph, i.e. the build.");
        mavenSession.getProjectDependencyGraph().getSortedProjects().forEach(project -> {
            String projectId =
                String.format("%s:%s:%s", project.getGroupId(), project.getArtifactId(),
                    project.getVersion());
            getLog().debug("    " + projectId);

            if (isIncluded(project)) {
                if (projectsAreEquals(project, currentProject) || (!TRUE.equals(this.includeRootProject)
                    && projectsAreEquals(project, mavenSession.getTopLevelProject()))) {
                    getLog().debug("Filter out project: "
                        + String.format("%s:%s", project.getGroupId(), project.getArtifactId()));
                } else {
                    includedProjects.add(project);
                }
            }
        });
        getLog().debug("includedProjects=%s" + includedProjects);
        List<MavenProject> missingProjects = new ArrayList<>();
        for (MavenProject project : includedProjects) {
            @SuppressWarnings("unchecked") List<Dependency> dependencies =
                currentProject.getDependencies();
            if (!dependenciesContains(dependencies, project)) {
                missingProjects.add(project);
            }
        }
        if (!missingProjects.isEmpty()) {
            List<String> errors = new ArrayList<>(missingProjects.size());
            for (MavenProject missingProject : missingProjects) {
                errors.add(String.format("Project '%s:%s' is missing dependency '%s:%s:%s'.",
                    currentProject.getGroupId(), currentProject.getArtifactId(),
                    missingProject.getGroupId(), missingProject.getArtifactId(),
                    missingProject.getPackaging()));
            }
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("Missing definitions from the project '%s:%s':",
                currentProject.getGroupId(), currentProject.getArtifactId()));
            sb.append(System.lineSeparator());
            sb.append("<!--     Created by Maven Enforcer rule dependOnAllProjects     --->");
            sb.append(System.lineSeparator());
            for (MavenProject missingProject : missingProjects) {
                sb.append(formatDependency(projectToDependency(missingProject), INDENT_DEPENDENCY));
                sb.append(System.lineSeparator());
            }
            sb.append("<!--     / Created by Maven Enforcer rule dependOnAllProjects     --->");
            errors.add(sb.toString());
            throw new EnforcerRuleException(String.join("\n", errors));
        }
        getLog().debug("End of iterate");
    }

    /**
     * The main entry point for rule.
     *
     * @throws EnforcerRuleException if rule fails.
     */
    @Override
    public void execute() throws EnforcerRuleException {
        MavenProject currentProject = mavenSession.getCurrentProject();
        getLog().debug(String.format("Current Project: %s:%s", currentProject.getGroupId(),
            currentProject.getArtifactId()));
        MavenProject topLevelProject = mavenSession.getTopLevelProject();
        getLog().debug(String.format("Top Level Project: %s:%s", topLevelProject.getGroupId(),
            topLevelProject.getArtifactId()));

        validateAndPrepareParameters();

        dependOnAllProjects();
    }

    /**
     * String representation of the rule.
     * Output is used in verbose Maven logs, can help during investigate problems.
     *
     * @return rule description
     */
    @Override
    public String toString() {
        return String.format(
            "DependOnAllProjects[includes=%s;excludes=%s;includeRootProject=%s;errorIfUnknownProject=%s]",
            includes, excludes, includeRootProject, errorIfUnknownProject);
    }

    boolean isIncluded(MavenProject mavenProject) {
        boolean r = isProjectIncluded(this.includes, this.excludes, mavenProject);
        getLog().debug(String.format("isIncluded(%s:%s:%s:%s): %b", mavenProject.getGroupId(),
            mavenProject.getArtifactId(), mavenProject.getVersion(), mavenProject.getPackaging(),
            r));
        return r;
    }

}
