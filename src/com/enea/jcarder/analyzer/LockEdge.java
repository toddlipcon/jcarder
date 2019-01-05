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

import net.jcip.annotations.NotThreadSafe;

import com.enea.jcarder.common.contexts.ContextReaderIfc;

/**
 * A LockEdge instance represents a directed edge from a source LockNode to a
 * target LockNode.
 * Could contain several transitions
 */
@NotThreadSafe
class LockEdge {
    private final LockNode mSource;
    private final LockNode mTarget;
    private Map<LockTransition, LockTransition> mTransitions;

    LockEdge(LockNode source, LockNode target) {
        mSource = source;
        mTarget = target;
        mTransitions = new HashMap<>();
    }

    void addTransition(LockTransition newTransition) {
        LockTransition existingTransition = mTransitions.get(newTransition);
        if (existingTransition == null) {
            mTransitions.put(newTransition, newTransition);
        } else {
            existingTransition.merge(newTransition);
        }
    }

    Collection<LockTransition> getTransitions() {
        return mTransitions.values();
    }

    long numberOfUniqueTransitions() {
        return mTransitions.size();
    }

    long numberOfDuplicatedTransitions() {
        long numberOfDuplicatedEdges = 0;
        for (LockTransition transition : mTransitions.values()) {
            numberOfDuplicatedEdges += transition.getDuplicates();
        }
        return numberOfDuplicatedEdges;
    }

    void populateContextIdTranslationMap(Map<Integer, Integer> translationMap) {
        for (LockTransition transition : mTransitions.values()) {
            translationMap.put(transition.getSourceLockingContextId(),
                               transition.getSourceLockingContextId());
            translationMap.put(transition.getTargetLockingContextId(),
                               transition.getTargetLockingContextId());
        }
    }

    void translateContextIds(Map<Integer, Integer> translation) {
        Map<LockTransition, LockTransition> oldTransitions = mTransitions;
        mTransitions = new HashMap<>(oldTransitions.size());
        for (LockTransition edge : oldTransitions.values()) {
            edge.translateContextIds(translation);
            addTransition(edge);
        }
    }

    void removeAlikeTransitions(ContextReaderIfc reader) {
        if (mTransitions.size() > 1) {
            Collection<LockTransition> uniqueTransitions = new ArrayList<>();
            Iterator<LockTransition> iter = mTransitions.values().iterator();
            while (iter.hasNext()) {
                final LockTransition transition = iter.next();
                if (containsAlike(transition, uniqueTransitions, reader)) {
                    iter.remove();
                } else {
                    uniqueTransitions.add(transition);
                }
            }
        }
    }

    private boolean containsAlike(LockTransition transition, Collection<LockTransition> others, ContextReaderIfc reader) {
        for (LockTransition other : others) {
            if (transition.alike(other, reader)) {
                return true;
            }
        }
        return false;
    }

    boolean alike(LockEdge other, ContextReaderIfc reader) {
        if (!mSource.alike(other.mSource, reader)
                || !mTarget.alike(other.mTarget, reader)
                || mTransitions.size() != other.mTransitions.size()) {
            return false;
        }

        // TODO Refactor the following code?
        LinkedList<LockTransition> otherTransitions =
            new LinkedList<>(other.mTransitions.values());
        outerLoop:
        for (LockTransition transition : mTransitions.values()) {
            Iterator<LockTransition> iter = otherTransitions.iterator();
            while (iter.hasNext()) {
                if (transition.alike(iter.next(), reader)) {
                    iter.remove();
                    continue outerLoop;
                }
            }
            return false;
        }
        return true;
    }

    public boolean equals(Object obj) {
        /*
         * TODO It might be a potential problem to use LockEdges in HashMaps
         * since they are mutable and this equals method depends on them?
         */
        try {
            LockEdge other = (LockEdge) obj;
            return (mTarget.getLockId() == other.mTarget.getLockId())
            && (mSource.getLockId() == other.mSource.getLockId())
//            && (mTransitions.equals(other.mTransitions))
            ;
        } catch (Exception e) {
            return false;
        }
    }

    public int hashCode() {
        final int prime = 31;
        int result = prime + mSource.getLockId();
        result = prime * result + mTarget.getLockId();
//        for (LockTransition transition : mTransitions.values())
//            result = prime * result + transition.hashCode();
        return result;
    }

    LockNode getTarget() {
        return mTarget;
    }

    LockNode getSource() {
        return mSource;
    }

    long getUniqueThreadId() {
        long uniqueThreadId = -1;
        for (LockTransition transition : mTransitions.values()) {
            long transitionThreadId = transition.getThreadId();
            if (uniqueThreadId == -1) {
                uniqueThreadId = transitionThreadId;
            } else if (uniqueThreadId != transitionThreadId) {
                return -1;
            }
        }
        return uniqueThreadId;
    }

    boolean hasUniqueThreadId(long threadId) {
        for (LockTransition transition : mTransitions.values()) {
            if (transition.getThreadId() != threadId) {
                return false;
            }
        }
        return true;
    }

    Set<Integer> getCommonGateLockIds() {
        Set<Integer> commonGateLockIds;
        Iterator<LockTransition> iter = mTransitions.values().iterator();
        LockTransition firstTransition = iter.next();
        // optimization for single transition
        if (iter.hasNext()) {
            commonGateLockIds = new HashSet<>(firstTransition.getGateLockIds());
            while (iter.hasNext()) {
                commonGateLockIds.retainAll(iter.next().getGateLockIds());
            }
        } else {
            commonGateLockIds = firstTransition.getGateLockIds();
        }
        return commonGateLockIds;
    }

    public String toString() {
        return "  " + mSource + "->" + mTarget;
    }
}
