package org.jenkinsci.plugins.gitflow.action;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.jenkinsci.plugins.gitclient.GitClient;
import org.jenkinsci.plugins.gitflow.GitflowBuildWrapper;
import org.jenkinsci.plugins.gitflow.action.buildtype.AbstractBuildTypeAction;
import org.jenkinsci.plugins.gitflow.cause.StartHotFixCause;
import org.jenkinsci.plugins.gitflow.data.GitflowPluginData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.plugins.git.GitSCM;

@RunWith(MockitoJUnitRunner.class)
public class StartHotFixActionTest {

    @Mock
    private AbstractBuild build;

    @Mock
    private Launcher launcher;

    @Mock
    private BuildListener listener;

    @Mock
    private GitSCM scm;

    @Mock
    private GitClient gitClient;

    @Mock
    private GitflowBuildWrapper.DescriptorImpl descriptor;

    @Mock
    private AbstractBuildTypeAction<?> buildTypeAction;

    private ByteArrayOutputStream outputStream;

    @Before
    public void setUp() throws Exception {
        AbstractProject project = mock(AbstractProject.class);
        when(project.getScm()).thenReturn(scm);
        when(build.getProject()).thenReturn(project);

        this.outputStream = new ByteArrayOutputStream();
        when(listener.getLogger()).thenReturn(new PrintStream(outputStream));

        when(descriptor.getMasterBranch()).thenReturn("master");
        when(descriptor.getHotfixBranchPrefix()).thenReturn("hotfix/");
    }

    //TODO This Method only exist for make UnitTesting work, the AbstractGitflowAction needs some refactoring
    private StartHotFixAction createAction(StartHotFixCause cause) throws IOException, InterruptedException, NoSuchFieldException, IllegalAccessException {
        StartHotFixAction action = new StartHotFixAction(build, launcher, listener, cause);
        setPrivateFinalField(action, "git", gitClient);
        setPrivateFinalField(action, "buildTypeAction", buildTypeAction);
        action.setDescriptor(descriptor);
        return action;
    }

    //TODO This Method only exist for make UnitTesting work, the AbstractGitflowAction needs some refactoring
    private void setPrivateFinalField(Object obj, String fieldName, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field f = obj.getClass().getSuperclass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(obj, value);
    }

    @Test
    public void testBeforeMainBuildInternal() throws Exception {
        StartHotFixCause cause = new StartHotFixCause("VeryHotFix", "1.0.2-Snapshot", false);
        StartHotFixAction action = createAction(cause);

        List<String> changeFiles = Arrays.asList("pom.xml", "child1/pom.xml", "child2/pom.xml", "child3/pom.xml");

        when(buildTypeAction.updateVersion("1.0.2-Snapshot")).thenReturn(changeFiles);

        action.beforeMainBuildInternal();

        verify(gitClient).checkoutBranch("hotfix/VeryHotFix","origin/master");
        verify(gitClient).add("pom.xml");
        verify(gitClient).add("child1/pom.xml");
        verify(gitClient).add("child2/pom.xml");
        verify(gitClient).add("child3/pom.xml");

        verify(gitClient).commit(any(String.class));

        verifyNoMoreInteractions(gitClient);
    }

    @Test
    public void testAfterMainBuildInternalSuccess() throws Exception {
        GitflowPluginData pluginData = mock(GitflowPluginData.class);

        when(build.getAction(GitflowPluginData.class)).thenReturn(pluginData);
        when(build.getResult()).thenReturn(Result.SUCCESS);

        StartHotFixCause cause = new StartHotFixCause("VeryHotFix", "1.0.2-Snapshot", false);
        StartHotFixAction action = createAction(cause);

        action.afterMainBuildInternal();

        verify(gitClient).push("origin","refs/heads/hotfix/VeryHotFix:refs/heads/hotfix/VeryHotFix");

        verify(pluginData).setDryRun(false);
        verify(pluginData).recordRemoteBranch("origin","hotfix/VeryHotFix", Result.SUCCESS, "1.0.2-Snapshot");

        verifyNoMoreInteractions(gitClient, pluginData);

    }

    @Test
    public void testAfterMainBuildInternalFail() throws Exception {
        GitflowPluginData pluginData = mock(GitflowPluginData.class);

        when(build.getAction(GitflowPluginData.class)).thenReturn(pluginData);
        when(build.getResult()).thenReturn(Result.FAILURE);

        StartHotFixCause cause = new StartHotFixCause("VeryHotFix", "1.0.2-Snapshot", false);
        StartHotFixAction action = createAction(cause);

        action.afterMainBuildInternal();

        verify(pluginData).setDryRun(false);
        verify(pluginData).recordRemoteBranch("origin","hotfix/VeryHotFix", Result.FAILURE, "1.0.2-Snapshot");

        verifyNoMoreInteractions(gitClient, pluginData);
    }

}