package com.enea.jcarder.analyzer;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.enea.jcarder.analyzer.Cycle;
import com.enea.jcarder.analyzer.CycleDetector;
import com.enea.jcarder.analyzer.LockEdge;
import com.enea.jcarder.analyzer.LockGraphBuilder;
import com.enea.jcarder.analyzer.LockNode;
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
    HashSet<Cycle> mExpectedCycles;

    @Before
    public void setUp() throws Exception {
        mBuilder = new LockGraphBuilder();
        mCycleDetector = new CycleDetector(new Logger(null));
        mNodes = new LinkedList<LockNode>();
        mExpectedCycles = new HashSet<Cycle>();
    }

    private LockEdge addEdge(int from, int to) {
        return addEdge(from, to, -1);
    }

    private LockEdge addEdge(int from, int to, int threadId) {
        final LockNode sourceLock = mBuilder.getLockNode(from);
        final LockNode targetLock = mBuilder.getLockNode(to);
        final LockEdge edge = new LockEdge(sourceLock,
                                           targetLock,
                                           threadId,
                                           -1,
                                           -1);
        sourceLock.addOutgoingEdge(edge);
        if (!mNodes.contains(sourceLock)) {
            mNodes.add(sourceLock);
        }
        if (!mNodes.contains(sourceLock)) {
            mNodes.add(sourceLock);
        }
        return edge;
    }

    private void addExpectedCycle(LockEdge[] edges) {
        Cycle cycle = new Cycle(Arrays.asList(edges));
        mExpectedCycles.add(cycle);
    }

    private void assertExpectedCycles() {
        Assert.assertEquals(mExpectedCycles, mCycleDetector.getCycles());
    }

    @Test
    public void testNoCycle() throws Exception {
        addEdge(1, 2);
        addEdge(2, 3);
        addEdge(1, 3);
        addEdge(3, 4);
        mCycleDetector.analyzeLockNodes(mNodes);
        assertExpectedCycles();
    }

    @Test
    public void testSmallCycle() throws Exception {
        LockEdge e1 = addEdge(1, 2);
        LockEdge e2 = addEdge(2, 1);
        mCycleDetector.analyzeLockNodes(mNodes);
        addExpectedCycle(new LockEdge[] {e1, e2});
        assertExpectedCycles();
    }

    @Test
    public void testCycle() throws Exception {
        addEdge(1, 2);
        LockEdge e2 = addEdge(2, 3);
        LockEdge e3 = addEdge(3, 4);
        LockEdge e4 = addEdge(4, 2);
        addEdge(4, 5);
        mCycleDetector.analyzeLockNodes(mNodes);
        addExpectedCycle(new LockEdge[] {e2, e3, e4});
        assertExpectedCycles();
    }

    @Test
    public void testCyclesWithTwoPaths() throws Exception {
        addEdge(1, 2);
        LockEdge e2 = addEdge(2, 3);
        LockEdge e3 = addEdge(3, 4);
        LockEdge e4 = addEdge(2, 4);
        LockEdge e5 = addEdge(4, 2);
        mCycleDetector.analyzeLockNodes(mNodes);
        addExpectedCycle(new LockEdge[] {e2, e3, e5});
        addExpectedCycle(new LockEdge[] {e4, e5});
        assertExpectedCycles();
    }

    @Test
    public void testCyclesWithTwoTimesTwoPathsLarge() throws Exception {
        LockEdge e1 = addEdge(1, 2);
        LockEdge e2 = addEdge(2, 3);
        LockEdge e3 = addEdge(3, 4);
        LockEdge e3b = addEdge(3, 4, 2);
        LockEdge e4 = addEdge(4, 5);
        LockEdge e5 = addEdge(5, 6);
        LockEdge e6 = addEdge(6, 1);
        LockEdge e6b = addEdge(6, 1, 2);
        mCycleDetector.analyzeLockNodes(mNodes);
        addExpectedCycle(new LockEdge[] {e1, e2, e3, e4, e5, e6});
        addExpectedCycle(new LockEdge[] {e1, e2, e3b, e4, e5, e6});
        addExpectedCycle(new LockEdge[] {e1, e2, e3b, e4, e5, e6b});
        addExpectedCycle(new LockEdge[] {e1, e2, e3, e4, e5, e6b});
        assertExpectedCycles();
    }

    @Test
    public void testCyclesWithTwoTimesTwoPathsSmall() throws Exception {
        LockEdge e1 = addEdge(1, 2);
        LockEdge e2 = addEdge(2, 1);
        LockEdge e1b = addEdge(1, 2, 2);
        LockEdge e2b = addEdge(2, 1, 2);
        mCycleDetector.analyzeLockNodes(mNodes);
        addExpectedCycle(new LockEdge[] {e1, e2});
        addExpectedCycle(new LockEdge[] {e1b, e2});
        addExpectedCycle(new LockEdge[] {e1, e2b});
        addExpectedCycle(new LockEdge[] {e1b, e2b});
        assertExpectedCycles();
    }


    @Test
    public void testCyclesWithTwoAlternateWays() throws Exception {
        LockEdge e1 = addEdge(1, 2);
        LockEdge e2 = addEdge(1, 3);
        LockEdge e3 = addEdge(2, 4);
        LockEdge e4 = addEdge(3, 4);
        LockEdge e5 = addEdge(4, 5);
        LockEdge e6 = addEdge(4, 6);
        LockEdge e7 = addEdge(5, 1);
        LockEdge e8 = addEdge(6, 1);

        mCycleDetector.analyzeLockNodes(mNodes);
        addExpectedCycle(new LockEdge[] {e1, e3, e5, e7});
        addExpectedCycle(new LockEdge[] {e1, e3, e6, e8});
        addExpectedCycle(new LockEdge[] {e2, e4, e5, e7});
        addExpectedCycle(new LockEdge[] {e2, e4, e6, e8});
        assertExpectedCycles();
    }


    @Test
    public void testComplexCycles() throws Exception {
        int a = 6;
        int b = 3;
        addEdge(1, 2);
        LockEdge e2 = addEdge(2, b);
        LockEdge e3 = addEdge(b, 4);
        LockEdge e4 = addEdge(4, b);
        LockEdge e5 = addEdge(b, 5);
        LockEdge e6 = addEdge(5, 7);
        LockEdge e7 = addEdge(7, 2);
        LockEdge e8 = addEdge(2, a);
        LockEdge e9 = addEdge(a, 5);
        LockEdge e10 = addEdge(5, 8);
        LockEdge e11 = addEdge(8, 9);
        LockEdge e12 = addEdge(9, 5);
        mCycleDetector.analyzeLockNodes(mNodes);
        addExpectedCycle(new LockEdge[] {e3, e4});
        addExpectedCycle(new LockEdge[] {e10, e11, e12});
        addExpectedCycle(new LockEdge[] {e8, e9, e6, e7});
        addExpectedCycle(new LockEdge[] {e2, e5, e6, e7});
        assertExpectedCycles();
    }


    @Test
    public void testLotsOfCycles() throws Exception {
        int a = 1;
        int b = 2;
        int c = 3;
        int d = 4;

        LockEdge e1 = addEdge(a, b);
        LockEdge e2 = addEdge(a, c);
        LockEdge e3 = addEdge(a, d);
        LockEdge e4 = addEdge(b, a);
        LockEdge e5 = addEdge(b, c);
        LockEdge e6 = addEdge(c, b);
        LockEdge e7 = addEdge(c, d);
        LockEdge e8 = addEdge(d, b);
        mCycleDetector.analyzeLockNodes(mNodes);
        addExpectedCycle(new LockEdge[] {e1, e4});
        addExpectedCycle(new LockEdge[] {e5, e6});
        addExpectedCycle(new LockEdge[] {e2, e6, e4});
        addExpectedCycle(new LockEdge[] {e3, e8, e4});
        addExpectedCycle(new LockEdge[] {e5, e7, e8});
        addExpectedCycle(new LockEdge[] {e2, e7, e8, e4});
        assertExpectedCycles();
    }


    @Test
    public void testComplexCycles2() throws Exception {
        int a = 6;
        int b = 3;
        LockEdge e1 = addEdge(2, b);
        LockEdge e2 = addEdge(b, 5);
        LockEdge e3 = addEdge(5, 7);
        LockEdge e4 = addEdge(7, 2);
        LockEdge e5 = addEdge(2, a);
        LockEdge e6 = addEdge(a, 5);
        mCycleDetector.analyzeLockNodes(mNodes);
        addExpectedCycle(new LockEdge[] {e5, e6, e3, e4 });
        addExpectedCycle(new LockEdge[] {e1, e2, e3, e4 });
        assertExpectedCycles();
    }


    @Test
    public void testTwoSeparateCycles() throws Exception {
        LockEdge e1 = addEdge(1, 2);
        LockEdge e2 = addEdge(2, 1);
        LockEdge e3 = addEdge(3, 4);
        LockEdge e4 = addEdge(4, 3);
        addEdge(4, 5);
        mCycleDetector.analyzeLockNodes(mNodes);
        addExpectedCycle(new LockEdge[] {e1, e2});
        addExpectedCycle(new LockEdge[] {e3, e4});
        assertExpectedCycles();
    }

    @Test
    public void testTwoConnectedCycles() throws Exception {
        LockEdge e1 = addEdge(1, 2);
        LockEdge e2 = addEdge(2, 1);
        LockEdge e3 = addEdge(2, 3);
        LockEdge e4 = addEdge(3, 2);
        addEdge(2, 4);
        addEdge(3, 4);
        mCycleDetector.analyzeLockNodes(mNodes);
        addExpectedCycle(new LockEdge[] {e1, e2});
        addExpectedCycle(new LockEdge[] {e3, e4});
        assertExpectedCycles();
    }

    @Test
    public void testDuplicatedEdges() throws Exception {
        LockEdge e1 = addEdge(1, 2);
        LockEdge e2 = addEdge(1, 2, 10);
        LockEdge e3 = addEdge(2, 1);
        addEdge(3, 4);
        addEdge(4, 5);
        addEdge(4, 5, 10);
        mCycleDetector.analyzeLockNodes(mNodes);
        addExpectedCycle(new LockEdge[] {e1, e3});
        addExpectedCycle(new LockEdge[] {e2, e3});
        assertExpectedCycles();
    }
}
