# Jenkins Gitflow Plugin

The _Jenkins Gitflow Plugin_ provides a job action page in the Jenkins UI that allows to execute _Gitflow_ actions
(see [A successful Git branching model](http://nvie.com/posts/a-successful-git-branching-model)) on Maven projects.

To properly support iterative bugfixing, the model had to be extended. See [Presentation](/doc/presentation/index.html)
for more information.

## Installation

### Requirements

* JDK 1.7.0
* Commit/push rights to the Git repository

## Development

### Hints

Switch off logger console:

    mvn hpi:run -Ddebug.YUI=false

### Maintainer Information

We don't release the _Giflow_ plugin using the _Maven Release plugin_ - we've something better here. The plugin will be
released using itself. We'll currently do this on an internal/custom or even local Jenkins installation until we find a
better way. Releasing to the Jenkins Update Center currently works as follows:

Prerequisite:
[Request upload permissions](https://wiki.jenkins.io/display/JENKINS/Hosting+Plugins#HostingPlugins-Requestuploadpermissions)
for the [Jenkins Releases Maven Repository](https://repo.jenkins-ci.org/releases/) and add the credentials to Your Maven
_settings.xml_.

1. _Start Release_ (or _Start Hotfix_)
2. Test the release (hotfix) and iteratively execute _Test Release_ (or _Test Hotfix_).
3. _Publish Release_ once the release is considered stable.
4. Check out the _master_ branch
5. Run the Maven command

       mvn clean source:jar javadoc:jar deploy -DupdateReleaseInfo=true

Steps 1 - 3 can be executed using the job _gitflow-plugin-gitflow_ in the development Jenkins on this project. Steps 4
and 5 can be executed using the job _gitflow-plugin-deploy_.
