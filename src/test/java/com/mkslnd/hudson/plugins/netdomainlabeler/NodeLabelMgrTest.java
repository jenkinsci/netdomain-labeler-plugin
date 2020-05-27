/*
 * The MIT License
 *
 * Copyright 2020 campom10.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.mkslnd.hudson.plugins.netdomainlabeler;
// import static com.google.common.base.Predicates.in;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Computer;
import hudson.model.Node;
import hudson.model.TaskListener;
import hudson.model.TopLevelItem;
import hudson.model.labels.LabelAtom;
import hudson.remoting.Callable;
import hudson.remoting.VirtualChannel;
import hudson.slaves.NodeDescriptor;
import hudson.slaves.NodeProperty;
import hudson.slaves.NodePropertyDescriptor;
import hudson.slaves.RetentionStrategy;
import hudson.util.ClockDifference;
import hudson.util.DescribableList;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.logging.LogRecord;
import javax.servlet.ServletException;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/** @author campom10 */
public class NodeLabelMgrTest {

  @Rule public JenkinsRule r = new JenkinsRule();

  public NodeLabelMgrTest() {}

  private Computer computer;
  private Set<LabelAtom> labelsBefore;
  private NodeLabelMgr nodeLabelMrg;

  @Before
  public void setUp() {
    computer = r.jenkins.toComputer();
    labelsBefore = computer.getNode().getAssignedLabels();
    assertThat(labelsBefore, is(not(empty())));
    nodeLabelMrg = new NodeLabelMgr();
  }

  @After
  public void tearDown() {
    Set<LabelAtom> labelsAfter = computer.getNode().getAssignedLabels();
    assertThat(labelsBefore, everyItem(in(labelsAfter)));
  }

  @Test
  public void testOnOnline() throws Exception {
    nodeLabelMrg.onOnline(computer, TaskListener.NULL);
  }

  @Test
  public void testCacheLabels() throws Exception {
    nodeLabelMrg.cacheLabels(computer);
  }
  /*
    @Test
    public void testRefreshModel() {
      nodeLabelMrg.refreshModel(computer);
    }

    @Test
    public void testRefreshModelNullComputer() {
      nodeLabelMrg.refreshModel(null);
    }

    @Test
    public void testRefreshModelNullingComputer() {
      Computer nullingComputer = new NullingComputer(computer.getNode());
      nodeLabelMrg.refreshModel(nullingComputer);
    }

    @Test(expected = IOException.class)
    public void testCacheLabelsNullingComputer() throws Exception {
      Computer nullingComputer = new NullingComputer(computer.getNode());
      nodeLabelMrg.cacheLabels(nullingComputer);
    }
  */
  @Test
  public void testGetLabelsForNode_IsNull() throws Exception {
    Node nullingNode = new NullingNode();
    Collection<LabelAtom> labels = nodeLabelMrg.getLabelsForNode(nullingNode);
    assertThat(labels, is(empty()));
  }
  /*
    @Test(expected = IOException.class)
    public void testRequestComputerPlatformDetails_ChannelThrows() throws Exception {
      Computer throwingComputer = new NullingComputer(computer.getNode(), new IOException());
      nodeLabelMrg.requestComputerPlatformDetails(throwingComputer);
    }
  */
  /** Class that intentionally returns nulls for test purposes. */
  private class NullingComputer extends Computer {

    private final IOException exceptionToThrow;

    public NullingComputer(Node node) {
      super(node);
      exceptionToThrow = null;
    }

    public NullingComputer(Node node, IOException throwThisException) {
      super(node);
      exceptionToThrow = throwThisException;
    }

    @Override
    public Node getNode() {
      /* Intentionally return null to test null node handling */
      return null;
    }

    @Override
    public VirtualChannel getChannel() {
      if (exceptionToThrow != null) {
        return new ThrowingChannel(exceptionToThrow);
      }
      /* Intentionally return null to test null channel handling */
      return null;
    }

    @Override
    public Charset getDefaultCharset() {
      throw new UnsupportedOperationException("Unsupported");
    }

    @Override
    public List<LogRecord> getLogRecords() throws IOException, InterruptedException {
      throw new UnsupportedOperationException("Unsupported");
    }

    @Override
    public void doLaunchSlaveAgent(StaplerRequest sr, StaplerResponse sr1)
        throws IOException, ServletException {
      throw new UnsupportedOperationException("Unsupported");
    }

    @Override
    protected Future<?> _connect(boolean bln) {
      throw new UnsupportedOperationException("Unsupported");
    }

    @Override
    public Boolean isUnix() {
      throw new UnsupportedOperationException("Unsupported");
    }

    @Override
    public boolean isConnecting() {
      throw new UnsupportedOperationException("Unsupported");
    }

    @Override
    public RetentionStrategy getRetentionStrategy() {
      throw new UnsupportedOperationException("Unsupported");
    }
  }

  private class ThrowingChannel implements VirtualChannel {
    private final IOException exceptionToThrow;

    public ThrowingChannel(IOException exceptionToThrow) {
      this.exceptionToThrow = exceptionToThrow;
    }

    public <V, T extends Throwable> V call(Callable<V, T> callable) throws IOException {
      if (exceptionToThrow != null) {
        throw exceptionToThrow;
      }
      return null;
    }

    public <V, T extends Throwable> hudson.remoting.Future<V> callAsync(Callable<V, T> callable) {
      return null;
    }

    public <T> T export(java.lang.Class<T> type, T instance) {
      return null;
    }

    public void join() {
      throw new UnsupportedOperationException("Unsupported");
    }

    public void join(long timeout) {
      throw new UnsupportedOperationException("Unsupported");
    }

    public void syncLocalIO() {
      throw new UnsupportedOperationException("Unsupported");
    }

    public void close() {
      throw new UnsupportedOperationException("Unsupported");
    }
  }

  private class NullingNode extends Node {
    public Callable<ClockDifference, IOException> getClockDifferenceCallable() {
      throw new UnsupportedOperationException("Unsupported");
    }

    public NodeDescriptor getDescriptor() {
      throw new UnsupportedOperationException("Unsupported");
    }

    public DescribableList<NodeProperty<?>, NodePropertyDescriptor> getNodeProperties() {
      throw new UnsupportedOperationException("Unsupported");
    }

    public FilePath getRootPath() {
      throw new UnsupportedOperationException("Unsupported");
    }

    public FilePath getWorkspaceFor(TopLevelItem item) {
      throw new UnsupportedOperationException("Unsupported");
    }

    public String getLabelString() {
      throw new UnsupportedOperationException("Unsupported");
    }

    public Computer createComputer() {
      throw new UnsupportedOperationException("Unsupported");
    }

    public Node.Mode getMode() {
      throw new UnsupportedOperationException("Unsupported");
    }

    public int getNumExecutors() {
      throw new UnsupportedOperationException("Unsupported");
    }

    public Launcher createLauncher(TaskListener listener) {
      throw new UnsupportedOperationException("Unsupported");
    }

    public String getNodeDescription() {
      throw new UnsupportedOperationException("Unsupported");
    }

    @Deprecated
    public void setNodeName(String name) {
      throw new UnsupportedOperationException("Unsupported");
    }

    public String getNodeName() {
      throw new UnsupportedOperationException("Unsupported");
    }
  }
}
