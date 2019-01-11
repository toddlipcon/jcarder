package com.enea.jcarder.analyzer;

import com.enea.jcarder.common.contexts.ContextReaderIfc;

import java.util.*;

/**
 * Same as {@link LockEdge} but could contain several transitions
 */
public class LockMultiEdge extends LockEdge {
    private Map<LockTransition, LockTransition> mTransitions;

    LockMultiEdge(LockNode source, LockNode target) {
        super(source, target, null);
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

    @Override
    void merge(LockEdge other) {
        // multi-edges cannot merge
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


    boolean alike(LockEdge otherEdge, ContextReaderIfc reader) {
        LockMultiEdge other = (LockMultiEdge) otherEdge;
        if (!getSource().alike(other.getSource(), reader)
                || !getTarget().alike(other.getTarget(), reader)
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
            return (getTarget().getLockId() == other.getTarget().getLockId())
            && (getSource().getLockId() == other.getSource().getLockId())
//            && (mTransitions.equals(other.mTransitions))
            ;
        } catch (Exception e) {
            return false;
        }
    }

    public int hashCode() {
        final int prime = 31;
        int result = prime + getSource().getLockId();
        result = prime * result + getTarget().getLockId();
//        for (LockTransition transition : mTransitions.values())
//            result = prime * result + transition.hashCode();
        return result;
    }

    long getThreadId() {
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

    boolean hasThreadId(long threadId) {
        for (LockTransition transition : mTransitions.values()) {
            if (transition.getThreadId() != threadId) {
                return false;
            }
        }
        return true;
    }

    Set<Integer> getGateLockIds() {
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
        return "  " + getSource() + "->" + getTarget();
    }
}
