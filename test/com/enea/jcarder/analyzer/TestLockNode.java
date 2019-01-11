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
    public void setUp() {
        node1 = new LockNode(1);
        node2 = new LockNode(2);
        node3 = new LockNode(3);
    }

    @Test
    public void testAddOutgoingEdge() {
        node1.addOutgoingEdge(new LockEdge(node1, node2, new LockTransition(5, 5, 5)));
        node1.addOutgoingEdge(new LockEdge(node1, node2, new LockTransition(5, 5, 5)));
        node1.addOutgoingEdge(new LockEdge(node1, node2, new LockTransition(5, 5, 5)));
        node1.addOutgoingEdge(new LockEdge(node1, node3, new LockTransition(5, 5, 5)));
        node1.addOutgoingEdge(new LockEdge(node1, node2, new LockTransition(6, 5, 5)));
        node1.addOutgoingEdge(new LockEdge(node1, node2, new LockTransition(5, 6, 5)));
        node1.addOutgoingEdge(new LockEdge(node1, node2, new LockTransition(5, 5, 6)));
        assertEquals(5L, node1.numberOfUniqueTransitions());
        assertEquals(2L, node1.numberOfDuplicatedTransitions());
    }

    @Test
    public void testPopulateContextIdTranslationMap() {
        node1.addOutgoingEdge(new LockEdge(node1, node2, new LockTransition(1, 2, 3)));
        node1.addOutgoingEdge(new LockEdge(node1, node2, new LockTransition(1, 4, 5)));
        node1.addOutgoingEdge(new LockEdge(node1, node2, new LockTransition(1, 4, 6)));
        node1.addOutgoingEdge(new LockEdge(node1, node2, new LockTransition(1, 4, 6)));
        node1.addOutgoingEdge(new LockEdge(node1, node3, new LockTransition(1, 4, 6)));
        node1.addOutgoingEdge(new LockEdge(node1, node3, new LockTransition(1, 7, 8)));
        final HashMap<Integer, Integer> translationMap =
                new HashMap<>();
        node1.populateContextIdTranslationMap(translationMap);
        final HashMap<Integer, Integer> expectedTranslationMap =
                new HashMap<>();
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
        node1.addOutgoingEdge(new LockEdge(node1, node2, new LockTransition(1, 2, 3)));
        node1.addOutgoingEdge(new LockEdge(node1, node2, new LockTransition(1, 2, 3)));
        node1.addOutgoingEdge(new LockEdge(node1, node2, new LockTransition(99, 2, 3)));
        node1.addOutgoingEdge(new LockEdge(node1, node2, new LockTransition(1, 4, 5)));
        node1.addOutgoingEdge(new LockEdge(node1, node2, new LockTransition(1, 6, 7)));
        node1.addOutgoingEdge(new LockEdge(node1, node3, new LockTransition(1, 2, 3)));
        assertEquals(1L, node1.numberOfDuplicatedTransitions());
        assertEquals(5L, node1.numberOfUniqueTransitions());
        final HashMap<Integer, Integer> translationMap =
                new HashMap<>();
        translationMap.put(2, 12);
        translationMap.put(3, 13);
        translationMap.put(4, 12);
        translationMap.put(5, 13);
        translationMap.put(6, 6);
        node1.translateContextIds(translationMap);
        assertEquals(2L, node1.numberOfDuplicatedTransitions());
        assertEquals(4L, node1.numberOfUniqueTransitions());
        final Collection<LockEdge> edges = node1.getOutgoingEdges();
        assertTrue(edges.contains(new LockEdge(node1, node2, new LockTransition(1, 12, 13))));
        assertTrue(edges.contains(new LockEdge(node1, node2, new LockTransition(99, 12, 13))));
        assertTrue(edges.contains(new LockEdge(node1, node2, new LockTransition(1, 6, 7))));
        assertTrue(edges.contains(new LockEdge(node1, node3, new LockTransition(1, 12, 13))));
    }
}
