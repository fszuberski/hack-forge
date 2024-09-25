package com.fszuberski.snippets.core;

public class AbstractClasses {

    private static abstract class A {

        public abstract void publicMethod();

        protected abstract void protectedMethod();

        abstract void packagePrivateMethod();

        // illegal combination of modifiers 'abstract' and 'private'
        // private abstract void privateMethod();
    }

    private static class B extends A {

        @Override
        public void publicMethod() {
            System.out.println("public method invoked");
        }

        @Override
        protected void protectedMethod() {
            System.out.println("protected method invoked");
        }

        @Override
        void packagePrivateMethod() {
            System.out.println("package private method invoked");
        }
    }
}
