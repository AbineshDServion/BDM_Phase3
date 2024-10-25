package com.servion.dao;

import java.math.BigDecimal;

import javax.xml.datatype.XMLGregorianCalendar;

import com.servion.model.CallInfo;
import com.servion.model.fundsTransfer.UpdateFTRemittPayment_HostRes;
import com.servion.model.thirdPartyRemittance.TPR_UpdatePaymentDetails_HostRes;
import com.servion.ws.exception.DaoException;

public interface FundsTransferRemittDAO {

	/**
	 * @param xferID
	 * @param debitAmt
	 * @param creditAmt
	 * @param debitValueDate
	 * @param acctID
	 * @param accWithBank
	 * @param bankCode
	 * @param customerRate
	 * @param paymentDetails
	 * @param purposeCode
	 * @param txnCode
	 * @param fullName
	 * @param nostroBankName
	 * @param gsmNo
	 * @param benfCode
	 * @param benfCustomer
	 * @param purchaseBenfAcctNo
	 * @param benfLocation
	 * @param benfBranch
	 * @param benfAcctID
	 * @param ccyRate
	 * @return
	 * @throws DaoException
	 */
	
	UpdateFTRemittPayment_HostRes getFTTOBMUpdatePaymentOHostRes (CallInfo callInfo, String xferID, BigDecimal debitAmt, BigDecimal creditAmt, XMLGregorianCalendar debitValueDate, String acctID, 
			 String accWithBank, String bankCode, BigDecimal customerRate, String paymentDetails, String purposeCode, int txnCode,
			 String fullName, String nostroBankName, String gsmNo, String benfCode, String benfCustomer, String purchaseBenfAcctNo, 
			 String benfLocation, String benfBranch, String benfAcctID, BigDecimal ccyRate, String requestType) throws DaoException;

	/**
	 * @param xferID
	 * @param debitAmt
	 * @param creditAmt
	 * @param debitValueDate
	 * @param acctID
	 * @param accWithBank
	 * @param bankCode
	 * @param customerRate
	 * @param paymentDetails
	 * @param purposeCode
	 * @param txnCode
	 * @param fullName
	 * @param nostroBankName
	 * @param gsmNo
	 * @param benfCode
	 * @param benfCustomer
	 * @param purchaseBenfAcctNo
	 * @param benfLocation
	 * @param benfBranch
	 * @param benfAcctID
	 * @param ccyRate
	 * @return
	 * @throws DaoException
	 */
	
	TPR_UpdatePaymentDetails_HostRes getTPRUpdatePaymentHostRes (CallInfo callInfo, String xferID, BigDecimal debitAmt, BigDecimal creditAmt, XMLGregorianCalendar debitValueDate, String acctID, 
			 String accWithBank, String bankCode, BigDecimal customerRate, String paymentDetails, String purposeCode, int txnCode,
			 String fullName, String nostroBankName, String gsmNo, String benfCode, String benfCustomer, String purchaseBenfAcctNo, 
			 String benfLocation, String benfBranch, String benfAcctID, BigDecimal ccyRate, String requestType) throws DaoException;

}
