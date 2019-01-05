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

import java.util.*;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.enea.jcarder.util.logging.Logger;

/*
 * The purpose of this junit class is to test the class CycleDetector.
 *
 * TODO Some important methods in CycleDetector are not tested yet.
 */
public final class TestCycleDetector {
    LockGraphBuilder mBuilder;
    CycleDetector mCycleDetector;
    LinkedList<LockNode> mNodes;
    Set<Set<LockTransition>> mExpectedCycles;

    @Before
    public void setUp() {
        mBuilder = new LockGraphBuilder(new Logger(null), null);
        mCycleDetector = new CycleDetector(new Logger(null));
        mNodes = new LinkedList<>();
        mExpectedCycles = new HashSet<>();
    }

    private LockTransition addTransition(int from, int to) {
        return addTransition(from, to, -1);
    }

    private LockTransition addTransition(int from, int to, int threadId) {
        final LockNode sourceLock = mBuilder.getLockNode(from);
        final LockNode targetLock = mBuilder.getLockNode(to);
        final LockEdge edge = new LockEdge(sourceLock, targetLock);
        LockTransition transition = new LockTransition(threadId, -1, -1);
        sourceLock.addOutgoingEdge(edge)
                .addTransition(transition);
        if (!mNodes.contains(sourceLock)) {
            mNodes.add(sourceLock);
        }
        if (!mNodes.contains(sourceLock)) {
            mNodes.add(sourceLock);
        }
        return transition;
    }

    private void addExpectedCycle(LockTransition[] transitions) {
        Set<LockTransition> set = Collections.newSetFromMap(new IdentityHashMap<>());
        set.addAll(Arrays.asList(transitions));
        mExpectedCycles.add(set);
    }

    private void assertExpectedCycles() {
        Assert.assertEquals(mExpectedCycles, getTransitionCycles(mCycleDetector.getCycles()));
    }

    private Set<Set> getTransitionCycles(HashSet<Cycle> cycles) {
        Set<Set> transitionCycles = new HashSet<>();
        for (Cycle cycle : cycles) {
            addAllTransitionCycles(new ArrayList<>(cycle.getEdges()), transitionCycles, new LockTransition[cycle.getEdges().size()], 0);
        }
        return transitionCycles;
    }

    private void addAllTransitionCycles(List<LockEdge> edges, Collection<Set> cycles, LockTransition[] transitionsContainer, int start) {
        if (start == edges.size()) {
            Set<LockTransition> set = Collections.newSetFromMap(new IdentityHashMap<>());
            set.addAll(Arrays.asList(transitionsContainer));
            cycles.add(set);
            return;
        }
        for (LockTransition transition : edges.get(start).getTransitions()) {
            transitionsContainer[start] = transition;
            addAllTransitionCycles(edges, cycles, transitionsContainer, start + 1);
        }
    }

    @Test
    public void testNoCycle() {
        addTransition(1, 2);
        addTransition(2, 3);
        addTransition(1, 3);
        addTransition(3, 4);
        mCycleDetector.analyzeLockNodes(mNodes);
        assertExpectedCycles();
    }

    @Test
    public void testSmallCycle() {
        LockTransition e1 = addTransition(1, 2);
        LockTransition e2 = addTransition(2, 1);
        mCycleDetector.analyzeLockNodes(mNodes);
        addExpectedCycle(new LockTransition[] {e1, e2});
        assertExpectedCycles();
    }

    @Test
    public void testCycle() {
        addTransition(1, 2);
        LockTransition e2 = addTransition(2, 3);
        LockTransition e3 = addTransition(3, 4);
        LockTransition e4 = addTransition(4, 2);
        addTransition(4, 5);
        mCycleDetector.analyzeLockNodes(mNodes);
        addExpectedCycle(new LockTransition[] {e2, e3, e4});
        assertExpectedCycles();
    }

    @Test
    public void testCyclesWithTwoPaths() {
        addTransition(1, 2);
        LockTransition e2 = addTransition(2, 3);
        LockTransition e3 = addTransition(3, 4);
        LockTransition e4 = addTransition(2, 4);
        LockTransition e5 = addTransition(4, 2);
        mCycleDetector.analyzeLockNodes(mNodes);
        addExpectedCycle(new LockTransition[] {e2, e3, e5});
        addExpectedCycle(new LockTransition[] {e4, e5});
        assertExpectedCycles();
    }

    @Test
    public void testCyclesWithTwoTimesTwoPathsLarge() {
        LockTransition e1 = addTransition(1, 2);
        LockTransition e2 = addTransition(2, 3);
        LockTransition e3 = addTransition(3, 4);
        LockTransition e3b = addTransition(3, 4, 2);
        LockTransition e4 = addTransition(4, 5);
        LockTransition e5 = addTransition(5, 6);
        LockTransition e6 = addTransition(6, 1);
        LockTransition e6b = addTransition(6, 1, 2);
        mCycleDetector.analyzeLockNodes(mNodes);
        addExpectedCycle(new LockTransition[] {e1, e2, e3, e4, e5, e6});
        addExpectedCycle(new LockTransition[] {e1, e2, e3b, e4, e5, e6});
        addExpectedCycle(new LockTransition[] {e1, e2, e3b, e4, e5, e6b});
        addExpectedCycle(new LockTransition[] {e1, e2, e3, e4, e5, e6b});
        assertExpectedCycles();
    }

    @Test
    public void testCyclesWithTwoTimesTwoPathsSmall() {
        LockTransition e1 = addTransition(1, 2);
        LockTransition e2 = addTransition(2, 1);
        LockTransition e1b = addTransition(1, 2, 2);
        LockTransition e2b = addTransition(2, 1, 2);
        mCycleDetector.analyzeLockNodes(mNodes);
        addExpectedCycle(new LockTransition[] {e1, e2});
        addExpectedCycle(new LockTransition[] {e1b, e2});
        addExpectedCycle(new LockTransition[] {e1, e2b});
        addExpectedCycle(new LockTransition[] {e1b, e2b});
        assertExpectedCycles();
    }


    @Test
    public void testCyclesWithTwoAlternateWays() {
        LockTransition e1 = addTransition(1, 2);
        LockTransition e2 = addTransition(1, 3);
        LockTransition e3 = addTransition(2, 4);
        LockTransition e4 = addTransition(3, 4);
        LockTransition e5 = addTransition(4, 5);
        LockTransition e6 = addTransition(4, 6);
        LockTransition e7 = addTransition(5, 1);
        LockTransition e8 = addTransition(6, 1);

        mCycleDetector.analyzeLockNodes(mNodes);
        addExpectedCycle(new LockTransition[] {e1, e3, e5, e7});
        addExpectedCycle(new LockTransition[] {e1, e3, e6, e8});
        addExpectedCycle(new LockTransition[] {e2, e4, e5, e7});
        addExpectedCycle(new LockTransition[] {e2, e4, e6, e8});
        assertExpectedCycles();
    }


    @Test
    public void testComplexCycles() {
        int a = 6;
        int b = 3;
        addTransition(1, 2);
        LockTransition e2 = addTransition(2, b);
        LockTransition e3 = addTransition(b, 4);
        LockTransition e4 = addTransition(4, b);
        LockTransition e5 = addTransition(b, 5);
        LockTransition e6 = addTransition(5, 7);
        LockTransition e7 = addTransition(7, 2);
        LockTransition e8 = addTransition(2, a);
        LockTransition e9 = addTransition(a, 5);
        LockTransition e10 = addTransition(5, 8);
        LockTransition e11 = addTransition(8, 9);
        LockTransition e12 = addTransition(9, 5);
        mCycleDetector.analyzeLockNodes(mNodes);
        addExpectedCycle(new LockTransition[] {e3, e4});
        addExpectedCycle(new LockTransition[] {e10, e11, e12});
        addExpectedCycle(new LockTransition[] {e8, e9, e6, e7});
        addExpectedCycle(new LockTransition[] {e2, e5, e6, e7});
        assertExpectedCycles();
    }


    @Test
    public void testLotsOfCycles() {
        int a = 1;
        int b = 2;
        int c = 3;
        int d = 4;

        LockTransition e1 = addTransition(a, b);
        LockTransition e2 = addTransition(a, c);
        LockTransition e3 = addTransition(a, d);
        LockTransition e4 = addTransition(b, a);
        LockTransition e5 = addTransition(b, c);
        LockTransition e6 = addTransition(c, b);
        LockTransition e7 = addTransition(c, d);
        LockTransition e8 = addTransition(d, b);
        mCycleDetector.analyzeLockNodes(mNodes);
        addExpectedCycle(new LockTransition[] {e1, e4});
        addExpectedCycle(new LockTransition[] {e5, e6});
        addExpectedCycle(new LockTransition[] {e2, e6, e4});
        addExpectedCycle(new LockTransition[] {e3, e8, e4});
        addExpectedCycle(new LockTransition[] {e5, e7, e8});
        addExpectedCycle(new LockTransition[] {e2, e7, e8, e4});
        assertExpectedCycles();
    }


    @Test
    public void testComplexCycles2() {
        int a = 6;
        int b = 3;
        LockTransition e1 = addTransition(2, b);
        LockTransition e2 = addTransition(b, 5);
        LockTransition e3 = addTransition(5, 7);
        LockTransition e4 = addTransition(7, 2);
        LockTransition e5 = addTransition(2, a);
        LockTransition e6 = addTransition(a, 5);
        mCycleDetector.analyzeLockNodes(mNodes);
        addExpectedCycle(new LockTransition[] {e5, e6, e3, e4 });
        addExpectedCycle(new LockTransition[] {e1, e2, e3, e4 });
        assertExpectedCycles();
    }


    @Test
    public void testTwoSeparateCycles() {
        LockTransition e1 = addTransition(1, 2);
        LockTransition e2 = addTransition(2, 1);
        LockTransition e3 = addTransition(3, 4);
        LockTransition e4 = addTransition(4, 3);
        addTransition(4, 5);
        mCycleDetector.analyzeLockNodes(mNodes);
        addExpectedCycle(new LockTransition[] {e1, e2});
        addExpectedCycle(new LockTransition[] {e3, e4});
        assertExpectedCycles();
    }

    @Test
    public void testTwoConnectedCycles() {
        LockTransition e1 = addTransition(1, 2);
        LockTransition e2 = addTransition(2, 1);
        LockTransition e3 = addTransition(2, 3);
        LockTransition e4 = addTransition(3, 2);
        addTransition(2, 4);
        addTransition(3, 4);
        mCycleDetector.analyzeLockNodes(mNodes);
        addExpectedCycle(new LockTransition[] {e1, e2});
        addExpectedCycle(new LockTransition[] {e3, e4});
        assertExpectedCycles();
    }

    @Test
    public void testDuplicatedEdges() {
        LockTransition e1 = addTransition(1, 2);
        LockTransition e2 = addTransition(1, 2, 10);
        LockTransition e3 = addTransition(2, 1);
        addTransition(3, 4);
        addTransition(4, 5);
        addTransition(4, 5, 10);
        mCycleDetector.analyzeLockNodes(mNodes);
        addExpectedCycle(new LockTransition[] {e1, e3});
        addExpectedCycle(new LockTransition[] {e2, e3});
        assertExpectedCycles();
    }
}
