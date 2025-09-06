package com.github.mikkoi.maven_enforcer_plugin.rule;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.maven.enforcer.rule.api.AbstractEnforcerRule;
import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;
import org.apache.maven.rtinfo.RuntimeInformation;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Maven Enforcer Custom Rule
 */
@Named("dependOnAllProjects")
public class DependOnAllProjects extends AbstractEnforcerRule {

    /**
     * Include by project [groupId:]artifactId[:packagingType].
     * Default value: all projects included.
     */
//    @Parameter(property = "projects" + ".addInternal" + ".includes")
    private List<String> includes;

    /**
     * Exclude by project [groupId:]artifactId[:packagingType].
     * Default value: No projects excluded.
     * If includes list contains any items, they are evaluated first.
     * Then excludes are excluded from them.
     */
//    @Parameter(property = "projects" + ".addInternal" + ".excludes")
    List<String> excludes;

    /**
     * Error if unknown project in includes/excludes.
     * If a wildcard (*) is used in the name, this parameter has no effect.
     */
//    @Parameter(property = "projects" + ".errorIfUnknownProject", defaultValue = "true")
    private boolean errorIfUnknownProject;

    /**
     * Include Maven root project
     */
    private boolean includeRootProject;

    // Inject needed Maven components
    private final MavenProject mavenProject;
    private final MavenSession mavenSession;
    private final RuntimeInformation runtimeInformation;

    @Inject
    @SuppressFBWarnings
    public DependOnAllProjects(MavenProject project, MavenSession session, RuntimeInformation runtimeInformation) {
        this.mavenProject = Objects.requireNonNull(project);
        this.mavenSession = Objects.requireNonNull(session);
        this.runtimeInformation = Objects.requireNonNull(runtimeInformation);
    }

    /**
     * Validate parameters provided via properties
     * either on the command line or using configuration element in pom.
     */
    private void validateAndPrepareParameters() throws EnforcerRuleException {
        getLog().debug("includes=" + includes);
        getLog().debug("excludes=" + excludes);
        getLog().debug("errorIfUnknownProject=" + errorIfUnknownProject);

        if(includes == null) {
            includes = new ArrayList<>();
        }
        if(excludes == null) {
            excludes = new ArrayList<>();
        }
        for ( String a : includes ) {
            if (a == null) {
                throw new EnforcerRuleException("Failure in parameter 'includes'. String is null");
            }
        }
        if (includes.isEmpty()) {
            includes.add("*");
        }

        for ( String a : excludes ) {
            if (a == null) {
                throw new EnforcerRuleException("Failure in parameter 'excludes'. String is null");
            }
            // If the String is a full dependency name without wildcards,
            // check that the dependency exists among the projects in the build.
            // If not, throw EnforcerRuleException.
        }

        getLog().debug("includes(resolved)=" + includes);
        getLog().debug("excludes(resolved)=" + excludes);
        getLog().debug("errorIfUnknownProject=" + errorIfUnknownProject);
    }

    /**
     * The rule logic
     * Collect all projects in the build and filter according to includes/excludes.
     * Match the list with the dependencies of the current project.
     * If the two lists do not match, Raise EnforcerRuleException
     */
    public void dependOnAllProjects(MavenSession mavenSession) throws EnforcerRuleException {
        List<MavenProject> includedProjects = new ArrayList<>();
        MavenProject currentProject = this.mavenSession.getCurrentProject();
        getLog().debug(String.format("Current Project: %s:%s", currentProject.getGroupId(), currentProject.getArtifactId()));

        getLog().debug("Iterate through all projects in Maven Dependency Graph, i.e. the build.");
        mavenSession.getProjectDependencyGraph().getSortedProjects().forEach(project -> {
            String projectId = String.format("%s:%s:%s",
                    project.getGroupId(),
                    project.getArtifactId(),
                    project.getVersion()
            );
            getLog().debug("    " + projectId);

            if(isIncluded(project)) {
//                if(
//                        !projectsAreEquals(project, currentProject)
//                        && !( projectsAreEquals(project, mavenSession.getTopLevelProject()) && !this.includeRootProject )
//                ) {
//                    includedProjects.add(project);
//                }

                if(
                        projectsAreEquals(project, currentProject)
                                || ( !this.includeRootProject && projectsAreEquals(project, mavenSession.getTopLevelProject()) )
                ) {
                    getLog().debug("Filter out project: " + String.format("%s:%s", project.getGroupId(), project.getArtifactId()));
                } else {
                    includedProjects.add(project);
                }
            }
        });
        getLog().debug("includedProjects=%s" + includedProjects);
        List<MavenProject> missingProjects = new ArrayList<>();
        for (MavenProject project : includedProjects) {
            @SuppressWarnings("unchecked") List<Dependency> dependencies = (List<Dependency>) currentProject.getDependencies();
            if(!dependenciesContains(dependencies, project)) {
                missingProjects.add(project);
            }
        }
        if(!missingProjects.isEmpty()) {
            StringBuilder error = new StringBuilder();
            for (MavenProject missingProject : missingProjects) {
                error.append(String.format("Project '%s:%s' missing from dependencies.",
                        missingProject.getGroupId(),
                        missingProject.getArtifactId()
                ));
            }
            throw new EnforcerRuleException(error.toString());
        }
        getLog().debug("End of iterate");
    }

    /**
     * The main entry point for rule.
     */
    @Override
    public void execute() throws EnforcerRuleException {
        MavenProject currentProject = this.mavenSession.getCurrentProject();
        getLog().debug(String.format("Current Project: %s:%s", currentProject.getGroupId(), currentProject.getArtifactId()));
        MavenProject topLevelProject = this.mavenSession.getTopLevelProject();
        getLog().debug(String.format("Top Level Project: %s:%s", topLevelProject.getGroupId(), topLevelProject.getArtifactId()));

        validateAndPrepareParameters();

        dependOnAllProjects(mavenSession);
    }

    /**
     * If your rule is cacheable, you must return a unique id when parameters or conditions
     * change that would cause the result to be different. Multiple cached results are stored
     * based on their id.
     * <p>
     * The easiest way to do this is to return a hash computed from the values of your parameters.
     * <p>
     * If your rule is not cacheable, then you don't need to override this method or return null
     */
    @Override
    public String getCacheId() {
        //no hash on boolean...only parameter so no hash is needed.
//        return Boolean.toString(shouldIfail);

        // Parameters + current project dependencies + all maven projects in the build
        return "1";
    }

    /**
     * A good practice is provided toString method for Enforcer Rule.
     * <p>
     * Output is used in verbose Maven logs, can help during investigate problems.
     *
     * @return rule description
     */
    @Override
    public String toString() {
        return String.format("DependOnAllProjects[includes=%s;excludes=%s;errorIfUnknownProject=%b]", includes, excludes, errorIfUnknownProject);
    }

    private boolean isIncluded(MavenProject mavenProject) {
        boolean r = isProjectIncluded(this.includes, this.excludes, mavenProject);
        getLog().debug(String.format("isIncluded(%s:%s:%s:%s): %b", mavenProject.getGroupId(), mavenProject.getArtifactId(), mavenProject.getVersion(), mavenProject.getPackaging(), r));
        return r;
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
     * Compare groupId, artifactId, version and packagingType.
     */
    public static boolean projectsAreEquals(MavenProject a, MavenProject b) {
        return
                a.getGroupId().equals(b.getGroupId())
                && a.getArtifactId().equals(b.getArtifactId())
                && a.getVersion().equals(b.getVersion())
                && a.getPackaging().equals(b.getPackaging());
    }
    public static boolean dependenciesAreEquals(Dependency a, Dependency b) {
        return
                a.getGroupId().equals(b.getGroupId())
                        && a.getArtifactId().equals(b.getArtifactId())
                        && a.getVersion().equals(b.getVersion())
                        && a.getType().equals(b.getType());
    }

    public static boolean dependenciesContains(Iterable<Dependency> projects, MavenProject project) {
        for(Dependency p : projects) {
            if(dependenciesAreEquals(p, projectToDependency(project))) {
                return true;
            }
        }
        return false;
    }

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
     * @return             True or false.
     */
    public static boolean isProjectIncluded(List<String> includes, List<String> excludes, MavenProject mavenProject) {
        String projectId = String.format("%s:%s:%s", mavenProject.getGroupId(), mavenProject.getArtifactId(), mavenProject.getPackaging());

        // Match from the end of the id, artifactId alone is enough.
        Predicate<String> predicateForProjectId = s -> projectId.matches(convertStringForMatching(s));
        return includes.stream().anyMatch(predicateForProjectId) && excludes.stream().noneMatch(predicateForProjectId);
    }

}
