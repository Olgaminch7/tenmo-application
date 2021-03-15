package com.techelevator.view;


import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.techelevator.tenmo.models.Account;
import com.techelevator.tenmo.models.AuthenticatedUser;
import com.techelevator.tenmo.models.Transaction;

public class ConsoleService {

	private PrintWriter out;
	private Scanner in;

	public ConsoleService(InputStream input, OutputStream output) {
		this.out = new PrintWriter(output, true);
		this.in = new Scanner(input);
	}

	public Object getChoiceFromOptions(Object[] options) {
		Object choice = null;
		while (choice == null) {
			displayMenuOptions(options);
			choice = getChoiceFromUserInput(options);
		}
		out.println();
		return choice;
	}

	private Object getChoiceFromUserInput(Object[] options) {
		Object choice = null;
		String userInput = in.nextLine();
		try {
			int selectedOption = Integer.valueOf(userInput);
			if (selectedOption > 0 && selectedOption <= options.length) {
				choice = options[selectedOption - 1];
			}
		} catch (NumberFormatException e) {
			// eat the exception, an error message will be displayed below since choice will be null
		}
		if (choice == null) {
			out.println(System.lineSeparator() + "*** " + userInput + " is not a valid option ***" + System.lineSeparator());
		}
		return choice;
	}

	private void displayMenuOptions(Object[] options) {
		out.println();
		for (int i = 0; i < options.length; i++) {
			int optionNum = i + 1;
			out.println(optionNum + ") " + options[i]);
		}
		out.print(System.lineSeparator() + "Please choose an option >>> ");
		out.flush();
	}

	public String getUserInput(String prompt) {
		out.print(prompt);
		out.flush();
		return in.nextLine();
	}

	public Integer getUserInputInteger(String prompt) {
		Integer result = null;
		do {
			out.print(prompt);
			out.flush();
			String userInput = in.nextLine();
			try {
				result = Integer.parseInt(userInput);
			} catch(NumberFormatException e) {
				out.println(System.lineSeparator() + "*** " + userInput + " is not valid ***" + System.lineSeparator());
			}
		} while(result == null);
		return result;
	}
	
	public void printCurrentBalance(Account account) {
		System.out.println("Your current account balance is: " + account.getBalance());
	}
	
	
	public void printOutListOfAccounts(List<Account> accounts, AuthenticatedUser currentUser) {
		for(Account account : accounts) {
			if (account.getAccountHolderName().equals(currentUser.getUser().getUsername())){
				continue;
			}
			System.out.println(account.getId() + " " + account.getAccountHolderName());
			
		}
	}
	
	public void printOutListOfTransactionsByAccountId(List <Transaction> transactions, AuthenticatedUser currentUser) {
		
		System.out.println("----------------------------------------");
		System.out.println("Transfers");
		System.out.printf("%-5s %-25s %-30s %n", "ID", "From/To", "Amount");
		System.out.println("----------------------------------------");
		for(Transaction transaction : transactions) {
			double amount = 0;
			String from_to = "";
			amount = transaction.getTransferAmount();
			
			if (transaction.getSenderName().equalsIgnoreCase(currentUser.getUser().getUsername())) {
				from_to = "To: "+ transaction.getReceiverName();
			}
			if (transaction.getReceiverName().equalsIgnoreCase(currentUser.getUser().getUsername())) {
				from_to = "From: " + transaction.getSenderName();
			}		
			System.out.print(String.format("%-5s %-25s %-30s %n",transaction.getId(), from_to, "$" + amount));
		}
		System.out.println("----------------------------------------");
		String input = getUserInput("Please enter transfer ID to view details (0 to cancel):");
		long transferId = -1;
		try {
			transferId = Long.parseLong(input);
		}
		catch(NumberFormatException e) {
			errorMessage("Please enter a number.");
		}
		if (transferId == 0) {
			// go back to main menu when user chooses zero
			return;
		}
		else { 
			getTransferByTransferDetails(transactions, transferId);
		}
	}
	
	public void printOutPendingListOfTransactionsByAccountId(List <Transaction> transactions, AuthenticatedUser currentUser) {
		
		System.out.println("----------------------------------------");
		System.out.println("Pending Transfers");
		System.out.printf("%-5s %-25s %-30s %n", "ID", "To", "Amount");
		System.out.println("----------------------------------------");
		for(Transaction transaction : transactions) {
			double amount = 0;
			String from_to = "";
			amount = transaction.getTransferAmount();
			
			if (transaction.getSenderName().equalsIgnoreCase(currentUser.getUser().getUsername())) {
				continue;
			}
			
			if (transaction.getReceiverName().equalsIgnoreCase(currentUser.getUser().getUsername())) {
				from_to = transaction.getSenderName();
			}		
			System.out.print(String.format("%-5s %-25s %-30s %n",transaction.getId(), from_to, "$" + amount));
		}
		System.out.println("----------------------------------------");
	}
		
	
	
	public void getTransferByTransferDetails(List<Transaction> transactions, long userInput) {
		boolean foundId = false;
		List <Long> transactionIds = new ArrayList<>(); 
		while(foundId == false) {
			for (Transaction transaction : transactions) {
				transactionIds.add(transaction.getId());
				if(transaction.getId() == userInput) {
					System.out.println("Transfer Details");
					System.out.println("Id: " + transaction.getId());
					System.out.println("From: " + transaction.getSenderName());
					System.out.println("To: " + transaction.getReceiverName());
					int typeId = transaction.getTypeId();
					String typeName = "";
					if(typeId == 1) {
						typeName = "Request";
					} else if (typeId == 2) {
						typeName = "Sent";
					}
					int statusId = transaction.getStatusId();
					String statusName = "";
					if(statusId == 1) {
						statusName = "Pending";
					} else if (statusId == 2) {
						statusName = "Accepted";
					} else if (statusId == 3) {
						statusName = "Rejected";
					}
					System.out.println("Type: " + typeName);
					System.out.println("Status: " + statusName);
					System.out.println("Amount: $" + transaction.getTransferAmount());
					foundId = true;
					break;
				}	
			}
			if (!transactionIds.contains(userInput)) {
				System.out.println("Please enter a valid Transfer ID number.");
				break;
			}	
		}
	}
	
	public void printMenuForApproveOrReject() {
		System.out.println("1: Approve");
		System.out.println("2: Reject");
		System.out.println("0: Don't approve or reject");
		System.out.println("----------\n");
	}

	
	public void errorMessage(String error) {
		System.out.print(error);
	}
}
