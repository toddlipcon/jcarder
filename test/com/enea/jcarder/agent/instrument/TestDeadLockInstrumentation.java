/*
 * JCarder -- cards Java programs to keep threads disentangled
 *
 * Copyright (C) 2006-2007 Enea AB
 * Copyright (C) 2007 Ulrik Svensson
 * Copyright (C) 2007 Joel Rosdahl
 *
 * This program is made available under the GNU GPL version 2. See the
 * accompanying file LICENSE.txt for details.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.enea.jcarder.agent.instrument;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.File;
import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.enea.jcarder.agent.EventListenerIfc;
import com.enea.jcarder.agent.StaticEventListener;
import com.enea.jcarder.common.LockingContext;
import com.enea.jcarder.testclasses.instrumentation.SynchronizationTestIfc;
import com.enea.jcarder.testclasses.instrumentation.SynchronizedArray;
import com.enea.jcarder.testclasses.instrumentation.SynchronizedClass;
import com.enea.jcarder.testclasses.instrumentation.SynchronizedExpression;
import com.enea.jcarder.testclasses.instrumentation.SynchronizedField;
import com.enea.jcarder.testclasses.instrumentation.SynchronizedFromLocalVariable;
import com.enea.jcarder.testclasses.instrumentation.SynchronizedFromMethod;
import com.enea.jcarder.testclasses.instrumentation.SynchronizedFromPrivateMethod;
import com.enea.jcarder.testclasses.instrumentation.SynchronizedFromStaticMethod;
import com.enea.jcarder.testclasses.instrumentation.SynchronizedInBlock;
import com.enea.jcarder.testclasses.instrumentation.SynchronizedInStaticBlock;
import com.enea.jcarder.testclasses.instrumentation.SynchronizedMethod;
import com.enea.jcarder.testclasses.instrumentation.SynchronizedMethodWithDoubleReturn;
import com.enea.jcarder.testclasses.instrumentation.SynchronizedMethodWithException;
import com.enea.jcarder.testclasses.instrumentation.SynchronizedMethodWithLongReturn;
import com.enea.jcarder.testclasses.instrumentation.SynchronizedMethodWithMultipleFloatReturns;
import com.enea.jcarder.testclasses.instrumentation.SynchronizedMethodWithObjectReturn;
import com.enea.jcarder.testclasses.instrumentation.SynchronizedNewObject;
import com.enea.jcarder.testclasses.instrumentation.SynchronizedNull;
import com.enea.jcarder.testclasses.instrumentation.SynchronizedStaticField;
import com.enea.jcarder.testclasses.instrumentation.SynchronizedStaticMethod;
import com.enea.jcarder.testclasses.instrumentation.SynchronizedStaticMethodWithException;
import com.enea.jcarder.testclasses.instrumentation.SynchronizedStaticMethodWithMultipleIntReturns;
import com.enea.jcarder.testclasses.instrumentation.SynchronizedThis;
import com.enea.jcarder.util.logging.Logger;

/*
 * The purpose of this junit class is to test the classes:
 *  - MonitorClassAdapter
 *  - MonitorMethodAdapter
 *  - StackAnalyzeMethodVisitor
 *
 *  TODO StackAnalyzeMethodVisitor is not fully tested by this junit class.
 */
public final class TestDeadLockInstrumentation implements EventListenerIfc {
    private final ArrayList<MonitorWithContext> mEnteredMonitors;

    private final TransformClassLoader mClassLoader;

    public TestDeadLockInstrumentation() {
        ClassTransformer classTransformer =
            new ClassTransformer(new Logger(null),
                                 new File("."),
                                 new InstrumentConfig());
        mClassLoader = new TransformClassLoader(classTransformer);
        StaticEventListener.setListener(this);
        mEnteredMonitors = new ArrayList<MonitorWithContext>();
    }

    private SynchronizationTestIfc transformAsSynchronizationTest(Class clazz)
    throws Exception {
        Class c = mClassLoader.transform(clazz);
        return (SynchronizationTestIfc) c.newInstance();
    }

    private void testClass(Class clazz) throws Exception {
        SynchronizationTestIfc test = transformAsSynchronizationTest(clazz);
        test.go();
        assertEquals(test.getExpectedMonitorEnterings(),
                     mEnteredMonitors.toArray());
    }

    public void beforeMonitorEnter(Object monitor, LockingContext context) {
        // onMonitorEnter shall be invoked BEFORE the look is taken
        // in order to be able to generate the event even if the
        // lock can never be taken and the thread is blocked forever.
        // Otherwise we would only be able to generate lock graphs
        // for possible deadlocks and not for deadlocks that acctually
        // occured.
        if (monitor != null) {
            assertFalse(Thread.holdsLock(monitor));
            mEnteredMonitors.add(new MonitorWithContext(monitor, context));
        }
    }

    @Before
    public void setUp() throws Exception {
        mEnteredMonitors.clear();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testSynchronizedField() throws Exception {
        testClass(SynchronizedField.class);
    }

    @Test
    public void testSynchronizedArray() throws Exception {
        testClass(SynchronizedArray.class);
    }

    @Test
    public void testSynchronizedClass() throws Exception {
        testClass(SynchronizedClass.class);
    }


    @Test
    public void testSynchronizedExpression() throws Exception {
        testClass(SynchronizedExpression.class);
    }


    @Test
    public void testSynchronizedFromLocalVariable() throws Exception {
        testClass(SynchronizedFromLocalVariable.class);
    }


    @Test
    public void testSynchronizedFromMethod() throws Exception {
        testClass(SynchronizedFromMethod.class);
    }


    @Test
    public void testSynchronizedFromPrivateMethod() throws Exception {
        testClass(SynchronizedFromPrivateMethod.class);
    }


    @Test
    public void testSynchronizedFromStaticMethod() throws Exception {
        testClass(SynchronizedFromStaticMethod.class);
    }


    @Test
    public void testSynchronizedMethod() throws Exception {
        testClass(SynchronizedMethod.class);
    }


    @Test
    public void testSynchronizedNull() throws Exception {
        testClass(SynchronizedNull.class);
    }


    @Test
    public void testSynchronizedStaticField() throws Exception {
        testClass(SynchronizedStaticField.class);
    }


    @Test
    public void testSynchronizedStaticMethod() throws Exception {
        testClass(SynchronizedStaticMethod.class);
    }

    @Test
    public void testSynchronizedThis() throws Exception {
        testClass(SynchronizedThis.class);
    }

    @Test
    public void testSynchronizedInStaticBlock() throws Exception {
        testClass(SynchronizedInStaticBlock.class);
    }

    @Test
    public void testSynchronizedInBlock() throws Exception {
        testClass(SynchronizedInBlock.class);
    }

    @Test
    public void testSynchronizedStaticMethodWithMultipleIntReturns()
    throws Exception {
        testClass(SynchronizedStaticMethodWithMultipleIntReturns.class);
    }

    @Test
    public void testSynchronizedMethodWithMultipleFloatReturns()
    throws Exception {
        testClass(SynchronizedMethodWithMultipleFloatReturns.class);
    }

    @Test
    public void testSynchronizedMethodWithDoubleReturn() throws Exception {
        testClass(SynchronizedMethodWithDoubleReturn.class);
    }

    @Test
    public void testSynchronizedMethodWithLongReturn() throws Exception {
        testClass(SynchronizedMethodWithLongReturn.class);
    }

    @Test
    public void testSynchronizedMethodWithObjectReturn() throws Exception {
        testClass(SynchronizedMethodWithObjectReturn.class);
    }

    @Test
    public void testSynchronizedStaticMethodWithException() throws Exception {
        testClass(SynchronizedStaticMethodWithException.class);
    }

    @Test
    public void testSynchronizedMethodWithException() throws Exception {
        testClass(SynchronizedMethodWithException.class);
    }

    @Test
    public void testSynchronizedNewObject() throws Exception {
        SynchronizationTestIfc test =
            transformAsSynchronizationTest(SynchronizedNewObject.class);
        test.go();
        assertEquals(1, mEnteredMonitors.size());
        MonitorWithContext acctual = mEnteredMonitors.get(0);
        MonitorWithContext expected = test.getExpectedMonitorEnterings()[0];
        assertEquals(expected.getContext(), acctual.getContext());
    }

    public void accessField(Object owner, int fieldId, boolean isVolatile,
                            boolean writeAccess) {
        // TODO Auto-generated method stub
    }

    public void accessStaticField(Class owner, int fieldId, boolean isVolatile,
                                  boolean writeAccess) {
        // TODO Auto-generated method stub
    }

    public void afterMonitorEnter(Object monitor, boolean foo) {
        // TODO Auto-generated method stub
    }


    public void beforeMonitorExit(Object monitor, boolean foo) {
        // TODO Auto-generated method stub
    }

    public void classInitialized(Class clazz) {
        // TODO Auto-generated method stub
    }

    // TODO Add test case for instrumenting a non-anonymous inner class.

    // TODO Add test case for instrumenting a non-anonymous static inner class.

    // TODO Add test case for instrumenting an anonymous inner class

    // TODO Add test case for instrumenting an anonymous method.

    // TODO Add test case for instrumenting a class file version < 49
    //      (before Java 1.5) with a static synchronized method.

    // TODO Add test case for instrumenting a synchronized block in
    //      constructor.

    // TODO Add test case for what's happening with native methods.
}

