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

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import hudson.model.labels.LabelAtom;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

/** @author campom10 */
public class NetDomainLabelerTest {
  @Rule public final JenkinsRule j = new JenkinsRule();
  private static final transient Logger LOGGER =
      Logger.getLogger(NetDomainLabelerTest.class.getName());

  @Test
  public void testLookupCached() {

    Collection<LabelAtom> expected = new HashSet<>();
    expected.add(j.jenkins.getLabelAtom("foo"));
    expected.add(j.jenkins.getLabelAtom("bar"));
    try {
      System.out.println("SETLABELS");
      NodeLabelMgr.addLabelsForNode(j.jenkins, expected);
      System.out.println("DONELABELS");
    } catch (IOException ex) {
      Logger.getLogger(NetDomainLabelerTest.class.getName()).log(Level.SEVERE, null, ex);
    } catch (InterruptedException ex) {
      Logger.getLogger(NetDomainLabelerTest.class.getName()).log(Level.SEVERE, null, ex);
    }
    //    NodeLabelMgr.printlabels(expected);
    Collection<LabelAtom> labels;
    labels = new NetDomainLabeler().findLabels(j.jenkins);
    //    NodeLabelMgr.printlabels(labels);
    assertThat(labels, is(expected));
  }

  @Test
  public void testLookupUncached() throws Exception {
    /* remove the Jenkins node from the cache */

    if (NodeLabelMgr.nodeLabels.containsKey(j.jenkins)) {
      NodeLabelMgr.removeLabelsForNode(j.jenkins);
      //            NodeLabelMgr.nodeLabels.remove(j.jenkins);
    }
    Collection<LabelAtom> labels;
    labels = new NetDomainLabeler().findLabels(j.jenkins);
    assertThat(labels, is(empty()));

    assert true;
  }
}
