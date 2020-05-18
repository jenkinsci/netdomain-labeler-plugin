/*
 * The MIT License
 *
 * Copyright 2020 guybrush.
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

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.Computer;
import hudson.model.Node;
import hudson.model.TaskListener;
import hudson.model.labels.LabelAtom;
import hudson.remoting.VirtualChannel;
import hudson.slaves.ComputerListener;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;

/**
 *
 * @author guybrush
 */
/** A cache of Node labels for the LabelFinder in our package. */
@Extension
public class NodeLabelMrg extends ComputerListener{
  private static transient Map<Computer, Domains> nodesDomains =
      Collections.synchronizedMap(new WeakHashMap<>());
  /** The labels computed for nodes - accessible package wide. */
  static transient Map<Node, Collection<LabelAtom>> nodeLabels = new WeakHashMap<>();
  /** Logging of issues. */
  private static final transient Logger LOGGER =
      Logger.getLogger("org.mkslnd.hudson.plugins.netdomainlabeler");

  /**
   * When a computer comes online, probe it for its platform labels.
   *
   * @param computer agent whose labels will be cached
   * @param ignored TaskListener that is ignored
   * @throws java.io.IOException on IO error
   * @throws java.lang.InterruptedException on thread interrupt
   */
  @Override
  public final void onOnline(final Computer computer, final TaskListener ignored)
      throws IOException, InterruptedException {
    cacheLabels(computer);
    refreshModel(computer);
  }

  /** When any computer has changed, update the platform labels according to the configuration. */
  @Override
  public final void onConfigurationChange() {
    synchronized (nodesDomains) {
      nodesDomains.forEach(
          (node, labels) -> {
            refreshModel(node);
          });
    }
  }

  /**
   * Caches the labels for the computer against its node.
   *
   * @param computer node whose labels will be cached
   * @throws IOException on I/O error
   * @throws InterruptedException on thread interruption
   */
  final void cacheLabels(final Computer computer) throws IOException, InterruptedException {
    /* Cache the labels for the node */
//    nodeLabels.put(computer, requestComputerPlatformDetails(computer));
  }

  /**
   * Update Jenkins' model so that labels for this computer are up to date.
   *
   * @param computer node whose labels will be cached
   */
  final void refreshModel(final Computer computer) {
    if (computer != null) {
      Node node = computer.getNode();
      if (node != null) {
        nodeLabels.put(node, getLabelsForNode(node));
        node.getAssignedLabels();
      }
    }
  }

  /**
   * Return collection of generated labels for the given node.
   *
   * @param node Node whose labels should be generated
   * @return Collection with labels
   */
  Collection<LabelAtom> getLabelsForNode(final Node node) {

    Set<LabelAtom> result = new HashSet<>();

    Computer computer = node.toComputer();

    if (computer == null) {
      return result;
    }

//    Domains domains = nodesDomains.get(computer);

    //for some reason we are asking labelAtom from the isntance not creating new one.
    final Jenkins jenkins = Jenkins.getInstanceOrNull();
      if (jenkins != null )
        result.add(jenkins.getLabelAtom("LosMAnolos"));

    return result;
  }

    
}
