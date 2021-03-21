
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


	//	REQUEST MAPPING FOR ALL METHODS NEEDED TO RENDER TENMO CLIENT FUNCTIONALITY 
	//	REQUIRES PRE-AUTHORIZATION 

	@RequestMapping(path = "/accounts/{userId}/balance", method = RequestMethod.GET)
	public BigDecimal getBalance(@PathVariable int userId) {
		return tsDAO.getUserCurrentBalanceByID(userId);
	}

	@RequestMapping(path = "/users", method = RequestMethod.GET)
	public List<User> getAll() {
		return tsDAO.getAllUsers();
	}

	@ResponseStatus(HttpStatus.CREATED)
	@RequestMapping(path = "/transfers", method = RequestMethod.POST)
	public void createTransfer(@RequestBody Transfer newTransfer) throws UserIdNotFoundException {
		tsDAO.transfer(newTransfer.getAccount_from(), newTransfer.getAccount_to(), newTransfer.getAmount());
	}

	@RequestMapping(path = "/accounts/{userId}/decreased/balance", method = RequestMethod.PUT)
	public void fromUserUpdate(@RequestBody Account newAccountUpdate, @PathVariable int userId)
			throws UserIdNotFoundException {
		tsDAO.UpdateFromUserBalance(userId, newAccountUpdate.getBalance());
	}

	@RequestMapping(path = "/accounts/{userId}/increased/balance", method = RequestMethod.PUT)
	public void toUserUpdate(@RequestBody Account newAccountUpdate, @PathVariable int userId)
			throws UserIdNotFoundException {

		tsDAO.UpdateToUserBalance(userId, newAccountUpdate.getBalance());
	}

	@RequestMapping(path = "/transfers", method = RequestMethod.GET)
	public List<Transfer> getAllTranfers() {
		return tsDAO.getAllTransfers();
	}

	@RequestMapping(path = "/transfers/{transferId}", method = RequestMethod.GET)
	public Transfer getTransfers(@PathVariable int transferId) throws TransferIdNotFoundException {
		return tsDAO.getTransferByID(transferId);
	}

	@RequestMapping(path = "/accounts/{userId}/accountid", method = RequestMethod.GET)
	public int getAccountIdFromUserId(@PathVariable int userId) throws UserIdNotFoundException {
		return tsDAO.getAccountIdFromUserId(userId);
	}

	@RequestMapping(path = "/users/{userId}/username", method = RequestMethod.GET)
	public String getUsernameFromUserId(@PathVariable int userId) throws UserIdNotFoundException {
		return tsDAO.getUsernameFromUserId(userId);
	}

	@RequestMapping(path = "/accounts/{accountId}/userId", method = RequestMethod.GET)
	public int getUserIdFromAccountId(@PathVariable int accountId) {
		return tsDAO.getUserIdFromAccountId(accountId);
	}

	@RequestMapping(path = "/transfer_types/{transferTypeId}/trasnfer_type_desc", method = RequestMethod.GET)
	public String TransferTypeDescFromTransfTypeId(@PathVariable int transferTypeId) {
		return tsDAO.getTransferTypeDescFromTransferTypeId(transferTypeId);
	}

	@RequestMapping(path = "/transfer_statuses/{transferStatusId}/transfer_status_desc", method = RequestMethod.GET)
	public String TransferStatusDescFromTransfStatusId(@PathVariable int transferStatusId) {
		return tsDAO.getTransferStatusDescFromTransferStatusId(transferStatusId);
	}
	
	@ResponseStatus(HttpStatus.CREATED)
	@RequestMapping(path = "/transfers/request", method = RequestMethod.POST)
	public void requestTransfer(@RequestBody Transfer newTransfer) throws UserIdNotFoundException {
		tsDAO.requestTransfer(newTransfer.getAccount_from(), newTransfer.getAccount_to(), newTransfer.getAmount());
	}
	
	@RequestMapping(path = "/transfers/{transferID}/approval", method = RequestMethod.PUT)
	public void approvedTransfer(@RequestBody Transfer transferUpdate, @PathVariable int transferID)
			throws UserIdNotFoundException {

		tsDAO.approvedTransfer(transferID);
	}
	
	@RequestMapping(path = "/transfers/{transferID}/rejected", method = RequestMethod.PUT)
	public void rejectedTransfer(@RequestBody Transfer transferUpdate, @PathVariable int transferID)
			throws UserIdNotFoundException {

		tsDAO.rejectedTransfer(transferID);
	}
	
	@RequestMapping(path = "/transfers/transfer_status_id_1", method = RequestMethod.GET)
	public List<Transfer> getAllPendingTransfers() {
		return tsDAO.getAllPendingTransfers();
	}

	@RequestMapping(path = "/transfers/{transferId}/amount", method = RequestMethod.GET)
	public BigDecimal getAmountByTransferID(@PathVariable int transferId) {
		return tsDAO.getTransferAmountByTransferID(transferId);
	}
	
	@RequestMapping(path = "/transfers/{transferID}/accounts", method = RequestMethod.GET)
	public int getUserFromTransferID(@PathVariable int transferID) throws UserIdNotFoundException {
		return tsDAO.getUserIdFromTransferId(transferID);
	}
}
