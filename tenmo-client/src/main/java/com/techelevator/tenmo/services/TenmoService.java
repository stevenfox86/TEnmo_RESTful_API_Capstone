package com.techelevator.tenmo.services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import com.techelevator.tenmo.App;
import com.techelevator.tenmo.models.Account;
import com.techelevator.tenmo.models.Transfer;
import com.techelevator.tenmo.models.User;
import com.techelevator.view.ConsoleService;
import com.techelevator.tenmo.models.AuthenticatedUser;

public class TenmoService {

	public static String AUTH_TOKEN = "";
	private final String BASE_URL;
	public RestTemplate restTemplate = new RestTemplate();
	// private final ConsoleService console = new ConsoleService(System.in,
	// System.out);
	// private AuthenticatedUser currentUser = new AuthenticatedUser();

	public TenmoService(String url) {
		BASE_URL = url;
	}

	public BigDecimal viewCurrentBalance(int userId) throws TenmoServiceException {

		BigDecimal balance = null;
		// EXCHANGE CURRENT USERS ID FOR CURRENT USERS BALANCE
		try {
			balance = restTemplate.exchange(BASE_URL + "/accounts/" + userId + "/balance", HttpMethod.GET,
					makeAuthEntity(), BigDecimal.class).getBody();

		} catch (RestClientResponseException ex) {
			throw new TenmoServiceException(ex.getRawStatusCode() + " : " + ex.getResponseBodyAsString());
		}
		return balance;

	}

	public void listUsers() throws TenmoServiceException {

		try {
			// CREATE USER ARRAY AND POPULATE WITH LIST OF USERS
			User[] userArray = restTemplate
					.exchange(BASE_URL + "/users", HttpMethod.GET, makeAuthEntity(), User[].class).getBody();

			// LOOP THROUGH EACH ELEMENT IN ARRAY
			for (User thisUser : userArray) {

				// IF USERNAME DOES NOT MATCH NAME OF THE CURRENT USER
				if (App.USER_ID != thisUser.getId()) {
					System.out.println(thisUser.getId() + "\t\t" + thisUser.getUsername());
				}

			}
		} catch (RestClientResponseException ex) {
			throw new TenmoServiceException(ex.getRawStatusCode() + " : " + ex.getResponseBodyAsString());
		}
	}

	public boolean makeATransfer(int accountFromUserId, int accountToUserId, BigDecimal amountTEBucks)
			throws TenmoServiceException {

		int fromUserAccountId = 0;
		int toUserAccountId = 0;

		// EXCHANGE USER ID OF THE SENDING ACCOUNT FOR SENDING ACCOUNT ID
		fromUserAccountId = restTemplate.exchange(BASE_URL + "/accounts/" + accountFromUserId + "/accountid",
				HttpMethod.GET, makeAuthEntity(), int.class).getBody();

		// EXCHANGE USER ID OF THE RECEIVING ACCOUNT FOR RECEIVING ACCOUNT ID
		toUserAccountId = restTemplate.exchange(BASE_URL + "/accounts/" + accountToUserId + "/accountid",
				HttpMethod.GET, makeAuthEntity(), int.class).getBody();

		Transfer transferObject = new Transfer();

		// CREATE A TRANSFER OBJECT
		transferObject.setTransfer_status_id(2);
		transferObject.setTransfer_type_id(2);
		transferObject.setAccount_from(fromUserAccountId);
		transferObject.setAccount_to(toUserAccountId);
		transferObject.setAmount(amountTEBucks);

		// SETTING THE SENDING USER'S BALANCE TO A VARIABLE
		BigDecimal currentUserBalance = viewCurrentBalance(App.USER_ID);

		// SUBTRACT THE FUNDS TRANSFERED FROM THE SENDER'S BALANCE
		BigDecimal currentUserUpdatedBalance = currentUserBalance.subtract(amountTEBucks);

		// CREATE ZERO BALANCE VARIABLE IN BIG DECIMAL
		BigDecimal zeroBalance = new BigDecimal(0);

		// MAKE SURE THE SENDING USER HAS AT LEAST $0.00 LEFT AFTER TRANSFER
		if (currentUserUpdatedBalance.compareTo(zeroBalance) >= 0) {

			try {

				// SEND THE FUNDS - MAKE A TRANSFER
				restTemplate.exchange(BASE_URL + "/transfers", HttpMethod.POST, makeTransferEntity(transferObject),
						Transfer.class);
				return true;
			} catch (RestClientResponseException ex) {

				throw new TenmoServiceException(ex.getRawStatusCode() + " : " + ex.getResponseBodyAsString());
			}
		}
		return false;

	}

	public void currentUserAccountUpdate(int userId, BigDecimal amountTEBucks) throws TenmoServiceException {

		// GET CURRENT USER/ SENDER OF FUNDS -BALANCE
		BigDecimal currentUserBalance = viewCurrentBalance(App.USER_ID);
		// SUBTRACT THE FUNDS TRANSFERED FROM THE SENDER'S BALANCE
		BigDecimal currentUserUpdatedBalance = currentUserBalance.subtract(amountTEBucks);

		// CREATE AN ACCOUNT OBJECT TO SEND AN ACCOUNT UPDATE
		Account accountObject = new Account();
		accountObject.setUser_id(userId);
		accountObject.setBalance(currentUserUpdatedBalance);

		try {
			// SEND ACCOUNT UPDATE
			restTemplate.exchange(BASE_URL + "/accounts/" + App.USER_ID + "/decreased/balance", HttpMethod.PUT,
					makeAccountEntity(accountObject), Account.class);

		} catch (RestClientResponseException ex) {

			throw new TenmoServiceException(ex.getRawStatusCode() + " : " + ex.getResponseBodyAsString());
		}

	}

	public void toUserAccountUpdate(int userId, BigDecimal amountTEBucks) throws TenmoServiceException {

		// GET RECEIVING PARTY USER BALANCE
		BigDecimal toUserBalance = viewCurrentBalance(App.TO_USER_ID);
		// ADD THE FUNDS TRANSFERED TO THE RECEIVERS BALANCE
		BigDecimal currentUserUpdatedBalance = toUserBalance.add(amountTEBucks);

		// CREATE AN ACCOUNT OBJECT TO SEND AN ACCOUNT UPDATE
		Account accountObject = new Account();
		accountObject.setUser_id(userId);
		accountObject.setBalance(currentUserUpdatedBalance);

		try {
			// SEND ACCOUNT UPDATE
			restTemplate.exchange(BASE_URL + "/accounts/" + App.TO_USER_ID + "/increased/balance", HttpMethod.PUT,
					makeAccountEntity(accountObject), Account.class);

		} catch (RestClientResponseException ex) {

			throw new TenmoServiceException(ex.getRawStatusCode() + " : " + ex.getResponseBodyAsString());
		}

	}

	public void listTransfers() {

		String fromUsername = "";
		String toUserName = "";
		int toUserId = 0;
		int fromUserId = 0;

		// CREATE A TRANSFER ARRAY AND POPULATE WITH THE LIST OF TRANSFERS
		Transfer[] transferArray = restTemplate
				.exchange(BASE_URL + "/transfers", HttpMethod.GET, makeAuthEntity(), Transfer[].class).getBody();

		// ITERATE THROUGH TRANSFER ARRAY
		for (Transfer thisTransfer : transferArray) {

			// EXCHANGE ACCOUNT_ID ("ACCOUNT_TO") FOR RECEIVING USER_ID
			toUserId = restTemplate.exchange(BASE_URL + "/accounts/" + thisTransfer.getAccount_to() + "/userId",
					HttpMethod.GET, makeAuthEntity(), int.class).getBody();

			// EXCHANGE RECEIVING USER_ID FOR RECEIVING USERSNAME
			toUserName = restTemplate.exchange(BASE_URL + "/users/" + toUserId + "/username", HttpMethod.GET,
					makeAuthEntity(), String.class).getBody();

			// CHECK UF CURRENT USERNAME EQUALS THE RECEIVING USERSNAME
			if (App.USER_NAME.equals(toUserName)) {

				// EXCHANGE ACCOUNT_ID ("ACCOUNT_FROM") FOR THE SENDING USER_ID
				fromUserId = restTemplate.exchange(BASE_URL + "/accounts/" + thisTransfer.getAccount_from() + "/userId",
						HttpMethod.GET, makeAuthEntity(), int.class).getBody();

				// EXCHANGE SENDING USER_ID FOR SENDING USERNAME
				fromUsername = restTemplate.exchange(BASE_URL + "/users/" + fromUserId + "/username", HttpMethod.GET,
						makeAuthEntity(), String.class).getBody();

				System.out.println(thisTransfer.getTransfer_id() + "\t\tFrom: " + fromUsername + "\t\t\t$ "
						+ thisTransfer.getAmount());

			}
		}

		// CREATE A TRANSFER ARRAY AND POPULATE WITH THE LIST OF TRANSFERS
		Transfer[] transferArray2 = restTemplate
				.exchange(BASE_URL + "/transfers", HttpMethod.GET, makeAuthEntity(), Transfer[].class).getBody();

		// ITERATE THROUGH TRANSFER ARRAY
		for (Transfer thisTransfer : transferArray2) {

			// EXCHANGE ACCOUNT_ID ("ACCOUNT_FROM") FOR THE SENDING USER_ID
			fromUserId = restTemplate.exchange(BASE_URL + "/accounts/" + thisTransfer.getAccount_from() + "/userId",
					HttpMethod.GET, makeAuthEntity(), int.class).getBody();

			// EXCHANGE SENDING USER_ID FOR SENDING USERSNAME
			fromUsername = restTemplate.exchange(BASE_URL + "/users/" + fromUserId + "/username", HttpMethod.GET,
					makeAuthEntity(), String.class).getBody();

			// CHECK UF CURRENT USERNAME EQUALS THE SENDING USERSNAME
			if (App.USER_NAME.equals(fromUsername)) {

				// EXCHANGE ACCOUNT_ID ("ACCOUNT_TO") FOR RECEIVING USER_ID
				toUserId = restTemplate.exchange(BASE_URL + "/accounts/" + thisTransfer.getAccount_to() + "/userId",
						HttpMethod.GET, makeAuthEntity(), int.class).getBody();

				// EXCHANGE RECEIVING USER_ID FOR RECEIVING USERSNAME
				toUserName = restTemplate.exchange(BASE_URL + "/users/" + toUserId + "/username", HttpMethod.GET,
						makeAuthEntity(), String.class).getBody();

				System.out.println(thisTransfer.getTransfer_id() + "\t\tTo: " + toUserName + "\t\t\t$ "
						+ thisTransfer.getAmount());

			}
		}

	}

	public void TransferDetailsByTransferId(int transferId) throws TenmoServiceException {

		Transfer selectedTransfer = new Transfer();
		int fromUserId = 0;
		String fromUserName = "";
		int toUserId = 0;
		String toUserName = "";
		String transferType = "";
		String transferStatus = "";

		try {
			// EXCHANGE TRANSFER ID FOR TRANSFER DETAILS
			selectedTransfer = restTemplate
					.exchange(BASE_URL + "/transfers/" + transferId, HttpMethod.GET, makeAuthEntity(), Transfer.class)
					.getBody();

			// EXCHANGE ACCOUNT_ID ("ACCOUNT_FROM") FOR THE SENDING USER_ID
			fromUserId = restTemplate.exchange(BASE_URL + "/accounts/" + selectedTransfer.getAccount_from() + "/userId",
					HttpMethod.GET, makeAuthEntity(), int.class).getBody();
			// EXCHANGE SEDNING USER_ID FOR THE SENDING USERNAME
			fromUserName = restTemplate.exchange(BASE_URL + "/users/" + fromUserId + "/username", HttpMethod.GET,
					makeAuthEntity(), String.class).getBody();
			// EXCHANGE ACCOUNT_ID ("ACCOUNT_TO") FOR THE RECEIVING USER_ID
			toUserId = restTemplate.exchange(BASE_URL + "/accounts/" + selectedTransfer.getAccount_to() + "/userId",
					HttpMethod.GET, makeAuthEntity(), int.class).getBody();
			// EXCHANGE RECEIVING USER_ID FOR RECEIVING USERNAME
			toUserName = restTemplate.exchange(BASE_URL + "/users/" + toUserId + "/username", HttpMethod.GET,
					makeAuthEntity(), String.class).getBody();
			// EXCHANGE TRANSFER TYPE ID - CODE - FOR DESCRIPTION OF TRANSFER "SEND"
			transferType = restTemplate.exchange(
					BASE_URL + "/transfer_types/" + selectedTransfer.getTransfer_type_id() + "/trasnfer_type_desc",
					HttpMethod.GET, makeAuthEntity(), String.class).getBody();
			// EXCAHNGE TRANSFER STATUS ID -CODE - FOR STATUS OF TRANSFER "APPROVED"
			transferStatus = restTemplate
					.exchange(BASE_URL + "/transfer_statuses/" + selectedTransfer.getTransfer_status_id()
							+ "/transfer_status_desc", HttpMethod.GET, makeAuthEntity(), String.class)
					.getBody();

			System.out.println("Id: " + selectedTransfer.getTransfer_id() + "\r\nFrom: " + fromUserName + "\r\nTo: "
					+ toUserName + "\r\nType: " + transferType + "\r\nStatus: " + transferStatus + "\r\nAmount: $"
					+ selectedTransfer.getAmount());
		} catch (RestClientResponseException ex) {

			throw new TenmoServiceException(ex.getRawStatusCode() + " : " + ex.getResponseBodyAsString());
		}

	}

	public void requestATransfer(int accountFromUserId, int accountToUserId, BigDecimal amountTEBucks)
			throws TenmoServiceException {

		int fromUserAccountId = 0;
		int toUserAccountId = 0;

		// EXCHANGE USER ID OF THE SENDING ACCOUNT FOR SENDING ACCOUNT ID
		fromUserAccountId = restTemplate.exchange(BASE_URL + "/accounts/" + accountFromUserId + "/accountid",
				HttpMethod.GET, makeAuthEntity(), int.class).getBody();

		// EXCHANGE USER ID OF THE RECEIVING ACCOUNT FOR RECEIVING ACCOUNT ID
		toUserAccountId = restTemplate.exchange(BASE_URL + "/accounts/" + accountToUserId + "/accountid",
				HttpMethod.GET, makeAuthEntity(), int.class).getBody();

		Transfer transferObject = new Transfer();

		// CREATE A TRANSFER OBJECT
		transferObject.setTransfer_status_id(1);
		transferObject.setTransfer_type_id(1);
		transferObject.setAccount_from(fromUserAccountId);
		transferObject.setAccount_to(toUserAccountId);
		transferObject.setAmount(amountTEBucks);

		try {

			// MAKE A TRANSFER REQUEST
			restTemplate.exchange(BASE_URL + "/transfers/request", HttpMethod.POST, makeTransferEntity(transferObject),
					Transfer.class);

		} catch (RestClientResponseException ex) {

			throw new TenmoServiceException(ex.getRawStatusCode() + " : " + ex.getResponseBodyAsString());
		}
	}

	public void approveTransfer(int transferID) throws TenmoServiceException {

		Transfer selectedTransfer = new Transfer();
		// GET TRANSFER BY ID TO UPDATE TRANSFER
		selectedTransfer = restTemplate
				.exchange(BASE_URL + "/transfers/" + transferID, HttpMethod.GET, makeAuthEntity(), Transfer.class)
				.getBody();

		// SETTING THE SENDING USER BALANCE TO A VARIABLE
		BigDecimal currentUserBalance = viewCurrentBalance(App.USER_ID);

		// SUBTRACT THE FUNDS TRANSFERED FROM THE SENDER'S BALANCE
		BigDecimal currentUserUpdatedBalance = currentUserBalance.subtract(selectedTransfer.getAmount());

		// CREATE ZERO BALANCE VARIABLE IN BIG DECIMAL
		BigDecimal zeroBalance = new BigDecimal(0);

		// MAKE SURE THE SENDING USER HAS AT LEAST $0.00 LEFT AFTER TRANSFER
		if (currentUserUpdatedBalance.compareTo(zeroBalance) >= 0) {

			// CREATE A TRANSFER OBJECT TO CREATE AN APPROVED TRANSFER
			Transfer transferObject = new Transfer();
			transferObject.setTransfer_id(transferID);
			transferObject.setTransfer_status_id(2);

			try {
				// SEND TRANSFER UPDATE
				restTemplate.exchange(BASE_URL + "/transfers/" + transferID + "/approved/", HttpMethod.PUT,
						makeTransferEntity(transferObject), Transfer.class);

			} catch (RestClientResponseException ex) {

				throw new TenmoServiceException(ex.getRawStatusCode() + " : " + ex.getResponseBodyAsString());
			}

		} else {
			System.out.println("Insufficient Funds");
		}
	}

	public void rejectTransfer(int transferID) throws TenmoServiceException {

		// CREATE AN TRANSFER OBJECT TO CREATE A REJECTED TRANSFER
		Transfer transferObject = new Transfer();
		transferObject.setTransfer_id(transferID);
		transferObject.setTransfer_status_id(3);

		try {
			// SEND TRANSFER UPDATE
			restTemplate.exchange(BASE_URL + "/transfers/" + transferID + "/rejected/", HttpMethod.PUT,
					makeTransferEntity(transferObject), Transfer.class);

		} catch (RestClientResponseException ex) {

			throw new TenmoServiceException(ex.getRawStatusCode() + " : " + ex.getResponseBodyAsString());
		}

	}

	public void listPendingTransfers() {

		String fromUsername = "";
		String toUserName = "";
		int toUserId = 0;
		int fromUserId = 0;

		// CREATE A TRANSFER ARRAY AND POPULATE WITH THE LIST OF PENDING TRANSFERS
		Transfer[] transferArray = restTemplate.exchange(BASE_URL + "/transfers/transfer_status_id_1", HttpMethod.GET,
				makeAuthEntity(), Transfer[].class).getBody();

		// ITERATE THROUGH TRANSFER ARRAY
		for (Transfer thisTransfer : transferArray) {

			// EXCHANGE ACCOUNT_ID ("ACCOUNT_TO") FOR THE SENDING USER_ID
			toUserId = restTemplate.exchange(BASE_URL + "/accounts/" + thisTransfer.getAccount_to() + "/userId",
					HttpMethod.GET, makeAuthEntity(), int.class).getBody();

			// EXCHANGE SENDING USER_ID FOR SENDING USERNAME
			toUserName = restTemplate.exchange(BASE_URL + "/users/" + toUserId + "/username", HttpMethod.GET,
					makeAuthEntity(), String.class).getBody();

			// CHECK UF CURRENT USERNAME EQUALS THE SENDING USERNAME
			if (App.USER_NAME.equals(toUserName)) {

				// EXCHANGE ACCOUNT_ID ("ACCOUNT_FROM") FOR THE REQUESTING USER_ID
				fromUserId = restTemplate.exchange(BASE_URL + "/accounts/" + thisTransfer.getAccount_from() + "/userId",
						HttpMethod.GET, makeAuthEntity(), int.class).getBody();

				// EXCHANGE REQUESTING USER_ID FOR THE REQUESTING USERNAME
				fromUsername = restTemplate.exchange(BASE_URL + "/users/" + fromUserId + "/username", HttpMethod.GET,
						makeAuthEntity(), String.class).getBody();

				System.out.println(
						thisTransfer.getTransfer_id() + "\t\t " + fromUsername + "\t\t\t$ " + thisTransfer.getAmount());

			}
		}
	}

	public BigDecimal getAmountByTransferId(int transferId) throws TenmoServiceException {

		BigDecimal transferAmount = null;
		// EXCHANGE CURRENT USER'S USER_ID FOR CURRENT USER'S BALANCE
		try {
			transferAmount = restTemplate.exchange(BASE_URL + "/transfers/" + transferId + "/amount", HttpMethod.GET,
					makeAuthEntity(), BigDecimal.class).getBody();

		} catch (RestClientResponseException ex) {
			throw new TenmoServiceException(ex.getRawStatusCode() + " : " + ex.getResponseBodyAsString());
		}
		return transferAmount;

	}

	public int getRecUserIdByTranferId(int transferId) {

		int userId = 0;
		// EXCHANGE TRANSFER ID FOR RECIEVING USER'S USER_ID
		userId = restTemplate.exchange(BASE_URL + "/transfers/" + transferId + "/accounts", HttpMethod.GET,
				makeAuthEntity(), int.class).getBody();
		return userId;
	}

	public void recievingUserAccountUpdate(int userId, BigDecimal amountTEBucks) throws TenmoServiceException {

		// GET RECEIVING PARTY USER BALANCE
		BigDecimal toUserBalance = viewCurrentBalance(userId);
		// ADD THE FUNDS TRANSFERED TO THE RECEIVERS BALANCE
		BigDecimal currentUserUpdatedBalance = toUserBalance.add(amountTEBucks);

		// CREATE AN ACCOUNT OBJECT TO SEND AN ACCOUNT UPDATE
		Account accountObject = new Account();
		accountObject.setUser_id(userId);
		accountObject.setBalance(currentUserUpdatedBalance);

		try {
			// SEND ACCOUNT UPDATE
			restTemplate.exchange(BASE_URL + "/accounts/" + userId + "/increased/balance", HttpMethod.PUT,
					makeAccountEntity(accountObject), Account.class);

		} catch (RestClientResponseException ex) {

			throw new TenmoServiceException(ex.getRawStatusCode() + " : " + ex.getResponseBodyAsString());
		}

	}

	// AUTHORIZATION ENTITIES
	private HttpEntity makeAuthEntity() {
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(App.AUTH_TOKEN);
		HttpEntity entity = new HttpEntity<>(headers);
		return entity;

	}

	private HttpEntity<Transfer> makeTransferEntity(Transfer transfer) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setBearerAuth(App.AUTH_TOKEN);
		HttpEntity<Transfer> entity = new HttpEntity<>(transfer, headers);
		return entity;
	}

	private HttpEntity<Account> makeAccountEntity(Account account) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setBearerAuth(App.AUTH_TOKEN);
		HttpEntity<Account> entity = new HttpEntity<>(account, headers);
		return entity;
	}
}
