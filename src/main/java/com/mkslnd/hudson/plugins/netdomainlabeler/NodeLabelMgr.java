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

import hudson.Extension;
import hudson.model.Computer;
import hudson.model.Node;
import hudson.model.TaskListener;
import hudson.model.labels.LabelAtom;
import hudson.slaves.ComputerListener;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import jenkins.model.Jenkins;

/** @author guybrush */
/** A cache of Node labels for the LabelFinder in our package. */
@Extension
public class NodeLabelMgr extends ComputerListener {
  private static transient Map<Computer, Domains> nodesDomains =
      Collections.synchronizedMap(new WeakHashMap<>());
  /** The labels computed for nodes - accessible package wide. */
  static transient Map<Node, Collection<LabelAtom>> nodeLabels = new WeakHashMap<>();
  /** Logging of issues. */
  private static final transient Logger LOGGER = Logger.getLogger(NodeLabelMgr.class.getName());

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
            try {

              refreshModel(node);
            } catch (IOException | InterruptedException e) {
              LOGGER.log(Level.INFO, "Ostia tremenda");
            }
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
    if (computer != null) {
      Node node = computer.getNode();
      if (node != null) nodeLabels.put(node, getLabelsForNode(node));
    }
  }

  /**
   * Update Jenkins' model so that labels for this computer are up to date.
   *
   * @param computer node whose labels will be cached
   */
  final void refreshModel(final Computer computer) throws IOException, InterruptedException {
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
  public Collection<LabelAtom> getLabelsForNode(final Node node)
      throws IOException, InterruptedException {
    dumpExistingLabels();
    Set<LabelAtom> result = new HashSet<>();
    try {
      Computer computer = node.toComputer();

      if (computer != null) {
        String hostname = "NoName";
        hostname =
            computer.getHostName(); // Esto devuelve el host bueno. fqdn No need of reverse resolve.
        String domain;
        domain = getDomainOf(hostname);
        //        LOGGER.log(Level.INFO, "SET DOMAIN:" + domain);
        final Jenkins jenkins = Jenkins.getInstanceOrNull();
        if (jenkins != null) result.add(jenkins.getLabelAtom(domain));
      }
    } catch (IOException | InterruptedException ex) {
      Logger.getLogger(NetDomainLabeler.class.getName()).log(Level.SEVERE, null, ex);
      throw ex;
    }
    return result;
  }

  /**
   * Return collection of generated/cached labels for the given node.
   *
   * @param node Node whose labels should be generated
   * @return Collection with labels
   */
  public static Collection<LabelAtom> getCachedLabelsForNode(final Node node)
      throws IOException, InterruptedException {
    return nodeLabels.get(node);
  }

  public static void addLabelsForNode(final Node node, Collection<LabelAtom> labels)
      throws IOException, InterruptedException {
    //    LOGGER.log(Level.INFO, "Setting Testing Labels:");
    nodeLabels.put(node, labels);
    /*    LOGGER.log(
            Level.INFO,
            ("SETTING NODE: " + node.getDisplayName() + " NLabels:" + String.valueOf(labels.size()))
                .replace("\r\n", ""));
        printlabels(labels);
        LOGGER.log(Level.INFO, "DONE Setting Testing Labels:");
    */
  }

  private static void dumpExistingLabels() {
    // static transient Map<Node, Collection<LabelAtom>> nodeLabels
    LOGGER.log(Level.INFO, "Listando Hash:");
    LOGGER.log(Level.INFO, String.valueOf(nodeLabels.size()));
    for (Iterator<Node> kset = nodeLabels.keySet().iterator(); kset.hasNext(); ) {
      Node nodito = kset.next();
      LOGGER.log(Level.INFO, "VALUE:" + nodito.getDisplayName());
    }

    nodeLabels.forEach(
        (node, labels) -> {
          LOGGER.log(Level.INFO, "NODENAME: " + node.getNodeName());
          printlabels(labels);
        });
  }

  public static void printlabels(Collection<LabelAtom> labels) {
    try {
      LOGGER.log(Level.INFO, "dump specific Labels:");
      labels.forEach(
          (LabelAtom tiqueta) -> {
            LOGGER.log(Level.INFO, "Dump Label Desc: " + tiqueta.getExpression());
          });
      LOGGER.log(Level.INFO, "DONE Setting Testing Labels:");
    } catch (Exception e) {
      LOGGER.log(Level.INFO, "Error Dumping Labels: ");
    }
  }

  /**
   * Return the Domain of a given string.
   *
   * @param hostvalue whatever you have for the host. ip, name, fqdn
   * @return domain or null
   */
  private String getDomainOf(String hostValue) {
    try {
      InetAddress host = InetAddress.getByName(hostValue);
      String fqdn = host.getHostName();
      //      String domain = fqdn.substring(fqdn.indexOf("."), (fqdn.length() -
      // fqdn.indexOf(".")));
      String domain = fqdn.substring(fqdn.indexOf(".") + 1);
      return domain;
      /*      if ( validIP(hostValue)) {
          //Is an IP address
          host = InetAddress.getByAddress(hostValue)
          }
          else {
          //is a Hostname
          host = InetAddress.getByName(hostValue);

          }
      */ } catch (UnknownHostException ex) {
      Logger.getLogger(NodeLabelMgr.class.getName()).log(Level.SEVERE, null, ex);
      return null;
    }
  }

  public static boolean validIP(String ip) {
    if (ip == null || ip.isEmpty()) return false;
    ip = ip.trim();
    if ((ip.length() < 6) & (ip.length() > 15)) return false;

    try {
      Pattern pattern =
          Pattern.compile(
              "^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");
      Matcher matcher = pattern.matcher(ip);
      return matcher.matches();
    } catch (PatternSyntaxException ex) {
      return false;
    }
  }
}
