package com.butent.bee.shared.testutils;

import static org.junit.Assert.*;
import java.util.Date;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.butent.bee.shared.DateTime;
import com.butent.bee.shared.JustDate;
import com.butent.bee.shared.exceptions.BeeRuntimeException;

public class TestJustDate {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public final void testHashCode() {
		JustDate dt = new JustDate(1970, 1, 11);
		assertEquals(10, dt.hashCode());

		dt = new JustDate(1969, 12, 22);
		assertEquals(-10, dt.hashCode());

		dt = new JustDate(1970, 2, 6);

		assertEquals(36, dt.hashCode());
	}

	@Test
	public final void testParse() {

		try {
			String str = "";
			JustDate.parse(str);
			fail("Exceptions not works!");
		} catch (BeeRuntimeException e) {
			assertTrue(true);
		} catch (Exception e) {
			fail("Need BeeRuntimeException: " + e.getMessage());
		}

		try {
			String str = null;
			JustDate.parse(str);
			fail("Exceptions not works!");
		} catch (BeeRuntimeException e) {
			assertTrue(true);
		} catch (Exception e) {
			fail("Need BeeRuntimeException: " + e.getMessage());
		}

		String str = "46";
		JustDate jd = JustDate.parse(str);

		assertEquals(1970, jd.getYear());
		assertEquals(2, jd.getMonth());
		assertEquals(16, jd.getDom());

		str = "2011-04-06";

		jd = JustDate.parse(str);

		assertEquals(2011, jd.getYear());
		assertEquals(4, jd.getMonth());
		assertEquals(6, jd.getDom());

		str = "2011;-04;-06";

		jd = JustDate.parse(str);

		assertEquals(2011, jd.getYear());
		assertEquals(4, jd.getMonth());
		assertEquals(6, jd.getDom());

		str = "2011/04/06";

		jd = JustDate.parse(str);

		assertEquals(2011, jd.getYear());
		assertEquals(4, jd.getMonth());
		assertEquals(6, jd.getDom());

		str = "2011/04/06 12:36:59,78";

		jd = JustDate.parse(str);

		assertEquals(2011, jd.getYear());
		assertEquals(4, jd.getMonth());
		assertEquals(6, jd.getDom());

		str = "2011-02";

		jd = JustDate.parse(str);

		assertEquals(2011, jd.getYear());
		assertEquals(2, jd.getMonth());
		assertEquals(1, jd.getDom());

		String str1 = "2010-";
		JustDate jd1 = JustDate.parse(str1);
		assertEquals("2010.01.01", jd1.toString());
	}

	@Test
	public final void testJustDate() {
		JustDate jd = new JustDate();
		DateTime dt = new DateTime();

		assertEquals(dt.getYear(), jd.getYear());
		assertEquals(dt.getMonth(), jd.getMonth());
		assertEquals(dt.getDom(), jd.getDom());
		System.out.println(jd.getYear() + "-" + jd.getMonth() + "-"
				+ jd.getDom());

	}

	@SuppressWarnings("unused")
	@Test
	public final void testJustDateDate() {

		DateTime d = new DateTime();
		JustDate dt = new JustDate(new Date());

		assertEquals(d.getYear(), dt.getYear());
		assertEquals(d.getMonth(), dt.getMonth());
		assertEquals(d.getDom(), dt.getDom());

		try {
			JustDate dtf = new JustDate((Date) null);
			assertEquals(-7200000, dtf.getDateTime().getTime());
		} catch (BeeRuntimeException e) {
			assertTrue(true);
		} catch (Exception e) {
			fail("Need BeeRuntimeException " + e.getMessage());
		}
	}

	@SuppressWarnings("unused")
	@Test
	public final void testJustDateDateTime() {
		DateTime dt = new DateTime(2011, 4, 8);
		JustDate jd = new JustDate(dt);

		assertEquals(2011, jd.getYear());
		assertEquals(4, jd.getMonth());
		assertEquals(8, jd.getDom());

		dt = new DateTime();
		jd = new JustDate(new DateTime());

		assertEquals(dt.getYear(), jd.getYear());
		assertEquals(dt.getMonth(), jd.getMonth());
		assertEquals(dt.getDom(), jd.getDom());

		try {
			JustDate dtf = new JustDate((DateTime) null);
			assertEquals(-7200000, dtf.getDateTime().getTime());
		} catch (BeeRuntimeException e) {
			assertTrue(true);
		} catch (Exception e) {
			fail("Need BeeRuntimeException " + e.getMessage());
		}
	}

	@Test
	public final void testJustDateInt() {
		JustDate a = new JustDate((int) 10);

		assertEquals(1970, a.getYear());
		assertEquals(1, a.getMonth());
		assertEquals(11, a.getDom());

		a = new JustDate((int) -10);
		assertEquals(1969, a.getYear());
		assertEquals(12, a.getMonth());
		assertEquals(22, a.getDom());
	}

	@Test
	public final void testJustDateIntIntInt() {
		JustDate dt = new JustDate(2011, 4, 8);

		assertEquals(2011, dt.getYear());
		assertEquals(4, dt.getMonth());
		assertEquals(8, dt.getDom());

		dt = new JustDate(0, 1, 1);

		assertEquals(0, dt.getYear());
		assertEquals(1, dt.getMonth());
		assertEquals(1, dt.getDom());

		dt = new JustDate(-21, 1, 1);

		assertEquals(-21, dt.getYear());
		assertEquals(1, dt.getMonth());
		assertEquals(1, dt.getDom());
	}

	@Test
	public final void testJustDateLong() {
		JustDate jd = new JustDate((long) 1298362388227L);

		assertEquals(2011, jd.getYear());
		assertEquals(2, jd.getMonth());
		assertEquals(22, jd.getDom());
	}

	@Test
	public final void testCompareTo() {
		JustDate jd = new JustDate(2011, 4, 8);
		JustDate jd1 = new JustDate(1298362388227L);
		JustDate jd2 = new JustDate(2021, 4, 8);
		JustDate jd3 = JustDate.parse("2011,04,08");

		assertEquals(true, jd.compareTo(jd1) > 0);
		assertEquals(0, jd.compareTo(jd3));
		assertEquals(true, jd.compareTo(jd2) < 0);
	}

	@Test
	public final void testDeserialize() {
		JustDate a = new JustDate();
		a.deserialize("10");

		assertEquals(1970, a.getYear());
		assertEquals(1, a.getMonth());
		assertEquals(11, a.getDom());

		a = new JustDate();
		a.deserialize("-10");

		assertEquals(1969, a.getYear());
		assertEquals(12, a.getMonth());
		assertEquals(22, a.getDom());
	}

	@Test
	public final void testEqualsObject() {
		JustDate jd = new JustDate(1298362388227L);

		assertEquals(true, jd.equals(new JustDate(2011, 2, 22)));
		assertEquals(false, jd.equals(new DateTime(2011, 2, 22)));
	}

	@Test
	public final void testGetDay() {
		JustDate dt = new JustDate(1970, 1, 11);
		assertEquals(10, dt.getDay());

		dt = new JustDate(1969, 12, 22);
		assertEquals(-10, dt.getDay());

		dt = new JustDate(1970, 2, 6);

		assertEquals(36, dt.getDay());

	}

	@Test
	public final void testGetDom() {
		JustDate jd = new JustDate(2011, 2, 25);

		assertEquals(25, jd.getDom());

		jd = new JustDate(2011, 2, 29);
		assertEquals(1, jd.getDom());
		assertEquals(3, jd.getMonth());

		jd = new JustDate(1298362388227L);
		assertEquals(22, jd.getDom());

		jd = new JustDate(2011, 2, -1);
		assertEquals(1, jd.getDom());
		assertEquals(2, jd.getMonth());

		jd = new JustDate(2011, 2, 0);
		assertEquals(1, jd.getDom());
		assertEquals(2, jd.getMonth());
	}

	@Test
	public final void testGetDow() {
		JustDate jd = new JustDate(2011, 2, 25);

		assertEquals(25, jd.getDom());

		jd = new JustDate(2011, 2, 29);
		assertEquals(3, jd.getDow());
		assertEquals(3, jd.getMonth());

		jd = new JustDate(1298362388227L);
		assertEquals(3, jd.getDow());

		jd = new JustDate(2011, 2, -1);
		assertEquals(3, jd.getDow());
		assertEquals(2, jd.getMonth());

		jd = new JustDate(2011, 2, 0);
		assertEquals(3, jd.getDow());
		assertEquals(2, jd.getMonth());
	}

	@Test
	public final void testGetDoy() {
		JustDate jd = new JustDate(2011, 2, 22);
		assertEquals(53, jd.getDoy());

		jd = new JustDate(2011, 2, 22);
		assertEquals(53, jd.getDoy());

		jd = new JustDate(2011, 03, 30);
		assertEquals(89, jd.getDoy());

		jd = new JustDate(2011, 03, 6);
		assertEquals(65, jd.getDoy());

		jd = new JustDate(2011, 03, 19);
		assertEquals(78, jd.getDoy());

		jd = new JustDate(2011, 01, 01);
		assertEquals(1, jd.getDoy());

		jd = new JustDate(2011, 12, 31);
		assertEquals(365, jd.getDoy());

		jd = new JustDate(2012, 12, 31);
		assertEquals(366, jd.getDoy());
	}

	@Test
	public final void testGetMonth() {

		JustDate dt = new JustDate(2011, 1, 12);
		assertEquals(1, dt.getMonth());

		dt = new JustDate(2011, 12, 12);
		assertEquals(12, dt.getMonth());

		dt = new JustDate(2012, 0, 1);
		assertEquals(2012, dt.getYear());
		assertEquals(1, dt.getMonth());
		assertEquals(1, dt.getDom());

		dt = new JustDate(2011, 0, 1);
		assertEquals(2011, dt.getYear());
		assertEquals(01, dt.getMonth());

		dt = new JustDate(2011, -1, 1);
		assertEquals(2011, dt.getYear());
		assertEquals(1, dt.getMonth());
	}

	@Test
	public final void testSerialize() {
		JustDate jd = new JustDate(1970, 1, 1);

		assertEquals("0", jd.serialize());

		jd = new JustDate(1969, 12, 1);
		assertEquals("-31", jd.serialize());

		jd = new JustDate(1971, 1, 1);
		assertEquals("365", jd.serialize());

		jd = new JustDate(1972, 1, 1);
		assertEquals("730", jd.serialize());

		jd = new JustDate(1973, 1, 1);
		assertEquals("1096", jd.serialize());
	}

	@Test
	public final void testSetDay() {
		JustDate jd = new JustDate();

		jd.setDay(0);

		assertEquals(1970, jd.getYear());
		assertEquals(1, jd.getMonth());
		assertEquals(1, jd.getDom());

		jd.setDay(1098);

		assertEquals(1973, jd.getYear());
		assertEquals(1, jd.getMonth());
		assertEquals(3, jd.getDom());

	}

	@Test
	public final void testToString() {
		JustDate jd = new JustDate(2011, 4, 8);

		assertEquals("2011.04.08", jd.toString());

		jd = new JustDate(1298362388227L);
		assertEquals("2011.02.22", jd.toString());
	}

}
