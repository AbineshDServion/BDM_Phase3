package com.servion.dao;

import java.util.ArrayList;

import com.servion.model.CallInfo;
import com.servion.model.billPayment.Utility_BenfPayeeDetails_HostRes;
import com.servion.model.creditCardPayment.CCPayment_BenfPayeeDetails_HostRes;
import com.servion.model.fundsTransfer.FT_BenfPayeeDetails_HostRes;
import com.servion.model.thirdPartyRemittance.TPR_BenfPayeeDetails_HostRes;
import com.servion.ws.exception.DaoException;

public interface BeneficiaryDtlsInquiryDAO {
	
	/**
	 * 
	 * @param callInfo
	 * @param benefID
	 * @return
	 */
	FT_BenfPayeeDetails_HostRes getFTTWBMBenfDelsHostRes(CallInfo callInfo, ArrayList<String> benefIDList, String requestType)throws DaoException ;
	
	/**
	 * 
	 * @param callInfo
	 * @param benefID
	 * @return
	 */
	FT_BenfPayeeDetails_HostRes getFTTOBMBenfDelsHostRes(CallInfo callInfo, ArrayList<String> benefIDList, String requestType)throws DaoException;
	
	/**
	 * 
	 * @param callInfo
	 * @param benefID
	 * @return
	 */
	FT_BenfPayeeDetails_HostRes getFTCharityBenfDelsHostRes(CallInfo callInfo, ArrayList<String> benefIDList, String requestType)throws DaoException ;
	
	/**
	 * 
	 * @param callInfo
	 * @param benefList
	 * @return
	 */
	CCPayment_BenfPayeeDetails_HostRes getCCPaymentTPWBMBenfDelsHostRes(CallInfo callInfo, ArrayList<String> benefIDList, String requestType)throws DaoException ;
	
	
	/**
	 * 
	 * @param callInfo
	 * @param benefList
	 * @return
	 */
	Utility_BenfPayeeDetails_HostRes getMobBroadBandBenfDelsHostRes(CallInfo callInfo, ArrayList<String> benefIDList, String requestType)throws DaoException ;
	
	/**
	 * 
	 * @param callInfo
	 * @param benefList
	 * @return
	 */
	Utility_BenfPayeeDetails_HostRes getElecBillBenfDelsHostRes(CallInfo callInfo, ArrayList<String> benefIDList, String requestType)throws DaoException ;
	
	/**
	 * 
	 * @param callInfo
	 * @param benefList
	 * @return
	 */
	Utility_BenfPayeeDetails_HostRes getWaterBillBenfDelsHostRes(CallInfo callInfo, ArrayList<String> benefIDList, String requestType)throws DaoException ;
	
	/**
	 * 
	 * @param callInfo
	 * @param benefList
	 * @return
	 */
	Utility_BenfPayeeDetails_HostRes getSchoolBillBenfDelsHostRes(CallInfo callInfo, ArrayList<String> benefIDList, String requestType)throws DaoException ;
	
	/**
	 * 
	 * @param callInfo
	 * @param benefIDList
	 * @return
	 * @throws DaoException
	 */
	TPR_BenfPayeeDetails_HostRes getTPRBeneficiaryDetailsHostRes (CallInfo callInfo, ArrayList<String> benefIDList, String requestType)throws DaoException ;

}
