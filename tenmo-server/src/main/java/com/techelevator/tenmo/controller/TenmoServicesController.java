
package com.techelevator.tenmo.controller;

import java.math.BigDecimal;
import java.util.List;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.techelevator.tenmo.dao.TenmoServicesDAO;
import com.techelevator.tenmo.dao.UserDAO;
import com.techelevator.tenmo.exception.TransferIdNotFoundException;
import com.techelevator.tenmo.exception.UserIdNotFoundException;
import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;

@PreAuthorize("isAuthenticated()")
@RestController
public class TenmoServicesController {

	private UserDAO userDAO;
	private TenmoServicesDAO tsDAO;

	public TenmoServicesController(TenmoServicesDAO tsDAO, UserDAO userDAO) {
		this.tsDAO = tsDAO;
		this.userDAO = userDAO;
	}

	// REQUEST MAPPING FOR ALL METHODS NEEDED TO RENDER TENMO CLIENT FUNCTIONALITY
	//	CONTROLLER SPEAKS TO BOTH THE CLIENT AND THE MODEL (JdbcTenmoServicesDAO/ts.dao)
	// REQUIRES PRE-AUTHORIZATION

	// GET BALANCE BY USER_ID
	@RequestMapping(path = "/accounts/{userId}/balance", method = RequestMethod.GET)
	public BigDecimal getBalance(@PathVariable int userId) {
		return tsDAO.getUserCurrentBalanceByID(userId);
	}

	// GET LIST OF ALL USER_ID's + USERNAME's
	@RequestMapping(path = "/users", method = RequestMethod.GET)
	public List<User> getAll() {
		return tsDAO.getAllUsers();
	}

	// CREATE A TRANSFER IN TRANSFERS TABLE	
	//	STATUS 201
	@ResponseStatus(HttpStatus.CREATED)
	@RequestMapping(path = "/transfers", method = RequestMethod.POST)
	public void createTransfer(@RequestBody Transfer newTransfer) throws UserIdNotFoundException {
		tsDAO.transfer(newTransfer.getAccount_from(), newTransfer.getAccount_to(), newTransfer.getAmount());
	}

	// UPDATE FROM USER ACCOUNT BALANCE (SENDBUCKS)
	@RequestMapping(path = "/accounts/{userId}/decreased/balance", method = RequestMethod.PUT)
	public void fromUserUpdate(@RequestBody Account AccountUpdate, @PathVariable int userId)
			throws UserIdNotFoundException {
		tsDAO.UpdateFromUserBalance(userId, AccountUpdate.getBalance());
	}

	// UPDATE TO USER ACCOUNT BALANCE (SENDBUCKS)
	@RequestMapping(path = "/accounts/{userId}/increased/balance", method = RequestMethod.PUT)
	public void toUserUpdate(@RequestBody Account AccountUpdate, @PathVariable int userId)
			throws UserIdNotFoundException {
		tsDAO.UpdateToUserBalance(userId, AccountUpdate.getBalance());
	}

	// GET A LIST OF ALL TRANSFERS
	// LOGIC TO ONLY RETURN THE TRANSFERS RELATED TO CURRENT USER IN CLIENT
	@RequestMapping(path = "/transfers", method = RequestMethod.GET)
	public List<Transfer> getAllTranfers() {
		return tsDAO.getAllTransfers();
	}

	// @todo - refactor to GET username from account_id with inner join
	// *****HELPER METHOD FOR LIST ALL TRANSFERS*****
	// GET USER_ID FROM ACCOUNT_ID
	@RequestMapping(path = "/accounts/{accountId}/userId", method = RequestMethod.GET)
	public int getUserIdFromAccountId(@PathVariable int accountId) {
		return tsDAO.getUserIdFromAccountId(accountId);
	}

	// *****HELPER METHOD FOR LIST ALL TRANSFERS*****
	// GET USERNAME FROM USER_ID
	@RequestMapping(path = "/users/{userId}/username", method = RequestMethod.GET)
	public String getUsernameFromUserId(@PathVariable int userId) throws UserIdNotFoundException {
		return tsDAO.getUsernameFromUserId(userId);
	}

	// GET TRANSFER DETAILS BY TRANSFER ID
	// **USE HELPER METHOD ^ GET USER_ID FROM ACCOUNT_ID ^**
	// **USE HELPER METHOD ^ GET USERNAME FROM USER_ID ^**
	@RequestMapping(path = "/transfers/{transferId}", method = RequestMethod.GET)
	public Transfer getTransferById(@PathVariable int transferId) throws TransferIdNotFoundException {
		return tsDAO.getTransferByID(transferId);
	}

	// *****HELPER METHOD FOR LIST TRANSFER DETAILS BY TRANSFER_ID******
	// GET TRANSFER_TYPE_DESCRIPTION FROM TRANSFER_TYPE_ID
	@RequestMapping(path = "/transfer_types/{transferTypeId}/trasnfer_type_desc", method = RequestMethod.GET)
	public String TransferTypeDescFromTransfTypeId(@PathVariable int transferTypeId) {
		return tsDAO.getTransferTypeDescFromTransferTypeId(transferTypeId);
	}

	// *****HELPER METHOD FOR LIST TRANSFER DETAILS BY TRANSFER_ID******
	// GET TRANSFER_STATUS_DESCRIPTION FROM TRANSFER_STATUS_ID
	@RequestMapping(path = "/transfer_statuses/{transferStatusId}/transfer_status_desc", method = RequestMethod.GET)
	public String TransferStatusDescFromTransfStatusId(@PathVariable int transferStatusId) {
		return tsDAO.getTransferStatusDescFromTransferStatusId(transferStatusId);
	}

	// CREATE A TRANSFER - TRANSFER TYPE REQUEST - IN TRANSFERS TABLE
	@ResponseStatus(HttpStatus.CREATED)
	@RequestMapping(path = "/transfers/request", method = RequestMethod.POST)
	public void requestTransfer(@RequestBody Transfer newTransfer) throws UserIdNotFoundException {
		tsDAO.requestTransfer(newTransfer.getAccount_from(), newTransfer.getAccount_to(), newTransfer.getAmount());
	}

	// *****HELPER MAPPING FOR CREATE TRANSFER TYPE REQUEST*****
	// GET ACCOUNT_ID FROM USER_ID
	@RequestMapping(path = "/accounts/{userId}/accountid", method = RequestMethod.GET)
	public int getAccountIdFromUserId(@PathVariable int userId) throws UserIdNotFoundException {
		return tsDAO.getAccountIdFromUserId(userId);
	}

	// GET A LIST OF ALL TRANSFERS - TRANSFER STATUS PENDING
		@RequestMapping(path = "/transfers/transfer_status_id_1", method = RequestMethod.GET)
		public List<Transfer> getAllPendingTransfers() {
			return tsDAO.getAllPendingTransfers();
		}
	
	// UPDATE TRANSFER STATUS TO APPROVED
	@RequestMapping(path = "/transfers/{transferID}/approved", method = RequestMethod.PUT)
	public void approvedTransfer(@RequestBody Transfer transferUpdate, @PathVariable int transferID)
			throws UserIdNotFoundException {

		tsDAO.approvedTransfer(transferID);
	}

	// UPDATE TRANSFER STATUS TO REJECTED
	@RequestMapping(path = "/transfers/{transferID}/rejected", method = RequestMethod.PUT)
	public void rejectedTransfer(@RequestBody Transfer transferUpdate, @PathVariable int transferID)
			throws UserIdNotFoundException {

		tsDAO.rejectedTransfer(transferID);
	}

	// GET TRANSFER AMOUNT BY TRANSFER_ID  
	// USED WHEN APPROVING A TRANSFER TO GET AN AMOUNT FOR UPDAE BALANCE METHOD
	@RequestMapping(path = "/transfers/{transferId}/amount", method = RequestMethod.GET)
	public BigDecimal getAmountByTransferID(@PathVariable int transferId) {
		return tsDAO.getTransferAmountByTransferID(transferId);
	}

	// GET USER_ID FROM TRANSFER_ID
	//	USED WHEN APPROVING A TRANFER TO GET THE USER ID FOR THE SENDING/ OTHER USER
	@RequestMapping(path = "/transfers/{transferID}/accounts", method = RequestMethod.GET)
	public int getUserFromTransferID(@PathVariable int transferID) throws UserIdNotFoundException {
		return tsDAO.getUserIdFromTransferId(transferID);
	}
}
