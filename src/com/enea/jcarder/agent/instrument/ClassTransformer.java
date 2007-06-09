package com.enea.jcarder.agent.instrument;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.util.CheckClassAdapter;
import com.enea.jcarder.util.Logger;

/**
 * This class is responsible for all instrumentations and handles related
 * issues with classloaders.
 *
 * TODO add basic test for this class.
 */
public class ClassTransformer implements ClassFileTransformer {
    private static final File ORGINAL_CLASSES_DIR = new File("orginalClasses");
    private static final File INSTRUMENTED_CLASSES_DIR =
        new File("instrumentedClasses");
    private static final Logger LOGGER =
        Logger.getLogger(ClassTransformer.class);
    private final ClassLoader mAgentClassLoader;
    private final InstrumentConfig mInstrumentConfig;

    public ClassTransformer(InstrumentConfig config) {
        mInstrumentConfig = config;
        mAgentClassLoader = getClass().getClassLoader();
        LOGGER.fine("jcarder loaded with "
                    + getClassLoaderName(mAgentClassLoader) + ".");
        deleteDirRecursively(INSTRUMENTED_CLASSES_DIR);
        deleteDirRecursively(ORGINAL_CLASSES_DIR);
    }

    public byte[] transform(final ClassLoader classLoader,
                            final String jvmInternalClassName,
                            final Class<?> classBeingRedefined,
                            final ProtectionDomain protectionDomain,
                            final byte[] orginalClassBuffer)
    throws IllegalClassFormatException {
        String className = jvmInternalClassName.replace('/', '.');
        try {
            return instrument(classLoader, orginalClassBuffer, className);
        } catch (Throwable t) {
            LOGGER.severe("Failed to transform the class: " + className,  t);
            dumpClassToFile(orginalClassBuffer, ORGINAL_CLASSES_DIR, className);
            return null;
        }
    }

    private byte[] instrument(final ClassLoader classLoader,
                              final byte[] orginalClassBuffer,
                              final String className) {
        if (className.startsWith("com.enea.jcarder")
            && !className.startsWith("com.enea.jcarder.testclasses")) {
            return null; // Don't instrument ourself.
        }
        if (isFromStandardLibrary(className)) {
            LOGGER.finest("Won't instrument class from standard library: "
                          + className);
            return null;
        }
        if (!isCompatibleClassLoader(classLoader)) {
            LOGGER.finest("Can't instrument class loaded with "
                          + getClassLoaderName(classLoader) + ": " + className);
            return null;
        }
        final ClassReader reader = new ClassReader(orginalClassBuffer);
        final ClassWriter writer = new ClassWriter(true);
        ClassVisitor visitor = writer;
        if (mInstrumentConfig.getValidateTransfomedClasses()) {
            visitor = new CheckClassAdapter(visitor);
        }
        visitor = new ClassAdapter(visitor, className);
        reader.accept(visitor, false);
        byte[] instrumentedClassfileBuffer = writer.toByteArray();
        if (mInstrumentConfig.getDumpClassFiles()) {
            dumpClassToFile(orginalClassBuffer, ORGINAL_CLASSES_DIR, className);
            dumpClassToFile(instrumentedClassfileBuffer,
                            INSTRUMENTED_CLASSES_DIR,
                            className);
        }
        return instrumentedClassfileBuffer;
    }

    /**
     * Instrumented classes must use the same static members in the
     * com.ena.jcarder.agent.StaticEventListener class as the java agent
     * and therefore they must be loaded with the same class loader
     * as the agent was loaded with, or with a class loader that has
     * the agent's class loader as a parent or anchestor.
     *
     * Note that the agentLoader may have been loaded with the bootstrap
     * class loader (null) and then "null" is a compatible class loader.
     */
    private boolean isCompatibleClassLoader(final ClassLoader classLoader) {
        ClassLoader c = classLoader;
        while (c != mAgentClassLoader) {
            if (c == null) {
                return false;
            }
            c = c.getParent();
        }
        return true;
    }

    private static String getClassLoaderName(final ClassLoader loader) {
        if (loader == null) {
            return "the bootstrap class loader";
        } else if (loader == ClassLoader.getSystemClassLoader()) {
            return "the system class loader";
        } else {
            return "the class loader \"" + loader + "\"";
        }
    }

    private static boolean isFromStandardLibrary(String className) {
        return className.startsWith("java.")
               || className.startsWith("javax.")
               || className.startsWith("sun.");
    }
  
    private static boolean deleteDirRecursively(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDirRecursively(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }
    
    /**
     * The dumped file can be decompiled with javap or with gnu.bytecode.dump
     * which also prints detailed information about the constant pool, which
     * javap does not.
     */
    private static void dumpClassToFile(byte[] content,
                                        File baseDir,
                                        String className) {
        try {
            String separator = System.getProperty("file.separator");
            File file = new File(baseDir + separator
                                 + className.replace(".", separator)
                                 + ".class");
            file.getParentFile().mkdirs();
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(content);
            fos.close();
        } catch (IOException e) {
            LOGGER.severe("Failed to dump class to file", e);
        }
    }
}
