/*
 * JCarder -- cards Java programs to keep threads disentangled
 *
 * Copyright (C) 2006-2007 Enea AB
 * Copyright (C) 2007 Ulrik Svensson
 * Copyright (C) 2007 Joel Rosdahl
 *
 * This program is made available under the GNU GPL version 2, with a special
 * exception for linking with JUnit. See the accompanying file LICENSE.txt for
 * details.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.enea.jcarder.analyzer;

import java.util.HashSet;

import com.enea.jcarder.common.LockingContext;
import com.enea.jcarder.common.contexts.ContextReaderIfc;

/**
 * This class can be used to generate a Graphviz <http://www.graphviz.org> graph
 * as a string.
 *
 * TODO Add tooltips to the graph? The tooltips might for example contain the
 * package names of classes.
 *
 * TODO If there are to many edges, merge them together in the graph and add an
 * href link to an html page that describes them all?
 *
 * TODO Optionaly merge edges that are identical except for the threads?
 */
final class GraphvizGenerator {
    private static final String HTML_EDGE_LABEL_FORMAT =
        " [fontsize=10, label=<\n" +
        "     <table align=\"left\" border=\"0\" cellborder=\"0\"\n" +
        "            cellspacing=\"0\" cellpadding=\"0\">\n" +
        "       <tr>\n" +
        "         <td align=\"left\">  </td>\n" +
        "         <td align=\"left\" colspan=\"2\">" +
        "Thread: %1$s<br align=\"left\"/>" +
        "</td>\n" +
        "       </tr>\n" +
        "       <tr>\n" +
        "         <td align=\"left\">  </td>\n" +
        "         <td align=\"left\" colspan=\"2\">" +
        "holding: %2$s<br align=\"left\"/>" +
        "</td>\n" +
        "       </tr>\n" +
        "       <tr>\n" +
        "         <td align=\"left\">  </td>\n" +
        "         <td align=\"left\">  </td>\n" +
        "         <td align=\"left\">" +
        "in: %3$s<br align=\"left\"/>" +
        "</td>\n" +
        "       </tr>\n" +
        "       <tr>\n" +
        "         <td align=\"left\">  </td>\n" +
        "         <td align=\"left\" colspan=\"2\">" +
        "taking: %4$s<br align=\"left\"/>" +
        "</td>\n" +
        "       </tr> \n" +
        "       <tr>\n" +
        "         <td align=\"left\">  </td>\n" +
        "         <td align=\"left\">  </td>\n" +
        "         <td align=\"left\">" +
        "in: %5$s<br align=\"left\"/>" +
        "</td>\n" +
        "       </tr>\n" +
        "     </table>\n" +
        "    >]";

    private static final String EDGE_LABEL_FORMAT =
        " [fontsize=10, label=<\n" +
        "Thread: %1$s<br/>\n" +
        "holding: %2$s<br/>\n" +
        "in: %3$s<br />\n" +
        "taking: %4$s<br />\n" +
        "in: %5$s<br />\n" +
        ">]";

    public String generate(Iterable<LockEdge> edgesToBePrinted,
                           ContextReaderIfc reader,
                           boolean includePackages) {
        StringBuffer sb = new StringBuffer();
        sb.append("digraph G {\n");
        sb.append("  node [shape=ellipse, style=filled, fontsize=12];\n");
        final HashSet<LockNode> alreadyAppendedNodes = new HashSet<LockNode>();
        for (LockEdge edge : edgesToBePrinted) {
            appendNodeIfNotAppended(reader,
                                    sb,
                                    alreadyAppendedNodes,
                                    edge.getSource());
            appendNodeIfNotAppended(reader,
                                    sb,
                                    alreadyAppendedNodes,
                                    edge.getTarget());
            sb.append("  " + edge.getSource().toString() + "");
            sb.append(" -> " + edge.getTarget().toString() + "");
            sb.append(createEdgeLabel(reader, edge, includePackages));
            sb.append(";\n");
        }
        sb.append("}\n");
        return sb.toString();
    }

    private String createEdgeLabel(ContextReaderIfc reader,
                                   LockEdge edge,
                                   boolean includePackages) {
        final LockingContext source =
            reader.readContext(edge.getSourceLockingContextId());
        final LockingContext target =
            reader.readContext(edge.getTargetLockingContextId());
        return String.format(EDGE_LABEL_FORMAT,
                             escape(handlePackage(target.getThreadName(),
                                                  includePackages)),
                             escape(handlePackage(source.getLockReference(),
                                                  includePackages)),
                             escape(handlePackage(source.getMethodWithClass(),
                                                  includePackages)),
                             escape(handlePackage(target.getLockReference(),
                                                  includePackages)),
                             escape(handlePackage(target.getMethodWithClass(),
                                                  includePackages)));
    }

    private String handlePackage(String s,
                                 boolean includePackages) {
        String[] parts = s.split("\\.");
        if (parts.length >= 2 && !includePackages) {
            return parts[parts.length - 2] + "." + parts[parts.length - 1];
        } else {
            return s;
        }
    }

    private static String escape(String s) {
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("\'", "&#039;");
    }

    private String getLockNodeString(LockNode node,
                                     ContextReaderIfc reader) {
        final String color;
        switch (node.getCycleType()) {
        case CYCLE:
            color = "firebrick1";
            break;
        case SINGLE_THREADED_CYCLE:
            color = "yellow";
            break;
        default:
            color = "white";
        }
        return "  " + node.toString() + " [label = \""
               + escape(reader.readLock(node.getLockId()).toString())
               + "\" , fillcolor=" + color + "];\n";
    }

    private void appendNodeIfNotAppended(ContextReaderIfc reader,
                                         StringBuffer sb,
                                         HashSet<LockNode> alreadyAppendedNodes,
                                         LockNode node) {
        if (!alreadyAppendedNodes.contains(node)) {
            alreadyAppendedNodes.add(node);
            sb.append(getLockNodeString(node, reader));
        }
    }
}
