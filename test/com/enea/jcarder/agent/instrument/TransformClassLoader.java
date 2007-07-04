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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * This class loader is able to transform classes while they are loaded.
 * It uses the classpath from its parent class loader.
 *
 * The class loader that loaded the TransformClassLoader will be used
 * as parrent class loader.
 *
 * This TransformClassLoader is not able to transform classes that begin
 * with java.*.
 */
public final class TransformClassLoader extends ClassLoader {
    private final ClassFileTransformer mTransformer;
    private final ClassLoader mParentClassLoader;
    private final Pattern mClassNamePattern;
    private final Set<String> mTansformedClasses = new HashSet<String>();

    /**
     * Create a TransformClassLoader that only transforms classes that are
     * explicitly loaded with the transform(Class) method. All other
     * requests are forwarded to the parent class loader.
     *
     */
    public TransformClassLoader(final ClassFileTransformer transformer) {
        this(transformer, null);
    }

    /**
     * Create a TransformClassLoader that transforms all classes with names
     * that match the given classNamePattern regexp. All other requests are
     * forwarded to the parent class loader.
     */
    public TransformClassLoader(final ClassFileTransformer transformer,
                                final Pattern classNamePattern) {
        mTransformer = transformer;
        mParentClassLoader = TransformClassLoader.class.getClassLoader();
        mClassNamePattern = classNamePattern;
    }

    /**
     * This method transforms a class even if its name does not match the
     * classNamePattern regexp that this TransformClassLoader may have been
     * configured
     * with.
     */
    public Class<?> transform(Class clazz)
    throws ClassNotFoundException,
           IllegalClassFormatException,
           ClassNotTransformedException {
        byte[] classBuffer = getClassBytes(clazz);
        return createTransformedClass(clazz.getName(), classBuffer);
    }


    /**
     * Load a new non-instrumented class with this class loader.
     */
    public Class<?> loadNew(Class clazz) throws ClassNotFoundException {
        byte[] classBuffer = getClassBytes(clazz);
        return defineClass(clazz.getName(),
                           classBuffer,
                           0,
                           classBuffer.length);
    }


    protected synchronized Class<?> loadClass(String className, boolean resolve)
    throws ClassNotFoundException {
        Class c = findLoadedClass(className);
        if (c == null) {
            if (isClassNameToBeTransformed(className)) {
                c = findClass(className);
            } else {
                c = mParentClassLoader.loadClass(className);
            }
        }
        if (resolve) {
            resolveClass(c);
        }
        return c;
    }

    private boolean isClassNameToBeTransformed(String className) {
        return mClassNamePattern != null
               && mClassNamePattern.matcher(className).matches();
    }

    protected synchronized Class<?> findClass(String className)
    throws ClassNotFoundException {
        byte[] classBuffer = getClassBytes(className, mParentClassLoader);
        try {
            return createTransformedClass(className, classBuffer);
        } catch (IllegalClassFormatException e) {
            throw new ClassNotFoundException(e.getMessage(), e);
        } catch (ClassNotTransformedException e) {
            throw new ClassNotFoundException(e.getMessage(), e);
        }
    }

    private Class<?> createTransformedClass(String className,
                                            byte[] classBuffer)
    throws ClassNotFoundException,
           IllegalClassFormatException,
           ClassNotTransformedException {
        byte[] transformedClassBuffer = mTransformer.transform(this,
                                                               className,
                                                               null,
                                                               null,
                                                               classBuffer);
        if (transformedClassBuffer == null) {
            throw new ClassNotTransformedException("Class not transformed: "
                                                   + className);
        }
        mTansformedClasses.add(className);
        return defineClass(className,
                           transformedClassBuffer,
                           0,
                           transformedClassBuffer.length);
    }

    public boolean hasBeenTransformed(String className) {
        return mTansformedClasses.contains(className);
    }
    
    public static String resourceNameForClass(String className) {
        return className.replace(".", "/") + ".class";
    }    
    
    /**
    *
    * @param className
    * @param classLoader null if the system classloader should be used.
    * @return
    * @throws ClassNotFoundException
    */
   public static byte[] getClassBytes(String className,
                                      ClassLoader classLoader)
   throws ClassNotFoundException {
       final String resourceName = resourceNameForClass(className);
       final InputStream is;
       if (classLoader == null) {
           is = ClassLoader.getSystemResourceAsStream(resourceName);
       } else {
           is = classLoader.getResourceAsStream(resourceName);
       }
       if (is == null) {
           throw new ClassNotFoundException("Resource not found: "
                                            + resourceName);
       }
       final ByteArrayOutputStream baos = new ByteArrayOutputStream();
       final byte[] readBuffer = new byte[1024 * 4];
       int readBytes;
       try {
           while ((readBytes = is.read(readBuffer)) != -1) {
               baos.write(readBuffer, 0, readBytes);
           }
       } catch (IOException e) {
           throw new
              ClassNotFoundException("Failure while reading class contents", e);
       }
       return baos.toByteArray();

   }

   public static byte[] getClassBytes(Class clazz)
   throws ClassNotFoundException {
       return getClassBytes(clazz.getName(), clazz.getClassLoader());
   }
}
