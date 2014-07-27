package org.jenkinsci.plugins.gitflow.action;

import java.io.IOException;
import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.gitflow.cause.NoGitflowCause;
import org.jenkinsci.plugins.gitflow.data.RemoteBranch;
import org.jenkinsci.plugins.gitflow.gitclient.GitClientDelegate;

import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.plugins.git.GitTagAction;

/**
 * This class executes some actions that are required when the <i>Gitflow</i> plugin is configured for a project and the standard (non-Gitflow) job is started.
 *
 * @param <B> the build in progress.
 * @author Marc Rohlfs, Silpion IT-Solutions GmbH - rohlfs@silpion.de
 */
public class NoGitflowAction<B extends AbstractBuild<?, ?>> extends AbstractGitflowAction<B, NoGitflowCause> {

    private static final String ACTION_NAME = "";
    private static final String CONSOLE_MESSAGE_PREFIX = "Gitflow - " + ACTION_NAME + ": ";

    /**
     * Initialises a new action for a non-Gitflow build.
     *
     * @param build the <i>Publish Release</i> build that is in progress.
     * @param launcher can be used to launch processes for this build - even if the build runs remotely.
     * @param listener can be used to send any message.
     * @param git the Git client used to execute commands for the Gitflow actions.
     * @throws IOException if an error occurs that causes/should cause the build to fail.
     * @throws InterruptedException if the build is interrupted during execution.
     */
    public <BC extends B> NoGitflowAction(final BC build, final Launcher launcher, final BuildListener listener, final GitClientDelegate git) throws IOException, InterruptedException {
        super(build, launcher, listener, git, new NoGitflowCause());
    }

    @Override
    protected String getConsoleMessagePrefix() {
        return CONSOLE_MESSAGE_PREFIX;
    }

    @Override
    protected String getActionName() {
        return ACTION_NAME;
    }

    @Override
    protected void cleanCheckout() throws InterruptedException {
        // Override without actually cleaning up, because standard builds should folllow the cleanup configuration of the Git plugin.
    }

    @Override
    protected void beforeMainBuildInternal() throws IOException, InterruptedException {
        // Nothing to do.
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void afterMainBuildInternal() throws IOException, InterruptedException {

        // Record the data about the Gitflow branches that have been built.
        final Collection<String> remoteBranchNames = this.build.getAction(GitTagAction.class).getTags().keySet();
        for (final String remoteBranchName : remoteBranchNames) {
            final String[] remoteBranchNameTokens = StringUtils.split(remoteBranchName, "/", 2);

            final RemoteBranch remoteBranch = this.gitflowPluginData.getOrAddRemoteBranch(remoteBranchNameTokens[0], remoteBranchNameTokens[1]);
            remoteBranch.setLastBuildResult(this.build.getResult());
            remoteBranch.setLastBuildVersion(this.buildTypeAction.getCurrentVersion());
        }
    }
}
