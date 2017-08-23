package com.enea.jcarder.agent.instrument;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import java.util.*;

/**
 * Class created for correct implementation of {@link ClassWriter#getCommonSuperClass(java.lang.String, java.lang.String)} method
 */
class HierarchyListener extends ClassVisitor {
    private static final String JAVA_LANG_OBJECT = "java/lang/Object";

    private final Set<String> interfaces_ = new HashSet<>();
    private final Map<String, String> superClassMap_ = new HashMap<>();

    public HierarchyListener(int api, ClassVisitor classVisitor) {
        super(api, classVisitor);
    }

    public void setDelegate(ClassVisitor classVisitor) {
        this.cv = classVisitor;
    }

    @Override
    public void visit(int version, int attrs, String clazz, String s, String superClass, String[] strings) {
        // if class
        if ((attrs & Opcodes.ACC_INTERFACE) == 0) {
            superClassMap_.put(clazz, superClass);
        } else // interfaces
        {
            interfaces_.add(clazz);
        }

        super.visit(version, attrs, clazz, s, superClass, strings);
    }

    public String getCommonSuperClass(String class1, String class2) {
        try {
//                System.out.println("getCommonSuperClass for " + class1 + " and " + class2);
            Clazz clazz1 = getClazz(class1);
            Clazz clazz2 = getClazz(class2);

            if (clazz1 == null || clazz2 == null || clazz1.isInterface() || clazz2.isInterface())
                return JAVA_LANG_OBJECT;

            Deque<String> clazz1Parents = getClassParents(clazz1);
            Deque<String> clazz2Parents = getClassParents(clazz2);

            String currentParent = JAVA_LANG_OBJECT;

            while (!clazz1Parents.isEmpty() && !clazz2Parents.isEmpty()) {
                String clazz1Parent = clazz1Parents.pop();
                String clazz2Parent = clazz2Parents.pop();
                if (!clazz1Parent.equals(clazz2Parent))
                    break;
                currentParent = clazz1Parent;
            }

//                System.out.println("\tresult is " + currentParent);
            return currentParent;
        } catch (ClassNotFoundException e) {
            return JAVA_LANG_OBJECT;
        }
    }

    private static Deque<String> getClassParents(Clazz clazz) throws ClassNotFoundException {
        ArrayDeque<String> result = new ArrayDeque<>();
        while (clazz != null) {
            result.push(clazz.getName());
            clazz = clazz.getParent();
        }
        return result;
    }

    private Clazz getClazz(String name) throws ClassNotFoundException {
        if (name.equals(JAVA_LANG_OBJECT))
            return null;

        if (superClassMap_.containsKey(name))
            return new Clazz(name);
        else
            return new ExternalClazz(name);
    }

    public void visitClass(byte[] classfileBuffer) {
        ClassVisitor cv = this.cv;
        this.cv = null;
        {
            ClassReader reader = new ClassReader(classfileBuffer);
            reader.accept(this, 0);
        }
        this.cv = cv;
    }

    private class Clazz {
        private String name_;

        public Clazz(String name) {
            name_ = name;
        }

        public String getName() {
            return name_;
        }

        public boolean isInterface() {
            return interfaces_.contains(name_);
        }

        public Clazz getParent() throws ClassNotFoundException {
            String superClass = superClassMap_.get(name_);
            // dangerous case - if parent class is not known - return Object.class
            if (superClass == null)
                return new ExternalClazz(name_).getParent();
            return getClazz(superClass);
        }
    }

    private class ExternalClazz extends Clazz {
        private final Class<?> clazz_;

        public ExternalClazz(String name) throws ClassNotFoundException {
            super(name);

            ClassLoader localClassLoader = getClass().getClassLoader();
            clazz_ = Class.forName(name.replace('/', '.'), false, localClassLoader);
        }

        public ExternalClazz(Class<?> clazz) {
            super(clazz.getName().replace('.', '/'));

            clazz_ = clazz;
        }

        @Override
        public boolean isInterface() {
            return clazz_.isInterface();
        }

        @Override
        public Clazz getParent() throws ClassNotFoundException {
            Class<?> superclass = clazz_.getSuperclass();
            return superclass != Object.class
                    ? new ExternalClazz(superclass)
                    : null;
        }
    }
}
