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

import com.enea.jcarder.util.logging.Logger;

/**
 * This class is responsible for all instrumentations and handles related issues
 * with class loaders.
 *
 * TODO Add basic test for this class.
 */
public class ClassTransformer implements ClassFileTransformer {
    private static final String ORIGINAL_CLASSES_DIRNAME =
        "jcarder_original_classes";
    private static final String INSTRUMENTED_CLASSES_DIRNAME =
        "jcarder_instrumented_classes";
    private final Logger mLogger;
    private final ClassLoader mAgentClassLoader;
    private final InstrumentConfig mInstrumentConfig;
    private File mOriginalClassesDir;
    private File mInstrumentedClassesDir;

    public ClassTransformer(Logger logger,
                            File outputDirectory,
                            InstrumentConfig config) {
        mLogger = logger;
        mOriginalClassesDir =
            new File(outputDirectory, ORIGINAL_CLASSES_DIRNAME);
        mInstrumentedClassesDir =
            new File(outputDirectory, INSTRUMENTED_CLASSES_DIRNAME);
        mInstrumentConfig = config;
        mAgentClassLoader = getClass().getClassLoader();
        mLogger.fine("JCarder loaded with "
                     + getClassLoaderName(mAgentClassLoader) + ".");
        deleteDirRecursively(mInstrumentedClassesDir);
        deleteDirRecursively(mOriginalClassesDir);
    }

    public byte[] transform(final ClassLoader classLoader,
                            final String jvmInternalClassName,
                            final Class<?> classBeingRedefined,
                            final ProtectionDomain protectionDomain,
                            final byte[] originalClassBuffer)
    throws IllegalClassFormatException {
        String className = jvmInternalClassName.replace('/', '.');
        try {
            return instrument(classLoader, originalClassBuffer, className);
        } catch (Throwable t) {
            mLogger.severe("Failed to transform the class "
                           + className + ": " + t.getMessage());
            dumpClassToFile(originalClassBuffer,
                            mOriginalClassesDir,
                            className);
            return null;
        }
    }

    private byte[] instrument(final ClassLoader classLoader,
                              final byte[] originalClassBuffer,
                              final String className) {
        if (className.startsWith("com.enea.jcarder")
            && !className.startsWith("com.enea.jcarder.testclasses")) {
            return null; // Don't instrument ourself.
        }
        if (isFromStandardLibrary(className)) {
            mLogger.finest("Won't instrument standard library class "
                           + className);
            return null;
        }
        if (!isCompatibleClassLoader(classLoader)) {
            mLogger.finest("Can't instrument class " + className
                           + " loaded with " + getClassLoaderName(classLoader));
            return null;
        }
        final ClassReader reader = new ClassReader(originalClassBuffer);
        final ClassWriter writer = new ClassWriter(true);
        ClassVisitor visitor = writer;
        if (mInstrumentConfig.getValidateTransfomedClasses()) {
            visitor = new CheckClassAdapter(visitor);
        }
        visitor = new ClassAdapter(mLogger, visitor, className);
        reader.accept(visitor, false);
        byte[] instrumentedClassfileBuffer = writer.toByteArray();
        if (mInstrumentConfig.getDumpClassFiles()) {
            dumpClassToFile(originalClassBuffer,
                            mOriginalClassesDir,
                            className);
            dumpClassToFile(instrumentedClassfileBuffer,
                            mInstrumentedClassesDir,
                            className);
        }
        return instrumentedClassfileBuffer;
    }

    /**
     * Instrumented classes must use the same static members in the
     * com.ena.jcarder.agent.StaticEventListener class as the Java agent and
     * therefore they must be loaded with the same class loader as the agent was
     * loaded with, or with a class loader that has the agent's class loader as
     * a parent or ancestor.
     *
     * Note that the agentLoader may have been loaded with the bootstrap class
     * loader (null) and then "null" is a compatible class loader.
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
                boolean success =
                    deleteDirRecursively(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

    /**
     * The dumped file can be decompiled with javap or with gnu.bytecode.dump.
     * The latter also prints detailed information about the constant pool,
     * something which javap does not.
     */
    private void dumpClassToFile(byte[] content,
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
            mLogger.severe("Failed to dump class to file: " + e.getMessage());
        }
    }
}
