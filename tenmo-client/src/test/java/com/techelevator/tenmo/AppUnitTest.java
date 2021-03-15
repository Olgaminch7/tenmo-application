package com.techelevator.tenmo;

import org.junit.*;

import com.techelevator.tenmo.models.Account;

public class AppUnitTest {
	
	private Account account;
	private App app;
	
	@Before
	public void setup() {
		account = new Account();
		app = new App();
		account.setAccountHolderName("TestName");
		account.setBalance(1000.00);
		account.setId((long)123456);
	}
	
	@Test
	public void do_you_have_enough_money_to_transfer() {
		boolean enoughMoney = app.isBalanceMoreThanTransferAmount(1000, account);
		boolean notEnoughMoney = app.isBalanceMoreThanTransferAmount(1001, account);
		boolean negativeMoneySent = app.isBalanceMoreThanTransferAmount(-1, account);
		
		Assert.assertTrue(enoughMoney);
		Assert.assertFalse(notEnoughMoney);
		Assert.assertFalse(negativeMoneySent);
	}


}
