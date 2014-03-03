package org.jenkinsci.plugins.gitflow.action;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.gitclient.GitClient;
import org.jenkinsci.plugins.gitflow.GitflowBuildWrapper;
import org.jenkinsci.plugins.gitflow.GitflowPluginProperties;
import org.jenkinsci.plugins.gitflow.action.buildtype.AbstractBuildTypeAction;
import org.jenkinsci.plugins.gitflow.action.buildtype.BuildTypeActionFactory;

import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.plugins.git.GitSCM;

import jenkins.model.Jenkins;

/**
 * Abstract base class for the different Gitflow actions to be executed - before and after the main build.
 *
 * @author Marc Rohlfs, Silpion IT-Solutions GmbH - rohlfs@silpion.de
 */
public abstract class AbstractGitflowAction<T extends AbstractBuild<?, ?>> extends AbstractActionBase<T> {

    private static final String MSG_CLEAN_WORKING_DIRECTORY = "Ensuring clean working/checkout directory";

    protected final AbstractBuildTypeAction<?> buildTypeAction;
    protected final GitClient git;

    protected final GitflowPluginProperties gitflowPluginProperties;

    /**
     * Initialises a new Gitflow action.
     *
     * @param build the <i>Start Release</i> build that is in progress.
     * @param launcher can be used to launch processes for this build - even if the build runs remotely.
     * @param listener can be used to send any message.
     * @throws IOException if an error occurs that causes/should cause the build to fail.
     * @throws InterruptedException if the build is interrupted during execution.
     */
    protected AbstractGitflowAction(final T build, final Launcher launcher, final BuildListener listener) throws IOException, InterruptedException {
        super(build, listener);

        final GitSCM gitSCM = (GitSCM) build.getProject().getScm();
        this.git = gitSCM.createClient(listener, build.getEnvironment(listener), build);

        this.buildTypeAction = BuildTypeActionFactory.newInstance(build, launcher, listener);
        this.gitflowPluginProperties = new GitflowPluginProperties(build.getProject());
    }

    /**
     * Returns the value for the specified parameter from the provided parameters. If the value is emtpy or contains only whitespaces,
     * an {@link IOException} is thrown (note that only an @{@link IOException} prpoperly causes a build to fail).
     *
     * @param parameters the parameter map containing the entry with the requested value.
     * @param parameterName the name of the requested parameter.
     * @return the requested parameter value - if not blank.
     * @throws IOException if the requested parameter value is blank.
     */
    protected static String getParameterValueAssertNotBlank(final Map<String, String> parameters, final String parameterName) throws IOException {
        final String value = parameters.get(parameterName).trim();
        if (StringUtils.isBlank(value)) {
            throw new IOException(MessageFormat.format("{0} must be set with a non-empty value", parameterName));
        } else {
            return value;
        }
    }

    /**
     * Returns the build wrapper descriptor.
     *
     * @return the build wrapper descriptor.
     */
    protected static GitflowBuildWrapper.DescriptorImpl getBuildWrapperDescriptor() {
        return (GitflowBuildWrapper.DescriptorImpl) Jenkins.getInstance().getDescriptor(GitflowBuildWrapper.class);
    }

    /**
     * Runs the Gitflow actions that must be executed before the main build.
     *
     * @throws IOException if an error occurs that causes/should cause the build to fail.
     * @throws InterruptedException if the build is interrupted during execution.
     */
    public final void beforeMainBuild() throws IOException, InterruptedException {
        this.cleanCheckout();
        this.beforeMainBuildInternal();
    }

    /**
     * Runs the Gitflow actions that must be executed after the main build.
     *
     * @throws IOException if an error occurs that causes/should cause the build to fail.
     * @throws InterruptedException if the build is interrupted during execution.
     */
    public final void afterMainBuild() throws IOException, InterruptedException {
        this.afterMainBuildInternal();
    }

    /**
     * Runs the Gitflow actions that must be executed before the main build.
     *
     * @throws IOException if an error occurs that causes/should cause the build to fail.
     * @throws InterruptedException if the build is interrupted during execution.
     */
    protected abstract void beforeMainBuildInternal() throws IOException, InterruptedException;

    /**
     * Runs the Gitflow actions that must be executed after the main build.
     *
     * @throws IOException if an error occurs that causes/should cause the build to fail.
     * @throws InterruptedException if the build is interrupted during execution.
     */
    protected abstract void afterMainBuildInternal() throws IOException, InterruptedException;

    /**
     * Adds the provided files to the Git stages - executing {@code git add [file1] [file2] ...}.
     * <p/>
     * TODO Instead of adding the modified files manually, it would be more reliable to ask the Git client for the files that have been mofified and add those.
     * Unfortunately the {@link hudson.plugins.git.GitSCM GitSCM} class doesn't offer a method to get the modified files. We might file a feature request and/or
     * implement it ourselves and then do a pull request on GitHub. The method to be implemented should execute something like {@code git ls-files -m}).
     *
     * @param files the files to be staged.
     * @throws InterruptedException if the build is interrupted during execution.
     */
    protected void addFilesToGitStage(final List<String> files) throws InterruptedException {
        for (final String file : files) {
            this.git.add(file);
        }
    }

    /**
     * Before entering the {@link #beforeMainBuildInternal()}, the checkout directory is cleaned up so that there a no modified files.
     *
     * @throws InterruptedException if the build is interrupted during execution.
     */
    protected void cleanCheckout() throws InterruptedException {
        this.consoleLogger.println(this.getConsoleMessagePrefix() + MSG_CLEAN_WORKING_DIRECTORY);
        this.git.clean();
    }

    /**
     * Returns the action-specific prefix for console messages.
     *
     * @return the action-specific prefix for console messages.
     */
    protected abstract String getConsoleMessagePrefix();
}
