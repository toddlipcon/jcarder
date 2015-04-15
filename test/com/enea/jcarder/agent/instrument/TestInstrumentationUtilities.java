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

package com.enea.jcarder.agent.instrument;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Assert;
import org.junit.Test;

import com.enea.jcarder.common.Lock;

public final class TestInstrumentationUtilities {
    @Test
    public void testCountParameters() {
        assertEquals(3, InstrumentationUtilities.countParameters(
                         "(IDLjava/lang/Thread;)Ljava/lang/Object;"));
        assertEquals(3, InstrumentationUtilities.countParameters(
                         "(IDZ)Ljava/lang/Object;"));
        assertEquals(0, InstrumentationUtilities.countParameters(
                         "()Z"));
    }
}
