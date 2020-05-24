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
import hudson.model.LabelFinder;
import hudson.model.Node;
import hudson.model.labels.LabelAtom;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

/** @author guybrush */
@Extension
public class NetDomainLabeler extends LabelFinder {
  /**
   * Returns collection of LabelAtom for the node argument.
   *
   * @param node agent whose labels are returned
   * @return collection of LabelAtom for the node argument
   */
  private static final transient Logger LOGGER = Logger.getLogger(NetDomainLabeler.class.getName());

  @Override
  public final Collection<LabelAtom> findLabels(final Node node) {
    //    LOGGER.info("NetdomainLabeler.findlabels");
    Collection<LabelAtom> result = Collections.emptySet();
    try {
      result = NodeLabelMgr.getCachedLabelsForNode(node);
    } catch (IOException ex) {
      Logger.getLogger(NetDomainLabeler.class.getName()).log(Level.SEVERE, null, ex);
    } catch (InterruptedException ex) {
      Logger.getLogger(NetDomainLabeler.class.getName()).log(Level.SEVERE, null, ex);
    }
    if (null == result) /* Node that has just attached and we don't have labels yet */ {
      return Collections.emptySet();
    }
    return result;
  }

  public final void debuggin(Node node) {
    System.out.println("Debuggin");
    Computer compu = node.toComputer();
    try {
      String hostname = "kk";
      if (compu != null) {
        hostname =
            compu.getHostName(); // Esto devuelve el host bueno. fqdn No need of reverse resolve.
        System.out.println("compu:" + compu.getName());
        //              hudson.slaves.SlaveComputer hola = (hudson.slaves.SlaveComputer)
        // compu.getTarget();
        //              System.out.println(hola.getClass().getName());
      }
      System.out.println(node.getNodeName());
      System.out.println(node.getNodeDescription());
      System.out.println(hostname); // Este es el bueno
    } catch (IOException | InterruptedException ex) {
      Logger.getLogger(NetDomainLabeler.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
}
