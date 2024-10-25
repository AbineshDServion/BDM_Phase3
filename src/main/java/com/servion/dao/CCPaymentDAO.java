package com.servion.dao;

import com.servion.model.CallInfo;
import com.servion.model.creditCardPayment.UpdCCPaymentDetails_HostRes;
import com.servion.ws.exception.DaoException;

public interface CCPaymentDAO {
	/**
	 * @param debitAcctID
	 * @param beneficiaryRegCode
	 * @param creditCardNum
	 * @param amount
	 * @param creditCardAcctNo
	 * @return
	 * @throws DaoException
	 */
	
	UpdCCPaymentDetails_HostRes getCCPaymentUpdHostRes(CallInfo callInfo, String debitAcctID, String beneficiaryRegCode, String creditCardNum, String amount, String creditCardAcctNo, String requestType, String reasonCode, String text,
			String currencyType, String postTo, String transPostType, String paymentReference, String utilityCode, String serviceProviderCode) throws DaoException;

}
