package com.servion.dao;

import java.util.ArrayList;

import com.servion.model.CallInfo;
import com.servion.model.apinValidation.APINCustomerProfileDetails_HostRes;
import com.servion.model.creditCardBalance.CreditCardBalanceDetails_HostRes;
import com.servion.model.creditCardPayment.CreditCardDetails_HostRes;
import com.servion.ws.exception.DaoException;

public interface CCEntityInquiryDAO {
	/**
	 * 
	 * @param callInfo
	 * @param entyityInquiryType
	 * @param inquiryReference
	 * @param creditCardNum
	 * @param cardAccountNum
	 * @param customerID
	 * @param internalCustomerID
	 * @param nationalID
	 * @return
	 * @throws DaoException
	 */
	APINCustomerProfileDetails_HostRes getAPINValCustProfDetailsHostRes(CallInfo callInfo, String entyityInquiryType, String inquiryReference, ArrayList<String> numberList, String returnReplacedCards, String entityEnqSize, String requestType) throws DaoException;

	/**
	 * 
	 * @param callInfo
	 * @param entyityInquiryType
	 * @param inquiryReference
	 * @param creditCardNum
	 * @param cardAccountNum
	 * @param customerID
	 * @param internalCustomerID
	 * @param nationalID
	 * @return
	 * @throws DaoException
	 */
	CreditCardBalanceDetails_HostRes getCCBalanceHostRes(CallInfo callInfo, String entyityInquiryType, String inquiryReference, ArrayList<String> numberList, String returnReplacedCards, String entityEnqSize, String requestType)throws DaoException;
	
	/**
	 * 
	 * @param callInfo
	 * @param entyityInquiryType
	 * @param inquiryReference
	 * @param creditCardNum
	 * @param cardAccountNum
	 * @param customerID
	 * @param internalCustomerID
	 * @param nationalID
	 * @return
	 * @throws DaoException
	 */
	CreditCardDetails_HostRes getCCPaymentIntraCardDetailHostRes(CallInfo callInfo, String entyityInquiryType, String inquiryReference, ArrayList<String> numberList, String returnReplacedCards, String entityEnquirySize, String requestType)throws DaoException;
}
