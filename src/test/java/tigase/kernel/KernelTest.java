package tigase.kernel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class KernelTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	public KernelTest() {
		Logger logger = Logger.getLogger("tigase.kernel.Kernel");

		// create a ConsoleHandler
		Handler handler = new ConsoleHandler();
		handler.setLevel(Level.ALL);
		logger.addHandler(handler);
		logger.setLevel(Level.ALL);

		if (logger.isLoggable(Level.CONFIG))
			logger.config("Logger successfully initialized");
	}

	@Test
	public void test() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Kernel krnl = new Kernel();
		krnl.registerBeanClass("bean1", Bean1.class);
		krnl.registerBeanClass("bean2", Bean2.class);
		krnl.registerBeanClass("bean3", Bean3.class);
		krnl.registerBeanClass("bean4", Bean4.class);
		krnl.registerBeanClass("bean4_1", Bean4.class);
		krnl.registerBeanClass("bean5", Bean5.class, Bean5Factory.class);

		DependencyGrapher dg = new DependencyGrapher(krnl);
		System.out.println(dg.getDependencyGraph());

		Bean1 b1 = krnl.getInstance("bean1");
		Bean2 b2 = krnl.getInstance("bean2");
		Bean3 b3 = krnl.getInstance("bean3");
		Bean4 b4 = krnl.getInstance("bean4");
		Bean4 b41 = krnl.getInstance("bean4_1");

		assertNotNull(b1);
		assertNotNull(b2);
		assertNotNull(b3);
		assertNotNull(b4);
		assertNotNull(b41);

		assertTrue(b1 instanceof Bean1);
		assertTrue(b2 instanceof Bean2);
		assertTrue(b3 instanceof Bean3);
		assertTrue(b4 instanceof Bean4);
		assertTrue(b41 instanceof Bean4);

		assertEquals(b2, b1.getBean2());
		assertEquals(b3, b1.getBean3());
		assertEquals(b3, b2.getBean3());
		assertEquals(b41, b2.getBean4());
		assertEquals(b4, b3.getBean4());
		assertEquals(b41, b3.getBean41());

		assertNotNull(b1.getSs());
		assertEquals(3, b1.getSs().length);

		assertEquals(3, b1.getXxx().size());
		assertTrue(b1.getXxx().contains(b3));
		assertTrue(b1.getXxx().contains(b4));
		assertTrue(b1.getXxx().contains(b41));

		krnl.unregister("bean4_1");
		try {
			assertNull(krnl.getInstance("bean4_1"));
			Assert.fail();
		} catch (KernelException e) {
			assertEquals("Unknown bean 'bean4_1'.", e.getMessage());
		}
		assertNull(b3.getBean41());
		assertNull(b2.getBean4());

		assertEquals(2, b1.getSs().length);

		assertEquals(2, b1.getXxx().size());
		assertTrue(b1.getXxx().contains(b3));
		assertTrue(b1.getXxx().contains(b4));

		krnl.registerBeanClass("bean6", Bean6.class);

		assertEquals(3, b1.getSs().length);
		assertEquals(3, b1.getXxx().size());

		System.out.println(dg.getDependencyGraph());

	}

	@Test
	public void test2() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Kernel krnl = new Kernel();
		krnl.registerBeanClass("bean7", Bean7.class);

		krnl.registerBeanClass("beanX", Bean4.class);
		krnl.registerBeanClass("beanX", Bean5.class, Bean5Factory.class);

		assertEquals(Bean5.class, krnl.getInstance(Bean7.class).getObj().getClass());
		assertEquals(Bean5.class, krnl.getInstance("beanX").getClass());

		krnl.registerBeanClass("beanX", Bean6.class);

		assertEquals(Bean6.class, krnl.getInstance("beanX").getClass());
		assertEquals(Bean6.class, krnl.getInstance(Bean7.class).getObj().getClass());
	}
}
