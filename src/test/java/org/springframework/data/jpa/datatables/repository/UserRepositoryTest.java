package org.springframework.data.jpa.datatables.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.datatables.Config;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.data.jpa.datatables.model.User;
import org.springframework.data.jpa.datatables.model.User.UserRole;
import org.springframework.data.jpa.datatables.model.User.UserStatus;
import org.springframework.data.jpa.datatables.parameter.ColumnParameter;
import org.springframework.data.jpa.datatables.parameter.OrderParameter;
import org.springframework.data.jpa.datatables.parameter.SearchParameter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Config.class)
public class UserRepositoryTest {

	@Autowired
	private UserRepository userRepository;

	/**
	 * Insert sample data at the beginning of all tests
	 */
	@Before
	public void setUp() {
		if (userRepository.count() > 0)
			return;
		for (int i = 0; i < 24; i++) {
			User user = new User();
			user.setUsername("john" + i);
			user.setRole(UserRole.values()[i % 3]);
			user.setStatus(UserStatus.values()[i % 2]);
			userRepository.save(user);
		}
	}

	@Test
	public void testSort() {
		DataTablesInput input = getBasicInput();

		// sorting by id asc
		DataTablesOutput<User> output = userRepository.findAll(input);
		assertNotNull(output);
		assertEquals(1, (int) output.getDraw());
		assertNull(output.getError());
		assertEquals(24, (long) output.getRecordsFiltered());
		assertEquals(24, (long) output.getRecordsTotal());

		List<User> users = output.getData();
		assertNotNull(users);
		assertEquals(10, users.size());

		User firstUser = users.get(0);
		User lastUser = users.get(9);
		assertEquals(1, (int) firstUser.getId());
		assertEquals(10, (int) lastUser.getId());

		// sorting by id desc
		input.setDraw(2);
		input.getOrder().get(0).setDir("desc");
		output = userRepository.findAll(input);
		assertNotNull(output);
		assertEquals(2, (int) output.getDraw());
		assertNull(output.getError());
		assertEquals(24, (long) output.getRecordsFiltered());
		assertEquals(24, (long) output.getRecordsTotal());

		users = output.getData();
		assertNotNull(users);
		assertEquals(10, users.size());

		firstUser = users.get(0);
		lastUser = users.get(9);
		assertEquals(24, (int) firstUser.getId());
		assertEquals(15, (int) lastUser.getId());
	}

	@Test
	public void testFilterGlobal() {
		DataTablesInput input = getBasicInput();

		input.getSearch().setValue("hn1");

		DataTablesOutput<User> output = userRepository.findAll(input);
		assertNotNull(output);
		assertEquals(1, (int) output.getDraw());
		assertNull(output.getError());
		assertEquals(11, (long) output.getRecordsFiltered());
		assertEquals(24, (long) output.getRecordsTotal());

		List<User> users = output.getData();
		assertNotNull(users);
		assertEquals(10, users.size());

		User firstUser = users.get(0);
		User lastUser = users.get(9);
		assertEquals("john1", firstUser.getUsername());
		assertEquals("john18", lastUser.getUsername());
	}

	@Test
	public void testFilterOnSeveralColumns() {
		DataTablesInput input = getBasicInput();

		input.getColumns().get(2).getSearch().setValue("ADMIN");
		input.getColumns().get(3).getSearch().setValue("ACTIVE");

		DataTablesOutput<User> output = userRepository.findAll(input);
		assertNotNull(output);
		assertEquals(1, (int) output.getDraw());
		assertNull(output.getError());
		assertEquals(4, (long) output.getRecordsFiltered());
		assertEquals(24, (long) output.getRecordsTotal());

		List<User> users = output.getData();
		assertNotNull(users);
		assertEquals(4, users.size());

		User firstUser = users.get(0);
		User lastUser = users.get(3);
		assertEquals("john0", firstUser.getUsername());
		assertEquals("ADMIN", firstUser.getRole().toString());
		assertEquals("ACTIVE", firstUser.getStatus().toString());
		assertEquals("john18", lastUser.getUsername());
		assertEquals("ADMIN", lastUser.getRole().toString());
		assertEquals("ACTIVE", lastUser.getStatus().toString());
	}

	@Test
	public void testMultiFilterOnSameColumn() {
		DataTablesInput input = getBasicInput();

		input.getColumns().get(2).getSearch().setValue("ADMIN+USER");

		DataTablesOutput<User> output = userRepository.findAll(input);
		assertNotNull(output);
		assertEquals(1, (int) output.getDraw());
		assertNull(output.getError());
		assertEquals(16, (long) output.getRecordsFiltered());
		assertEquals(24, (long) output.getRecordsTotal());

		List<User> users = output.getData();
		assertNotNull(users);
		assertEquals(10, users.size());

		User firstUser = users.get(0);
		User lastUser = users.get(9);
		assertEquals("john0", firstUser.getUsername());
		assertEquals("ADMIN", firstUser.getRole().toString());
		assertEquals("john14", lastUser.getUsername());
		assertEquals("USER", lastUser.getRole().toString());
	}

	/**
	 * 
	 * @return basic input parameters
	 */
	private static DataTablesInput getBasicInput() {
		DataTablesInput input = new DataTablesInput();
		input.setDraw(1);
		input.setStart(0);
		input.setLength(10);
		input.setSearch(new SearchParameter("", false));
		input.setOrder(new ArrayList<OrderParameter>());
		input.getOrder().add(new OrderParameter(0, "asc"));

		input.setColumns(new ArrayList<ColumnParameter>());
		input.getColumns().add(
				new ColumnParameter("id", "", true, true, new SearchParameter(
						"", false)));
		input.getColumns().add(
				new ColumnParameter("username", "", true, true,
						new SearchParameter("", false)));
		input.getColumns().add(
				new ColumnParameter("role", "", true, true,
						new SearchParameter("", false)));
		input.getColumns().add(
				new ColumnParameter("status", "", true, true,
						new SearchParameter("", false)));

		return input;
	}
}