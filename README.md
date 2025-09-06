# Maven Enforcer Plugin Rule dependOnAllProjects

# Introduction

This is a custom rule to Maven Enforcer.

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

You can also define exceptions to this rule with the following parameters:

* includes
* excludes
* includeRoot

## License

Apache License Version 2.0, January 2004
