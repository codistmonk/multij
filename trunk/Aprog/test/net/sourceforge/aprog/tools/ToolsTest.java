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
import static net.sourceforge.aprog.tools.Launcher.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.Callable;

import net.sourceforge.aprog.tools.Factory.DefaultFactory;

import org.junit.Test;

/**
 * Automated tests using JUnit 4 for {@link Tools}.
 *
 * @author codistmonk (creation 2010-06-11)
 */
public final class ToolsTest {
    
	@Test
	public final void testDebugPrint() {
		final PrintStream tmp = System.out;
		
		try {
			final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			
			System.setOut(new PrintStream(buffer));
			
			final Object object1 = 6;
			final Object object2 = "*";
			final Object object3 = "7";
			final Object object4 = "= 42";
			
			Tools.debugPrint(object1, object2, object3, object4);
			
			final String expectedPrefix = this.getClass().getCanonicalName() + "." + Tools.getThisMethodName() + "(ToolsTest.java:";
			final String expectedTrimmedSuffix = (object1 + " " + object2 + " " + object3 + " " + object4).trim();
			final String string = buffer.toString().trim();
			
			assertEquals(expectedPrefix, string.substring(0, expectedPrefix.length()));
			assertEquals(expectedTrimmedSuffix, string.substring(string.length() - expectedTrimmedSuffix.length()));
		} finally {
			System.setOut(tmp);
		}
	}
	
	@Test
	public final void testDebugError() {
		final PrintStream tmp = System.err;
		
		try {
			final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			
			System.setErr(new PrintStream(buffer));
			
			final Object object1 = 6;
			final Object object2 = "*";
			final Object object3 = "7";
			final Object object4 = "= 42";
			
			Tools.debugError(object1, object2, object3, object4);
			
			final String expectedPrefix = this.getClass().getCanonicalName() + "." + Tools.getThisMethodName() + "(ToolsTest.java:";
			final String expectedTrimmedSuffix = (object1 + " " + object2 + " " + object3 + " " + object4).trim();
			final String string = buffer.toString().trim();
			
			assertEquals(expectedPrefix, string.substring(0, expectedPrefix.length()));
			assertEquals(expectedTrimmedSuffix, string.substring(string.length() - expectedTrimmedSuffix.length()));
		} finally {
			System.setErr(tmp);
		}
	}
	
	@Test
	public final void testInstances() {
		{
			final Object array = Tools.instances(42, DefaultFactory.forClass(Object.class));
			
			assertEquals(42, Array.getLength(array));
			assertEquals(Object[].class, array.getClass());
		}
		
		{
			final Object array = Tools.instances(42, DefaultFactory.ARRAY_LIST_FACTORY);
			
			assertEquals(42, Array.getLength(array));
			assertEquals(ArrayList[].class, array.getClass());
		}
	}
	
	@Test
	public final void testObjectIO() throws Exception {
		final File tmp = File.createTempFile(Tools.getThisMethodName(), ".jo");
		
		tmp.deleteOnExit();
		
		Tools.writeObject(42, tmp.getPath());
		assertEquals((Object) 42, Tools.readObject(tmp.getPath()));
	}
	
	@Test
	public final void testGetThisMethodName() {
		assertEquals("testGetThisMethodName", Tools.getThisMethodName());
	}
	
	@Test
	public final void testGetOrCreate() {
		final Map<Object, Collection<?>> map = new HashMap<Object, Collection<?>>();
		
		Tools.getOrCreate(map, 42, (Factory) DefaultFactory.forClass(ArrayList.class)).add(42);
		
		assertEquals(Arrays.asList(42), map.get(42));
	}
	
	@Test
	public final void testJoin() {
		assertEquals("", Tools.join(","));
		assertEquals("42", Tools.join(",", 42));
		assertEquals("42,33", Tools.join(",", 42, 33));
	}
	
    @Test
    public final void testGCAndUsedMemory1() {
    	Tools.gc(100L);
    	
        final long usedMemoryBeforeAllocation = Tools.usedMemory();
        Object object = new int[1000000];
        final long usedMemoryAfterAllocation = Tools.usedMemory();
        final WeakReference<Object> weakReference = new WeakReference<Object>(object);
        
        assertNotNull(weakReference.get());
        
        object = null;
        
        Tools.gc(100L);
        
        final long usedMemoryAfterGC = Tools.usedMemory();
        
        assertNull(weakReference.get());
        assertTrue(usedMemoryBeforeAllocation < usedMemoryAfterAllocation);
        assertTrue(usedMemoryAfterGC < usedMemoryAfterAllocation);
    }
    
    @Test
    public final void testGCAndUsedMemory2() {
    	Tools.gc(100L);
    	
    	final long usedMemoryBeforeAllocation = Tools.usedMemory();
    	Object object = new int[1000000];
    	final long usedMemoryAfterAllocation = Tools.usedMemory();
    	final WeakReference<Object> weakReference = new WeakReference<Object>(object);
    	
    	assertNotNull(weakReference.get());
    	
    	object = null;
    	
    	Tools.gc();
    	
    	final long usedMemoryAfterGC = Tools.usedMemory();
    	
    	assertNull(weakReference.get());
    	assertTrue(usedMemoryBeforeAllocation < usedMemoryAfterAllocation);
    	assertTrue(usedMemoryAfterGC < usedMemoryAfterAllocation);
    }

    /**
     * This creates a temporary folder with a name containing a space "tmp dirXXXXXX" (XXXXXX is a random number generated by {@link File#createTempFile(String, String)}.
     * <br>Into this folder the minimum files needed to run {@link EchoApplicationFile} are copied:<ul>
     *  <li>EchoApplicationFile.class
     *  <li>Tools.class
     *  <li>IllegalInstantiationException.class
     * </ul>
     * <br>To make sure that {@link Tools#getApplicationFile()} returns the root for EchoApplicationFile (and not Tools), Tools and IllegalInstantiationException are placed in a different location (a subfolder "aprog").
     * <br>Now, when running EchoApplicationFile with the temporary classpath, the temporary root folder path should be written to the standard output.
     * <br>The test makes sure that this result is correct.
     * 
     * @throws Exception If an unexpected error occurs
     */
    @Test
    public final void testGetApplicationFile() throws Exception {
        assertTrue(Tools.getApplicationFile().exists());

        final File tmpRoot = File.createTempFile("tmp dir", "");
        final File aprogRoot = new File(tmpRoot, "aprog");

        assertTrue(tmpRoot.delete());
        assertTrue(tmpRoot.mkdir());
        assertTrue(aprogRoot.mkdir());

        Tools.debugPrint(tmpRoot);

        copyToTmp(IllegalInstantiationException.class, aprogRoot);
        copyToTmp(Tools.class, aprogRoot);
        copyToTmp(EchoApplicationFile.class, tmpRoot);

        final String[] command = Tools.array("java", "-cp", aprogRoot.toString() + File.pathSeparator + tmpRoot, EchoApplicationFile.class.getCanonicalName());

        Tools.debugPrint((Object[]) command);

        final Process process = Runtime.getRuntime().exec(command);

        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        pipe(process.getInputStream(), new PrintStream(buffer));
        pipe(process.getErrorStream(), System.err);

        assertEquals(0, process.waitFor());

        Tools.debugPrint(buffer);

        assertEquals(tmpRoot.getCanonicalPath(), buffer.toString().trim());
        
        tmpRoot.delete();
    }

    @Test
    public final void testCreateTemporaryFile() {
        {
            final File temporaryFile = Tools.createTemporaryFile("prefix", "suffix", null);

            assertTrue(temporaryFile.exists());
            assertTrue(temporaryFile.getName().startsWith("prefix"));
            assertTrue(temporaryFile.getName().endsWith("suffix"));
            assertEquals(0L, temporaryFile.length());
        }
        {
            final File temporaryFile = Tools.createTemporaryFile("prefix", "suffix",
                    Tools.getResourceAsStream(Tools.getThisPackagePath() + "test.txt"));

            assertTrue(temporaryFile.exists());
            assertTrue(temporaryFile.getName().startsWith("prefix"));
            assertTrue(temporaryFile.getName().endsWith("suffix"));
            assertEquals(2L, temporaryFile.length());
        }
    }

    @Test
    public final void testWriteAndClose() throws IOException {
    	final File tmp = File.createTempFile("test", "");
    	
    	tmp.deleteOnExit();
    	
    	final InputStream input = new ByteArrayInputStream("42".getBytes());
    	final OutputStream output = new FileOutputStream(tmp);
    	
    	Tools.writeAndCloseOutput(input, output);
    	
    	try {
    		output.write(0);
    		assertFalse(true);
    	} catch (final IOException expected) {
    		Tools.ignore(expected);
    	}
    }
    
    @Test
    public final void testClose() throws IOException {
        final InputStream input = Tools.getResourceAsStream(Tools.getThisPackagePath() + "test.txt");

        assertTrue(input.available() > 0);

        Tools.close(input);

        try {
            input.available();

            fail("This point shouldn't be reached");
        } catch (final IOException exception) {
            assertEquals("Stream closed", exception.getMessage());
        }
    }

    @Test
    public final void testListAndIterable() {
        assertEquals(Arrays.asList("42", "33"), Tools.list(Tools.iterable(new StringTokenizer("42 33"))));
    }

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
    public final void testAppend() {
        final Object[] empty = new Object[0];

        assertArrayEquals(Tools.array(42, 33, 42), Tools.append(Tools.array(42), Tools.array(33, 42)));
        assertArrayEquals(Tools.array(42), Tools.append(Tools.array(42), empty));
        assertArrayEquals(Tools.array(42), Tools.append(empty, Tools.array((Object) 42)));
        assertArrayEquals(empty, Tools.append(empty, empty));
    }

    @Test
    public final void testGetResourceAsStream() {
        assertNotNull(Tools.getResourceAsStream(Tools.getThisPackagePath() + "test.txt"));
    }

    @Test
    public final void testGetResourceURL() {
        assertNotNull(Tools.getResourceURL(Tools.getThisPackagePath() + "test.txt"));
        assertEquals(null, Tools.getResourceURL("missing_resource"));
    }

    @Test
    public final void testInvoke() {
        assertEquals((Object) 42, Tools.invoke(Integer.class, "parseInt", "42"));
        assertEquals((Object) 42, Tools.invoke(42L, "intValue"));

        {
            final ObjectWithArbitraryProperties objectWithArbitraryProperties = new ObjectWithArbitraryProperties();

            assertEquals((Object) null, Tools.invoke(objectWithArbitraryProperties, "setPrivateStringProperty", "42"));
            assertEquals("42", Tools.invoke(objectWithArbitraryProperties, "getPrivateStringProperty"));
        }
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
        {
            final Method getter = Tools.getGetter(objectWithArbitraryProperties, "privateStringProperty");

            assertNotNull(getter);
            assertEquals("getPrivateStringProperty", getter.getName());
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
        {
            final Method setter = Tools.getSetter(objectWithArbitraryProperties, "privateStringProperty", String.class);

            assertNotNull(setter);
            assertEquals("setPrivateStringProperty", setter.getName());
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
                Tools.ignore(expectedException);
            }

        }
        {
            try {
                // Bad casing
                final Method setter = Tools.getSetter(objectWithArbitraryProperties, "INTPROPERTY", int.class);

                fail("getSetter() should have failed but instead returned " + setter);
            } catch (final RuntimeException expectedException) {
                Tools.ignore(expectedException);
            }
        }
        {
            try {
                // Mismatching parameter type
                final Method setter = Tools.getSetter(objectWithArbitraryProperties, "intProperty", boolean.class);

                fail("getSetter() should have failed but instead returned " + setter);
            } catch (final RuntimeException expectedException) {
                Tools.ignore(expectedException);
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
        assertEquals("net/sourceforge/aprog/tools/", Tools.getThisPackagePath());
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
     *
     * @param cls
     * <br>Not null
     * @param tmpRoot
     * <br>Not null
     * <br>Input-output
     * @throws FileNotFoundException If an error occurs
     */
    private static final void copyToTmp(final Class<?> cls, final File tmpRoot) throws FileNotFoundException {
        File file = tmpRoot;

        for (final String pathElement : Tools.getThisPackagePath().split("/")) {
            file = new File(file, pathElement);

            assertTrue(file.isDirectory() || file.mkdir());
        }

        final String classFileName = cls.getSimpleName() + ".class";

        file = new File(file, classFileName);

        Tools.write(
                new FileInputStream(Tools.getClassRoot(cls) + File.separator + Tools.getThisPackagePath() + classFileName),
                new FileOutputStream(file));
    }

}

/**
 * @author codistmonk (creation 2010-05-19)
 */
class ObjectWithArbitraryProperties {

    private int intProperty;

    private boolean booleanProperty1;

    private boolean booleanProperty2;

    private boolean booleanProperty3;

    private String packagePrivateStringProperty;

    private String privateStringProperty;

    {
        // The following instruction removes "unused" warnings
        this.setPrivateStringProperty(this.getPrivateStringProperty());
    }

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

    /**
     *
     * @return
     * <br>A possibly null value
     * <br>A shared value
     */
    private final String getPrivateStringProperty() {
        return this.privateStringProperty;
    }

    /**
     *
     * @param privateStringProperty
     * <br>Can be null
     * <br>Shared parameter
     */
    private final void setPrivateStringProperty(final String privateStringProperty) {
        this.privateStringProperty = privateStringProperty;
    }

}
