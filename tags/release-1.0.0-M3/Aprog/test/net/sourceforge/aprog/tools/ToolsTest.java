/*
 *  The MIT License
 * 
 *  Copyright 2010 Codist Monk.
 * 
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 * 
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 * 
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */

package net.sourceforge.aprog.tools;

import static org.junit.Assert.*;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.concurrent.Callable;

import org.junit.Test;

/**
 * Automated tests using JUnit 4 for {@link Tools}.
 *
 * @author codistmonk (creation 2010-06-11)
 */
public final class ToolsTest {

    @Test
    public final void testArray() {
        final Object[] array = new Object[] { 42, 33 };

        assertSame(array, Tools.array(array));
        assertArrayEquals(array, Tools.array(42, 33));
    }

    @Test
    public final void testSet() {
        final Set<?> set = Tools.set(42, 33, 42);

        assertArrayEquals(Tools.array(42, 33), set.toArray());
    }

    @Test
    @SuppressWarnings("unchecked")
    public final void testAppend() {
        assertArrayEquals(Tools.array(42, 33, 42), Tools.append(Tools.array(42), Tools.array(33, 42)));
        assertArrayEquals(Tools.array(42), Tools.append(Tools.array(42), Tools.array()));
        assertArrayEquals(Tools.array(42), Tools.append(Tools.array(), Tools.array((Object) 42)));
        assertArrayEquals(Tools.array(), Tools.append(Tools.array(), Tools.array()));
    }

    @Test
    public final void testInvoke() {
        assertEquals(42, Tools.invoke(Integer.class, "parseInt", "42"));
        assertEquals(42, Tools.invoke(42L, "intValue"));
    }

    @Test
    public final void testGetGetter() {
        final ObjectWithArbitraryProperties objectWithArbitraryProperties = new ObjectWithArbitraryProperties();

        {
            final Method getter = Tools.getGetter(objectWithArbitraryProperties, "intProperty");

            assertNotNull(getter);
            assertEquals("getIntProperty", getter.getName());
        }
        {
            final Method getter = Tools.getGetter(objectWithArbitraryProperties, "booleanProperty1");

            assertNotNull(getter);
            assertEquals("isBooleanProperty1", getter.getName());
        }
        {
            final Method getter = Tools.getGetter(objectWithArbitraryProperties, "booleanProperty2");

            assertNotNull(getter);
            assertEquals("hasBooleanProperty2", getter.getName());
        }
        {
            final Method getter = Tools.getGetter(objectWithArbitraryProperties, "booleanProperty3");

            assertNotNull(getter);
            assertEquals("getBooleanProperty3", getter.getName());
        }
        {
            final Method getter = Tools.getGetter(objectWithArbitraryProperties, "packagePrivateStringProperty");

            assertNotNull(getter);
            assertEquals("getPackagePrivateStringProperty", getter.getName());
        }
    }

    @Test
    public final void testGetGetterFailure() {
        final ObjectWithArbitraryProperties objectWithArbitraryProperties = new ObjectWithArbitraryProperties();
        {
            try {
                // Missing property
                final Method getter = Tools.getGetter(objectWithArbitraryProperties, "missingProperty");

                fail("getGetter() should have failed but instead returned " + getter);
            } catch (final RuntimeException expectedException) {
                // Do nothing
            }
        }
        {
            try {
                // Bad casing
                final Method getter = Tools.getGetter(objectWithArbitraryProperties, "INTPROPERTY");

                fail("getGetter() should have failed but instead returned " + getter);
            } catch (final RuntimeException expectedException) {
                // Do nothing
            }
        }
    }

    @Test
    public final void testGetSetter() {
        final ObjectWithArbitraryProperties objectWithArbitraryProperties = new ObjectWithArbitraryProperties();

        {
            final Method setter = Tools.getSetter(objectWithArbitraryProperties, "intProperty", int.class);

            assertNotNull(setter);
            assertEquals("setIntProperty", setter.getName());
        }
        {
            final Method setter = Tools.getSetter(objectWithArbitraryProperties, "booleanProperty1", boolean.class);

            assertNotNull(setter);
            assertEquals("setBooleanProperty1", setter.getName());
        }
        {
            final Method setter = Tools.getSetter(objectWithArbitraryProperties, "packagePrivateStringProperty", String.class);

            assertNotNull(setter);
            assertEquals("setPackagePrivateStringProperty", setter.getName());
        }
    }

    @Test
    public final void testGetSetterFailure() {
        final ObjectWithArbitraryProperties objectWithArbitraryProperties = new ObjectWithArbitraryProperties();

        {
            try {
                // Missing property
                final Method setter = Tools.getGetter(objectWithArbitraryProperties, "missingProperty");

                fail("getSetter() should have failed but instead returned " + setter);
            } catch (final RuntimeException expectedException) {
                // Do nothing
            }

        }
        {
            try {
                // Bad casing
                final Method setter = Tools.getSetter(objectWithArbitraryProperties, "INTPROPERTY", int.class);

                fail("getSetter() should have failed but instead returned " + setter);
            } catch (final RuntimeException expectedException) {
                // Do nothing
            }
        }
        {
            try {
                // Mismatching parameter type
                final Method setter = Tools.getSetter(objectWithArbitraryProperties, "intProperty", boolean.class);

                fail("getSetter() should have failed but instead returned " + setter);
            } catch (final RuntimeException expectedException) {
                // Do nothing
            }
        }
    }

    @Test
    public final void testToUpperCamelCase() {
        assertEquals("CamelCase", Tools.toUpperCamelCase("camelCase"));
    }

    @Test
    public final void testEmptyIfNull() {
        assertEquals("", Tools.emptyIfNull(null));
        assertSame("", Tools.emptyIfNull(""));
        assertSame("42", Tools.emptyIfNull("42"));
    }

    @Test
    public final void testGetPackagePath() {
        assertEquals("net/sourceforge/aprog/tools/", Tools.getPackagePath(ToolsTest.class));
    }

    @Test
    public final void testGetCallerPackagePath() {
        assertEquals("net/sourceforge/aprog/tools/", Tools.getCallerPackagePath());
    }

    @Test
    public final void testGetTopLevelEclosingClass() throws Exception {
        assertEquals(this.getClass(), new Callable<Class<?>>() {

            @Override
            public final Class<?> call() throws Exception {
                return Tools.getTopLevelEnclosingClass(this.getClass());
            }

        }.call());
    }

    @Test
    public final void testGetCallerClass() {
        assertEquals(this.getClass(), ToolsTest.getCallerClass());
    }

    @Test
    public final void testGetLoggerForThisMethod() {
        assertTrue(Tools.getLoggerForThisMethod().getName().endsWith("testGetLoggerForThisMethod"));
    }

    @Test
    public final void testThrowUnchecked() {
        {
            final Throwable originalThrowable = new RuntimeException();

            try {
                Tools.throwUnchecked(originalThrowable);
            } catch(final RuntimeException caughtThrowable) {
                assertSame(originalThrowable, caughtThrowable);
            }
        }

        {
            final Throwable originalThrowable = new Exception();

            try {
                Tools.throwUnchecked(originalThrowable);
            } catch(final RuntimeException caughtThrowable) {
                assertNotNull(caughtThrowable.getCause());
                assertSame(originalThrowable, caughtThrowable.getCause());
            }
        }

        {
            final Throwable originalThrowable = new Error();

            try {
                Tools.throwUnchecked(originalThrowable);
            } catch(final Error caughtThrowable) {
                assertSame(originalThrowable, caughtThrowable);
            }
        }

        {
            final Throwable originalThrowable = new Throwable();

            try {
                Tools.throwUnchecked(originalThrowable);
            } catch(final Throwable caughtThrowable) {
                assertSame(originalThrowable, caughtThrowable.getCause());
            }
        }
    }

    @Test
    public final void testUnchecked() {
        {
            final Throwable cause = new Throwable();

            assertSame(cause, Tools.unchecked(cause).getCause());
        }
        {
            final Throwable cause = new Error();

            assertSame(cause, Tools.unchecked(cause).getCause());
        }
        {
            final Throwable cause = new Exception();

            assertSame(cause, Tools.unchecked(cause).getCause());
        }
        {
            final Throwable cause = new RuntimeException();

            assertSame(cause, Tools.unchecked(cause));
        }
    }

    @Test
    public final void testCast() {
        final Object object = "42";
        final String that = Tools.cast(String.class, object);

        assertSame(object, that);

        final Integer badCast = Tools.cast(Integer.class, object);

        assertNull(badCast);
    }

    @Test
    public final void testCastToCurrentClass() {
        assertNull(Tools.castToCurrentClass(42));
        assertSame(this, Tools.castToCurrentClass(this));
        assertNotNull(Tools.castToCurrentClass(new ToolsTest()));
    }

    @Test
    public final void testEquals() {
        final Object object = "42";

        assertTrue(Tools.equals(null, null));
        assertFalse(Tools.equals(object, null));
        assertTrue(Tools.equals(object, object));
        assertTrue(Tools.equals(new Integer(6 * 7).toString(), object));
        assertFalse(Tools.equals(object, 42));
    }

    @Test
    public final void testHashCode() {
        final Object object = "42";

        assertEquals(0, Tools.hashCode(null));
        assertEquals(object.hashCode(), Tools.hashCode(object));
    }

    /**
     *
     * @return
     * <br>Maybe null
     */
    private static final Class<?> getCallerClass() {
        return Tools.getCallerClass();
    }

    /**
     * This class is package-private to suppress visibility and usage warnings.
     *
     * @author codistmonk (creation 2010-05-19)
     *
     */
    static class ObjectWithArbitraryProperties {

        private int intProperty;

        private boolean booleanProperty1;

        private boolean booleanProperty2;

        private boolean booleanProperty3;

        private String packagePrivateStringProperty;

        /**
         *
         * @return
         * <br>Range: Any integer
         */
        public final int getIntProperty() {
            return this.intProperty;
        }

        /**
         *
         * @param intProperty an arbitrary integer
         * <br>Range: Any integer
         */
        public final void setIntProperty(final int intProperty) {
            this.intProperty = intProperty;
        }

        public final boolean isBooleanProperty1() {
            return this.booleanProperty1;
        }

        /**
         *
         * @param booleanProperty1 an arbitrary boolean
         */
        public final void setBooleanProperty1(final boolean booleanProperty1) {
            this.booleanProperty1 = booleanProperty1;
        }

        public final boolean hasBooleanProperty2() {
            return this.booleanProperty2;
        }

        /**
         *
         * @param booleanProperty2 an arbitrary boolean
         */
        public final void setBooleanProperty2(final boolean booleanProperty2) {
            this.booleanProperty2 = booleanProperty2;
        }

        public final boolean getBooleanProperty3() {
            return this.booleanProperty3;
        }

        /**
         *
         * @param booleanProperty3 an arbitrary boolean
         */
        public final void setBooleanProperty3(final boolean booleanProperty3) {
            this.booleanProperty3 = booleanProperty3;
        }

        /**
         *
         * @return
         * <br>A possibly null value
         * <br>A shared value
         */
        final String getPackagePrivateStringProperty() {
            return this.packagePrivateStringProperty;
        }

        /**
         *
         * @param packagePrivateStringProperty
         * <br>Can be null
         * <br>Shared parameter
         */
        final void setPackagePrivateStringProperty(final String packagePrivateStringProperty) {
            this.packagePrivateStringProperty = packagePrivateStringProperty;
        }

    }

}