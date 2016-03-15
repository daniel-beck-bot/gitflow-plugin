package org.jenkinsci.plugins.gitflow.action;

import static org.jenkinsci.plugins.gitflow.GitflowBuildWrapper.getGitflowBuildWrapperDescriptor;

import java.io.IOException;

import org.jenkinsci.plugins.gitflow.cause.FinishReleaseCause;
import org.jenkinsci.plugins.gitflow.proxy.gitclient.GitClientProxy;

import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;

/**
 * This class executes the required steps for the Gitflow action <i>Finish Release</i>.
 *
 * @param <B> the build in progress.
 * @author Marc Rohlfs, Silpion IT-Solutions GmbH - rohlfs@silpion.de
 */
public class FinishReleaseAction<B extends AbstractBuild<?, ?>> extends AbstractGitflowAction<B, FinishReleaseCause> {

    private static final String ACTION_NAME = "Finish Release";

    /**
     * Initialises a new <i>Finish Release</i> action.
     *
     * @param build the <i>Finish Release</i> build that is in progress.
     * @param launcher can be used to launch processes for this build - even if the build runs remotely.
     * @param listener can be used to send any message.
     * @param git the Git client used to execute commands for the Gitflow actions.
     * @param gitflowCause the cause for the new action.
     * @throws IOException if an error occurs that causes/should cause the build to fail.
     * @throws InterruptedException if the build is interrupted during execution.
     */
    public <BC extends B> FinishReleaseAction(final BC build, final Launcher launcher, final BuildListener listener, final GitClientProxy git, final FinishReleaseCause gitflowCause)
            throws IOException, InterruptedException {
        super(build, launcher, listener, git, gitflowCause);
    }

    /** {@inheritDoc} */
    @Override
    protected String getActionName() {
        return ACTION_NAME;
    }

    /** {@inheritDoc} */
    @Override
    protected void beforeMainBuildInternal() throws IOException, InterruptedException {

        // Add environment and property variables
        final String releaseBranch = this.gitflowCause.getReleaseBranch();
        this.additionalBuildEnvVars.put("GIT_SIMPLE_BRANCH_NAME", releaseBranch);
        this.additionalBuildEnvVars.put("GIT_REMOTE_BRANCH_NAME", "origin/" + releaseBranch);
        this.additionalBuildEnvVars.put("GIT_BRANCH_TYPE", getGitflowBuildWrapperDescriptor().getBranchType(releaseBranch));

        // Finish Release: just delete the release branch.
        this.deleteBranch(releaseBranch);

        // There's no need to execute the main build.
        this.omitMainBuild();
    }

    /** {@inheritDoc} */
    @Override
    protected void afterMainBuildInternal() throws IOException, InterruptedException {
        // Nothing to do.
    }
}
