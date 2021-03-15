package com.techelevator.tenmo.dao;

import static org.junit.Assert.assertEquals;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.junit.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.Transaction;

public class JdbcTransactionDaoIntegrationTest extends DAOIntegrationTest{

	private JdbcTemplate jdbcTemplate;
	private int testUserId;
	private int testUserId2;
	private int accountFrom;
	private int accountTo;
	private static final String TEST_NAME_ONE = "testNameOne";
	private static final String TEST_NAME_TWO = "testNameTwo";
	private TransactionDAO transactionDao;
	private AccountDao accountDao;
	
	@Before
	public void setup() {
		jdbcTemplate = new JdbcTemplate(getDataSource());
		transactionDao = new JdbcTransactionDao(jdbcTemplate);
		accountDao = new JdbcAccountDao(jdbcTemplate);	//	testUserId3 = createTestUser("testName");
		testUserId = createTestUser(TEST_NAME_ONE);
		testUserId2 = createTestUser(TEST_NAME_TWO);
		accountFrom = insertAccountsUser(testUserId);
		accountTo = insertAccountsUser(testUserId2);

	}
	
	// addPendingTransfer, addTransfer, updateTransaction
	
	@Test
	public void test_add_transfer() {
		Transaction transaction = createTestTransaction(accountFrom, accountTo, 1, 1);
		Transaction testTransaction = createTestTransaction(accountFrom, accountTo, 2, 2);
		transactionDao.addTransfer(transaction, TEST_NAME_ONE);
		transactionDao.addTransfer(testTransaction, TEST_NAME_TWO);
		List<Transaction> transactionFromDatabase = transactionDao.getApprovedTransactionsByAccountId(TEST_NAME_ONE, accountTo);
		Assert.assertNotNull(transaction);
		Assert.assertEquals(2, transactionFromDatabase.size());
	}
	
	@Test
	public void approved_transaction_deposits_money_to_account_to() {
		Account account = null;
		Transaction testTransaction = createTestTransaction(accountFrom, accountTo, 2, 2);
		transactionDao.addTransfer(testTransaction, TEST_NAME_TWO);
		List<Transaction> transactionFromDatabase = transactionDao.getApprovedTransactionsByAccountId(TEST_NAME_TWO, accountTo);
		for (Transaction transaction : transactionFromDatabase) {
			account = accountDao.getAccountInfoByUsername(transaction.getReceiverName());
		}
		double actualBalance = account.getBalance();
		assertEquals(1999.00, actualBalance, .009);
	}
	
	@Test
	public void approved_transaction_withdrawals_money_from_account_from() {
		Account account = null;
		Transaction testTransaction = createTestTransaction(accountFrom, accountTo, 2, 2);
		transactionDao.addTransfer(testTransaction, TEST_NAME_ONE);
		List<Transaction> transactionFromDatabase = transactionDao.getApprovedTransactionsByAccountId(TEST_NAME_ONE, accountFrom);
		for (Transaction transaction : transactionFromDatabase) {
			account = accountDao.getAccountInfoByUsername(transaction.getSenderName());
		}
		double actualBalance = account.getBalance();
		assertEquals(1.00, actualBalance, .009);
	}
	
	
	@Test
	public void test_update_transaction() {
		Transaction actualTransaction = null;
		Transaction testTransaction = createTestTransaction(accountFrom, accountTo, 1, 1);
		testTransaction.setStatusId(2);
		transactionDao.addTransfer(testTransaction, TEST_NAME_ONE);
		transactionDao.updateTransactionIfApproved(testTransaction, TEST_NAME_ONE);
		List<Transaction> transactionFromDataBase = transactionDao.getApprovedTransactionsByAccountId(TEST_NAME_ONE, accountFrom);
		for (Transaction transaction : transactionFromDataBase) {
			actualTransaction = transaction;
		}
		assertEquals(testTransaction, actualTransaction);
	}
	
	//getTransaction
	
	@Test
	public void test_get_all_transactions() {
		truncateTransfersTable();
	    createTestTransaction(accountFrom, accountTo, 2, 2);
		List<Transaction> newList = new ArrayList<Transaction>();
		String sql = "SELECT transfer_id, account_from, username as sender_name, account_to, transfers.transfer_status_id as status_id, " +
				"transfers.transfer_type_id as type_id, amount " +
				"FROM transfers " + 
				"JOIN transfer_statuses ON transfer_statuses.transfer_status_id = transfers.transfer_status_id " + 
				"JOIN transfer_types ON transfer_types.transfer_type_id = transfers.transfer_type_id " + 
				"JOIN accounts ON accounts.account_id = transfers.account_from " + 
				"JOIN users ON users.user_id = accounts.user_id";
		SqlRowSet row = jdbcTemplate.queryForRowSet(sql);
		while(row.next()) {
			newList.add(mapRowToTransaction(row));
		}
		Assert.assertEquals(1, newList.size());
	}
	
	@Test
	public void test_get_pending_transactions() {
		truncateTransfersTable();
		  createTestTransaction(accountFrom, accountTo, 1, 2);
		List<Transaction> newList = new ArrayList<Transaction>();
		String sql = "SELECT transfer_id, account_from, username as sender_name, account_to, transfers.transfer_status_id as status_id, transfers.transfer_type_id as type_id, amount " + 
				"FROM transfers JOIN transfer_statuses ON transfer_statuses.transfer_status_id = transfers.transfer_status_id " +
				"JOIN transfer_types ON transfer_types.transfer_type_id = transfers.transfer_type_id JOIN accounts ON accounts.account_id = transfers.account_from " +
				"JOIN users ON users.user_id = accounts.user_id " +
				"WHERE transfers.transfer_status_id = 1 AND (account_from = ? OR account_to = ?)";
		SqlRowSet rows = jdbcTemplate.queryForRowSet(sql, accountTo, accountTo);
		while(rows.next()) {
			newList.add(mapRowToTransaction(rows));
		}
		Assert.assertEquals(1, newList.size());
	}
	
	
	private int createTestUser(String testUserName) {
		return getId("INSERT INTO users (user_id, username, password_hash) VALUES (DEFAULT, '" + testUserName + "', 'test') RETURNING user_id");

	}
	
	private int getId(String sql) {
		Integer id = jdbcTemplate.queryForObject(sql, Integer.class);
		return id;
	}
	
	private int insertAccountsUser(int testUserId) {
		String sql = "INSERT INTO accounts (account_id, user_id, balance) VALUES (DEFAULT, ?, 1000.0) RETURNING account_id";
		Integer accountId = jdbcTemplate.queryForObject(sql, Integer.class, testUserId);
		return accountId;
	}
	
	private Transaction createTestTransaction(int accountFrom, int accountTo, int statusId, int typeId) {
		Transaction transaction = new Transaction();
		String sql = "INSERT INTO transfers (transfer_id, transfer_type_id, transfer_status_id, account_from, account_to, amount) "
				+ "VALUES (DEFAULT, ?, ?, ?, ?, 999.00) RETURNING transfer_id";
		SqlRowSet row = jdbcTemplate.queryForRowSet(sql, typeId, statusId, accountFrom, accountTo);
		row.next();
		if(String.valueOf(row.getLong("transfer_id")) != null) {
			transaction.setId(row.getLong("transfer_id"));
		}
		transaction.setTypeId(typeId);
		transaction.setStatusId(statusId);
		transaction.setSenderName(TEST_NAME_ONE);
		transaction.setReceiverName(TEST_NAME_TWO);
		transaction.setSenderAccountId(accountFrom);
		transaction.setRecieverAccountId(accountTo);
		transaction.setTransferAmount(999.00);
		return transaction;
	}
	
	private Transaction mapRowToTransaction(SqlRowSet row) {
		Transaction transaction = new Transaction();
		transaction.setId(row.getLong("transfer_id"));
		transaction.setSenderName(row.getString("sender_name"));
		transaction.setSenderAccountId(row.getLong("account_from"));
		transaction.setRecieverAccountId(row.getLong("account_to"));
		transaction.setStatusId(row.getInt("status_id"));		
		transaction.setTypeId(row.getInt("type_id"));
		transaction.setTransferAmount(row.getDouble("amount"));
		return transaction;
	}
	
	private void truncateTransfersTable() {
		String sqlString = "TRUNCATE transfers CASCADE";
		jdbcTemplate.update(sqlString);
	}
	
	
	
}
