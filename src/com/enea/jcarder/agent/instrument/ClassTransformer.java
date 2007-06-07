package com.enea.jcarder.agent.instrument;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
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
    private static final File ORGINAL_CLASSES = new File("orginalClasses");
    private static final File INSTRUMENTED_CLASSES = new File("instrumentedClasses");
    protected static final Logger LOGGER = Logger.getLogger(ClassTransformer.class);
    private final ClassLoader mAgentClassLoader;
    private final InstrumentConfig mInstrumentConfig;
    // There might be potential datarace while accessing
    // mWarningAboutIncompatibleClassLoaderPrinted but it does
    // not matter very much if the warning message is occasionally
    // printed more than once.
    private boolean mWarningAboutIncompatibleClassLoaderPrinted = false;

    public ClassTransformer(InstrumentConfig config) {
        mInstrumentConfig = config;
        mAgentClassLoader = getClass().getClassLoader();
        LOGGER.fine("jcarder loaded with "
                    + getClassLoaderName(mAgentClassLoader) + ".");
        deleteDirs(INSTRUMENTED_CLASSES);
        deleteDirs(ORGINAL_CLASSES);
    }

    public final byte[] transform(final ClassLoader classLoader,
                                  final String jvmInternalClassName,
                                  final Class<?> classBeingRedefined,
                                  final ProtectionDomain protectionDomain,
                                  final byte[] classfileBuffer)
    throws IllegalClassFormatException {
        String className = jvmInternalClassName;
        try {
            beforeTransformHook();
            className = jvmInternalClassName.replace('/', '.');
            return instrument(classLoader, classfileBuffer, className, false);
        } catch (Throwable t) {
            LOGGER.severe("Failed to transform the class: " + className,
                           t);
            dumpClassToFile(classfileBuffer, ORGINAL_CLASSES, className);
            return classfileBuffer;
        } finally {
            afterTransformHook();
        }
    }

    protected void beforeTransformHook() { }
    protected void afterTransformHook() { }

    protected boolean isClassToBeInstrumented(String className,
                                              ClassLoader classLoader) {
        return !isFromStandardLibrary(className)
               && isCompatibleClassLoader(classLoader);
    }

    private static boolean isFromStandardLibrary(String className) {
        return className.startsWith("java.")
               || className.startsWith("javax.")
               || className.startsWith("sun.");
    }

    protected ClassAdapter getClassAdapter(ClassVisitor baseVisitor,
                                           String className,
                                           ClassLoader classLoader,
                                           boolean redefiningAlreadyLoadedClass) {
        return new ClassAdapter(baseVisitor,
                                className,
                                redefiningAlreadyLoadedClass);
    }

    /**
    * The instrumentation needs to know if it the class beeing instrumented
    * has been loaded before (and is redefined now) or if it is loaded for the
    * first time. There are som limitations on what can be done with already
    * loaded classes:
    *
    * "The redefinition may change method bodies, the constant pool and
    * attributes. The redefinition must not add, remove or rename fields
    * or methods, change the signatures of methods, or change inheritance.
    * These restrictions maybe be lifted in future versions." - JavaDoc 1.5
    */
    private byte[] instrument(final ClassLoader classLoader,
                              final byte[] classfileBuffer,
                              final String className,
                              final boolean redefiningAlreadyLoadedClass) {
        if (className.startsWith("com.enea.jcarder")
                && !className.startsWith("com.enea.jcarder.testclasses")) {
            return null; // Don't instrument ourself.
        }
        if (!isClassToBeInstrumented(className, classLoader)) {
            LOGGER.finer("Won't instrument: " + className);
            return null;
        }
        if (!isCompatibleClassLoader(classLoader)) {
            warnAboutIncompatibleClassLoader(classLoader, className);
            return null;
        }
        final ClassReader reader = new ClassReader(classfileBuffer);
        final ClassWriter writer = new ClassWriter(true);
        ClassVisitor visitor = writer;
        if (mInstrumentConfig.getValidateTransfomedClasses()) {
            visitor = new CheckClassAdapter(visitor);
        }
        visitor = getClassAdapter(visitor,
                                  className,
                                  classLoader,
                                  redefiningAlreadyLoadedClass);
        reader.accept(visitor, false);
        byte[] instrumentedClassfileBuffer = writer.toByteArray();
        if (mInstrumentConfig.getDumpClassFiles()) {
            dumpClassToFile(classfileBuffer, ORGINAL_CLASSES, className);
            dumpClassToFile(instrumentedClassfileBuffer,
                            INSTRUMENTED_CLASSES,
                            className);
        }
        return instrumentedClassfileBuffer;
    }

    /**
     * The dumped file can be decompiled with javap or with gnu.bytecode.dump
     * (jcarder/3pp/bytecodeDump.jar) which also prints detailed information about
     * the constant pool, which javap does not.
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
            // TODO
            e.printStackTrace();
        }
    }

    public static boolean deleteDirs(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDirs(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

    /**
     * Instrumented classes must use the same static members in the
     * com.ena.jcarder.agent.*CallbackListener class as the java agent
     * and therefore they must be loaded with the same class loader
     * as the agent was loaded with, or with a class loader that has
     * the agent's class loader as a parent or anchestor.
     *
     * Note that the agentLoader may have been loaded with the bootstrap
     * class loader (null) and then "null" is a compatible class loader.
     */
    protected final boolean isCompatibleClassLoader(final ClassLoader classLoader) {
        ClassLoader c = classLoader;
        while (c != mAgentClassLoader) {
            if (c == null) {
                return false;
            }
            c = c.getParent();
        }
        return true;
    }

    private void warnAboutIncompatibleClassLoader(final ClassLoader classLoader,
                                                  final String className) {
        if (!mWarningAboutIncompatibleClassLoaderPrinted) {
            String msg = "Failed to instrument some classes due to the"
                       + " current classloader configuration."
                       + " The file jcarder.jar must be added to the boot"
                       + " class path if any class loaded by the"
                       + " bootstrap class loader is to be"
                       + " instrumented. Please add the JVM command"
                       + " line parameter:"
                       + " -Xbootclasspath/a:jcarder.jar";
            LOGGER.warning(msg);
            mWarningAboutIncompatibleClassLoaderPrinted = true;
        }
        LOGGER.fine("Can't instrument " + className + " loaded with "
                     + getClassLoaderName(classLoader) + ".");
    }

    private static String getClassLoaderName(final ClassLoader loader) {
        if (loader == null) {
            return "the bootstrap class loader";
        } else if (loader == ClassLoader.getSystemClassLoader()) {
            return "the system class loader";
        } else {
            return "an unknown class loader (" + loader + ")";
        }
    }

    /**
     */
    public final void redefineClasses(Instrumentation instr) {
        try {
            for (Class c : instr.getAllLoadedClasses()) {
                if (c.getName().startsWith("[")) {
                    continue;
                }
                redefineClass(instr, c);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void redefineClass(Instrumentation instr, Class clazz) throws Exception {
        final byte[] oldBytes = getClassBytes(clazz);
        final byte[] newBytes = instrument(clazz.getClassLoader(),
                                           oldBytes,
                                           clazz.getName(),
                                           true);
        if (newBytes == null) {
            LOGGER.finest("Class not redefined: " + clazz.getName());
        } else if (oldBytes.equals(newBytes)) {
            LOGGER.finer("Instrumentation did not change the class: "
                          + clazz.getName());
        } else {
            ClassDefinition classDef = new ClassDefinition(clazz, newBytes);
            instr.redefineClasses(new ClassDefinition[] {classDef});
        }
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
                                       ClassLoader classLoader) throws ClassNotFoundException {
        final String resourceName = resourceNameForClass(className);
        final InputStream is;
        if (classLoader == null) {
            LOGGER.finer("Loading resource: " + resourceName
                          + " using: system class loader");
            is = ClassLoader.getSystemResourceAsStream(resourceName);
        } else {
            LOGGER.finer("Loading resource: " + resourceName + " using: "
                          + classLoader);
            is = classLoader.getResourceAsStream(resourceName);
        }
        if (is == null) {
            throw new ClassNotFoundException("Resource not found: " + resourceName);
        }
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final byte[] readBuffer = new byte[1024 * 4];
        int readBytes;
        try {
            while ((readBytes = is.read(readBuffer)) != -1) {
                baos.write(readBuffer, 0, readBytes);
            }
        } catch (IOException e) {
            throw new ClassNotFoundException("Failure while reading class contents", e);
        }
        return baos.toByteArray();

    }

    public static byte[] getClassBytes(Class clazz) throws ClassNotFoundException {
        return getClassBytes(clazz.getName(), clazz.getClassLoader());
    }

//    private static boolean isAlreadyInstrumented(byte[] classfileBuffer) {
//        final ClassReader reader = new ClassReader(classfileBuffer);
//        CheckIfAlreadyInstrumentedClassVisitor visitor =
//            new CheckIfAlreadyInstrumentedClassVisitor();
//        reader.accept(visitor,
//                      new Attribute[] { new InstrumentedAttribute() },
//                      true);
//        return visitor.isInstrumented();
//    }

//    private static class CheckIfAlreadyInstrumentedClassVisitor extends EmptyVisitor {
//        private boolean mAlreadyInstrumented = false;
//
//        public CheckIfAlreadyInstrumentedClassVisitor() {
//        }
//
//        boolean isInstrumented() {
//            return mAlreadyInstrumented;
//        }
//
//        @Override
//        public void visitAttribute(Attribute a) {
//            if (InstrumentedAttribute.matchAttribute(a)) {
//                System.out.println("Is already instrumented!!!");
//                mAlreadyInstrumented = true;
//            }
//        }
//    }
}
