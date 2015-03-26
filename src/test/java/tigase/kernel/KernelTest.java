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

		Bean4 bean4_1_o = new Bean4();
		krnl.registerBean(Bean1.class).exec();
		krnl.registerBean("bean2").asClass(Bean2.class).exec();
		krnl.registerBean("bean3").asClass(Bean3.class).exec();
		krnl.registerBean("bean4").asClass(Bean4.class).exec();
		krnl.registerBean("bean4_1").asInstance(bean4_1_o).exec();
		krnl.registerBean("bean5").asClass(Bean5.class).withFactory(Bean5Factory.class).exec();

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

		assertEquals(b41, bean4_1_o);

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

		krnl.registerBean("bean6").asClass(Bean6.class).exec();

		assertEquals(3, b1.getSs().length);
		assertEquals(3, b1.getXxx().size());

		System.out.println(dg.getDependencyGraph());

	}

	@Test
	public void test2() throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Kernel krnl = new Kernel();
		krnl.registerBean("bean7").asClass(Bean7.class).exec();

		krnl.registerBean("beanX").asClass(Bean4.class).exec();
		krnl.registerBean("beanX").asClass(Bean5.class).withFactory(Bean5Factory.class).exec();

		assertEquals(Bean5.class, krnl.getInstance(Bean7.class).getObj().getClass());
		assertEquals(Bean5.class, krnl.getInstance("beanX").getClass());

		krnl.registerBean("beanX").asClass(Bean6.class).exec();

		assertEquals(Bean6.class, krnl.getInstance("beanX").getClass());
		assertEquals(Bean6.class, krnl.getInstance(Bean7.class).getObj().getClass());
	}
}
