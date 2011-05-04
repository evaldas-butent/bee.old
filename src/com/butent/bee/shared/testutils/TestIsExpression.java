package com.butent.bee.shared.testutils;

import static org.junit.Assert.*;
import java.util.Date;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.butent.bee.shared.BeeConst;
import com.butent.bee.shared.DateTime;
import com.butent.bee.shared.JustDate;
import com.butent.bee.shared.exceptions.BeeRuntimeException;
import com.butent.bee.server.sql.BeeConstants.DataType;
import com.butent.bee.server.sql.IsCondition;
import com.butent.bee.server.sql.IsExpression;
import com.butent.bee.server.sql.SqlBuilder;
import com.butent.bee.server.sql.SqlBuilderFactory;
import com.butent.bee.server.sql.SqlSelect;
import com.butent.bee.server.sql.SqlUtils;

public class TestIsExpression {

	@Before
	public void setUp() throws Exception {

	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public final void testSetDefaultEngine() {
		SqlBuilderFactory.setDefaultEngine("unknown");
		assertEquals("unknown", SqlBuilderFactory.getEngine());

		SqlBuilderFactory.setDefaultEngine("MySql");
		assertEquals(BeeConst.MYSQL, SqlBuilderFactory.getEngine());

		SqlBuilderFactory.setDefaultEngine("MsSql");
		assertEquals(BeeConst.MSSQL, SqlBuilderFactory.getEngine());

		SqlBuilderFactory.setDefaultEngine("Oracle");
		assertEquals(BeeConst.ORACLE, SqlBuilderFactory.getEngine());

		SqlBuilderFactory.setDefaultEngine("PostgreSql");
		assertEquals(BeeConst.PGSQL, SqlBuilderFactory.getEngine());
	}

	@Test
	public void testNameExpression() {
		IsExpression ie = SqlUtils.name("A longer name");
		assertEquals("A longer name", ie.getValue());
		assertEquals(null, ie.getSqlParams());

		SqlBuilderFactory.setDefaultEngine("unknown");
		assertEquals("A longer name",
				ie.getSqlString(SqlBuilderFactory.getBuilder(), false));
		IsExpression ie2 = SqlUtils.name("A.longer.name");
		assertEquals("A.longer.name",
				ie2.getSqlString(SqlBuilderFactory.getBuilder(), false));
		IsExpression ie3 = SqlUtils.name("select.*.from.name");
		assertEquals("select.*.from.name",
				ie3.getSqlString(SqlBuilderFactory.getBuilder(), false));
		
		IsExpression iemysql = SqlUtils.name("A longer name");
		SqlBuilderFactory.setDefaultEngine(BeeConst.MYSQL);
		assertEquals("`A longer name`",
				iemysql.getSqlString(SqlBuilderFactory.getBuilder(), false));

		IsExpression ie2mysql = SqlUtils.name("A.longer.name");
		assertEquals("`A`.`longer`.`name`",
				ie2mysql.getSqlString(SqlBuilderFactory.getBuilder(), false));
		IsExpression ie3mysql = SqlUtils.name("select.*.from.name");
		assertEquals("`select`.*.`from`.`name`",
				ie3mysql.getSqlString(SqlBuilderFactory.getBuilder(), false));

		IsExpression ie4mysql = SqlUtils
				.name("Select * from table where name=\"tester\" or table1.type=table.*.concret.type");
		assertEquals(
				"`Select * from table where name=\"tester\" or table1`.`type=table`.*.`concret`.`type`",
				ie4mysql.getSqlString(SqlBuilderFactory.getBuilder(), false));

		IsExpression iemssql = SqlUtils.name("A longer name");
		SqlBuilderFactory.setDefaultEngine(BeeConst.MSSQL);
		assertEquals("[A longer name]",
				iemssql.getSqlString(SqlBuilderFactory.getBuilder(), false));

		IsExpression ie2mssql = SqlUtils.name("A.longer.name");
		assertEquals("[A].[longer].[name]",
				ie2mssql.getSqlString(SqlBuilderFactory.getBuilder(), false));
		IsExpression ie3mssql = SqlUtils.name("select.*.from.name");
		assertEquals("[select].*.[from].[name]",
				ie3mssql.getSqlString(SqlBuilderFactory.getBuilder(), false));

		IsExpression ie4mssql = SqlUtils
				.name("Select * from table where name=\"tester\" or table1.type=table.*.concret.type");
		assertEquals(
				"[Select * from table where name=\"tester\" or table1].[type=table].*.[concret].[type]",
				ie4mssql.getSqlString(SqlBuilderFactory.getBuilder(), false));

		IsExpression ieoracle = SqlUtils.name("A longer name");
		SqlBuilderFactory.setDefaultEngine(BeeConst.ORACLE);
		assertEquals("\"A longer name\"",
				ieoracle.getSqlString(SqlBuilderFactory.getBuilder(), false));

		IsExpression ie2oracle = SqlUtils.name("A.longer.name");
		assertEquals("\"A\".\"longer\".\"name\"",
				ie2oracle.getSqlString(SqlBuilderFactory.getBuilder(), false));
		IsExpression ie3oracle = SqlUtils.name("select.*.from.name");
		assertEquals("\"select\".*.\"from\".\"name\"",
				ie3oracle.getSqlString(SqlBuilderFactory.getBuilder(), false));

		IsExpression ie4oracle = SqlUtils
				.name("Select * from table where name=\"tester\" or table1.type=table.*.concret.type");
		assertEquals(
				"\"Select * from table where name=\"tester\" or table1\".\"type=table\".*.\"concret\".\"type\"",
				ie4oracle.getSqlString(SqlBuilderFactory.getBuilder(), false));

		IsExpression iepg = SqlUtils.name("A longer name");
		SqlBuilderFactory.setDefaultEngine(BeeConst.PGSQL);
		assertEquals("\"A longer name\"",
				iepg.getSqlString(SqlBuilderFactory.getBuilder(), false));

		IsExpression ie2pg = SqlUtils.name("A.longer.name");
		assertEquals("\"A\".\"longer\".\"name\"",
				ie2pg.getSqlString(SqlBuilderFactory.getBuilder(), false));
		IsExpression ie3pg = SqlUtils.name("select.*.from.name");
		assertEquals("\"select\".*.\"from\".\"name\"",
				ie3pg.getSqlString(SqlBuilderFactory.getBuilder(), false));

		IsExpression ie4pg = SqlUtils
				.name("Select * from table where name=\"tester\" or table1.type=table.*.concret.type");
		assertEquals(
				"\"Select * from table where name=\"tester\" or table1\".\"type=table\".*.\"concret\".\"type\"",
				ie4pg.getSqlString(SqlBuilderFactory.getBuilder(), false));
	}

	@Test
	public void testConstantExpressionGeneric() {
		Date d = new Date(1298362388227L);
		JustDate jd = new JustDate(1298362388227L);
		DateTime dt = new DateTime(1298362388227L);

		SqlBuilderFactory.setDefaultEngine("unknown");
		IsExpression ce = SqlUtils.constant(null);
		assertEquals("null",
				ce.getSqlString(SqlBuilderFactory.getBuilder(), false));

		IsExpression ce2 = SqlUtils.constant(false);
		assertEquals("0",
				ce2.getSqlString(SqlBuilderFactory.getBuilder(), false));

		IsExpression ce3 = SqlUtils.constant(jd);
		assertEquals("15027",
				ce3.getSqlString(SqlBuilderFactory.getBuilder(), false));

		IsExpression ce4 = SqlUtils.constant(d);
		assertEquals("15027",
				ce4.getSqlString(SqlBuilderFactory.getBuilder(), false));

		IsExpression ce5 = SqlUtils.constant(dt);
		assertEquals("1298362388227",
				ce5.getSqlString(SqlBuilderFactory.getBuilder(), false));

		IsExpression ce6 = SqlUtils.constant(5);
		assertEquals("5",
				ce6.getSqlString(SqlBuilderFactory.getBuilder(), false));

		IsExpression ce7 = SqlUtils.constant(" 'from' ");
		assertEquals("' ''from'' '",
				ce7.getSqlString(SqlBuilderFactory.getBuilder(), false));

		IsExpression ce8 = SqlUtils.constant(true);
		assertEquals("1",
				ce8.getSqlString(SqlBuilderFactory.getBuilder(), false));

		IsExpression ce9 = SqlUtils.constant(true);
		assertEquals("?",
				ce9.getSqlString(SqlBuilderFactory.getBuilder(), true));
	}

	@Test
	public void testConstantExpressionMySql() {
		SqlBuilderFactory.setDefaultEngine(BeeConst.MYSQL);
		IsExpression ce7 = SqlUtils.constant(" \\abc ");
		assertEquals("com.butent.bee.server.sql.MySqlBuilder",
				SqlBuilderFactory.getBuilder().getClass().getCanonicalName());
		assertEquals("' \\\\abc '",
				ce7.getSqlString(SqlBuilderFactory.getBuilder(), false));
	}

	@Test
	public void testConstantExpressionMsSql() {
		SqlBuilderFactory.setDefaultEngine(BeeConst.MSSQL);
		IsExpression ce7 = SqlUtils.constant(" 'from' ");
		assertEquals("' ''from'' '",
				ce7.getSqlString(SqlBuilderFactory.getBuilder(), false));
	}

	@SuppressWarnings("unused")
	@Test
	public void testConstantExpressionOracle() {
		Date d = new Date(1298362388227L);
		JustDate jd = new JustDate(1298362388227L);
		DateTime dt = new DateTime(1298362388227L);
		SqlBuilderFactory.setDefaultEngine(BeeConst.ORACLE);
		IsExpression ce7 = SqlUtils.constant(" \from' ");
		assertEquals("' \from'' '",
				ce7.getSqlString(SqlBuilderFactory.getBuilder(), false));
	}

	@Test
	public void testConstantExpressionPostGre() {
		SqlBuilderFactory.setDefaultEngine(BeeConst.PGSQL);
		IsExpression ce7 = SqlUtils.constant(" \\from\\ ");
		assertEquals("' \\\\from\\\\ '",
				ce7.getSqlString(SqlBuilderFactory.getBuilder(), false));
	}

	@Test
	public void testConstantExpGetSqlParams() {
		IsExpression ce = SqlUtils.constant(null);
		ce = SqlUtils.constant("Labas");
		assertEquals("Labas", ce.getSqlParams().get(0));
	}

	@Test
	public void testComplexExpressionGetAndParams() {

		SqlBuilderFactory.setDefaultEngine("nto found");
		SqlSelect select = new SqlSelect();
		SqlSelect select1 = new SqlSelect();

		select.addField("JUnit", "method_names", "metodai");
		select.addFrom("JUnit");
		select.setParamMode(true);
		select.addFields("JUnit", "class", "method_name", "params");
		select.addFrom("Tests");
		IsCondition clause = SqlUtils.contains(SqlUtils.name("DumpsList"),
				"Default dump at 00000x00000");
		select.setWhere(clause);
		select1.addField("JUnit1", "method_names1", "metodai1");
		select1.addFrom("JUnit1");
		IsCondition clause1 = SqlUtils.contains(SqlUtils.name("Computers"),
				"190.0.1.2");
		select1.setWhere(clause1);

		select1.setParamMode(true);
		select1.addFields("JUnit1", "class1", "method_name1", "params1");
		select1.addFrom(select, "tst");

		IsExpression ce2 = SqlUtils.expression(select);

		assertEquals("%Default dump at 00000x00000%", ce2.getSqlParams()
				.toArray()[0]);

		IsExpression ce3 = SqlUtils.expression(select1);
		assertEquals(
				"SELECT JUnit1.method_names1 AS metodai1, JUnit1.class1, JUnit1.method_name1, JUnit1.params1 FROM JUnit1, (SELECT JUnit.method_names AS metodai, JUnit.class, JUnit.method_name, JUnit.params FROM JUnit, Tests WHERE DumpsList LIKE ?) tst WHERE Computers LIKE ?",
				select1.getQuery());
		assertEquals("%Default dump at 00000x00000%", ce3.getSqlParams()
				.toArray()[0]);
		assertEquals("%190.0.1.2%", ce3.getSqlParams().toArray()[1]);
	}

	@Test
	public void testComplexExpressionGeneric() {

		SqlBuilderFactory.setDefaultEngine("unknown");
		IsExpression ce = SqlUtils.expression(1, "string", 5.0);
		assertEquals("1string5.0",
				ce.getSqlString(SqlBuilderFactory.getBuilder(), false));

		SqlSelect select = new SqlSelect();
		select.addField("users", "username", "vardas");
		select.addFrom("users");

		IsExpression ce2 = SqlUtils.expression(select);
		assertEquals("SELECT users.username AS vardas FROM users",
				ce2.getSqlString(SqlBuilderFactory.getBuilder(), false));

		SqlSelect select2 = new SqlSelect();
		select2.addField("phones", "phone_names", "tel_vardai");
		select2.addFrom("phones");
		IsExpression ce3 = SqlUtils.expression(select2);
		assertEquals("SELECT phones.phone_names AS tel_vardai FROM phones",
				ce3.getSqlString(SqlBuilderFactory.getBuilder(), false));

		select.addFrom(select2, "alias2");

		IsExpression ce4 = SqlUtils.expression(select);
		assertEquals(
				"SELECT users.username AS vardas FROM users, (SELECT phones.phone_names AS tel_vardai FROM phones) alias2",
				ce4.getSqlString(SqlBuilderFactory.getBuilder(), false));

		select2 = select2.reset();
		select2.addField("JUnit", "method_names", "klases");
		select2.setDistinctMode(true);
		select2.addFrom("JUnit");
		IsExpression ce5 = SqlUtils.expression(select2);
		assertEquals(
				"SELECT DISTINCT JUnit.method_names AS klases FROM phones, JUnit",
				ce5.getSqlString(SqlBuilderFactory.getBuilder(), false));
	}

	@Test
	public void testComplexExpressionMYSQL() {
		SqlBuilderFactory.setDefaultEngine(BeeConst.MYSQL);
		IsExpression ce = SqlUtils.expression(1, "string", 5.0);
		assertEquals("1string5.0",
				ce.getSqlString(SqlBuilderFactory.getBuilder(), false));

		SqlSelect select = new SqlSelect();
		select.addField("users", "username", "vardas");
		select.addFrom("users");

		IsExpression ce2 = SqlUtils.expression(select);
		assertEquals("SELECT `users`.`username` AS `vardas` FROM `users`",
				ce2.getSqlString(SqlBuilderFactory.getBuilder(), false));

		SqlSelect select2 = new SqlSelect();
		select2.addField("phones", "phone_names", "tel_vardai");
		select2.addFrom("phones");
		IsExpression ce3 = SqlUtils.expression(select2);
		assertEquals(
				"SELECT `phones`.`phone_names` AS `tel_vardai` FROM `phones`",
				ce3.getSqlString(SqlBuilderFactory.getBuilder(), false));

		select.addFrom(select2, "alias2");

		IsExpression ce4 = SqlUtils.expression(select);
		assertEquals(
				"SELECT `users`.`username` AS `vardas` FROM `users`, (SELECT `phones`.`phone_names` AS `tel_vardai` FROM `phones`) `alias2`",
				ce4.getSqlString(SqlBuilderFactory.getBuilder(), false));

		select2 = select2.reset();
		select2.addField("JUnit", "method_names", "klases");
		select2.setDistinctMode(true);
		select2.addFrom("JUnit");
		IsExpression ce5 = SqlUtils.expression(select2);
		assertEquals(
				"SELECT DISTINCT `JUnit`.`method_names` AS `klases` FROM `phones`, `JUnit`",
				ce5.getSqlString(SqlBuilderFactory.getBuilder(), false));
	}

	@Test
	public void testComplexExpressionMSSQL() {
		SqlBuilderFactory.setDefaultEngine(BeeConst.MSSQL);
		IsExpression ce = SqlUtils.expression(1, "string", 5.0);
		assertEquals("1string5.0",
				ce.getSqlString(SqlBuilderFactory.getBuilder(), false));

		SqlSelect select = new SqlSelect();
		select.addField("users", "username", "vardas");
		select.addFrom("users");

		IsExpression ce2 = SqlUtils.expression(select);
		assertEquals("SELECT [users].[username] AS [vardas] FROM [users]",
				ce2.getSqlString(SqlBuilderFactory.getBuilder(), false));

		SqlSelect select2 = new SqlSelect();
		select2.addField("phones", "phone_names", "tel_vardai");
		select2.addFrom("phones");
		IsExpression ce3 = SqlUtils.expression(select2);
		assertEquals(
				"SELECT [phones].[phone_names] AS [tel_vardai] FROM [phones]",
				ce3.getSqlString(SqlBuilderFactory.getBuilder(), false));

		select.addFrom(select2, "alias2");

		IsExpression ce4 = SqlUtils.expression(select);
		assertEquals(
				"SELECT [users].[username] AS [vardas] FROM [users], (SELECT [phones].[phone_names] AS [tel_vardai] FROM [phones]) [alias2]",
				ce4.getSqlString(SqlBuilderFactory.getBuilder(), false));

		select2 = select2.reset();
		select2.addField("JUnit", "method_names", "klases");
		select2.setDistinctMode(true);
		select2.addFrom("JUnit");
		IsExpression ce5 = SqlUtils.expression(select2);
		assertEquals(
				"SELECT DISTINCT [JUnit].[method_names] AS [klases] FROM [phones], [JUnit]",
				ce5.getSqlString(SqlBuilderFactory.getBuilder(), false));
	}

	@Test
	public void testComplexExpressionOracle() {
		SqlBuilderFactory.setDefaultEngine(BeeConst.ORACLE);
		IsExpression ce = SqlUtils.expression(1, "string", 5.0);
		assertEquals("1string5.0",
				ce.getSqlString(SqlBuilderFactory.getBuilder(), false));

		SqlSelect select = new SqlSelect();
		select.addField("users", "username", "vardas");
		select.addFrom("users");

		IsExpression ce2 = SqlUtils.expression(select);
		assertEquals(
				"SELECT \"users\".\"username\" AS \"vardas\" FROM \"users\"",
				ce2.getSqlString(SqlBuilderFactory.getBuilder(), false));

		SqlSelect select2 = new SqlSelect();
		select2.addField("phones", "phone_names", "tel_vardai");
		select2.addFrom("phones");
		IsExpression ce3 = SqlUtils.expression(select2);
		assertEquals(
				"SELECT \"phones\".\"phone_names\" AS \"tel_vardai\" FROM \"phones\"",
				ce3.getSqlString(SqlBuilderFactory.getBuilder(), false));

		select.addFrom(select2, "alias2");

		IsExpression ce4 = SqlUtils.expression(select);
		assertEquals(
				"SELECT \"users\".\"username\" AS \"vardas\" FROM \"users\", (SELECT \"phones\".\"phone_names\" AS \"tel_vardai\" FROM \"phones\") \"alias2\"",
				ce4.getSqlString(SqlBuilderFactory.getBuilder(), false));

		select2 = select2.reset();
		select2.addField("JUnit", "method_names", "klases");
		select2.setDistinctMode(true);
		select2.addFrom("JUnit");
		IsExpression ce5 = SqlUtils.expression(select2);
		assertEquals(
				"SELECT DISTINCT \"JUnit\".\"method_names\" AS \"klases\" FROM \"phones\", \"JUnit\"",
				ce5.getSqlString(SqlBuilderFactory.getBuilder(), false));
	}

	@Test
	public void testComplexExpressionPGSql() {
		SqlBuilderFactory.setDefaultEngine(BeeConst.PGSQL);
		IsExpression ce = SqlUtils.expression(1, "string", 5.0);
		assertEquals("1string5.0",
				ce.getSqlString(SqlBuilderFactory.getBuilder(), false));

		SqlSelect select = new SqlSelect();
		select.addField("users", "username", "vardas");
		select.addFrom("users");

		IsExpression ce2 = SqlUtils.expression(select);
		assertEquals(
				"SELECT \"users\".\"username\" AS \"vardas\" FROM \"users\"",
				ce2.getSqlString(SqlBuilderFactory.getBuilder(), false));

		SqlSelect select2 = new SqlSelect();
		select2.addField("phones", "phone_names", "tel_vardai");
		select2.addFrom("phones");
		IsExpression ce3 = SqlUtils.expression(select2);
		assertEquals(
				"SELECT \"phones\".\"phone_names\" AS \"tel_vardai\" FROM \"phones\"",
				ce3.getSqlString(SqlBuilderFactory.getBuilder(), false));

		select.addFrom(select2, "alias2");

		IsExpression ce4 = SqlUtils.expression(select);
		assertEquals(
				"SELECT \"users\".\"username\" AS \"vardas\" FROM \"users\", (SELECT \"phones\".\"phone_names\" AS \"tel_vardai\" FROM \"phones\") \"alias2\"",
				ce4.getSqlString(SqlBuilderFactory.getBuilder(), false));

		select2 = select2.reset();
		select2.addField("JUnit", "method_names", "klases");
		select2.setDistinctMode(true);
		select2.addFrom("JUnit");
		IsExpression ce5 = SqlUtils.expression(select2);
		assertEquals(
				"SELECT DISTINCT \"JUnit\".\"method_names\" AS \"klases\" FROM \"phones\", \"JUnit\"",
				ce5.getSqlString(SqlBuilderFactory.getBuilder(), false));
	}

	@Test
	public final void testBitAndStringStringObjectGeneric() {
		SqlBuilderFactory.setDefaultEngine(BeeConst.UNKNOWN);
		SqlSelect select = new SqlSelect();
		select.addFields("Table1", "field1", "field2", "field3");
		select.addFrom("Table1");

		IsExpression clause = SqlUtils.bitAnd("Table1", "field1", "val1"); // SqlUtils.and(SqlUtils.equal(SqlUtils.name("field1"),
																			// "Something val"));

		select.addExpr(clause, "expr1");

		assertEquals(
				"SELECT Table1.field1, Table1.field2, Table1.field3, (Table1.field1&val1) AS expr1 FROM Table1",
				select.getQuery());
	}

	@Test
	public final void testBitAndStringStringObjectOracle() {
		SqlBuilderFactory.setDefaultEngine(BeeConst.ORACLE);
		SqlSelect select = new SqlSelect();
		select.addFields("Table1", "field1", "field2", "field3");
		select.addFrom("Table1");
		IsExpression clause = SqlUtils.bitAnd("Table1", "field1", "val1"); // SqlUtils.and(SqlUtils.equal(SqlUtils.name("field1"),
																			// "Something val"));

		select.addExpr(clause, "expr1");

		assertEquals(
				"SELECT \"Table1\".\"field1\", \"Table1\".\"field2\", \"Table1\".\"field3\", BITAND(\"Table1\".\"field1\", val1) AS \"expr1\" FROM \"Table1\"",
				select.getQuery());
	}

	@Test
	public final void testFields() {
		SqlBuilderFactory.setDefaultEngine("Generic");
		SqlBuilder builder = SqlBuilderFactory.getBuilder();

		IsExpression[] expressions = SqlUtils.fields("Table1", "field1",
				"field2");
		String[] expected = { "Table1.field1", "Table1.field2" };

		for (int i = 0; i < expressions.length; i++) {
			assertEquals(expected[i].toString(),
					expressions[i].getSqlString(builder, false));
		}
	}

	@Test
	public final void testSqlIf() {

		SqlBuilderFactory.setDefaultEngine("Generic");
		SqlSelect select = new SqlSelect();
		select.addFields("Table1", "field1");
		select.addFrom("Table1");

		IsCondition cond1 = SqlUtils.equal(SqlUtils.name("value1"),
				"pirmadienis");

		IsExpression expr = SqlUtils.sqlIf(cond1,
				SqlUtils.field("Table2", "field1"),
				SqlUtils.field("Table3", "field1"));
		select.addExpr(expr, "expr1");

		assertEquals(
				"SELECT Table1.field1, CASE WHEN value1='pirmadienis' THEN Table2.field1 ELSE Table3.field1 END AS expr1 FROM Table1",
				select.getQuery());
	}

	@Test
	public final void testTemporaryName() {
		SqlSelect select1 = new SqlSelect();
		select1.addFields("Table1", "field1", "field2");
		select1.addFrom("Table1");

		IsCondition clause1 = SqlUtils.less(SqlUtils.bitAnd("Table2",
				SqlUtils.temporaryName("tempName1"), "val21"), 10E1);
		select1.setWhere(clause1);

		assertEquals(
				"SELECT Table1.field1, Table1.field2 FROM Table1 WHERE (Table2.tempName1&val21)<100",
				select1.getQuery());
	}

	@Test
	public final void testTemporaryNameRandomParams() {
		// ----------------------------------------------------------------------
		String str = SqlUtils.temporaryName("");
		for (int i = 0; i < 100; i++) {
			assertEquals(true,
					str.compareTo("aaa") >= 0 && str.compareTo("zzz") <= 0);
		}
	}

	@Test
	public final void testCast() {
		SqlBuilderFactory.setDefaultEngine(BeeConst.UNKNOWN);
		SqlSelect s = new SqlSelect();
		s.addFields("Table1", "field1");
		s.addFrom("Table1");
		s.addExpr(SqlUtils.cast(SqlUtils.field("Table1", "field2"),
				DataType.DOUBLE, 5, 10), "TB1");

		assertEquals(
				"SELECT Table1.field1, CAST(Table1.field2 AS DOUBLE) AS TB1 FROM Table1",
				s.getQuery());

		s = new SqlSelect();
		s.addFields("Table1", "field1");
		s.addFrom("Table1");
		s.addExpr(SqlUtils.cast(SqlUtils.field("Table1", "field2"),
				DataType.CHAR, 5, 10), "TB1");

		assertEquals(
				"SELECT Table1.field1, CAST(Table1.field2 AS CHAR(5)) AS TB1 FROM Table1",
				s.getQuery());

		s = new SqlSelect();
		s.addFields("Table1", "field1");
		s.addFrom("Table1");
		s.addExpr(SqlUtils.cast(SqlUtils.field("Table1", "field2"),
				DataType.NUMERIC, 5, 10), "TB1");

		assertEquals(
				"SELECT Table1.field1, CAST(Table1.field2 AS NUMERIC(5, 10)) AS TB1 FROM Table1",
				s.getQuery());

		s = new SqlSelect();
		s.addFields("Table1", "field1");
		s.addFrom("Table1");
		s.addExpr(SqlUtils.cast(SqlUtils.field("Table1", "field2"),
				DataType.BOOLEAN, 5, 10), "TB1");

		assertEquals(
				"SELECT Table1.field1, CAST(Table1.field2 AS BIT) AS TB1 FROM Table1",
				s.getQuery());

		s = new SqlSelect();
		s.addFields("Table1", "field1");
		s.addFrom("Table1");
		s.addExpr(SqlUtils.cast(SqlUtils.field("Table1", "field2"),
				DataType.INTEGER, 5, 10), "TB1");

		assertEquals(
				"SELECT Table1.field1, CAST(Table1.field2 AS INTEGER) AS TB1 FROM Table1",
				s.getQuery());

		s = new SqlSelect();
		s.addFields("Table1", "field1");
		s.addFrom("Table1");
		s.addExpr(SqlUtils.cast(SqlUtils.field("Table1", "field2"),
				DataType.DATE, 5, 10), "TB1");

		assertEquals(
				"SELECT Table1.field1, CAST(Table1.field2 AS INTEGER) AS TB1 FROM Table1",
				s.getQuery());

		s = new SqlSelect();
		s.addFields("Table1", "field1");
		s.addFrom("Table1");
		s.addExpr(SqlUtils.cast(SqlUtils.field("Table1", "field2"),
				DataType.LONG, 5, 10), "TB1");

		assertEquals(
				"SELECT Table1.field1, CAST(Table1.field2 AS BIGINT) AS TB1 FROM Table1",
				s.getQuery());

		s = new SqlSelect();
		s.addFields("Table1", "field1");
		s.addFrom("Table1");
		s.addExpr(SqlUtils.cast(SqlUtils.field("Table1", "field2"),
				DataType.DATETIME, 5, 10), "TB1");

		assertEquals(
				"SELECT Table1.field1, CAST(Table1.field2 AS BIGINT) AS TB1 FROM Table1",
				s.getQuery());

		s = new SqlSelect();
		s.addFields("Table1", "field1");
		s.addFrom("Table1");
		s.addExpr(SqlUtils.cast(SqlUtils.field("Table1", "field2"),
				DataType.STRING, -5, 10), "TB1");

		assertEquals(
				"SELECT Table1.field1, CAST(Table1.field2 AS VARCHAR(-5)) AS TB1 FROM Table1",
				s.getQuery());

		SqlBuilderFactory.setDefaultEngine(BeeConst.MYSQL);
		s = new SqlSelect();
		s.addFields("Table1", "field1");
		s.addFrom("Table1");
		s.addExpr(SqlUtils.cast(SqlUtils.field("Table1", "field2"),
				DataType.DOUBLE, 5, 10), "TB1");

		assertEquals(
				"SELECT `Table1`.`field1`, CAST(`Table1`.`field2` AS DECIMAL(65, 30)) AS `TB1` FROM `Table1`",
				s.getQuery());

		s = new SqlSelect();
		s.addFields("Table1", "field1");
		s.addFrom("Table1");
		s.addExpr(SqlUtils.cast(SqlUtils.field("Table1", "field2"),
				DataType.CHAR, 5, 10), "TB1");

		assertEquals(
				"SELECT `Table1`.`field1`, CAST(`Table1`.`field2` AS CHAR(5)) AS `TB1` FROM `Table1`",
				s.getQuery());

		s = new SqlSelect();
		s.addFields("Table1", "field1");
		s.addFrom("Table1");
		s.addExpr(SqlUtils.cast(SqlUtils.field("Table1", "field2"),
				DataType.NUMERIC, 5, 10), "TB1");

		assertEquals(
				"SELECT `Table1`.`field1`, CAST(`Table1`.`field2` AS DECIMAL(5, 10)) AS `TB1` FROM `Table1`",
				s.getQuery());

		s = new SqlSelect();
		s.addFields("Table1", "field1");
		s.addFrom("Table1");
		s.addExpr(SqlUtils.cast(SqlUtils.field("Table1", "field2"),
				DataType.BOOLEAN, 5, 10), "TB1");

		assertEquals(
				"SELECT `Table1`.`field1`, CAST(`Table1`.`field2` AS DECIMAL(1)) AS `TB1` FROM `Table1`",
				s.getQuery());

		s = new SqlSelect();
		s.addFields("Table1", "field1");
		s.addFrom("Table1");
		s.addExpr(SqlUtils.cast(SqlUtils.field("Table1", "field2"),
				DataType.INTEGER, 5, 10), "TB1");

		assertEquals(
				"SELECT `Table1`.`field1`, CAST(`Table1`.`field2` AS DECIMAL(10)) AS `TB1` FROM `Table1`",
				s.getQuery());

		s = new SqlSelect();
		s.addFields("Table1", "field1");
		s.addFrom("Table1");
		s.addExpr(SqlUtils.cast(SqlUtils.field("Table1", "field2"),
				DataType.DATE, 5, 10), "TB1");

		assertEquals(
				"SELECT `Table1`.`field1`, CAST(`Table1`.`field2` AS DECIMAL(10)) AS `TB1` FROM `Table1`",
				s.getQuery());

		s = new SqlSelect();
		s.addFields("Table1", "field1");
		s.addFrom("Table1");
		s.addExpr(SqlUtils.cast(SqlUtils.field("Table1", "field2"),
				DataType.LONG, 5, 10), "TB1");

		assertEquals(
				"SELECT `Table1`.`field1`, CAST(`Table1`.`field2` AS DECIMAL(19)) AS `TB1` FROM `Table1`",
				s.getQuery());

		s = new SqlSelect();
		s.addFields("Table1", "field1");
		s.addFrom("Table1");
		s.addExpr(SqlUtils.cast(SqlUtils.field("Table1", "field2"),
				DataType.DATETIME, 5, 10), "TB1");

		assertEquals(
				"SELECT `Table1`.`field1`, CAST(`Table1`.`field2` AS DECIMAL(19)) AS `TB1` FROM `Table1`",
				s.getQuery());

		s = new SqlSelect();
		s.addFields("Table1", "field1");
		s.addFrom("Table1");
		s.addExpr(SqlUtils.cast(SqlUtils.field("Table1", "field2"),
				DataType.STRING, -5, 10), "TB1");

		assertEquals(
				"SELECT `Table1`.`field1`, CAST(`Table1`.`field2` AS CHAR(-5)) AS `TB1` FROM `Table1`",
				s.getQuery());

		SqlBuilderFactory.setDefaultEngine(BeeConst.MSSQL);
		s = new SqlSelect();
		s.addFields("Table1", "field1");
		s.addFrom("Table1");
		s.addExpr(SqlUtils.cast(SqlUtils.field("Table1", "field2"),
				DataType.DOUBLE, 5, 10), "TB1");

		assertEquals(
				"SELECT [Table1].[field1], CAST([Table1].[field2] AS FLOAT) AS [TB1] FROM [Table1]",
				s.getQuery());

		s = new SqlSelect();
		s.addFields("Table1", "field1");
		s.addFrom("Table1");
		s.addExpr(SqlUtils.cast(SqlUtils.field("Table1", "field2"),
				DataType.CHAR, 5, 10), "TB1");

		assertEquals(
				"SELECT [Table1].[field1], CAST([Table1].[field2] AS CHAR(5)) AS [TB1] FROM [Table1]",
				s.getQuery());

		s = new SqlSelect();
		s.addFields("Table1", "field1");
		s.addFrom("Table1");
		s.addExpr(SqlUtils.cast(SqlUtils.field("Table1", "field2"),
				DataType.NUMERIC, 5, 10), "TB1");

		assertEquals(
				"SELECT [Table1].[field1], CAST([Table1].[field2] AS NUMERIC(5, 10)) AS [TB1] FROM [Table1]",
				s.getQuery());

		s = new SqlSelect();
		s.addFields("Table1", "field1");
		s.addFrom("Table1");
		s.addExpr(SqlUtils.cast(SqlUtils.field("Table1", "field2"),
				DataType.BOOLEAN, 5, 10), "TB1");

		assertEquals(
				"SELECT [Table1].[field1], CAST([Table1].[field2] AS BIT) AS [TB1] FROM [Table1]",
				s.getQuery());

		s = new SqlSelect();
		s.addFields("Table1", "field1");
		s.addFrom("Table1");
		s.addExpr(SqlUtils.cast(SqlUtils.field("Table1", "field2"),
				DataType.INTEGER, 5, 10), "TB1");

		assertEquals(
				"SELECT [Table1].[field1], CAST([Table1].[field2] AS INTEGER) AS [TB1] FROM [Table1]",
				s.getQuery());

		s = new SqlSelect();
		s.addFields("Table1", "field1");
		s.addFrom("Table1");
		s.addExpr(SqlUtils.cast(SqlUtils.field("Table1", "field2"),
				DataType.DATE, 5, 10), "TB1");

		assertEquals(
				"SELECT [Table1].[field1], CAST([Table1].[field2] AS INTEGER) AS [TB1] FROM [Table1]",
				s.getQuery());

		s = new SqlSelect();
		s.addFields("Table1", "field1");
		s.addFrom("Table1");
		s.addExpr(SqlUtils.cast(SqlUtils.field("Table1", "field2"),
				DataType.LONG, 5, 10), "TB1");

		assertEquals(
				"SELECT [Table1].[field1], CAST([Table1].[field2] AS BIGINT) AS [TB1] FROM [Table1]",
				s.getQuery());

		s = new SqlSelect();
		s.addFields("Table1", "field1");
		s.addFrom("Table1");
		s.addExpr(SqlUtils.cast(SqlUtils.field("Table1", "field2"),
				DataType.DATETIME, 5, 10), "TB1");

		assertEquals(
				"SELECT [Table1].[field1], CAST([Table1].[field2] AS BIGINT) AS [TB1] FROM [Table1]",
				s.getQuery());

		s = new SqlSelect();
		s.addFields("Table1", "field1");
		s.addFrom("Table1");
		s.addExpr(SqlUtils.cast(SqlUtils.field("Table1", "field2"),
				DataType.STRING, -5, 10), "TB1"); // praleidziam

		assertEquals(
				"SELECT [Table1].[field1], CAST([Table1].[field2] AS VARCHAR(-5)) AS [TB1] FROM [Table1]",
				s.getQuery());

		SqlBuilderFactory.setDefaultEngine(BeeConst.ORACLE);
		s = new SqlSelect();
		s.addFields("Table1", "field1");
		s.addFrom("Table1");
		s.addExpr(SqlUtils.cast(SqlUtils.field("Table1", "field2"),
				DataType.DOUBLE, 5, 10), "TB1");

		assertEquals(
				"SELECT \"Table1\".\"field1\", CAST(\"Table1\".\"field2\" AS BINARY_DOUBLE) AS \"TB1\" FROM \"Table1\"",
				s.getQuery());

		s = new SqlSelect();
		s.addFields("Table1", "field1");
		s.addFrom("Table1");
		s.addExpr(SqlUtils.cast(SqlUtils.field("Table1", "field2"),
				DataType.CHAR, 5, 10), "TB1");

		assertEquals(
				"SELECT \"Table1\".\"field1\", CAST(\"Table1\".\"field2\" AS CHAR(5)) AS \"TB1\" FROM \"Table1\"",
				s.getQuery());

		s = new SqlSelect();
		s.addFields("Table1", "field1");
		s.addFrom("Table1");
		s.addExpr(SqlUtils.cast(SqlUtils.field("Table1", "field2"),
				DataType.NUMERIC, 5, 10), "TB1");

		assertEquals(
				"SELECT \"Table1\".\"field1\", CAST(\"Table1\".\"field2\" AS NUMERIC(5, 10)) AS \"TB1\" FROM \"Table1\"",
				s.getQuery());

		s = new SqlSelect();
		s.addFields("Table1", "field1");
		s.addFrom("Table1");
		s.addExpr(SqlUtils.cast(SqlUtils.field("Table1", "field2"),
				DataType.BOOLEAN, 5, 10), "TB1");

		assertEquals(
				"SELECT \"Table1\".\"field1\", CAST(\"Table1\".\"field2\" AS NUMERIC(1)) AS \"TB1\" FROM \"Table1\"",
				s.getQuery());

		s = new SqlSelect();
		s.addFields("Table1", "field1");
		s.addFrom("Table1");
		s.addExpr(SqlUtils.cast(SqlUtils.field("Table1", "field2"),
				DataType.INTEGER, 5, 10), "TB1");

		assertEquals(
				"SELECT \"Table1\".\"field1\", CAST(\"Table1\".\"field2\" AS NUMERIC(10)) AS \"TB1\" FROM \"Table1\"",
				s.getQuery());

		s = new SqlSelect();
		s.addFields("Table1", "field1");
		s.addFrom("Table1");
		s.addExpr(SqlUtils.cast(SqlUtils.field("Table1", "field2"),
				DataType.DATE, 5, 10), "TB1");

		assertEquals(
				"SELECT \"Table1\".\"field1\", CAST(\"Table1\".\"field2\" AS NUMERIC(10)) AS \"TB1\" FROM \"Table1\"",
				s.getQuery());

		s = new SqlSelect();
		s.addFields("Table1", "field1");
		s.addFrom("Table1");
		s.addExpr(SqlUtils.cast(SqlUtils.field("Table1", "field2"),
				DataType.LONG, 5, 10), "TB1");

		assertEquals(
				"SELECT \"Table1\".\"field1\", CAST(\"Table1\".\"field2\" AS NUMERIC(19)) AS \"TB1\" FROM \"Table1\"",
				s.getQuery());

		s = new SqlSelect();
		s.addFields("Table1", "field1");
		s.addFrom("Table1");
		s.addExpr(SqlUtils.cast(SqlUtils.field("Table1", "field2"),
				DataType.DATETIME, 5, 10), "TB1");

		assertEquals(
				"SELECT \"Table1\".\"field1\", CAST(\"Table1\".\"field2\" AS NUMERIC(19)) AS \"TB1\" FROM \"Table1\"",
				s.getQuery());

		s = new SqlSelect();
		s.addFields("Table1", "field1");
		s.addFrom("Table1");
		s.addExpr(SqlUtils.cast(SqlUtils.field("Table1", "field2"),
				DataType.STRING, -5, 10), "TB1");

		assertEquals(
				"SELECT \"Table1\".\"field1\", CAST(\"Table1\".\"field2\" AS NVARCHAR2(-5)) AS \"TB1\" FROM \"Table1\"",
				s.getQuery());

		/* PODTGRE SQL */

		SqlBuilderFactory.setDefaultEngine(BeeConst.PGSQL);
		s = new SqlSelect();
		s.addFields("Table1", "field1");
		s.addFrom("Table1");
		s.addExpr(SqlUtils.cast(SqlUtils.field("Table1", "field2"),
				DataType.DOUBLE, 5, 10), "TB1");

		assertEquals(
				"SELECT \"Table1\".\"field1\", CAST(\"Table1\".\"field2\" AS DOUBLE PRECISION) AS \"TB1\" FROM \"Table1\"",
				s.getQuery());

		s = new SqlSelect();
		s.addFields("Table1", "field1");
		s.addFrom("Table1");
		s.addExpr(SqlUtils.cast(SqlUtils.field("Table1", "field2"),
				DataType.CHAR, 5, 10), "TB1");

		assertEquals(
				"SELECT \"Table1\".\"field1\", CAST(\"Table1\".\"field2\" AS CHAR(5)) AS \"TB1\" FROM \"Table1\"",
				s.getQuery());

		s = new SqlSelect();
		s.addFields("Table1", "field1");
		s.addFrom("Table1");
		s.addExpr(SqlUtils.cast(SqlUtils.field("Table1", "field2"),
				DataType.NUMERIC, 5, 10), "TB1");

		assertEquals(
				"SELECT \"Table1\".\"field1\", CAST(\"Table1\".\"field2\" AS NUMERIC(5, 10)) AS \"TB1\" FROM \"Table1\"",
				s.getQuery());

		s = new SqlSelect();
		s.addFields("Table1", "field1");
		s.addFrom("Table1");
		s.addExpr(SqlUtils.cast(SqlUtils.field("Table1", "field2"),
				DataType.BOOLEAN, 5, 10), "TB1");

		assertEquals(
				"SELECT \"Table1\".\"field1\", CAST(\"Table1\".\"field2\" AS NUMERIC(1)) AS \"TB1\" FROM \"Table1\"",
				s.getQuery());

		s = new SqlSelect();
		s.addFields("Table1", "field1");
		s.addFrom("Table1");
		s.addExpr(SqlUtils.cast(SqlUtils.field("Table1", "field2"),
				DataType.INTEGER, 5, 10), "TB1");

		assertEquals(
				"SELECT \"Table1\".\"field1\", CAST(\"Table1\".\"field2\" AS INTEGER) AS \"TB1\" FROM \"Table1\"",
				s.getQuery());

		s = new SqlSelect();
		s.addFields("Table1", "field1");
		s.addFrom("Table1");
		s.addExpr(SqlUtils.cast(SqlUtils.field("Table1", "field2"),
				DataType.DATE, 5, 10), "TB1");

		assertEquals(
				"SELECT \"Table1\".\"field1\", CAST(\"Table1\".\"field2\" AS INTEGER) AS \"TB1\" FROM \"Table1\"",
				s.getQuery());

		s = new SqlSelect();
		s.addFields("Table1", "field1");
		s.addFrom("Table1");
		s.addExpr(SqlUtils.cast(SqlUtils.field("Table1", "field2"),
				DataType.LONG, 5, 10), "TB1");

		assertEquals(
				"SELECT \"Table1\".\"field1\", CAST(\"Table1\".\"field2\" AS BIGINT) AS \"TB1\" FROM \"Table1\"",
				s.getQuery());

		s = new SqlSelect();
		s.addFields("Table1", "field1");
		s.addFrom("Table1");
		s.addExpr(SqlUtils.cast(SqlUtils.field("Table1", "field2"),
				DataType.DATETIME, 5, 10), "TB1");

		assertEquals(
				"SELECT \"Table1\".\"field1\", CAST(\"Table1\".\"field2\" AS BIGINT) AS \"TB1\" FROM \"Table1\"",
				s.getQuery());

		s = new SqlSelect();
		s.addFields("Table1", "field1");
		s.addFrom("Table1");
		s.addExpr(SqlUtils.cast(SqlUtils.field("Table1", "field2"),
				DataType.STRING, -5, 10), "TB1");

		assertEquals(
				"SELECT \"Table1\".\"field1\", CAST(\"Table1\".\"field2\" AS VARCHAR(-5)) AS \"TB1\" FROM \"Table1\"",
				s.getQuery());

	}

	@Test
	public final void sqlCaseIsExpressionObjectArr() {
		SqlBuilderFactory.setDefaultEngine(BeeConst.UNKNOWN);
		SqlSelect sql;

		sql = new SqlSelect();
		sql.addFields("Table1", "field11", "field12");
		sql.addFrom("Table1");

		sql.addExpr(SqlUtils.sqlCase(SqlUtils.name("field21"), "pair1",
				"pair2", "pair3"), "Name1");

		assertEquals(
				"SELECT Table1.field11, Table1.field12, CASE field21 WHEN 'pair1' THEN pair2 ELSE pair3 END AS Name1 FROM Table1",
				sql.getQuery());

		sql = new SqlSelect();
		sql.addFields("Table1", "field11", "field12");
		sql.addFrom("Table1");

		sql.addExpr(
				SqlUtils.sqlCase(SqlUtils.name("field21"), "pair1",
						SqlUtils.name("field22"), "pair3"), "Name1");
		assertEquals(
				"SELECT Table1.field11, Table1.field12, CASE field21 WHEN 'pair1' THEN field22 ELSE pair3 END AS Name1 FROM Table1",
				sql.getQuery());

		sql = new SqlSelect();
		sql.addFields("Table1", "field11", "field12");
		sql.addFrom("Table1");

		sql.addExpr(
				SqlUtils.sqlCase(SqlUtils.name("field21"), "pair1",
						SqlUtils.constant("field22"), "pair3"), "Name1");
		assertEquals(
				"SELECT Table1.field11, Table1.field12, CASE field21 WHEN 'pair1' THEN 'field22' ELSE pair3 END AS Name1 FROM Table1",
				sql.getQuery());

		sql = new SqlSelect();
		sql.addFields("Table1", "field11", "field12");
		sql.addFrom("Table1");

		sql.addExpr(SqlUtils.sqlCase(SqlUtils.name("field21"), "pair1",
				"pair2", "pair3", "pair4", "pair5"), "Name1");

		assertEquals(
				"SELECT Table1.field11, Table1.field12, CASE field21 WHEN 'pair1' THEN pair2 WHEN 'pair3' THEN pair4 ELSE pair5 END AS Name1 FROM Table1",
				sql.getQuery());

		sql = new SqlSelect();
		sql.addFields("Table1", "field11", "field12");
		sql.addFrom("Table1");

		sql.addExpr(SqlUtils.sqlCase(SqlUtils.name("field21"), "pair1",
				"pair2", "pair3", "pair4", null), "Name1");

		assertEquals(
				"SELECT Table1.field11, Table1.field12, CASE field21 WHEN 'pair1' THEN pair2 WHEN 'pair3' THEN pair4 ELSE null END AS Name1 FROM Table1",
				sql.getQuery());

		try {
			sql = new SqlSelect();
			sql.addFields("Table1", "field11", "field12");
			sql.addFrom("Table1");

			sql.addExpr(SqlUtils.sqlCase(SqlUtils.name("field21"), "pair1",
					"pair2", "pair3", "pair4", ""), "Name1");

			assertEquals(
					"SELECT Table1.field11, Table1.field12, CASE field21 WHEN 'pair1' THEN pair2 WHEN 'pair3' THEN pair4 ELSE  END AS Name1 FROM Table1",
					sql.getQuery());
		} catch (BeeRuntimeException e) {
			assertTrue(false);
		} catch (Exception e) {
			e.printStackTrace();
			fail("java.lang.Exception, need BeeRuntimeException "
					+ e.getMessage());
		}

		try {
			sql = new SqlSelect();
			sql.addFields("Table1", "field11", "field12");
			sql.addFrom("Table1");

			sql.addExpr(SqlUtils.sqlCase(SqlUtils.name("field21"), "pair1",
					"pair2", "pair3", "pair4"), "Name1");

			fail("Exception not work: " + sql.getQuery());

		} catch (BeeRuntimeException e) {
			assertTrue(true);
		} catch (Exception e) {
			e.printStackTrace();
			fail("java.lang.Exception, need BeeRuntimeException "
					+ e.getMessage());
		}

		try {
			sql = new SqlSelect();
			sql.addFields("Table1", "field11", "field12");
			sql.addFrom("Table1");

			sql.addExpr(SqlUtils.sqlCase(SqlUtils.name("field21"), "pair1",
					"pair2", SqlUtils.constant("field22"), "pair4", "pair5"),
					"Name1");

			fail("Exception not works: " + sql.getQuery());
		} catch (BeeRuntimeException e) {
			assertTrue(true);
		} catch (Exception e) {
			e.printStackTrace();
			fail("java.lang.Exception need BeeRumtimeException: "
					+ e.getMessage());
		}

		try {
			sql = new SqlSelect();
			sql.addFields("Table1", "field11", "field12");
			sql.addFrom("Table1");

			sql.addExpr(SqlUtils.sqlCase(SqlUtils.name("field21"), "pair1",
					"pair2"), "Name1");

			fail("Exception not work: " + sql.getQuery());

		} catch (BeeRuntimeException e) {
			assertTrue(true);
		} catch (Exception e) {
			e.printStackTrace();
			fail("java.lang.Exception, need BeeRuntimeException "
					+ e.getMessage());
		}

		try {
			sql = new SqlSelect();
			sql.addFields("Table1", "field11", "field12");
			sql.addFrom("Table1");

			sql.addExpr(SqlUtils.sqlCase(SqlUtils.name("field21"), "pair1"),
					"Name1");

			fail("Exception not work: " + sql.getQuery());

		} catch (BeeRuntimeException e) {
			assertTrue(true);
		} catch (Exception e) {
			e.printStackTrace();
			fail("java.lang.Exception, need BeeRuntimeException "
					+ e.getMessage());
		}

		try {
			sql = new SqlSelect();
			sql.addFields("Table1", "field11", "field12");
			sql.addFrom("Table1");

			sql.addExpr(SqlUtils.sqlCase(null, "pair1", "pair2", "pair3",
					"pair4", ""), "Name1");

			assertEquals("SELECT Table1.field11, Table1.field12, CASE null WHEN 'pair1' THEN pair2 WHEN 'pair3' THEN pair4 ELSE  END AS Name1 FROM Table1", sql.getQuery());

		} catch (BeeRuntimeException e) {
			assertTrue(true);
		} catch (Exception e) {
			e.printStackTrace();
			fail("java.lang.Exception, need BeeRuntimeException "
					+ e.getMessage());
		}

	}
}
