package com.enea.jcarder.analyzer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.Collection;
import java.util.HashMap;
import org.junit.Before;
import org.junit.Test;

import com.enea.jcarder.analyzer.LockEdge;
import com.enea.jcarder.analyzer.LockNode;

public final class TestLockNode {
    private LockNode node1;
    private LockNode node2;
    private LockNode node3;

    @Before
    public void setUp() throws Exception {
        node1 = new LockNode(1);
        node2 = new LockNode(2);
        node3 = new LockNode(3);
    }

    @Test
    public void testAddOutgoingEdge() {
        node1.addOutgoingEdge(new LockEdge(node1, node2, 5, 5, 5));
        node1.addOutgoingEdge(new LockEdge(node1, node2, 5, 5, 5));
        node1.addOutgoingEdge(new LockEdge(node1, node2, 5, 5, 5));
        node1.addOutgoingEdge(new LockEdge(node1, node3, 5, 5, 5));
        node1.addOutgoingEdge(new LockEdge(node1, node2, 6, 5, 5));
        node1.addOutgoingEdge(new LockEdge(node1, node2, 5, 6, 5));
        node1.addOutgoingEdge(new LockEdge(node1, node2, 5, 5, 6));
        assertEquals(5L, node1.numberOfUniqueEdges());
        assertEquals(2L, node1.numberOfDuplicatedEdges());
    }

    @Test
    public void testPopulateContextIdTranslationMap() {
        node1.addOutgoingEdge(new LockEdge(node1, node2, 1, 2, 3));
        node1.addOutgoingEdge(new LockEdge(node1, node2, 1, 4, 5));
        node1.addOutgoingEdge(new LockEdge(node1, node2, 1, 4, 6));
        node1.addOutgoingEdge(new LockEdge(node1, node2, 1, 4, 6));
        node1.addOutgoingEdge(new LockEdge(node1, node3, 1, 4, 6));
        node1.addOutgoingEdge(new LockEdge(node1, node3, 1, 7, 8));
        final HashMap<Integer, Integer> translationMap =
            new HashMap<Integer, Integer>();
        node1.populateContextIdTranslationMap(translationMap);
        final HashMap<Integer, Integer> expectedTranslationMap =
            new HashMap<Integer, Integer>();
        expectedTranslationMap.put(2, 2);
        expectedTranslationMap.put(3, 3);
        expectedTranslationMap.put(4, 4);
        expectedTranslationMap.put(5, 5);
        expectedTranslationMap.put(6, 6);
        expectedTranslationMap.put(7, 7);
        expectedTranslationMap.put(8, 8);
        assertEquals(expectedTranslationMap, translationMap);
    }

    @Test
    public void testUpdateContextIdsInEdges() {
        node1.addOutgoingEdge(new LockEdge(node1, node2, 1, 2, 3));
        node1.addOutgoingEdge(new LockEdge(node1, node2, 1, 2, 3));
        node1.addOutgoingEdge(new LockEdge(node1, node2, 99, 2, 3));
        node1.addOutgoingEdge(new LockEdge(node1, node2, 1, 4, 5));
        node1.addOutgoingEdge(new LockEdge(node1, node2, 1, 6, 7));
        node1.addOutgoingEdge(new LockEdge(node1, node3, 1, 2, 3));
        assertEquals(1L, node1.numberOfDuplicatedEdges());
        assertEquals(5L, node1.numberOfUniqueEdges());
        final HashMap<Integer, Integer> translationMap =
            new HashMap<Integer, Integer>();
        translationMap.put(2, 12);
        translationMap.put(3, 13);
        translationMap.put(4, 12);
        translationMap.put(5, 13);
        translationMap.put(6, 6);
        node1.translateContextIds(translationMap);
        assertEquals(2L, node1.numberOfDuplicatedEdges());
        assertEquals(4L, node1.numberOfUniqueEdges());
        final Collection<LockEdge> edges = node1.getOutgoingEdges();
        assertTrue(edges.contains(new LockEdge(node1, node2, 1, 12, 13)));
        assertTrue(edges.contains(new LockEdge(node1, node2, 99, 12, 13)));
        assertTrue(edges.contains(new LockEdge(node1, node2, 1, 6, 7)));
        assertTrue(edges.contains(new LockEdge(node1, node3, 1, 12, 13)));
    }
}
