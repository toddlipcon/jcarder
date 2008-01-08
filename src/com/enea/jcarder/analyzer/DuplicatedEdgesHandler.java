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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

import com.enea.jcarder.common.LockingContext;
import com.enea.jcarder.common.contexts.ContextReaderIfc;

/**
 * This class contains functionality for merging edges that have the same source
 * and target nodes and identical thread IDs and locking contexts content, but
 * different locking context IDs.
 *
 * Such a merge might be desirable since the producer of the context file is not
 * required to guarantee that identical locking contexts always get the same
 * IDs.
 *
 * @TODO Add basic tests for this class.
 */
public final class DuplicatedEdgesHandler {
    private final Iterable<LockNode> mLockNodes;
    private final Map<Integer, Integer> mContextIdTranslation;
    private final Map<LockingContext, TreeSet<Integer>> mContextToIdMap;

    /**
     * The constructor is made private to prevent that someone creates an
     * instance of this class and then forgets to release the reference to it.
     * That would be undesirable since the mContextToIdMap structure in this
     * class might be very large and should be garbage collected as soon as
     * possible.
     *
     * @see DuplicatedEdgesHandler.mergeDuplicatedEdges() instead.
     */
    private DuplicatedEdgesHandler(Iterable<LockNode> lockNodes,
                                   ContextReaderIfc reader) {
        mLockNodes = lockNodes;
        mContextIdTranslation = populateTranslationMap();
        mContextToIdMap = createContextToIdMap(reader);
    }

    public static void mergeDuplicatedEdges(Iterable<LockNode> lockNodes,
                                            ContextReaderIfc reader) {
        DuplicatedEdgesHandler handler = new DuplicatedEdgesHandler(lockNodes,
                                                                    reader);
        handler.updateContextIdTranslationMap();
        handler.updateEdgesWithTranslationMap();
    }

    private void updateEdgesWithTranslationMap() {
        for (LockNode node : mLockNodes) {
            node.translateContextIds(mContextIdTranslation);
        }
    }

    private Map<Integer, Integer> populateTranslationMap() {
        final HashMap<Integer, Integer> contextIds =
            new HashMap<Integer, Integer>();
        for (LockNode node : mLockNodes) {
            node.populateContextIdTranslationMap(contextIds);
        }
        return contextIds;
    }

    private Map<LockingContext, TreeSet<Integer>>
    createContextToIdMap(ContextReaderIfc reader) {
        final Map<LockingContext, TreeSet<Integer>> contextToId =
            new HashMap<LockingContext, TreeSet<Integer>>();
        for (Integer id : mContextIdTranslation.values()) {
            LockingContext context = reader.readContext(id);
            TreeSet<Integer> ids = contextToId.get(context);
            if (ids == null) {
                ids = new TreeSet<Integer>();
                contextToId.put(context, ids);
            }
            ids.add(id);
        }
        return contextToId;
    }

    private void updateContextIdTranslationMap() {
        for (TreeSet<Integer> ids : mContextToIdMap.values()) {
            if (ids.size() > 1) {
                Iterator<Integer> iter = ids.iterator();
                Integer firstId = iter.next();
                while (iter.hasNext()) {
                    mContextIdTranslation.put(iter.next(), firstId);
                }
            }
        }
    }
}
