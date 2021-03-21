package com.techelevator.tenmo;

import java.math.BigDecimal;

import org.springframework.web.client.RestTemplate;

import com.techelevator.tenmo.models.AuthenticatedUser;
import com.techelevator.tenmo.models.User;
import com.techelevator.tenmo.models.UserCredentials;
import com.techelevator.tenmo.services.AuthenticationService;
import com.techelevator.tenmo.services.AuthenticationServiceException;
import com.techelevator.tenmo.services.TenmoService;
import com.techelevator.tenmo.services.TenmoServiceException;
import com.techelevator.view.ConsoleService;

public class App {

	private static final String API_BASE_URL = "http://localhost:8080/";

	private static final String MENU_OPTION_EXIT = "Exit & Logout";
	private static final String LOGIN_MENU_OPTION_REGISTER = "Register";
	private static final String LOGIN_MENU_OPTION_LOGIN = "Login";
	private static final String[] LOGIN_MENU_OPTIONS = { LOGIN_MENU_OPTION_REGISTER, LOGIN_MENU_OPTION_LOGIN,
			MENU_OPTION_EXIT };
	private static final String MAIN_MENU_OPTION_VIEW_BALANCE = "View your current balance";
	private static final String MAIN_MENU_OPTION_SEND_BUCKS = "Send TE bucks";
	private static final String MAIN_MENU_OPTION_VIEW_PAST_TRANSFERS = "View your past transfers";
	private static final String MAIN_MENU_OPTION_REQUEST_BUCKS = "Request TE bucks";
	private static final String MAIN_MENU_OPTION_VIEW_PENDING_REQUESTS = "View your pending requests";
	private static final String MAIN_MENU_OPTION_LOGIN = "Login as different user";
	private static final String[] MAIN_MENU_OPTIONS = { MAIN_MENU_OPTION_VIEW_BALANCE, MAIN_MENU_OPTION_SEND_BUCKS,
			MAIN_MENU_OPTION_VIEW_PAST_TRANSFERS, MAIN_MENU_OPTION_REQUEST_BUCKS,
			MAIN_MENU_OPTION_VIEW_PENDING_REQUESTS, MAIN_MENU_OPTION_LOGIN, MENU_OPTION_EXIT };

	public static String USER_NAME = "";
	public static String AUTH_TOKEN = "";
	public static int USER_ID = 0;
	public static int TO_USER_ID = 0;

	private AuthenticatedUser currentUser;
	private ConsoleService console;
	private AuthenticationService authenticationService;
	private TenmoService currentTenmoService;
	public RestTemplate restTemplate = new RestTemplate();

	public static void main(String[] args) {
		App app = new App(new ConsoleService(System.in, System.out), new AuthenticationService(API_BASE_URL),
				new TenmoService(API_BASE_URL));
		app.run();
	}

	public App(ConsoleService console, AuthenticationService authenticationService, TenmoService currentTenmoService) {
		this.console = console;
		this.authenticationService = authenticationService;
		this.currentTenmoService = currentTenmoService;
	}

	public void run() {
		System.out.println("*********************");
		System.out.println("* Welcome to TEnmo! *");
		System.out.println("*********************");

		registerAndLogin();
		mainMenu();
	}

	private void mainMenu() {
		while (true) {
			String choice = (String) console.getChoiceFromOptions(MAIN_MENU_OPTIONS);
			if (MAIN_MENU_OPTION_VIEW_BALANCE.equals(choice)) {
				viewCurrentBalance();
			} else if (MAIN_MENU_OPTION_VIEW_PAST_TRANSFERS.equals(choice)) {
				viewTransferHistory();
			} else if (MAIN_MENU_OPTION_VIEW_PENDING_REQUESTS.equals(choice)) {
				viewPendingRequests();
			} else if (MAIN_MENU_OPTION_SEND_BUCKS.equals(choice)) {
				sendBucks();
			} else if (MAIN_MENU_OPTION_REQUEST_BUCKS.equals(choice)) {
				requestBucks();
			} else if (MAIN_MENU_OPTION_LOGIN.equals(choice)) {
				login();
			} else {
				// the only other option on the main menu is to exit
				exitProgram();
			}
		}
	}

	private void viewCurrentBalance() {

		try {
			int userId = currentUser.getUser().getId();
			System.out.println("Your current account balance is: $"
					+ String.format("%.2f", currentTenmoService.viewCurrentBalance(userId).doubleValue()));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void viewTransferHistory() {
		// TODO Auto-generated method stub
		System.out.println("-------------------------------------------------------");
		System.out.println("Transfers");
		System.out.println("ID         \t From/To		Amount");
		System.out.println("-------------------------------------------------------");

		currentTenmoService.listTransfers();

		System.out.println("---------");
		int inputTransferId = 0;

		try {
			inputTransferId = console.getUserInputInteger("Please enter transfer ID to view details (0 to cancel) ");

			System.out.println("-------------------------------------------");
			System.out.println("Transfer Details");
			System.out.println("-------------------------------------------");

			currentTenmoService.TransferDetailsByTransferId(inputTransferId);
		} catch (TenmoServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void viewPendingRequests() {
		System.out.println("-------------------------------------------");
		System.out.println("Pending Transfers");
		System.out.println("ID         \t To		Amount");
		System.out.println("-------------------------------------------");
		currentTenmoService.listPendingTransfers();

		System.out.println("---------");
		int inputTransferId = 0;
		inputTransferId = console.getUserInputInteger("Please enter transfer ID to approve/reject (0 to cancel) ");

		System.out.println("1: Approve");
		System.out.println("2: Reject");
		System.out.println("0: Don't approve or reject");
		System.out.println("---------");
		int inputOption = 0;
		inputOption = console.getUserInputInteger("Please choose an option ");

		if (inputOption == 1) {

			try {
				currentTenmoService.approveTransfer(inputTransferId);
				currentTenmoService.currentUserAccountUpdate(currentUser.getUser().getId(), currentTenmoService.getAmountByTransferId(inputTransferId));
				int recievingUserId = currentTenmoService.getRecUserIdByTranferId(inputTransferId);
				currentTenmoService.recievingUserAccountUpdate(recievingUserId , currentTenmoService.getAmountByTransferId(inputTransferId));
				System.out.println("Congratulations, you have approved the transfer of $" + currentTenmoService.getAmountByTransferId(inputTransferId));
//				****************************************
//				WE ARE SUCCESFULLY CHANGING A PENDING TRANSFER TO APPROVED
//				WE NEED TO UPDATE BALNCES DUE TO APPROVED TRANSFER
//				****************************************
//				DEAL WITH PUSHING 0 - TO EXIT & SOME COMMENTS AND SYSTEM PRINT AND ERRORS
				
				
			} catch (TenmoServiceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (inputOption == 2) {
			try {
				currentTenmoService.rejectTransfer(inputTransferId);
				System.out.println("You have rejected this transfer request.");
			} catch (TenmoServiceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void sendBucks() {

		try {
			System.out.println("-------------------------------------------");
			System.out.println("Users");
			System.out.println("ID         \t Name");
			System.out.println("-------------------------------------------");
			currentTenmoService.listUsers();

			System.out.println("---------");

			int toUserInputId = 0;
			toUserInputId = console.getUserInputInteger("\nEnter ID of user you are sending to (0 to cancel) ");
			App.TO_USER_ID = toUserInputId;

			BigDecimal userInputAmount = null;
			userInputAmount = console.getUserInputBigDecimal("Enter amount ");

			if (currentTenmoService.makeATransfer(currentUser.getUser().getId(), toUserInputId,
					userInputAmount) == true) {

				currentTenmoService.currentUserAccountUpdate(currentUser.getUser().getId(), userInputAmount);
				currentTenmoService.toUserAccountUpdate(toUserInputId, userInputAmount);
				System.out.println("Congratulations, your transfer was successful");

			} else {
				System.out.println("You do not have enough funds for this transfer. Please try again!");
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void requestBucks() {

		try {

			System.out.println("-------------------------------------------");
			System.out.println("Users");
			System.out.println("ID         \t Name");
			System.out.println("-------------------------------------------");
			currentTenmoService.listUsers();

			System.out.println("---------");

			int toUserInputId = 0;
			toUserInputId = console.getUserInputInteger("\nEnter ID of user you are requesting from (0 to cancel) ");
			App.TO_USER_ID = toUserInputId;

			BigDecimal userInputAmount = null;
			userInputAmount = console.getUserInputBigDecimal("Enter amount ");

			currentTenmoService.requestATransfer(currentUser.getUser().getId(), toUserInputId, userInputAmount);

			System.out.println("Congratulations, your transfer request was successful");

		} catch (TenmoServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void exitProgram() {
		System.exit(0);
	}

	private void registerAndLogin() {
		while (!isAuthenticated()) {
			String choice = (String) console.getChoiceFromOptions(LOGIN_MENU_OPTIONS);
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
		while (!isRegistered) // will keep looping until user is registered
		{
			UserCredentials credentials = collectUserCredentials();
			try {
				authenticationService.register(credentials);
				isRegistered = true;
				System.out.println("Registration successful. You can now login.");
			} catch (AuthenticationServiceException e) {
				System.out.println("REGISTRATION ERROR: " + e.getMessage());
				System.out.println("Please attempt to register again.");
			}
		}
	}

	private void login() {
		System.out.println("Please log in");
		currentUser = null;
		while (currentUser == null) // will keep looping until user is logged in
		{
			UserCredentials credentials = collectUserCredentials();
			try {
				currentUser = authenticationService.login(credentials);

				App.AUTH_TOKEN = currentUser.getToken();
				App.USER_NAME = currentUser.getUser().getUsername();
				App.USER_ID = currentUser.getUser().getId();

				// final AuthenticatedUser userCredentials = currentUser;
			} catch (AuthenticationServiceException e) {
				System.out.println("LOGIN ERROR: " + e.getMessage());
				System.out.println("Please attempt to login again.");
			}
		}
	}

	private UserCredentials collectUserCredentials() {
		String username = console.getUserInput("Username");
		String password = console.getUserInput("Password");
		return new UserCredentials(username, password);
	}
}
