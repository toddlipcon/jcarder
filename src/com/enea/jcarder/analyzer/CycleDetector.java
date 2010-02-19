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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import net.jcip.annotations.NotThreadSafe;

import com.enea.jcarder.common.contexts.ContextReaderIfc;
import com.enea.jcarder.util.Counter;
import com.enea.jcarder.util.MaxValueCounter;
import com.enea.jcarder.util.logging.Logger;

/**
 * This class is responsible for finding and managing cycles.
 *
 * TODO Add possibility to ignore cycles guarded by a common lock.
 *
 * TODO Add possibility to ignore cycles created by two threads that cannot
 * possibly run at the same time. Is it possible to achieve that by tracking
 * Thread.start() and Thread.join()?
 *
 * TODO Add more basic tests for this class.
 */
@NotThreadSafe
class CycleDetector {
    private final HashSet<Cycle> mCycles;
    private final Logger mLogger;
    private final MaxValueCounter mMaxDepth;
    private final MaxValueCounter mMaxCycleDepth;
    private final MaxValueCounter mNoOfCycles;
    private final Counter mNoOfCreatedCycleObjects;

    CycleDetector(Logger logger) {
        mLogger = logger;
        mCycles = new HashSet<Cycle>();
        mMaxDepth = new MaxValueCounter("Graph Depth", mLogger);
        mMaxCycleDepth = new MaxValueCounter("Cycle Depth", mLogger);
        mNoOfCycles = new MaxValueCounter("Found cycles", mLogger);
        mNoOfCreatedCycleObjects = new Counter("Created cycle objects",
                                               mLogger,
                                               100000);
    }

    /**
     * Analyze a set of LockNodes and LockEdges. All cycles they form will be
     * stored within this class.
     */
    void analyzeLockNodes(final Iterable<LockNode> nodes) {
        ArrayList<LockNode> nodesOnStack = new ArrayList<LockNode>(10);
        ArrayList<LockEdge> edgesOnStack = new ArrayList<LockEdge>(10);
        HashSet<LockNode> visitedNodes = new HashSet<LockNode>();
        HashSet<LockEdge> visitedEdges = new HashSet<LockEdge>();
        for (LockNode lock : nodes) {
            if (!visitedNodes.contains(lock)) {
                analyzeNode(lock, nodesOnStack, edgesOnStack, visitedNodes,
                            visitedEdges);
            }
        }
        for (Cycle cycle : mCycles) {
            cycle.updateNodeCycleStatus();
        }
    }

    MaxValueCounter getMaxDepth() {
        return mMaxDepth;
    }

    MaxValueCounter getMaxCycleDepth() {
        return mMaxCycleDepth;
    }

    private void analyzeNode(final LockNode node,
                             final ArrayList<LockNode> nodesOnStack,
                             final ArrayList<LockEdge> edgesOnStack,
                             final HashSet<LockNode> visitedNodes,
                             final HashSet<LockEdge> visitedEdges) {
        visitedNodes.add(node);
        nodesOnStack.add(node);
        for (LockEdge edge : node.getOutgoingEdges()) {
            edgesOnStack.add(edge);
            analyzeEdge(edge, nodesOnStack, edgesOnStack, visitedNodes,
                        visitedEdges);
            edgesOnStack.remove(edgesOnStack.size() - 1);
        }
        nodesOnStack.remove(nodesOnStack.size() - 1);
    }

    private void analyzeEdge(final LockEdge edge,
                             final ArrayList<LockNode> nodesOnStack,
                             final ArrayList<LockEdge> edgesOnStack,
                             final HashSet<LockNode> visitedNodes,
                             final HashSet<LockEdge> visitedEdges) {
        if (!visitedEdges.contains(edge)) {
            mMaxDepth.set(nodesOnStack.size());
            final int index = nodesOnStack.indexOf(edge.getTarget());
            if (index >= 0) {
                final List<LockEdge> edgesInCycle =
                    edgesOnStack.subList(index, edgesOnStack.size());
                mNoOfCreatedCycleObjects.increment();
                mMaxCycleDepth.set(edgesInCycle.size());
                Cycle newCycle = new Cycle(edgesInCycle);
                mCycles.add(newCycle);
                mNoOfCycles.set(mCycles.size());
                /*
                 * Keeping the first edge from the cycle in the visitedEdges
                 * list is an optimization that avoids unnecessary (as I
                 * believe) repeated checks. The other edges have to be removed,
                 * otherwise all cycles won't be found. See the testcases for
                 * examples of such cases.
                 */
                List<LockEdge> edgesToRemove =
                    edgesInCycle.subList(1, edgesInCycle.size());
                visitedEdges.removeAll(edgesToRemove);
            } else {
                visitedEdges.add(edge);
                analyzeNode(edge.getTarget(),
                            nodesOnStack,
                            edgesOnStack,
                            visitedNodes,
                            visitedEdges);
            }
        }
    }


    HashSet<Cycle> getCycles() {
        return mCycles;
    }

    private static boolean containsAlike(Cycle cycle, Iterable<Cycle> others,
                                         ContextReaderIfc reader) {
        for (Cycle other : others) {
            if (cycle.alike(other, reader)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Reducing the number of cycles by ignoring those that are duplicates (very
     * similar) to another cycle.
     */
    void removeAlikeCycles(ContextReaderIfc reader) {
        int removedCycles = 0;
        ArrayList<Cycle> uniqueCycles = new ArrayList<Cycle>();
        Iterator<Cycle> iter = mCycles.iterator();
        while (iter.hasNext()) {
            final Cycle cycle = iter.next();
            if (containsAlike(cycle, uniqueCycles, reader)) {
                iter.remove();
                removedCycles++;
            } else {
                uniqueCycles.add(cycle);
            }
        }
        mLogger.info("Ignoring " + removedCycles
                     + " almost identical cycle(s).");
        assert uniqueCycles.equals(new ArrayList<Cycle>(mCycles));
    }

    /**
     * Remove cycles that are formed by a only one thread.
     */
    void removeSingleThreadedCycles() {
        int removedCycles = 0;
        Iterator<Cycle> iter = mCycles.iterator();
        while (iter.hasNext()) {
            final Cycle cycle = iter.next();
            if (cycle.isSingleThreaded()) {
                iter.remove();
                removedCycles++;
            }
        }
        mLogger.info("Ignoring "
                     + removedCycles
                     + " single threaded cycle(s).");
    }


    void removeGatedCycles() {
        int removedCycles = 0;
        Iterator<Cycle> iter = mCycles.iterator();
        while (iter.hasNext()) {
            final Cycle cycle = iter.next();
            if (cycle.isGated()) {
                iter.remove();
                removedCycles++;
            }
        }
        mLogger.info("Ignoring " + removedCycles + " gated cycle(s).");
    }

    /**
     * Get the total number of edges in all known cycles.
     */
    int getNumberOfEdges() {
        HashSet<LockEdge> edges = new HashSet<LockEdge>();
        for (Cycle cycle : mCycles) {
            edges.addAll(cycle.getEdges());
        }
        return edges.size();
    }

    /**
     * Get the total number of nodes in all known cycles.
     */
    int getNumberOfNodes() {
        HashSet<LockNode> nodes = new HashSet<LockNode>();
        for (Cycle cycle : mCycles) {
            for (LockEdge edge : cycle.getEdges()) {
                nodes.add(edge.getSource());
                nodes.add(edge.getTarget());
            }
        }
        return nodes.size();
    }

    /**
     * Find out the cycles that consist of identical locks and group them
     * together. Then return all edges in each group.
     *
     * The data structure in the CycleDetector class is unaffected.
     */
    Collection<HashSet<LockEdge>> mergeCyclesWithIdenticalLocks() {
        /*
         * TODO Refactor this method? The temporary data structure is too
         * complex?
         */
        HashMap<HashSet<LockNode>, HashSet<LockEdge>> setOfNodesToEdgesMap =
            new HashMap<HashSet<LockNode>, HashSet<LockEdge>>();
        for (Cycle cycle : mCycles) {
            final HashSet<LockNode> nodes = cycle.getNodes();
            HashSet<LockEdge> edges = setOfNodesToEdgesMap.get(nodes);
            if (edges == null) {
                edges = new HashSet<LockEdge>();
                setOfNodesToEdgesMap.put(nodes, edges);
            }
            edges.addAll(cycle.getEdges());
        }
        return setOfNodesToEdgesMap.values();
    }
}
