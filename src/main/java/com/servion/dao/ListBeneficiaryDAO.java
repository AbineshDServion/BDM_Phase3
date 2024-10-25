package com.servion.dao;

import com.servion.model.CallInfo;
import com.servion.model.billPayment.Utility_BeneficiaryDetailList_HostRes;
import com.servion.model.creditCardPayment.CCPayment_BeneficiaryDetailList_HostRes;
import com.servion.model.fundsTransfer.FT_BeneficiaryDetailList_HostRes;
import com.servion.model.thirdPartyRemittance.TPR_RetrieveBenfPayeeList_HostRes;
import com.servion.ws.exception.DaoException;

public interface ListBeneficiaryDAO {
	/**
	 * @param customerID
	 * @param paymentType,
	 * @return
	 * @throws DaoException
	 */
	FT_BeneficiaryDetailList_HostRes getFTTWBMBenfListHostRes(CallInfo callInfo, String customerID, String paymentType, String requestType)throws DaoException;

	/**
	 * @param customerID
	 * @param paymentType
	 * @return
	 * @throws DaoException
	 */
	FT_BeneficiaryDetailList_HostRes getFTTOBMBenfListHostRes(CallInfo callInfo, String customerID, String paymentType, String requestType)throws DaoException;

	/**
	 * @param customerID
	 * @param paymentType
	 * @return
	 * @throws DaoException
	 */
	FT_BeneficiaryDetailList_HostRes getFTCharityBenfListHostRes(CallInfo callInfo, String customerID, String paymentType, String requestType)throws DaoException;

	/**
	 * @param customerID
	 * @param paymentType
	 * @return
	 * @throws DaoException
	 */
	CCPayment_BeneficiaryDetailList_HostRes getCCPaymentTWBMBenfListHostRes(CallInfo callInfo, String customerID, String paymentType, String requestType)throws DaoException;

	/**
	 * @param customerID
	 * @param paymentType
	 * @return
	 * @throws DaoException
	 */
	Utility_BeneficiaryDetailList_HostRes getMobBroadBenfListHostRes(CallInfo callInfo, String customerID, String paymentType, String requestType, BeneficiaryDtlsInquiryDAO beneficiaryDtlsInquiryDAO)throws DaoException;

	/**
	 * @param customerID
	 * @param paymentType
	 * @return
	 * @throws DaoException
	 */
	Utility_BeneficiaryDetailList_HostRes getElecBillBenfListHostRes(CallInfo callInfo, String customerID, String paymentType, String requestType)throws DaoException;

	/**
	 * @param customerID
	 * @param paymentType
	 * @return
	 * @throws DaoException
	 */
	Utility_BeneficiaryDetailList_HostRes getWaterBillBenfListHostRes(CallInfo callInfo, String customerID, String paymentType, String requestType)throws DaoException;

	/**
	 * @param customerID
	 * @param paymentType
	 * @return
	 * @throws DaoException
	 */
	Utility_BeneficiaryDetailList_HostRes getSchoolBillBenfListHostRes(CallInfo callInfo, String customerID, String paymentType, String requestType)throws DaoException;

	/**
	 * 
	 * @param callInfo
	 * @param customerID
	 * @param paymentType
	 * @return
	 * @throws DaoException
	 */

	TPR_RetrieveBenfPayeeList_HostRes getTPRBeneficiaryPayeeList(CallInfo callInfo, String customerID, String paymentType, String requestType)throws DaoException;
}
