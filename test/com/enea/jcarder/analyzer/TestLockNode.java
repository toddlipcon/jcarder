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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.Collection;
import java.util.HashMap;
import org.junit.Before;
import org.junit.Test;

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
        addOutgoingEdge(node1, node1, node2, 5, 5, 5);
        addOutgoingEdge(node1, node1, node2, 5, 5, 5);
        addOutgoingEdge(node1, node1, node2, 5, 5, 5);
        addOutgoingEdge(node1, node1, node3, 5, 5, 5);
        addOutgoingEdge(node1, node1, node2, 6, 5, 5);
        addOutgoingEdge(node1, node1, node2, 5, 6, 5);
        addOutgoingEdge(node1, node1, node2, 5, 5, 6);
        assertEquals(5L, node1.numberOfUniqueTransitions());
        assertEquals(2L, node1.numberOfDuplicatedTransitions());
    }

    @Test
    public void testPopulateContextIdTranslationMap() {
        addOutgoingEdge(node1, node1, node2, 1, 2, 3);
        addOutgoingEdge(node1, node1, node2, 1, 4, 5);
        addOutgoingEdge(node1, node1, node2, 1, 4, 6);
        addOutgoingEdge(node1, node1, node2, 1, 4, 6);
        addOutgoingEdge(node1, node1, node3, 1, 4, 6);
        addOutgoingEdge(node1, node1, node3, 1, 7, 8);
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
        addOutgoingEdge(node1, node1, node2, 1, 2, 3);
        addOutgoingEdge(node1, node1, node2, 1, 2, 3);
        addOutgoingEdge(node1, node1, node2, 99, 2, 3);
        addOutgoingEdge(node1, node1, node2, 1, 4, 5);
        addOutgoingEdge(node1, node1, node2, 1, 6, 7);
        addOutgoingEdge(node1, node1, node3, 1, 2, 3);
        assertEquals(1L, node1.numberOfDuplicatedTransitions());
        assertEquals(5L, node1.numberOfUniqueTransitions());
        final HashMap<Integer, Integer> translationMap =
            new HashMap<Integer, Integer>();
        translationMap.put(2, 12);
        translationMap.put(3, 13);
        translationMap.put(4, 12);
        translationMap.put(5, 13);
        translationMap.put(6, 6);
        node1.translateContextIds(translationMap);
        assertEquals(2L, node1.numberOfDuplicatedTransitions());
        assertEquals(4L, node1.numberOfUniqueTransitions());
        final Collection<LockEdge> edges = node1.getOutgoingEdges();
        assertTrue(contains(edges, createEdge(node1, node2, 1, 12, 13)));
        assertTrue(contains(edges, createEdge(node1, node2, 99, 12, 13)));
        assertTrue(contains(edges, createEdge(node1, node2, 1, 6, 7)));
        assertTrue(contains(edges, createEdge(node1, node3, 1, 12, 13)));
    }

    private static void addOutgoingEdge(LockNode source, LockNode node1, LockNode node2, int threadId, int sourceContextId, int targetContextId) {
        source.addOutgoingEdge(new LockEdge(node1, node2))
                .addTransition(new LockTransition(threadId, sourceContextId, targetContextId));
    }
    
    private static LockEdge createEdge(LockNode node1, LockNode node2, int threadId, int sourceContextId, int targetContextId) {
        LockEdge lockEdge = new LockEdge(node1, node2);
        lockEdge.addTransition(new LockTransition(threadId, sourceContextId, targetContextId));
        return lockEdge;
    }

    private boolean contains(Collection<LockEdge> edges, LockEdge edge) {
        for (LockEdge lockEdge : edges) {
            if (lockEdge.equals(edge)) {
                if (lockEdge.getTransitions().containsAll(edge.getTransitions())) {
                    return true;
                }
            }
        }
        return false;
    }
}
