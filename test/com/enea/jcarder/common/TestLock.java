package com.enea.jcarder.common;

import org.junit.Assert;
import org.junit.Test;

import com.enea.jcarder.common.Lock;

public final class TestLock {

    @Test
    public void testGetClassName() {
        Lock lock = new Lock(this);
        Assert.assertEquals(getClass().getName(), lock.getClassName());
    }
}
