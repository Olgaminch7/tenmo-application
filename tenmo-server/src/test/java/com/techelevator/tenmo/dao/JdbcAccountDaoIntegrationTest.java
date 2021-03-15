package com.techelevator.tenmo.dao;


import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import com.techelevator.tenmo.model.Account;


public class JdbcAccountDaoIntegrationTest extends DAOIntegrationTest {
	
	private AccountDao accountDao;
	private JdbcTemplate jdbcTemplate;
	private int testUserId;
	private final static String TEST_USER_NAME = "test";
	
	@Before
	public void setup() {
		jdbcTemplate = new JdbcTemplate(getDataSource());
		accountDao = new JdbcAccountDao(jdbcTemplate);
		testUserId = createTestUser();
	}
	
	@Test
	public void get_account_info_by_user_name() {
		Account testAccount = createTestAccount(testUserId);
		Account accountFromDatabase = accountDao.getAccountInfoByUsername(TEST_USER_NAME);
		assertNotNull(testAccount);
		assertEquals(testAccount, accountFromDatabase);
	}
	
	private Account createTestAccount(int userId) {
		Account account = new Account();
		String sql = "INSERT INTO accounts (account_id, user_id, balance) VALUES (DEFAULT, ?, 1000.0) RETURNING account_id";
		SqlRowSet row = jdbcTemplate.queryForRowSet(sql, userId);
		row.next();
		if (String.valueOf(row.getLong("account_id")) != null) {
			account.setId(row.getLong("account_id"));
		}
		account.setAccountHolderName(TEST_USER_NAME);
		account.setBalance(1000.0);
		return account;
	}
	
	private int createTestUser() {
		return getId("INSERT INTO users (user_id, username, password_hash) VALUES (DEFAULT, '" + TEST_USER_NAME + "', 'test') RETURNING user_id");

	}
	
	private int getId(String sql) {
		Integer id = jdbcTemplate.queryForObject(sql, Integer.class);
		return id;
	}
	
	

}
