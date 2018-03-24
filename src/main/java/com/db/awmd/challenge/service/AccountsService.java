package com.db.awmd.challenge.service;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.Transfer;
import com.db.awmd.challenge.exception.InsufficientBalanceException;
import com.db.awmd.challenge.exception.InvalidAccountException;
import com.db.awmd.challenge.exception.InvalidAmountException;
import com.db.awmd.challenge.repository.AccountsRepository;
import static com.db.awmd.challenge.util.Constants.*;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AccountsService {

  @Getter
  private final AccountsRepository accountsRepository;

  @Getter 
  private NotificationService emailNotificationService;
  
  @Autowired
  public AccountsService(AccountsRepository accountsRepository, NotificationService emailNotificationService) {
    this.accountsRepository = accountsRepository;
    this.emailNotificationService = emailNotificationService;
  }
 
  public void createAccount(Account account) {
    this.accountsRepository.createAccount(account);
  }

  public Account getAccount(String accountId) {
    return this.accountsRepository.getAccount(accountId);
  }
  
  public JSONObject transferFunds(Transfer transfer) throws InsufficientBalanceException{
	  log.debug("Initiating transfer of funds");
	  log.debug("Validating request");
		
	  // Validate request and throw appropriate exception if request is invalid
	  validateRequest(transfer);
	  log.info("Request is validated successfully");

	  // Read request details
	  Account accFrom = accountsRepository.getAccount(transfer.getAccountFromId());
	  Account accTo = accountsRepository.getAccount(transfer.getAccountToId());
	  BigDecimal amountToTransfer = transfer.getAmountToTransfer();
	  
	  // Obtain locks on both account objects. Obtain first lock on account with lower id.
	  int accFromId = Integer.parseInt(accFrom.getAccountId());
	  int accToId = Integer.parseInt(accFrom.getAccountId());
	  Object lockOnAccWithLowerId = accFromId < accToId ? accFrom.getLock() : accTo.getLock();
	  Object lockOnAccWithHigherId = accFromId < accToId ? accTo.getLock() : accFrom.getLock();
	  
	  synchronized (lockOnAccWithLowerId) {
	     synchronized (lockOnAccWithHigherId) {
		 	  //Subtract amount from source account
			  BigDecimal accFromBalance = accFrom.getBalance();
			  accFromBalance = accFromBalance.subtract(amountToTransfer);
			  accFrom.setBalance(accFromBalance);
			  log.info("Account {} has been debited with amount:{}. Updated balance: {}", accFrom.getAccountId(), 
					  amountToTransfer, accFrom.getBalance());
			  
			  log.debug("Sending notfication to source acc holder");
			  // Send notification to source acc holder
			  emailNotificationService.notifyAboutTransfer(accFrom, "Your account has been"
			  		+ " debited with amount: "+ amountToTransfer + ". Your updated balance is: "+accFrom.getBalance());
	
			  //Add amount to target account
			  BigDecimal accToBalance = accTo.getBalance();
			  accToBalance = accToBalance.add(amountToTransfer);
			  accTo.setBalance(accToBalance);
			  log.info("Account {} has been credited with amount:{}. Updated balance: {}", accTo.getAccountId(), 
					  amountToTransfer, accTo.getBalance());
			  
			  log.debug("Sending notfication to dest acc holder");
			  // Send notification to target acc holder
			  emailNotificationService.notifyAboutTransfer(accTo, "Your account has been"
				  		+ " credited with amount: "+ amountToTransfer + ". Your updated balance is: "+accTo.getBalance());
    	}
	  }
	  
	  // Generate JSON response with transaction status details
	  JSONObject response = new JSONObject();
	  response.put(MESSAGE, "Funds has been transferred successfully");
	  return response;
  }
  
  
  private void validateRequest(Transfer transfer){
	  Account accFrom = accountsRepository.getAccount(transfer.getAccountFromId());
	  Account accTo = accountsRepository.getAccount(transfer.getAccountToId());
	  
	  if(null == accFrom){
		  log.error("Account with number '{}' does not exist. Please provide valid account number.", transfer.getAccountFromId());
		  throw new InvalidAccountException("Account with number '"+  transfer.getAccountFromId() + "' does not exist. "
		  		+ "Please provide valid account number.");
	  }
	  
	  if(null == accTo){
		  log.error("Account with number '{}' does not exist. Please provide valid account number.", transfer.getAccountToId());
		  throw new InvalidAccountException("Account with number '"+  transfer.getAccountToId() + "' does not exist. "
			  		+ "Please provide valid account number.");
	  }
	  
	  BigDecimal amountToTransfer = transfer.getAmountToTransfer();
	  if(amountToTransfer.compareTo(new BigDecimal(0)) == 0 || amountToTransfer.compareTo(new BigDecimal(0)) == -1){
		  log.error("Please provide valid(positive) amount to transfer.", transfer.getAccountToId());
		  throw new InvalidAmountException("Please provide valid(positive) amount to transfer.");
	  }
	  
	  BigDecimal accFromBalance = accFrom.getBalance();
	  if(accFromBalance.compareTo(amountToTransfer) == -1){
		  throw new InsufficientBalanceException("You don't have sufficient balance.");
	  }
	  
  }
   
}
