package com.servion.dao;

import java.math.BigDecimal;

import com.servion.model.CallInfo;
import com.servion.model.fundsTransfer.UpdateFTIntraPayment_HostRes;
import com.servion.ws.exception.DaoException;

public interface FundTransferIntraDAO {
	/**
	 * @param xferId
	 * @param benfCode
	 * @param debitAmt
	 * @param creditAmt
	 * @param acctID
	 * @param destAcctID
	 * @param serviceProviderID
	 * @param utilityID
	 * @param billID
	 * @param contractID
	 * @return
	 * @throws DaoException
	 */
	
	UpdateFTIntraPayment_HostRes getFTInterPaymentUpdHostRes(CallInfo callInfo, String xferId, String benfCode, BigDecimal debitAmt, BigDecimal creditAmt, String acctID,
			String destAcctID, String serviceProviderID, String utilityID, String billID, String contractID, String requestType) throws DaoException;

	
//	/**
//	 * @param xferId
//	 * @param benfCode
//	 * @param debitAmt
//	 * @param creditAmt
//	 * @param acctID
//	 * @param destAcctID
//	 * @param serviceProviderID
//	 * @param utilityID
//	 * @param billID
//	 * @param contractID
//	 * @return
//	 * @throws DaoException
//	 */
//	
//	UpdateFTIntraPayment_HostRes getFTTWBMPaymentUpdHostRes(CallInfo callInfo, String xferId, String benfCode, BigDecimal debitAmt, BigDecimal creditAmt, String acctID,
//			String destAcctID, String serviceProviderID, String utilityID, String billID, String contractID, String requestType) throws DaoException;

}
