package com.techelevator.tenmo;

import java.util.List;
import com.techelevator.tenmo.models.Account;
import com.techelevator.tenmo.models.AuthenticatedUser;
import com.techelevator.tenmo.models.Transaction;
import com.techelevator.tenmo.models.UserCredentials;
import com.techelevator.tenmo.services.AuthenticationService;
import com.techelevator.tenmo.services.AuthenticationServiceException;
import com.techelevator.tenmo.services.AccountService;
import com.techelevator.tenmo.services.AccountServiceException;
import com.techelevator.view.ConsoleService;

public class App {

private static final String API_BASE_URL = "http://localhost:8080/";
    
    private static final String MENU_OPTION_EXIT = "Exit";
    private static final String LOGIN_MENU_OPTION_REGISTER = "Register";
	private static final String LOGIN_MENU_OPTION_LOGIN = "Login";
	private static final String[] LOGIN_MENU_OPTIONS = { LOGIN_MENU_OPTION_REGISTER, LOGIN_MENU_OPTION_LOGIN, MENU_OPTION_EXIT };
	private static final String MAIN_MENU_OPTION_VIEW_BALANCE = "View your current balance";
	private static final String MAIN_MENU_OPTION_SEND_BUCKS = "Send TE bucks";
	private static final String MAIN_MENU_OPTION_VIEW_PAST_TRANSFERS = "View your past transfers";
	private static final String MAIN_MENU_OPTION_REQUEST_BUCKS = "Request TE bucks";
	private static final String MAIN_MENU_OPTION_VIEW_PENDING_REQUESTS = "View your pending requests";
	private static final String MAIN_MENU_OPTION_LOGIN = "Login as different user";
	private static final String[] MAIN_MENU_OPTIONS = { MAIN_MENU_OPTION_VIEW_BALANCE, MAIN_MENU_OPTION_SEND_BUCKS, MAIN_MENU_OPTION_VIEW_PAST_TRANSFERS, MAIN_MENU_OPTION_REQUEST_BUCKS, MAIN_MENU_OPTION_VIEW_PENDING_REQUESTS, MAIN_MENU_OPTION_LOGIN, MENU_OPTION_EXIT };
	
    private AuthenticatedUser currentUser;
    private ConsoleService console;
    private AuthenticationService authenticationService;
    private AccountService accountService;
    
    public static void main(String[] args) {
    	App app = new App(new ConsoleService(System.in, System.out), new AuthenticationService(API_BASE_URL));
    	app.run();
    }
    
    public App() {}
    // Comment out after testing

    public App(ConsoleService console, AuthenticationService authenticationService) {
		this.console = console;
		this.authenticationService = authenticationService;
	}

	public void run() {
		System.out.println("*********************");
		System.out.println("* Welcome to TEnmo! *");
		System.out.println("*********************");
		
		registerAndLogin();
		
		mainMenu();
	}

	private void mainMenu() {
		while(true) {
			String choice = (String)console.getChoiceFromOptions(MAIN_MENU_OPTIONS);
			try {
				if(MAIN_MENU_OPTION_VIEW_BALANCE.equals(choice)) {
					// View Current Balance
					showUserAccountBalance();
				} else if(MAIN_MENU_OPTION_VIEW_PAST_TRANSFERS.equals(choice)) {
					// View Transfer History
					printAllTransactionsByAccountId();
				} else if(MAIN_MENU_OPTION_VIEW_PENDING_REQUESTS.equals(choice)) {
					// View Pending Requests
					printPendingTransactionsByAccountId();
				} else if(MAIN_MENU_OPTION_SEND_BUCKS.equals(choice)) {
					// Send Bucks
					sendMoney();
				} else if(MAIN_MENU_OPTION_REQUEST_BUCKS.equals(choice)) {
					// Request Bucks
					requestMoney();
				} else if(MAIN_MENU_OPTION_LOGIN.equals(choice)) {
					login();
	
				} else {
					// the only other option on the main menu is to exit
					exitProgram();
				}
			} catch (AccountServiceException ex) {
				console.errorMessage(ex.getMessage());
			}
		}
	}

	private void exitProgram() {
		System.exit(0);
	}

	private void registerAndLogin() {
		while(!isAuthenticated()) {
			String choice = (String)console.getChoiceFromOptions(LOGIN_MENU_OPTIONS);
			if (LOGIN_MENU_OPTION_LOGIN.equals(choice)) {
				login();
			} else if (LOGIN_MENU_OPTION_REGISTER.equals(choice)) {
				register();
			} else {
				// the only other option on the login menu is to exit
				exitProgram();
			}
		}
	}

	private boolean isAuthenticated() {
		return currentUser != null;
	}

	private void register() {
		System.out.println("Please register a new user account");
		boolean isRegistered = false;
        while (!isRegistered) //will keep looping until user is registered
        {
            UserCredentials credentials = collectUserCredentials();
            try {
            	authenticationService.register(credentials);
            	isRegistered = true;
            	System.out.println("Registration successful. You can now login.");
            } catch(AuthenticationServiceException e) {
            	System.out.println("REGISTRATION ERROR: "+e.getMessage());
				System.out.println("Please attempt to register again.");
            }
        }
	}

	private void login() {
		System.out.println("Please log in");
		currentUser = null;
		while (currentUser == null) //will keep looping until user is logged in
		{
			UserCredentials credentials = collectUserCredentials();
		    try {
				currentUser = authenticationService.login(credentials);
				accountService = new AccountService(API_BASE_URL, currentUser);
			} catch (AuthenticationServiceException e) {
				System.out.println("LOGIN ERROR: "+e.getMessage());
				System.out.println("Please attempt to login again.");
			}
		}
	}
	
	private UserCredentials collectUserCredentials() {
		String username = console.getUserInput("Username: ");
		String password = console.getUserInput("Password: ");
		return new UserCredentials(username, password);
	}
	
	//MAIN_MENU_OPTION_VIEW_BALANCE
	
	private void showUserAccountBalance() throws AccountServiceException {
		Account account = accountService.getAccountInfoByUsername();
		console.printCurrentBalance(account);
	}
	
	//MAIN_MENU_OPTION_VIEW_PAST_TRANSFERS
	
	public void printAllTransactionsByAccountId() throws AccountServiceException {
		Account account = accountService.getAccountInfoByUsername();
		List <Transaction> transactions = accountService.getAllTransactionsByAccountId(account.getId(), 2);
		console.printOutListOfTransactionsByAccountId(transactions, currentUser);
	}
	
	//MAIN_MENU_OPTION_VIEW_PENDING_REQUESTS
	
	public void printPendingTransactionsByAccountId() throws AccountServiceException {
		Account account = accountService.getAccountInfoByUsername();
		List <Transaction> transactions = accountService.getAllTransactionsByAccountId(account.getId(), 1);
		console.printOutPendingListOfTransactionsByAccountId(transactions, currentUser);
		long transferIdFromUser = getTransferIdFromUser();
		getUserChoiceForPendingTransaction(transferIdFromUser, transactions);
	}
	
	public long getTransferIdFromUser() {
		String input = console.getUserInput("\nPlease enter transfer ID to approve/reject (0 to cancel): ");
		long transferId = -1;
		try {
			transferId = Long.parseLong(input);
		}
		catch(NumberFormatException e) {
			console.errorMessage("\nPlease enter a number.\n");
		}
		return transferId;
	}
	
	public void getUserChoiceForPendingTransaction(long transferId, List<Transaction> transactions) throws AccountServiceException {
		if (transferId == 0) {
			// go back to main menu when user chooses zero
			return;
		} else { 
			for (Transaction transaction : transactions) {
				if (transaction.getId() == transferId) {
					console.printMenuForApproveOrReject();
					String menuChoice = console.getUserInput("Please choose an option: ");
					int menuChoiceNumber = -1;
					try {
						menuChoiceNumber = Integer.parseInt(menuChoice);
					} catch (NumberFormatException e) {
						console.errorMessage("\nPlease enter a number.");
					}
					approveOrRejectPendingTransfer(menuChoiceNumber, transaction);
					break;
				}
			}
		}
	}
		
	public void approveOrRejectPendingTransfer(int menuChoiceNumber, Transaction transaction) throws AccountServiceException{
		Account account = accountService.getAccountInfoByUsername();
		if (menuChoiceNumber == 1 && !isBalanceMoreThanTransferAmount(transaction.getTransferAmount(), account)) {
				console.errorMessage("\nThere's not enough money in your account balance.\n");
		}
		if (menuChoiceNumber == 1 && isBalanceMoreThanTransferAmount(transaction.getTransferAmount(), account)) {
			transaction.setTypeId(2);
			transaction.setStatusId(2);
			accountService.updateTransaction(transaction);	
			console.errorMessage("\nYour transaction has been approved and the money were withdrawn from account.\n");
		} else if (menuChoiceNumber == 2) {
			transaction.setStatusId(3);
			accountService.updateTransaction(transaction);
			console.errorMessage("\nTransaction was rejected. Money hasn't been transfered.\n");
		} else if (menuChoiceNumber == 0){
			return;
		} else {
			System.out.println("\nPlease enter a valid choice.");
		}
	}
	
	public boolean isBalanceMoreThanTransferAmount(double transferAmount, Account account) {
		boolean result = false;
		if (account.getBalance() >= transferAmount && transferAmount > 0) {
			result = true;
		}	
		return result;
	}
	
	//MAIN_MENU_OPTION_SEND_BUCKS
	
	public void sendMoney() throws AccountServiceException {
		List<Account> accounts = accountService.getAllAccounts();
		Account currentUserAccount = accountService.getAccountInfoByUsername();
		printOutListOfAccounts();
		long accountIdChoice = getUserChoiceForSend();
		double moneyToSend = getUserSendMoneyAmount();
		Transaction transaction = new Transaction();
		
		if (isBalanceMoreThanTransferAmount(moneyToSend, currentUserAccount) == false) {
			console.errorMessage("\nThere's not enough money in your balance to complete this transaction or you've sent an incorrect amount of money.\n");
		}
		else {
			boolean running = true;
			while(running) {
				for(Account account : accounts) {
					if (account.getId() == accountIdChoice) {
						transaction.setReceiverName(account.getAccountHolderName());
						transaction.setRecieverAccountId(accountIdChoice);
						transaction.setSenderAccountId(currentUserAccount.getId());
						transaction.setSenderName(currentUserAccount.getAccountHolderName());
						transaction.setTypeId(2);
						transaction.setStatusId(2);
						transaction.setTransferAmount(moneyToSend);
						accountService.sendMoney(transaction);
						console.errorMessage("\nYour transaction was succesful!\n");
						running = false;
						break;
					}
				}
			}
		} 
	}
	
	public void printOutListOfAccounts() throws AccountServiceException {
		List<Account> accounts = accountService.getAllAccounts();
		console.printOutListOfAccounts(accounts, currentUser);
	}
	
	public long getUserChoiceForSend() {
		long accountIdChoice = 0;
		String userIdChoice = console.getUserInput("\nEnter ID # of user you want to send money to: ");
		try {
			accountIdChoice = Long.parseLong(userIdChoice);
		} catch (NumberFormatException e) {
			console.errorMessage("\nPlease enter a valid ID #.");
		}
		return accountIdChoice;
	}
		
	public double getUserSendMoneyAmount() {
		double moneyToSend = 0;
		String moneyAmount = console.getUserInput("Enter amount of money to send: ");
		try {
			moneyToSend = Double.parseDouble(moneyAmount);
		} catch (NumberFormatException e ) {
			console.errorMessage("\nPlease enter valid money amount.");
		}
		return moneyToSend;
	}
	
	//MAIN_MENU_OPTION_REQUEST_BUCKS
	
	public void requestMoney() throws AccountServiceException {
		List<Account> accounts = accountService.getAllAccounts();
		printOutListOfAccounts();
		long accountIdChoice = getUserChoiceForSend();
		double moneyToReceive = getUserRequestMoneyAmount();
		Transaction transaction = new Transaction();
		Account currentUserAccount = accountService.getAccountInfoByUsername();	
		boolean running = true;
		while(running) {
			for(Account account : accounts) {
				if (account.getId() == accountIdChoice) {
				transaction.setReceiverName(account.getAccountHolderName());
				transaction.setRecieverAccountId(accountIdChoice);
				transaction.setSenderAccountId(currentUserAccount.getId());
				transaction.setSenderName(currentUserAccount.getAccountHolderName());
				transaction.setTypeId(1);
				transaction.setStatusId(1);
				transaction.setTransferAmount(moneyToReceive);
				accountService.sendMoney(transaction);
				console.errorMessage("\nYour transaction is pending.\n");
				running = false;
				break;
				}
			}
		}		
	}
	
	public double getUserRequestMoneyAmount() {
		double moneyToSend = 0;
		String moneyAmount = console.getUserInput("Enter amount of money to request: ");
		try {
			moneyToSend = Double.parseDouble(moneyAmount);
		} catch (NumberFormatException e ) {
			console.errorMessage("\nPlease enter valid money amount.");
		}
		return moneyToSend;
	}
	

}
