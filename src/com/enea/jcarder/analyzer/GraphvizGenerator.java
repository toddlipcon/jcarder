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
    private static final String EDGE_LABEL_FORMAT =
        " [label=<\n" +
        "     <table align=\"left\" border=\"0\" cellborder=\"0\"" +
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

    public String generate(Iterable<LockEdge> edgesToBePrinted,
                           ContextReaderIfc ras,
                           boolean includePackages) {
        StringBuffer sb = new StringBuffer();
        sb.append("digraph G {\n");
        sb.append("  node [shape=ellipse, style=filled];\n");
        final HashSet<LockNode> alreadyAppendedNodes = new HashSet<LockNode>();
        for (LockEdge edge : edgesToBePrinted) {
            appendNodeIfNotAppended(ras,
                                            sb,
                                            alreadyAppendedNodes,
                                            edge.getSource());
            appendNodeIfNotAppended(ras,
                                            sb,
                                            alreadyAppendedNodes,
                                            edge.getTarget());
            sb.append("  " + edge.getSource().toString() + "");
            sb.append(" -> " + edge.getTarget().toString() + "");
            sb.append(createEdgeLabel(ras, edge, includePackages));
            sb.append(";\n");
        }
        sb.append("}\n");
        return sb.toString();
    }

    private String createEdgeLabel(ContextReaderIfc ras,
                                   LockEdge edge,
                                   boolean includePackages) {
        final LockingContext source =
            ras.readContext(edge.getSourceLockingContextId());
        final LockingContext target =
            ras.readContext(edge.getTargetLockingContextId());
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
        return s.replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("&", "&amp;")
                .replace("\"", "&quot;")
                .replace("\\", "&#092;")
                .replace("\'", "&#039;");
    }

    private String getLockNodeString(LockNode node,
                                     ContextReaderIfc ras) {
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
               + escape(ras.readLock(node.getLockId()).toString())
               + "\" , fillcolor=" + color + "];\n";
    }

    private void appendNodeIfNotAppended(ContextReaderIfc ras,
                                         StringBuffer sb,
                                         HashSet<LockNode> alreadyAppendedNodes,
                                         LockNode node) {
        if (!alreadyAppendedNodes.contains(node)) {
            alreadyAppendedNodes.add(node);
            sb.append(getLockNodeString(node, ras));
        }
    }
}
