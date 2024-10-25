package com.servion.services;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

import com.reportbean.IvrData;
import com.servion.common.util.LoggerObject;
import com.servion.common.util.WriteLog;
import com.servion.common.util.util;
import com.servion.context.Context;
import com.servion.dao.BeneficiaryDtlsInquiryDAO;
import com.servion.dao.ExchngRateInqDAO;
import com.servion.dao.FundsTransferRemittDAO;
import com.servion.dao.ListBeneficiaryDAO;
import com.servion.exception.ServiceException;
import com.servion.ice.RuleEngine.ICEFeatureData;
import com.servion.ice.RuleEngine.ICEGlobalConfig;
import com.servion.model.AnncIDMap;
import com.servion.model.CallInfo;
import com.servion.model.Constants;
import com.servion.model.DynaPhraseConstants;
import com.servion.model.FeatureIDMap;
import com.servion.model.Field;
import com.servion.model.MenuIDMap;
import com.servion.model.callerIdentification.AcctInfo;
import com.servion.model.reporting.HostReportDetails;
import com.servion.model.thirdPartyRemittance.TPR_BeneficiaryDetails;
import com.servion.model.thirdPartyRemittance.TPR_BenfPayeeDetails_HostRes;
import com.servion.model.thirdPartyRemittance.TPR_ExchangeRateDetails_HostRes;
import com.servion.model.thirdPartyRemittance.TPR_ExchangeRateInquiryDtls;
import com.servion.model.thirdPartyRemittance.TPR_RetrieveBenfPayeeList_HostRes;
import com.servion.model.thirdPartyRemittance.TPR_UpdatePaymentDetails_HostRes;

public class ThirdPartyRemittanceImpl implements IThirdPartyRemittance{

	private static Logger logger = LoggerObject.getLogger();

	private ExchngRateInqDAO exchngRateInqDAO;
	private BeneficiaryDtlsInquiryDAO beneficiaryDtlsInquiryDAO;
	private ListBeneficiaryDAO listBeneficiaryDAO;
	private FundsTransferRemittDAO fundsTransferRemittDAO;

	public ExchngRateInqDAO getExchngRateInqDAO() {
		return exchngRateInqDAO;
	}

	public void setExchngRateInqDAO(ExchngRateInqDAO exchngRateInqDAO) {
		this.exchngRateInqDAO = exchngRateInqDAO;
	}

	public BeneficiaryDtlsInquiryDAO getBeneficiaryDtlsInquiryDAO() {
		return beneficiaryDtlsInquiryDAO;
	}

	public void setBeneficiaryDtlsInquiryDAO(
			BeneficiaryDtlsInquiryDAO beneficiaryDtlsInquiryDAO) {
		this.beneficiaryDtlsInquiryDAO = beneficiaryDtlsInquiryDAO;
	}

	public ListBeneficiaryDAO getListBeneficiaryDAO() {
		return listBeneficiaryDAO;
	}

	public void setListBeneficiaryDAO(ListBeneficiaryDAO listBeneficiaryDAO) {
		this.listBeneficiaryDAO = listBeneficiaryDAO;
	}

	public FundsTransferRemittDAO getFundsTransferRemittDAO() {
		return fundsTransferRemittDAO;
	}

	public void setFundsTransferRemittDAO(
			FundsTransferRemittDAO fundsTransferRemittDAO) {
		this.fundsTransferRemittDAO = fundsTransferRemittDAO;
	}


	private MessageSource messageSource;

	public MessageSource getMessageSource() {
		return messageSource;
	}

	@Autowired
	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	@Override
	public String getFundTransferRemitt(CallInfo callInfo) throws ServiceException {
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: ThirdPartyRemittanceImpl.getFundTransferRemitt()");}
		String code = Constants.EMPTY_STRING;

		try{

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
			ICEGlobalConfig ivr_ICEGlobalConfig = (ICEGlobalConfig) callInfo.getICEGlobalConfig();

			if(util.isNullOrEmpty(ivr_ICEGlobalConfig)){
				throw new ServiceException("ICEGlobalConfig object is null");
			}

			ICEFeatureData ivr_FeatureData  = (ICEFeatureData) callInfo.getICEFeatureData();

			if(util.isNullOrEmpty(ivr_FeatureData)){
				throw new ServiceException("ivr_FeatureData object is null");
			}




			/**
			 * Checking the condition for calling list beneficiary and beneficiary details host access 
			 */
			boolean isOTPCalledAfterDisconnected = util.isNullOrEmpty(callInfo.getField(Field.ISOTPCALLEDAFTERDISCONNECT)) ? false : (boolean)callInfo.getField(Field.ISOTPCALLEDAFTERDISCONNECT);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Is OTP Called After Disconnected " +isOTPCalledAfterDisconnected );}
			if(isOTPCalledAfterDisconnected){
				String returnCode = Constants.ONE;
				if(Constants.FEATURENAME_THIRDPARTYREMITTANCE.equalsIgnoreCase((String)callInfo.getField(Field.FEATURENAME))){
					if(!util.isNullOrEmpty(Context.getiThirdPartyRemittance())){
						returnCode = Context.getiThirdPartyRemittance().getTPRemittanceBeneficiaryList(callInfo);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Return code of List Beneficiary web service of ThirdpartyRemittance is " +returnCode );}

						if(Constants.ZERO.equalsIgnoreCase(returnCode)){
							returnCode = Context.getiThirdPartyRemittance().getTPRBeneficiaryDetails(callInfo);
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Return code of Beneficiary Details web service is " +returnCode );}

							if(Constants.ONE.equalsIgnoreCase(returnCode)){
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got failure response from beneficiary Details host access of ThirdpartyRemittance");}
								throw new ServiceException("Got failure response from beneficiary Detail host access");
							}

						}else{
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got failure response from list beneficiary host access");}
							throw new ServiceException("Got failure response from list beneficiary host access");
						}

					}else{
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Context.getFundTransferRemitt() is null or empty");}
						throw new ServiceException("Context.getiFundsTransferCharity() is null or empty");
					}
				}
			}

			//END 

			/**
			 * For Reporting Purpose
			 */
			HostReportDetails hostReportDetails = new HostReportDetails();

			String featureId = (String)callInfo.getField(Field.FEATUREID);
			hostReportDetails.setHostActiveMenu(featureId);
			//hostReportDetails.setHostCounter(hostCounter);
			//hostReportDetails.setHostEndTime(hostEndTime);


			String customerIDObbj = util.isNullOrEmpty(callInfo.getField(Field.CUSTOMERID))?Constants.NA : (String)callInfo.getField(Field.CUSTOMERID);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Customer ID going to insert in host is " + customerIDObbj);}
			String strHostInParam = Constants.NA;
			try{
				strHostInParam = 
						Constants.HOST_INPUT_PARAM_CUSTOMERID + Constants.EQUALTO + customerIDObbj + Constants.COMMA + Constants.HOST_INPUT_PARAM_SOURCE_NO + Constants.EQUALTO +util.maskCardOrAccountNumber((String)callInfo.getField(Field.SRCNO))
						+Constants.COMMA + Constants.HOST_INPUT_PARAM_DESTINATION_NO + Constants.EQUALTO + util.maskCardOrAccountNumber((String)callInfo.getField(Field.DESTNO))
						+Constants.COMMA + Constants.HOST_INPUT_PARAM_AMT + Constants.EQUALTO + callInfo.getField(Field.AMOUNT)
						+Constants.COMMA + Constants.HOST_INPUT_PARAM_CURRENCY_CODE + Constants.EQUALTO + callInfo.getField(Field.TPRSELECTEDCURRTYPE)
						+Constants.COMMA + Constants.HOST_INPUT_PARAM_EXCHANGE_RATE + Constants.EQUALTO + callInfo.getField(Field.EXCHANGE_RATES_VALUE)
						+Constants.COMMA + Constants.HOST_INPUT_PARAM_CONVERTED_AMOUNT + Constants.EQUALTO + callInfo.getField(Field.CONVERTEDAMT)
						+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBREQREFNUM +Constants.EQUALTO +( util.isNullOrEmpty(callInfo.getField(Field.ESBREQREFNUM)) ?"NA" :callInfo.getField(Field.ESBREQREFNUM)) ;
			}catch(Exception e){}

			hostReportDetails.setHostInParams(strHostInParam);
			hostReportDetails.setHostMethod(Constants.HOST_METHOD_FUNDTRANSFERREMITT);
			//hostReportDetails.setHostOutParams(hostOutParams);
			hostReportDetails.setHostReserve1(Constants.NO_RESPONSE_FROM_HOST);
			hostReportDetails.setHostReserve2(Constants.NO_RESPONSE_FROM_HOST);
			hostReportDetails.setHostReserve3(Constants.NO_RESPONSE_FROM_HOST);
			hostReportDetails.setHostReserve4(Constants.NO_RESPONSE_FROM_HOST);

			hostReportDetails.setHostStartTime(util.getCurrentDateTime()); //It should be in the format of 31/07/2013 18:11:11
			hostReportDetails.setHostType(Constants.HOST_TYPE_ACCOUNTSERVICES);
			//End Reporting

			/*
			 *  Setting NA values
			 */
			hostReportDetails.setHostEndTime(Constants.NA);
			hostReportDetails.setHostOutParams(Constants.NA);
			hostReportDetails.setHostResponse(Constants.NA);

			callInfo.setHostReportDetails(hostReportDetails);
			IvrData ivrdata = (IvrData)callInfo.getField(Field.IVRDATA);
			callInfo.insertHostDetails(ivrdata);

			/* END */

			String hostEndTime = Constants.EMPTY_STRING;
			String hostResCode = Constants.EMPTY_STRING;

			/**
			 * Following field are not mandatory and we need to pass those value as empty.
			 */
			String benfAcctID = (String)callInfo.getField(Field.DESTNO);
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "Beneficiary Destination number is " + util.maskCardOrAccountNumber(benfAcctID));}

			TPR_BeneficiaryDetails tpr_BeneficiaryDetails = null;

			if(!util.isNullOrEmpty(callInfo.getTPR_BenfPayeeDetails_HostRes())){
				if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "Received Beneficiary Details Object " + callInfo.getTPR_BenfPayeeDetails_HostRes());}
				if(!util.isNullOrEmpty(callInfo.getTPR_BenfPayeeDetails_HostRes().getTPR_BeneficiaryAcctDetailsMap())){
					tpr_BeneficiaryDetails = callInfo.getTPR_BenfPayeeDetails_HostRes().getTPR_BeneficiaryAcctDetailsMap().get(benfAcctID);
					if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "Received Beneficiary Account Details " + tpr_BeneficiaryDetails);}
				}
			}

			String xferID = Constants.EMPTY_STRING; //For a new transaction XferID should be empty - mentioned in the specification document
			String benfCode = tpr_BeneficiaryDetails!=null ?tpr_BeneficiaryDetails.getBeneficiaryID() : Constants.EMPTY_STRING;  // This might only applicable for ouside BM / Within BM transactions
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "Beneficiary ID is " + benfCode);}

			String accWithBank = Constants.EMPTY_STRING;
			String bankCode = Constants.EMPTY_STRING;

			String customerRate =  Constants.ZERO;
			BigDecimal bigCutomerRate = new BigDecimal(customerRate);

			String paymentDetails = Constants.EMPTY_STRING;
			String purposeCode = Constants.EMPTY_STRING;
			int txnCode = Constants.GL_ZERO;
			String fullName = Constants.EMPTY_STRING;
			String nostroBankName = Constants.EMPTY_STRING;
			String gsmNo = Constants.EMPTY_STRING;
			String benfCustomer = Constants.EMPTY_STRING;
			String purchaseBenfAcctNo =tpr_BeneficiaryDetails!=null ? tpr_BeneficiaryDetails.getBenefAccountNo() : Constants.EMPTY_STRING;
			String benfLocation = Constants.EMPTY_STRING;
			String benfBranch = Constants.EMPTY_STRING;

			String ccyRate = Constants.ZERO;
			BigDecimal bigCCYRate = new BigDecimal(ccyRate);

			XMLGregorianCalendar debitValueDate = util.getXMLGregorianCalendarNow();


			String selectedCurrType = util.isNullOrEmpty(callInfo.getField(Field.TPRSELECTEDCURRTYPE)) ? Constants.EMPTY_STRING : (String)callInfo.getField(Field.TPRSELECTEDCURRTYPE);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Selected TPR Currency type is "+selectedCurrType);}		


			//			String debitAmt = Constants.ZERO;
			//(String)callInfo.getField(Field.AMOUNT);
			//			double doubleDebitAmt = Double.parseDouble(debitAmt);
			//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Double of curr rate amount "+doubleDebitAmt);}


			//			double double_CurrRate = util.isNullOrEmpty(callInfo.getField(Field.EXCHANGE_RATES_VALUE)) ? Constants.GL_ZERO :  Double.parseDouble((String)callInfo.getField(Field.EXCHANGE_RATES_VALUE));
			//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Double of curr rate amount "+double_CurrRate);}

			//			double total = double_CurrRate * double_CurrRate;
			BigDecimal big_DebitAmt = new BigDecimal(Constants.GL_ZERO);
			BigDecimal big_CreditAmt = new BigDecimal(Constants.GL_ZERO) ;

			if(Constants.CURR_TYPE_OMR.equalsIgnoreCase(selectedCurrType)){
				String debitAmt  =  util.isNullOrEmpty(callInfo.getField(Field.AMOUNT)) ? Constants.ZERO : (String)callInfo.getField(Field.AMOUNT);  //Either we need to set Debit amt or credit amt ..here we are setting debit amount
				big_DebitAmt = new BigDecimal(debitAmt);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Big Decimal Amount "+big_DebitAmt);}
			}
			else{
				String creditAmt = util.isNullOrEmpty(callInfo.getField(Field.AMOUNT)) ? Constants.ZERO : (String)callInfo.getField(Field.AMOUNT);  //Either we need to set Debit amt or credit amt ..here we are setting debit amount
				big_CreditAmt = new BigDecimal(creditAmt) ;
				if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "Big credit amount" + big_CreditAmt);}
			}
			String acctID = (String)callInfo.getField(Field.SRCNO);
			String destAcctID = (String)callInfo.getField(Field.DESTNO);

			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "Source acct ending with " + util.getSubstring(acctID, Constants.GL_FOUR));}
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "Destination acct ending with "+ util.getSubstring(destAcctID, Constants.GL_FOUR));}

			String requestType = (String)ivr_FeatureData.getConfig().getParamValue(Constants.CUI_FUNDSTRANSFER_REQUESTTYPE);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Configured Funds Transfer Remittance service Request type is "+ requestType);}

			TPR_UpdatePaymentDetails_HostRes tpr_UpdatePaymentDetails_HostRes = fundsTransferRemittDAO.getTPRUpdatePaymentHostRes(callInfo, xferID, big_DebitAmt, big_CreditAmt, debitValueDate, 
					acctID, accWithBank, bankCode, bigCutomerRate, paymentDetails, purposeCode, txnCode, fullName, nostroBankName, gsmNo, benfCode, 
					benfCustomer, purchaseBenfAcctNo, benfLocation, benfBranch, benfAcctID, bigCCYRate, requestType);

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "ThirdPartyRemittanceImpl.getFundTransferRemitt Object is :"+ tpr_UpdatePaymentDetails_HostRes);}

			callInfo.setTPR_UpdatePaymentDetails_HostRes(tpr_UpdatePaymentDetails_HostRes);

			code = tpr_UpdatePaymentDetails_HostRes.getErrorCode();

			//Setting transactionRefNo
			callInfo.setField(Field.Transaction_Ref_No, tpr_UpdatePaymentDetails_HostRes.getXferID());

			hostEndTime = tpr_UpdatePaymentDetails_HostRes.getHostEndTime();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host End time is is " + hostEndTime);}
			hostReportDetails.setHostEndTime(hostEndTime);

			hostResCode = tpr_UpdatePaymentDetails_HostRes.getHostResponseCode();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host response code is " + hostResCode);}
			hostReportDetails.setHostResponse(hostResCode);

			String responseDesc = Constants.HOST_FAILURE;
			if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
				responseDesc = Constants.HOST_SUCCESS;
			}
			
			/****Duplicate RRN Fix 25012016 *****/
			try{
				strHostInParam = 
						Constants.HOST_INPUT_PARAM_CUSTOMERID + Constants.EQUALTO + customerIDObbj + Constants.COMMA + Constants.HOST_INPUT_PARAM_SOURCE_NO + Constants.EQUALTO +util.maskCardOrAccountNumber((String)callInfo.getField(Field.SRCNO))
						+Constants.COMMA + Constants.HOST_INPUT_PARAM_DESTINATION_NO + Constants.EQUALTO + util.maskCardOrAccountNumber((String)callInfo.getField(Field.DESTNO))
						+Constants.COMMA + Constants.HOST_INPUT_PARAM_AMT + Constants.EQUALTO + callInfo.getField(Field.AMOUNT)
						+Constants.COMMA + Constants.HOST_INPUT_PARAM_CURRENCY_CODE + Constants.EQUALTO + callInfo.getField(Field.TPRSELECTEDCURRTYPE)
						+Constants.COMMA + Constants.HOST_INPUT_PARAM_EXCHANGE_RATE + Constants.EQUALTO + callInfo.getField(Field.EXCHANGE_RATES_VALUE)
						+Constants.COMMA + Constants.HOST_INPUT_PARAM_CONVERTED_AMOUNT + Constants.EQUALTO + callInfo.getField(Field.CONVERTEDAMT)
						+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBREQREFNUM +Constants.EQUALTO +( util.isNullOrEmpty(callInfo.getField(Field.ESBREQREFNUM)) ?"NA" :callInfo.getField(Field.ESBREQREFNUM)) ;
			}catch(Exception e){}

			hostReportDetails.setHostInParams(strHostInParam);
			/************************************/
						
			String hostOutputParam = Constants.HOST_OUTPUT_PARAM_RESPONSEDESC + Constants.EQUALTO + responseDesc +Constants.COMMA + Constants.HOST_OUTPUT_PARAM_RESPONSECODE
					+ Constants.EQUALTO + hostResCode+Constants.COMMA+Constants.HOST_OUTPUT_PARAM_TRANSREFNO + Constants.EQUALTO+ tpr_UpdatePaymentDetails_HostRes.getXferID()
					+Constants.COMMA+Constants.HOST_OUTPUT_PARAM_AMTCREDITED + Constants.EQUALTO+ tpr_UpdatePaymentDetails_HostRes.getCreditedAmt()
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBERRORDESC +Constants.EQUALTO +(util.isNullOrEmpty(tpr_UpdatePaymentDetails_HostRes.getErrorDesc()) ?"NA" :tpr_UpdatePaymentDetails_HostRes.getErrorDesc());
			hostReportDetails.setHostOutParams(hostOutputParam);

			callInfo.setHostReportDetails(hostReportDetails);
			ivrdata = (IvrData)callInfo.getField(Field.IVRDATA);

			callInfo.updateHostDetails(ivrdata);


			/**
			 * Updating the isOTPCalledAfterDisconnect flag as false
			 */
			callInfo.setField(Field.ISOTPCALLEDAFTERDISCONNECT, false);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "updating Is OTP Called After Disconnected " +false );}
			//END


			if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Success for ThirdPartyRemittanceImpl.getFundTransferRemitt");}

				/**
				 * Following are handled for updating the enter amount after getting success from host
				 */
				try{
					IEnterAmount iEnterAmount = Context.getiEnterAmount();
					String result = iEnterAmount.UpdateEnteredAmount(callInfo);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The result of updating the entered amount result is "+ result);}
				}catch(Exception e){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Exception occured in while updating the entered amount result is "+ e);}
				}
				//End
			}else{
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got failure response for updateFTIntraPayment_HostRes service");}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The original response code of host access is " + tpr_UpdatePaymentDetails_HostRes.getHostResponseCode());}

				util.setHostErrorCodePhrase(callInfo, Constants.HOST_METHOD_FUNDTRANSFERINTRA, tpr_UpdatePaymentDetails_HostRes.getHostResponseCode());
				/**
				 * Following will be called only if there occurred account selection before this host access
				 */
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting the ErrorCodeAnotherAccount as Y");}
				util.setEligibleAccountCounts(callInfo, hostResCode);
			}

		}catch(Exception e){
			WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at ThirdPartyRemittanceImpl.getFundTransferRemitt() "+ e.getMessage());
			throw new ServiceException(e);
		}
		return code;
	}

	@Override
	public String getTPRemittanceBeneficiaryList(CallInfo callInfo)
			throws ServiceException {

		//		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		//		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: ThirdPartyRemittanceImpl.getTPRemittanceBeneficiaryList()");}   

		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: ThirdPartyRemittanceImpl.getTPRemittanceBeneficiaryList()");}
		String code = Constants.EMPTY_STRING;
		try{
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
			ICEGlobalConfig ivr_ICEGlobalConfig = (ICEGlobalConfig) callInfo.getICEGlobalConfig();

			if(util.isNullOrEmpty(ivr_ICEGlobalConfig)){
				throw new ServiceException("ICEGlobalConfig object is null");
			}

			String customerId = (String) callInfo.getField(Field.CUSTOMERID);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Requested Customer ID is "+customerId);}

			String paymentType = Constants.HOST_FT_PAYMENTTYPE_SPEEDTRANSFER;
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Requested paymentType is "+paymentType);}


			/**
			 * For Reporting Purpose
			 */
			HostReportDetails hostReportDetails = new HostReportDetails();

			String featureId = (String)callInfo.getField(Field.FEATUREID);
			hostReportDetails.setHostActiveMenu(featureId);
			//hostReportDetails.setHostCounter(hostCounter);
			//hostReportDetails.setHostEndTime(hostEndTime);

			String customerIDObbj = util.isNullOrEmpty(callInfo.getField(Field.CUSTOMERID))?Constants.NA : (String)callInfo.getField(Field.CUSTOMERID);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Customer ID going to insert in host is " + customerIDObbj);}


			String strHostInParam = Constants.HOST_INPUT_PARAM_CUSTOMERID + Constants.EQUALTO + customerIDObbj + Constants.COMMA +Constants.HOST_INPUT_PARAM_CUSTOMERID + Constants.EQUALTO + customerId + Constants.COMMA + Constants.HOST_INPUT_PARAM_PAYMENTTYPE + Constants.EQUALTO + 
					paymentType
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBREQREFNUM +Constants.EQUALTO +( util.isNullOrEmpty(callInfo.getField(Field.ESBREQREFNUM)) ?"NA" :callInfo.getField(Field.ESBREQREFNUM)) ;
			hostReportDetails.setHostInParams(strHostInParam);
			hostReportDetails.setHostMethod(Constants.HOST_METHOD_LISTBENEFICIARY);
			//hostReportDetails.setHostOutParams(hostOutParams);
			hostReportDetails.setHostReserve1(Constants.NO_RESPONSE_FROM_HOST);
			hostReportDetails.setHostReserve2(Constants.NO_RESPONSE_FROM_HOST);
			hostReportDetails.setHostReserve3(Constants.NO_RESPONSE_FROM_HOST);
			hostReportDetails.setHostReserve4(Constants.NO_RESPONSE_FROM_HOST);

			hostReportDetails.setHostStartTime(util.getCurrentDateTime()); //It should be in the formate of 31/07/2013 18:11:11
			hostReportDetails.setHostType(Constants.HOST_TYPE_ACCOUNTSERVICES);
			//End Reporting

			ICEFeatureData iceFeatureData = util.isNullOrEmpty(callInfo.getICEFeatureData())? null : (ICEFeatureData)callInfo.getICEFeatureData();

			if(util.isNullOrEmpty(iceFeatureData)){
				throw new ServiceException("iceFeatureData object is null or empty");
			}

			String requestType = util.isNullOrEmpty(iceFeatureData.getConfig().getParamValue(Constants.CUI_LISTBENEFICIARY_REQUESTTYPE))? null : (String)iceFeatureData.getConfig().getParamValue(Constants.CUI_LISTBENEFICIARY_REQUESTTYPE);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "requestType configured is " + requestType);}



			TPR_RetrieveBenfPayeeList_HostRes tpr_RetrieveBenfPayeeList_HostRes = listBeneficiaryDAO.getTPRBeneficiaryPayeeList(callInfo, customerId, paymentType,requestType);

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "FT_BeneficiaryDetailList_HostRes Object is :"+ tpr_RetrieveBenfPayeeList_HostRes);}
			callInfo.setTPR_RetrieveBenfPayeeList_HostRes(tpr_RetrieveBenfPayeeList_HostRes);

			code = tpr_RetrieveBenfPayeeList_HostRes.getErrorCode();


			/*
			 * For Reporting Start
			 */

			String hostEndTime = tpr_RetrieveBenfPayeeList_HostRes.getHostEndTime();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host End time is is " + hostEndTime);}
			hostReportDetails.setHostEndTime(hostEndTime);

			String hostResCode = tpr_RetrieveBenfPayeeList_HostRes.getHostResponseCode();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host response code is " + hostResCode);}
			hostReportDetails.setHostResponse(hostResCode);

			String responseDesc = Constants.HOST_FAILURE;
			if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
				responseDesc = Constants.HOST_SUCCESS;
			}
			
			
			/****Duplicate RRN Fix 25012016 *****/
			strHostInParam = Constants.HOST_INPUT_PARAM_CUSTOMERID + Constants.EQUALTO + customerIDObbj + Constants.COMMA +Constants.HOST_INPUT_PARAM_CUSTOMERID + Constants.EQUALTO + customerId + Constants.COMMA + Constants.HOST_INPUT_PARAM_PAYMENTTYPE + Constants.EQUALTO + 
					paymentType
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBREQREFNUM +Constants.EQUALTO +( util.isNullOrEmpty(callInfo.getField(Field.ESBREQREFNUM)) ?"NA" :callInfo.getField(Field.ESBREQREFNUM)) ;
			hostReportDetails.setHostInParams(strHostInParam);
			/************************************/
			
			String hostOutputParam = Constants.HOST_OUTPUT_PARAM_RESPONSEDESC + Constants.EQUALTO + responseDesc +Constants.COMMA + Constants.HOST_OUTPUT_PARAM_RESPONSECODE
					+ Constants.EQUALTO + hostResCode
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBERRORDESC +Constants.EQUALTO +(util.isNullOrEmpty(tpr_RetrieveBenfPayeeList_HostRes.getErrorDesc()) ?"NA" :tpr_RetrieveBenfPayeeList_HostRes.getErrorDesc());
			hostReportDetails.setHostOutParams(hostOutputParam);

			callInfo.setHostReportDetails(hostReportDetails);
			IvrData ivrdata = (IvrData)callInfo.getField(Field.IVRDATA);

			callInfo.insertHostDetails(ivrdata);
			//End Reporting

			if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got Success response for list beneficiary");}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The beneficiary id list object is " + tpr_RetrieveBenfPayeeList_HostRes.getBeneficiaryIdList());}
				if(!util.isNullOrEmpty(tpr_RetrieveBenfPayeeList_HostRes.getBeneficiaryIdList()))
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The total number of beneficiary id is :" + tpr_RetrieveBenfPayeeList_HostRes.getBeneficiaryIdList().size());}

			}else{
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got failure response for List Beneficiary host service");}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The original response code of host access is " + tpr_RetrieveBenfPayeeList_HostRes.getHostResponseCode());}

				util.setHostErrorCodePhrase(callInfo, Constants.HOST_METHOD_LISTBENEFICIARY, tpr_RetrieveBenfPayeeList_HostRes.getHostResponseCode());

			}

		}catch(Exception e){
			WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at  ThirdPartyRemittanceImpl.getTPRemittanceBeneficiaryList() "+ e.getMessage());
			throw new ServiceException(e);
		}
		return code;

	}

	public String getTPRBeneficiaryDetails(CallInfo callInfo) throws ServiceException{
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: ThirdPartyRemittanceImpl.getTPRBeneficiaryDetails()");}
		String code = Constants.EMPTY_STRING;
		try{
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Going to call Beneficiary Detail Enquiry Service");}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
			ICEGlobalConfig ivr_ICEGlobalConfig = (ICEGlobalConfig) callInfo.getICEGlobalConfig();

			if(util.isNullOrEmpty(ivr_ICEGlobalConfig)){
				throw new ServiceException("ICEGlobalConfig object is null");
			}

			String customerID = (String)callInfo.getField(Field.CUSTOMERID);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Customer id is "+ customerID);}

			if(util.isNullOrEmpty(customerID)){
				throw new ServiceException("customerID value is null");
			}

			ArrayList<String> beneficiaryIdList = null;
			if(!util.isNullOrEmpty(callInfo.getTPR_RetrieveBenfPayeeList_HostRes())){
				if(!util.isNullOrEmpty(callInfo.getTPR_RetrieveBenfPayeeList_HostRes().getBeneficiaryIdList())){
					beneficiaryIdList = callInfo.getTPR_RetrieveBenfPayeeList_HostRes().getBeneficiaryIdList();
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The beneficiary ID list received is " + beneficiaryIdList);}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The number of beneficiary id records are" + beneficiaryIdList.size());}

				}
			}
			if(!util.isNullOrEmpty(beneficiaryIdList)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Total Beneficiary ID available is " +beneficiaryIdList.size() );}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Available Beneficiaries are " +beneficiaryIdList);}

			}

			/**
			 * For Reporting Purpose
			 */
			HostReportDetails hostReportDetailsForSecHost = new HostReportDetails();

			String featureIdForSecHost = (String)callInfo.getField(Field.FEATUREID);
			hostReportDetailsForSecHost.setHostActiveMenu(featureIdForSecHost);
			String strHostInParamForSecHost = Constants.HOST_INPUT_PARAM_CUSTOMERID + Constants.EQUALTO + customerID
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ACCOUNTNUMBER + Constants.EQUALTO + Constants.NA
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBREQREFNUM +Constants.EQUALTO +( util.isNullOrEmpty(callInfo.getField(Field.ESBREQREFNUM)) ?"NA" :callInfo.getField(Field.ESBREQREFNUM)) ;
			hostReportDetailsForSecHost.setHostInParams(strHostInParamForSecHost);
			hostReportDetailsForSecHost.setHostMethod(Constants.HOST_METHOD_BENEFICARYDTLSINQUIRY);
			//hostReportDetailsForSecHost.setHostOutParams(hostOutParams);
			hostReportDetailsForSecHost.setHostReserve1(Constants.NA);
			hostReportDetailsForSecHost.setHostReserve2(Constants.NA);
			hostReportDetailsForSecHost.setHostReserve3(Constants.NA);
			hostReportDetailsForSecHost.setHostReserve4(Constants.NA);

			hostReportDetailsForSecHost.setHostStartTime(util.getCurrentDateTime()); //It should be in the formate of 31/07/2013 18:11:11
			hostReportDetailsForSecHost.setHostType(Constants.HOST_TYPE_ACCOUNTSERVICES);
			//End Reporting

			/*
			 *  Setting NA values
			 */
			hostReportDetailsForSecHost.setHostEndTime(util.getCurrentDateTime());
			hostReportDetailsForSecHost.setHostOutParams(Constants.NA);
			hostReportDetailsForSecHost.setHostResponse(Constants.NA);

			callInfo.setHostReportDetails(hostReportDetailsForSecHost);
			IvrData ivrdataForSecHost = (IvrData)callInfo.getField(Field.IVRDATA);
			callInfo.insertHostDetails(ivrdataForSecHost);

			/* END */

			ICEFeatureData iceFeatureData = util.isNullOrEmpty(callInfo.getICEFeatureData())? null : (ICEFeatureData)callInfo.getICEFeatureData();

			if(util.isNullOrEmpty(iceFeatureData)){
				throw new ServiceException("iceFeatureData object is null or empty");
			}

			String requestType = util.isNullOrEmpty(iceFeatureData.getConfig().getParamValue(Constants.CUI_BENEFICIARYDELSINQUIRY_REQUESTTYPE))? null : (String)iceFeatureData.getConfig().getParamValue(Constants.CUI_BENEFICIARYDELSINQUIRY_REQUESTTYPE);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "requestType configured is " + requestType);}



			TPR_BenfPayeeDetails_HostRes tpr_BenfPayeeDetails_HostRes = beneficiaryDtlsInquiryDAO.getTPRBeneficiaryDetailsHostRes(callInfo, beneficiaryIdList,requestType);

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "tpr_BenfPayeeDetails_HostRes Object is :"+ tpr_BenfPayeeDetails_HostRes);}
			callInfo.setTPR_BenfPayeeDetails_HostRes(tpr_BenfPayeeDetails_HostRes);
			code = tpr_BenfPayeeDetails_HostRes.getErrorCode();

			/*
			 * For Reporting Start
			 */
			String hostEndTimeForSecHost = tpr_BenfPayeeDetails_HostRes.getHostEndTime();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host End time is is " + hostEndTimeForSecHost);}
			hostReportDetailsForSecHost.setHostEndTime(hostEndTimeForSecHost);

			String hostResCodeForSecHost = tpr_BenfPayeeDetails_HostRes.getHostResponseCode();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host response code is " + hostResCodeForSecHost);}
			hostReportDetailsForSecHost.setHostResponse(hostResCodeForSecHost);

			String responseDescForSecHost = Constants.HOST_FAILURE;
			if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
				responseDescForSecHost = Constants.HOST_SUCCESS;
			}
			
			/****Duplicate RRN Fix 25012016 *****/
			strHostInParamForSecHost = Constants.HOST_INPUT_PARAM_CUSTOMERID + Constants.EQUALTO + customerID
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ACCOUNTNUMBER + Constants.EQUALTO + Constants.NA
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBREQREFNUM +Constants.EQUALTO +( util.isNullOrEmpty(callInfo.getField(Field.ESBREQREFNUM)) ?"NA" :callInfo.getField(Field.ESBREQREFNUM)) ;
			hostReportDetailsForSecHost.setHostInParams(strHostInParamForSecHost);
			/************************************/
			
			String hostOutputParamForSecHost = Constants.HOST_OUTPUT_PARAM_RESPONSEDESC + Constants.EQUALTO + responseDescForSecHost +Constants.COMMA + Constants.HOST_OUTPUT_PARAM_RESPONSECODE
					+ Constants.EQUALTO + hostResCodeForSecHost;

			hostReportDetailsForSecHost.setHostOutParams(hostOutputParamForSecHost);

			callInfo.setHostReportDetails(hostReportDetailsForSecHost);
			ivrdataForSecHost = (IvrData)callInfo.getField(Field.IVRDATA);

			callInfo.updateHostDetails(ivrdataForSecHost);
			//End Reporting

			if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got success response for Beneficiary Details");}
			}else{

				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got failure response for calleridentification host service");}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The original response code of host access is " + tpr_BenfPayeeDetails_HostRes.getHostResponseCode());}

				util.setHostErrorCodePhrase(callInfo, Constants.HOST_METHOD_BENEFICARYDTLSINQUIRY, tpr_BenfPayeeDetails_HostRes.getHostResponseCode());
			}

			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: ThirdPartyRemittanceImpl.getTPRBeneficiaryDetails()");}
		}catch(Exception e){
			WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at ThirdPartyRemittanceImpl.getTPRBeneficiaryDetails() "+ e.getMessage());
			throw new ServiceException(e);
		}
		return code;

	}

	@Override
	public String getTPRemittanceBeneficiaryPhrases(CallInfo callInfo)
			throws ServiceException {

		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: ThirdPartyRemittanceImpl.getTPRemittanceBeneficiaryPhrases()");}
		String str_GetMessage, finalResult;

		try{
			ArrayList<Object> dynamicValueArray = new ArrayList<Object>();

			//Need to get the FeatureConfig Data
			boolean isExistingBenefAvail = false;

			if(!util.isNullOrEmpty(callInfo.getTPR_RetrieveBenfPayeeList_HostRes ())){
				if(!util.isNullOrEmpty(callInfo.getTPR_RetrieveBenfPayeeList_HostRes ().getBeneficiaryIdList())){
					isExistingBenefAvail = callInfo.getTPR_RetrieveBenfPayeeList_HostRes ().getBeneficiaryIdList().size() > Constants.GL_ZERO;
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG, session_ID_,"Is there existing beneficiary available ? " + isExistingBenefAvail);}
				}
			}


			String grammar = Constants.EMPTY_STRING;
			boolean moreOption = false;


			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
			ICEFeatureData ivr_ICEFeatureData = (ICEFeatureData) callInfo.getICEFeatureData();

			if(util.isNullOrEmpty(ivr_ICEFeatureData)){
				throw new ServiceException("ivr_ICEFeatureData object is null");
			}

			//Defaultly considering the value as true
			/**
			 * Kindly note that all the data type that we have configured in the CUI will be considered as the String...though it was defined as boolean
			 */
			String str_isToAddNewBeneficiary = util.isNullOrEmpty(ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_EnableAddingNewBeneficiary))? Constants.TRUE :(String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_EnableAddingNewBeneficiary);
			boolean isToAddNewBeneficiary = Constants.TRUE.equalsIgnoreCase(str_isToAddNewBeneficiary)?true : false;
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Is to Add New Beneficary ? "+isToAddNewBeneficiary);}

			//Need to handle the Dynamic phrase list and Mannual Grammar portions
			if(isExistingBenefAvail){

				if(!isToAddNewBeneficiary){
					dynamicValueArray.add(DynaPhraseConstants.Funds_Transfer_1003);
					dynamicValueArray.add(DynaPhraseConstants.PHRASE_PRESS_1);
					dynamicValueArray.add(Constants.NA);
					dynamicValueArray.add(Constants.NA);
					grammar = Constants.FT_ADD_EXISTING_BENEFICIARY;
				}else{
					dynamicValueArray.add(DynaPhraseConstants.Funds_Transfer_1002);
					dynamicValueArray.add(DynaPhraseConstants.PHRASE_PRESS_1);
					dynamicValueArray.add(DynaPhraseConstants.Funds_Transfer_1003);
					dynamicValueArray.add(DynaPhraseConstants.PHRASE_PRESS_2);
					grammar = Constants.FT_ADD_NEW_BENEFICIARY + Constants.COMMA + Constants.FT_ADD_EXISTING_BENEFICIARY;
				}

			}else{
				if(isToAddNewBeneficiary){
					dynamicValueArray.add(DynaPhraseConstants.Funds_Transfer_1002);
					dynamicValueArray.add(DynaPhraseConstants.PHRASE_PRESS_1);
					grammar = Constants.FT_ADD_NEW_BENEFICIARY;
				}else{
					throw new ServiceException("There is no any registered or add beneficiary option for this feature");
				}
			}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG, session_ID_,"Generated dynamic phrase list is"+dynamicValueArray);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG, session_ID_,"Formed dynamic grammar for application layer is" + grammar);}

			String languageKey  = util.getLanguageKey((String) callInfo.getField(Field.LANGUAGE));
			Locale locale = util.getLocale((String) callInfo.getField(Field.LANGUAGE));

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Language Key value is"+ languageKey);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Locale selected is "+ locale);}

			String menuID = MenuIDMap.getMenuID("THIRDPARTY_REMITTANCE_BENEFICIARY");
			//String anncID = AnncIDMap.getAnncID("");
			String featureID = FeatureIDMap.getFeatureID("Third_Party_Remittance");
			String combinedKey = languageKey+Constants.UNDERSCORE+featureID+Constants.UNDERSCORE+menuID;

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Combined Key is "+ combinedKey);}

			Object[] object = new Object[dynamicValueArray.size()];
			for(int count=0; count<dynamicValueArray.size();count++){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Adding "+ count +"element: "+dynamicValueArray.get(count) +"into Object array ");}
				object[count] = dynamicValueArray.get(count);

			}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"objArray  is :" + object);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Default wave file  is :" + DynaPhraseConstants.SILENCE_PHRASE);}


			str_GetMessage =  this.messageSource.getMessage(combinedKey, object, DynaPhraseConstants.SILENCE_PHRASE, locale );
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The property value for the get Message method is " + str_GetMessage);}

			if(str_GetMessage.equalsIgnoreCase(DynaPhraseConstants.SILENCE_PHRASE)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Assigning Silence phrase as result");}
				return (DynaPhraseConstants.SILENCE_PHRASE);
			}

			int totalPrompt = util.getTotalPromptCount(str_GetMessage);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Total Prompt received from the dynaproperty file is "+totalPrompt);}


			//Overriding the total prompts, received from the property file
			if(isExistingBenefAvail){
				totalPrompt = Constants.GL_FOUR;
			}
			else{
				totalPrompt = Constants.GL_TWO;
			}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Total Prompt is "+totalPrompt);}

			//To have the property file grammar, need to call that util method here
			//No Need

			callInfo.setField(Field.DYNAMICLIST, grammar);
			callInfo.setField(Field.MOREOPTION, moreOption);

			String dynamicPhraseKey = util.getDynaPhraseKey(str_GetMessage);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The dynamic phrase key is "+dynamicPhraseKey);}
			String dynamicMessageValue = util.getDynaPhraseMessage(str_GetMessage);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The dynamic message value is"+dynamicMessageValue);}

			finalResult = util.callDynaPhraseGeneration(dynamicPhraseKey, dynamicMessageValue, totalPrompt);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The final string formation is :" + finalResult);}


			//Need to handle if we want to append pipeseperator sign
			//No Need
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, session_ID_,"EXIT: ThirdPartyRemittanceImpl.getTPRemittanceBeneficiaryPhrase()");}

		}catch(Exception e){
			throw new ServiceException(e);
		}
		return finalResult;
	}


	public String OLD_getTPRemittanceBeneficiaryPhrases(CallInfo callInfo)throws ServiceException {


		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: ThirdPartyRemittanceImpl.getTPRemittanceBeneficiaryPhrases()");}
		String str_GetMessage, finalResult;

		try{
			ArrayList<Object> dynamicValueArray = new ArrayList<Object>();

			//Need to get the FeatureConfig Data
			int noOfExistingBenef = Constants.GL_ZERO;

			if(!util.isNullOrEmpty(callInfo.getTPR_RetrieveBenfPayeeList_HostRes())){
				if(!util.isNullOrEmpty(callInfo.getTPR_RetrieveBenfPayeeList_HostRes().getBeneficiaryIdList())){
					noOfExistingBenef = callInfo.getTPR_RetrieveBenfPayeeList_HostRes().getBeneficiaryIdList().size();
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "No Of existing beneficiary available ? " + noOfExistingBenef);}
				}
			}


			String grammar = Constants.EMPTY_STRING;
			boolean moreOption = false;
			String subIndex =Constants.EMPTY_STRING;


			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
			ICEFeatureData ivr_ICEFeatureData = (ICEFeatureData) callInfo.getICEFeatureData();

			if(util.isNullOrEmpty(ivr_ICEFeatureData)){
				throw new ServiceException("ivr_ICEFeatureData object is null");
			}

			//Defaultly considering the value as true
			String str_isToAddNewBeneficiary = util.isNullOrEmpty(ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_EnableAddingNewBeneficiary))? Constants.TRUE :(String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_EnableAddingNewBeneficiary);
			boolean isToAddNewBeneficiary = Constants.TRUE.equalsIgnoreCase(str_isToAddNewBeneficiary)?true : false;
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Is to Add New Beneficary ? "+isToAddNewBeneficiary);}



			//Need to handle the Dynamic phrase list and Mannual Grammar portions
			if(noOfExistingBenef == Constants.GL_ONE){

				//Calling the beneficiary Details host service to get all beneficiary details of the available utility type beneficiary ids
				HashMap<String, TPR_BeneficiaryDetails> beneficiaryDetailMap = null;
				String beneficiaryDetlHostCode = getTPRBeneficiaryDetails(callInfo);

				if(Constants.ONE.equalsIgnoreCase(beneficiaryDetlHostCode)){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Failed while calling Beneficiary details host service");}
					throw new ServiceException("FT_BenfPayeeDetails_HostRes object is null / Empty");
				}

				if(!util.isNullOrEmpty(callInfo.getTPR_BenfPayeeDetails_HostRes())){
					if(!util.isNullOrEmpty(callInfo.getTPR_BenfPayeeDetails_HostRes().getTPR_BeneficiaryDetailsMap())){
						beneficiaryDetailMap = callInfo.getTPR_BenfPayeeDetails_HostRes().getTPR_BeneficiaryDetailsMap();
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Beneficiary Detail map retrieved from host is "  + beneficiaryDetailMap);}
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Total no Of Beneficiary details  available is"  + beneficiaryDetailMap.size());}
					}else{
						throw new ServiceException("beneficiaryDetailMap object is null / Empty");
					}
				}else{
					throw new ServiceException("TPR_BenfPayeeDetails_HostRes  object is null / Empty");
				}

				String beneficiaryID = Constants.EMPTY_STRING;
				if(callInfo.getTPR_RetrieveBenfPayeeList_HostRes()!=null){
					if(callInfo.getTPR_RetrieveBenfPayeeList_HostRes().getBeneficiaryIdList() != null){
						beneficiaryID = callInfo.getTPR_RetrieveBenfPayeeList_HostRes().getBeneficiaryIdList().get(Constants.GL_ZERO);		
					}
				}


				TPR_BeneficiaryDetails TPR_BeneficiaryDetails =	beneficiaryDetailMap.get(beneficiaryID);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Single account present is "  + util.maskCardOrAccountNumber(TPR_BeneficiaryDetails.getBenefAccountNo()));}


				if(isToAddNewBeneficiary){
					dynamicValueArray.add(DynaPhraseConstants.Funds_Transfer_1002);
					dynamicValueArray.add(DynaPhraseConstants.PHRASE_PRESS_1);
					dynamicValueArray.add(DynaPhraseConstants.Funds_Transfer_1004);
					dynamicValueArray.add(TPR_BeneficiaryDetails.getBenefAccountNo());
					dynamicValueArray.add(DynaPhraseConstants.PHRASE_PRESS_2);
					grammar = Constants.FT_ADD_NEW_BENEFICIARY + Constants.COMMA + Constants.FT_ADD_EXISTING_BENEFICIARY;


				}else{

					dynamicValueArray.add(Constants.NA);
					dynamicValueArray.add(Constants.NA);
					dynamicValueArray.add(DynaPhraseConstants.Funds_Transfer_1004);
					dynamicValueArray.add(TPR_BeneficiaryDetails.getBenefAccountNo());
					dynamicValueArray.add(DynaPhraseConstants.PHRASE_PRESS_1);
					grammar = Constants.FT_ADD_EXISTING_BENEFICIARY;

				}

				callInfo.setField(Field.DESTNO, TPR_BeneficiaryDetails.getBenefAccountNo());
			}else if(noOfExistingBenef > Constants.GL_ONE){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "No of Existing beneficiary is > 1"  + noOfExistingBenef);}

				if(isToAddNewBeneficiary){
					dynamicValueArray.add(DynaPhraseConstants.Funds_Transfer_1002);
					dynamicValueArray.add(DynaPhraseConstants.PHRASE_PRESS_1);
					dynamicValueArray.add(DynaPhraseConstants.Funds_Transfer_1003);
					dynamicValueArray.add(Constants.EMPTY_STRING);
					dynamicValueArray.add(DynaPhraseConstants.PHRASE_PRESS_2);
					grammar = Constants.FT_ADD_NEW_BENEFICIARY + Constants.COMMA + Constants.FT_OBM_TRANSFER_BENEFICIARY;

				}else{
					dynamicValueArray.add(Constants.NA);
					dynamicValueArray.add(Constants.NA);
					dynamicValueArray.add(DynaPhraseConstants.Funds_Transfer_1003);
					dynamicValueArray.add(Constants.EMPTY_STRING);
					dynamicValueArray.add(DynaPhraseConstants.PHRASE_PRESS_1);
					grammar = Constants.FT_OBM_TRANSFER_BENEFICIARY;
				}
			}else{
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "There is no Existing beneficiary is"  + noOfExistingBenef);}


				if(isToAddNewBeneficiary){
					dynamicValueArray.add(DynaPhraseConstants.Funds_Transfer_1002);
					dynamicValueArray.add(DynaPhraseConstants.PHRASE_PRESS_1);
					dynamicValueArray.add(Constants.NA);
					dynamicValueArray.add(Constants.NA);
					dynamicValueArray.add(Constants.NA);
					grammar = Constants.FT_ADD_NEW_BENEFICIARY;

				}else{

					throw new ServiceException("There is no any option for adding or for existing beneficiary");
					//					dynamicValueArray.add(DynaPhraseConstants.Funds_Transfer_1002);
					//					dynamicValueArray.add(DynaPhraseConstants.PHRASE_PRESS_1);
					//					dynamicValueArray.add(Constants.NA);
					//					dynamicValueArray.add(Constants.NA);
					//					dynamicValueArray.add(Constants.NA);
					//					grammar = Constants.FT_ADD_NEW_BENEFICIARY;
				}

			}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Generated dynamic phrase list is"+dynamicValueArray);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Formed dynamic grammar for application layer is" + grammar);}

			String languageKey  = util.getLanguageKey((String) callInfo.getField(Field.LANGUAGE));
			Locale locale = util.getLocale((String) callInfo.getField(Field.LANGUAGE));

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Language Key value is"+ languageKey);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Locale selected is "+ locale);}

			String menuID = MenuIDMap.getMenuID("THIRDPARTY_REMITTANCE_BENEFICIARY");
			//String anncID = AnncIDMap.getAnncID("");
			String featureID = FeatureIDMap.getFeatureID("Third_Party_Remittance");
			String combinedKey = languageKey+Constants.UNDERSCORE+featureID+Constants.UNDERSCORE+menuID;
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Combined Key is "+ combinedKey);}

			Object[] object = new Object[dynamicValueArray.size()];
			for(int count=0; count<dynamicValueArray.size();count++){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Adding "+ count +"element: "+dynamicValueArray.get(count) +"into Object array ");}
				object[count] = dynamicValueArray.get(count);

			}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"objArray  is :" + object);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Default wave file  is :" + DynaPhraseConstants.SILENCE_PHRASE);}


			str_GetMessage =  this.messageSource.getMessage(combinedKey, object, DynaPhraseConstants.SILENCE_PHRASE, locale );
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The property value for the get Message method is " + str_GetMessage);}

			if(str_GetMessage.equalsIgnoreCase(DynaPhraseConstants.SILENCE_PHRASE)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Assigning Silence phrase as result");}
				return (DynaPhraseConstants.SILENCE_PHRASE);
			}

			int totalPrompt = util.getTotalPromptCount(str_GetMessage);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Total Prompt received from the dynaproperty file is "+totalPrompt);}


			//Overriding the total prompts, received from the property file
			/*if(noOfExistingBenef == Constants.GL_ONE){
				totalPrompt = Constants.GL_FIVE;
			}
			else{
				totalPrompt = Constants.GL_FOUR;
			}*/

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Total Prompt is "+totalPrompt);}

			//To have the property file grammar, need to call that util method here
			//No Need

			callInfo.setField(Field.DYNAMICLIST, grammar);
			callInfo.setField(Field.MOREOPTION, moreOption);

			String dynamicPhraseKey = util.getDynaPhraseKey(str_GetMessage);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The dynamic phrase key is "+dynamicPhraseKey);}
			String dynamicMessageValue = util.getDynaPhraseMessage(str_GetMessage);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The dynamic message value is"+dynamicMessageValue);}

			finalResult = util.callDynaPhraseGeneration(dynamicPhraseKey, dynamicMessageValue, totalPrompt);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The final string formation is :" + finalResult);}


			//Need to handle if we want to append pipe seperator sign
			//No Need
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: ThirdPartyRemittanceImpl.getTPRemittanceBeneficiaryPhrases()");}

		}catch(Exception e){
			WriteLog.write(WriteLog.INFO,session_ID_, "ERROR: ThirdPartyRemittanceImpl.getTPRemittanceBeneficiaryPhrases()");
			throw new ServiceException(e);
		}
		return finalResult;
	}

	@Override
	public String getTPRemittanceConfirmationPhrases(CallInfo callInfo)
			throws ServiceException {

		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: ThirdPartyRemittanceImpl.getTPRemittanceConfirmationPhrases()");}
		String str_GetMessage, finalResult;

		try{
			ArrayList<Object> dynamicValueArray = new ArrayList<Object>();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
			ICEFeatureData ivr_ICEFeatureData = (ICEFeatureData) callInfo.getICEFeatureData();

			//Need to get the FeatureConfig Data
			String lastNDigits = (String)callInfo.getField(Field.LastNDigits);
			int int_LastNDigit = util.isNullOrEmpty(lastNDigits)?Constants.GL_ZERO:Integer.parseInt(lastNDigits);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Configured last N digit length for announcement is "+ int_LastNDigit);}

			String amount = (String)callInfo.getField(Field.AMOUNT);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The selected amount is "+ amount);}

			String destNumber = (String)callInfo.getField(Field.DESTNO);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The selected destination number ending with "+  util.getSubstring(destNumber, int_LastNDigit));}


			/**
			 * Following are the fixes done for TPR issue raised after disconnected and called againg for otp validation issue
			 */
			boolean isOTPCalledAfterDisconnected = util.isNullOrEmpty(callInfo.getField(Field.ISOTPCALLEDAFTERDISCONNECT)) ? false : (boolean)callInfo.getField(Field.ISOTPCALLEDAFTERDISCONNECT);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Is OTP Called After Disconnected " +isOTPCalledAfterDisconnected );}
			//END Vinoth


			String descCurr = (String)callInfo.getField(Field.TPRSELECTEDCURRTYPE);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Desctination currency type is "+  descCurr);}

			String sourceNumber = (String)callInfo.getField(Field.SRCNO);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The selected Source number ending with "+ util.getSubstring(sourceNumber, int_LastNDigit));}

			String srcCurr = Constants.CURR_TYPE_OMR;
			AcctInfo acctInfo = null;
			if(!util.isNullOrEmpty(callInfo.getCallerIdentification_HostRes())){
				if(!util.isNullOrEmpty(callInfo.getCallerIdentification_HostRes().getAccountDetailMap())){
					acctInfo = callInfo.getCallerIdentification_HostRes().getAccountDetailMap().get(sourceNumber);
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got source account info object ");}
					if(!util.isNullOrEmpty(acctInfo)){
						srcCurr = acctInfo.getAcctCurr();
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The source account currency is "+ srcCurr);}
					}
				}
			}



			/**
			 * Getting the customer category type from the account detail map object
			 */
			String customerCategoryType = Constants.DEFAULT;
			if(!util.isNullOrEmpty(callInfo.getCallerIdentification_HostRes())){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "getCallerIdentification_HostRes is not empty or null ");}

				if(!util.isNullOrEmpty(callInfo.getCallerIdentification_HostRes().getAccountDetailMap())){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Account Detail Map object is null or empty");}

					HashMap<String, AcctInfo>acctDetailMap = callInfo.getCallerIdentification_HostRes().getAccountDetailMap();
					if(!util.isNullOrEmpty(sourceNumber) && !util.isNullOrEmpty(acctDetailMap.get(sourceNumber))){

						customerCategoryType = acctDetailMap.get(sourceNumber).getCategory();
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Category type is "+ customerCategoryType);}

						if(util.isNullOrEmpty(customerCategoryType) || util.isNullOrEmpty(ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_TransactionFee + Constants.UNDERSCORE+ customerCategoryType))){
							customerCategoryType = Constants.DEFAULT;
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Category type is resetted to Default"+ customerCategoryType);}	
						}
					}
				}

			}


			String transactionFee = util.isNullOrEmpty(ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_TransactionFee + Constants.UNDERSCORE+ customerCategoryType))?
					Constants.ZERO : (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_TransactionFee + Constants.UNDERSCORE+ customerCategoryType);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Configured UBP Transaction fee amount is "+transactionFee);}

			//			String transactionFee = (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_TransactionFee);
			callInfo.setField(Field.TransactionFee, transactionFee);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Configured Transaction fee amount is "+transactionFee);}

			double double_TransFee = util.isNullOrEmpty(transactionFee)?Constants.GL_ZERO : Double.parseDouble(transactionFee);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Transaction fee converted to double  is "+double_TransFee);}

			String featureName = (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.FEATURENAME);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Feature name is "+featureName);}

			String moreCount = (String)callInfo.getField(Field.MORECOUNT);
			int int_moreCount = util.isNullOrEmpty(moreCount)?Constants.GL_FIVE:Integer.parseInt(moreCount);

			String grammar = Constants.EMPTY_STRING;
			boolean moreOption = false;



			//Need to handle the Dynamic phrase list and Mannual Grammar portions
			dynamicValueArray.add(descCurr+Constants.WAV_EXTENSION);
			dynamicValueArray.add(srcCurr+Constants.WAV_EXTENSION);


			String currencyRate = Constants.EMPTY_STRING;

			if(isOTPCalledAfterDisconnected){
				currencyRate = util.isNullOrEmpty(callInfo.getField(Field.EXCHANGE_RATES_VALUE))?Constants.ZERO :(String)callInfo.getField(Field.EXCHANGE_RATES_VALUE);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Currency Exchange rate value is "+currencyRate);}
				dynamicValueArray.add(currencyRate);
			}else{
				if(callInfo.getTPR_ExchangeRateDetails_HostRes() != null){
					if(callInfo.getTPR_ExchangeRateDetails_HostRes().getTpr_ExchangeRateCurrMap() != null){
						TPR_ExchangeRateInquiryDtls exchangeRateInquiryDtls = callInfo.getTPR_ExchangeRateDetails_HostRes().getTpr_ExchangeRateCurrMap().get(descCurr);
						currencyRate = exchangeRateInquiryDtls.getSellRate();
						dynamicValueArray.add(currencyRate);

						//setting the value in the callinfo Field
						callInfo.setField(Field.EXCHANGE_RATES_VALUE, currencyRate);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Currency Exchange rate value is "+currencyRate);}
					}else{
						throw new ServiceException("Exchange rate is not available");
					}
				}

			}


			dynamicValueArray.add(amount);
			dynamicValueArray.add(descCurr+Constants.WAV_EXTENSION);



			double double_Amt = Double.parseDouble(amount);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Double of source amount "+double_Amt);}

			double double_CurrRate = util.isNullOrEmpty(currencyRate) ? Constants.GL_ZERO :  Double.parseDouble(currencyRate);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Double of curr rate amount "+double_CurrRate);}

			double total = double_Amt * double_CurrRate;
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "combined amount is "+total);}

			DecimalFormat twoDForm = new DecimalFormat("#.####");
			double dformat1 =  Double.valueOf(twoDForm.format(total));
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The converted decimal format for amount is "+dformat1);}

			dynamicValueArray.add(String.format(dformat1 + Constants.EMPTY));

			/**
			 * Following are the fixes made to avoid playing of OMR
			 */
			//			dynamicValueArray.add(srcCurr+Constants.WAV_EXTENSION);
			dynamicValueArray.add(Constants.NA);

			if(!util.isNullOrEmpty(double_TransFee) && double_TransFee > Constants.GL_ZERO){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "including transaction fee also for confirmation announcements");}
				dynamicValueArray.add(DynaPhraseConstants.PHRASE_CHARGING_TRANSFEE); 
				dynamicValueArray.add(transactionFee); 
			}else{
				dynamicValueArray.add(DynaPhraseConstants.SILENCE_PHRASE);
				dynamicValueArray.add(Constants.EMPTY);
			}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Generated dynamic phrase list is"+dynamicValueArray);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Formed dynamic grammar for application layer is" + grammar);}

			String languageKey  = util.getLanguageKey((String) callInfo.getField(Field.LANGUAGE));
			Locale locale = util.getLocale((String) callInfo.getField(Field.LANGUAGE));

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Language Key value is"+ languageKey);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Locale selected is "+ locale);}

			String menuID = MenuIDMap.getMenuID("THIRDPARTY_REMITTANCE_CONFIRMATION");
			//String anncID = AnncIDMap.getAnncID("");
			String featureID = FeatureIDMap.getFeatureID("Third_Party_Remittance");
			String combinedKey = languageKey+Constants.UNDERSCORE+featureID+Constants.UNDERSCORE+menuID;

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Combined Key is "+ combinedKey);}

			Object[] object = new Object[dynamicValueArray.size()];
			for(int count=0; count<dynamicValueArray.size();count++){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Adding "+ count +"element: "+dynamicValueArray.get(count) +"into Object array ");}
				object[count] = dynamicValueArray.get(count);

			}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"objArray  is :" + object);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Default wave file  is :" + DynaPhraseConstants.SILENCE_PHRASE);}


			str_GetMessage =  this.messageSource.getMessage(combinedKey, object, DynaPhraseConstants.SILENCE_PHRASE, locale );
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The property value for the get Message method is " + str_GetMessage);}

			if(str_GetMessage.equalsIgnoreCase(DynaPhraseConstants.SILENCE_PHRASE)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Assigning Silence phrase as result");}
				return (DynaPhraseConstants.SILENCE_PHRASE);
			}

			int totalPrompt = util.getTotalPromptCount(str_GetMessage);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Total Prompt received from the dynaproperty file is "+totalPrompt);}


			//Overriding the total prompts, received from the property file

			//To have the property file grammar, need to call that util method here


			callInfo.setField(Field.DYNAMICLIST, grammar);
			callInfo.setField(Field.MOREOPTION, moreOption);

			String dynamicPhraseKey = util.getDynaPhraseKey(str_GetMessage);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The dynamic phrase key is "+dynamicPhraseKey);}
			String dynamicMessageValue = util.getDynaPhraseMessage(str_GetMessage);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The dynamic message value is"+dynamicMessageValue);}

			finalResult = util.callDynaPhraseGeneration(dynamicPhraseKey, dynamicMessageValue, totalPrompt);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The final string formation is :" + finalResult);}


			//Need to handle if we want to append pipe seperator sign

			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: ThirdPartyRemittanceImpl.getTPRemittanceConfirmationPhrases()");}

		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.ERROR,session_ID_,"There was an error at ThirdPartyRemittanceImpl.getTPRemittanceConfirmationPhrases() "	+ e.getMessage());}
			throw new ServiceException(e);
		}
		return finalResult;
	}

	@Override
	public String getTPRemittanceExchangeRateInq(CallInfo callInfo)
			throws ServiceException {
		// TODO Auto-generated method stub
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: ThirdPartyRemittanceImpl.getTPRemittanceExchangeRateInq()");}   
		String code = Constants.EMPTY_STRING;
		//getConfigurationParam(callInfo);
		try{

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
			ICEGlobalConfig ivr_ICEGlobalConfig = (ICEGlobalConfig) callInfo.getICEGlobalConfig();

			if(util.isNullOrEmpty(ivr_ICEGlobalConfig)){
				throw new ServiceException("ICEGlobalConfig object is null");
			}

			/**
			 * If the currency code value is empty the we will receive all the exchange rate of all currency
			 */

			String currencyCode = Constants.EMPTY_STRING;
			if(!util.isNullOrEmpty(callInfo.getField(Field.TPRSELECTEDCURRTYPE))){
				currencyCode=(String)callInfo.getField(Field.TPRSELECTEDCURRTYPE);

			}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The source account currency is " + currencyCode);}

			String customerID = (String)callInfo.getField(Field.CUSTOMERID);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The customer id is " + customerID);}

			String ccyMarket = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_EXCHANGERATE_CCYMARKET);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Configured CCY Market value is " + ccyMarket);}

			/**
			 * For Reporting Purpose
			 */
			HostReportDetails hostReportDetails = new HostReportDetails();

			String featureId = (String)callInfo.getField(Field.FEATUREID);
			hostReportDetails.setHostActiveMenu(featureId);
			//hostReportDetails.setHostCounter(hostCounter);
			//hostReportDetails.setHostEndTime(hostEndTime);
			String customerIDObbj = util.isNullOrEmpty(callInfo.getField(Field.CUSTOMERID))?Constants.NA : (String)callInfo.getField(Field.CUSTOMERID);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Customer ID going to insert in host is " + customerIDObbj);}

			String strHostInParam = Constants.HOST_INPUT_PARAM_CUSTOMERID + Constants.EQUALTO + customerIDObbj + Constants.COMMA + Constants.HOST_INPUT_PARAM_CURRENCYCODE + Constants.EQUALTO + currencyCode
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBREQREFNUM +Constants.EQUALTO +( util.isNullOrEmpty(callInfo.getField(Field.ESBREQREFNUM)) ?"NA" :callInfo.getField(Field.ESBREQREFNUM)) ;
			hostReportDetails.setHostInParams(strHostInParam);
			hostReportDetails.setHostMethod(Constants.HOST_METHOD_EXCHNGRATEINQ);
			//hostReportDetails.setHostOutParams(hostOutParams);
			hostReportDetails.setHostReserve1(Constants.NO_RESPONSE_FROM_HOST);
			hostReportDetails.setHostReserve2(Constants.NO_RESPONSE_FROM_HOST);
			hostReportDetails.setHostReserve3(Constants.NO_RESPONSE_FROM_HOST);
			hostReportDetails.setHostReserve4(Constants.NO_RESPONSE_FROM_HOST);

			hostReportDetails.setHostStartTime(util.getCurrentDateTime()); //It should be in the format of 31/07/2013 18:11:11
			hostReportDetails.setHostType(Constants.HOST_TYPE_ACCOUNTSERVICES);
			//End Reporting

			/*
			 *  Setting NA values
			 */
			hostReportDetails.setHostEndTime(Constants.NA);
			hostReportDetails.setHostOutParams(Constants.NA);
			hostReportDetails.setHostResponse(Constants.NA);

			callInfo.setHostReportDetails(hostReportDetails);
			IvrData ivrdata = (IvrData)callInfo.getField(Field.IVRDATA);
			callInfo.insertHostDetails(ivrdata);

			/* END */

			ICEFeatureData iceFeatureData = util.isNullOrEmpty(callInfo.getICEFeatureData())? null : (ICEFeatureData)callInfo.getICEFeatureData();

			if(util.isNullOrEmpty(iceFeatureData)){
				throw new ServiceException("iceFeatureData object is null or empty");
			}

			String requestType = util.isNullOrEmpty(iceFeatureData.getConfig().getParamValue(Constants.CUI_EXCHANGERATEINQ_REQUESTTYPE))? null : (String)iceFeatureData.getConfig().getParamValue(Constants.CUI_EXCHANGERATEINQ_REQUESTTYPE);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "requestType configured is " + requestType);}


			TPR_ExchangeRateDetails_HostRes tpr_ExchangeRateInquiry_HostRes = exchngRateInqDAO.getTPRemittanceExchangeHostRes(callInfo, currencyCode, customerID, ccyMarket, requestType);

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "tpr_ExchangeRateInquiry_HostRes Object is :"+ tpr_ExchangeRateInquiry_HostRes);}
			callInfo.setTPR_ExchangeRateDetails_HostRes(tpr_ExchangeRateInquiry_HostRes);

			code = tpr_ExchangeRateInquiry_HostRes.getErrorCode();

			/*
			 * For Reporting Start
			 */

			String hostEndTime = tpr_ExchangeRateInquiry_HostRes.getHostEndTime();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host End time is is " + hostEndTime);}
			hostReportDetails.setHostEndTime(hostEndTime);

			String hostResCode = tpr_ExchangeRateInquiry_HostRes.getHostResponseCode();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host response code is " + hostResCode);}
			hostReportDetails.setHostResponse(hostResCode);

			String responseDesc = Constants.HOST_FAILURE;
			if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
				responseDesc = Constants.HOST_SUCCESS;

				String currencyRate = Constants.EMPTY_STRING;
				if(callInfo.getTPR_ExchangeRateDetails_HostRes() != null){
					if(callInfo.getTPR_ExchangeRateDetails_HostRes().getTpr_ExchangeRateCurrMap() != null){
						TPR_ExchangeRateInquiryDtls exchangeRateInquiryDtls = callInfo.getTPR_ExchangeRateDetails_HostRes().getTpr_ExchangeRateCurrMap().get(currencyCode);
						currencyRate = exchangeRateInquiryDtls.getSellRate();
						//setting the value in the callinfo Field
						callInfo.setField(Field.EXCHANGE_RATES_VALUE, currencyRate);
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Currency rate value is "+currencyRate);}
					}else{
						throw new ServiceException("Exchange rate is not available");
					}
				}


			}
			
			/****Duplicate RRN Fix 25012016 *****/
			strHostInParam = Constants.HOST_INPUT_PARAM_CUSTOMERID + Constants.EQUALTO + customerIDObbj + Constants.COMMA + Constants.HOST_INPUT_PARAM_CURRENCYCODE + Constants.EQUALTO + currencyCode
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBREQREFNUM +Constants.EQUALTO +( util.isNullOrEmpty(callInfo.getField(Field.ESBREQREFNUM)) ?"NA" :callInfo.getField(Field.ESBREQREFNUM)) ;
			hostReportDetails.setHostInParams(strHostInParam);
			/************************************/
			
			String hostOutputParam = Constants.HOST_OUTPUT_PARAM_RESPONSEDESC + Constants.EQUALTO + responseDesc +Constants.COMMA + Constants.HOST_OUTPUT_PARAM_RESPONSECODE
					+ Constants.EQUALTO + hostResCode
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBERRORDESC +Constants.EQUALTO +(util.isNullOrEmpty(tpr_ExchangeRateInquiry_HostRes.getErrorDesc()) ?"NA" :tpr_ExchangeRateInquiry_HostRes.getErrorDesc());
			hostReportDetails.setHostOutParams(hostOutputParam);

			callInfo.setHostReportDetails(hostReportDetails);
			ivrdata = (IvrData)callInfo.getField(Field.IVRDATA);

			callInfo.updateHostDetails(ivrdata);
			//End Reporting

			if(Constants.WS_SUCCESS_CODE.equalsIgnoreCase(code)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got Success response for the service exchangeRateInquiry");}

			}else{

				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Got failure response for ExchangeInq host service");}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The original response code of host access is " + tpr_ExchangeRateInquiry_HostRes.getHostResponseCode());}

				util.setHostErrorCodePhrase(callInfo, Constants.HOST_METHOD_EXCHNGRATEINQ, tpr_ExchangeRateInquiry_HostRes.getHostResponseCode());

				/**
				 * Following will be called only if there occurred account selection before this host access
				 */
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting the ErrorCodeAnotherAccount as Y");}
				util.setEligibleAccountCounts(callInfo, hostResCode);
			}
		}catch(Exception e){
			WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at  ThirdPartyRemittanceImpl.getTPRemittanceExchangeRateInq() "+ e.getMessage());
			throw new ServiceException(e);
		}
		return code;
	}

	@Override
	public String getTPAmountEnterPhrases(CallInfo callInfo)
			throws ServiceException {

		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: ThirdPartyRemittanceImpl.getTPAmountEnterPhrases()");}
		String str_GetMessage, finalResult;

		try{
			ArrayList<Object> dynamicValueArray = new ArrayList<Object>();
			//Need to get the FeatureConfig Data

			String tprSelectedCurr = (String)callInfo.getField(Field.TPRSELECTEDCURRTYPE);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Selected TPR selected currency is " + tprSelectedCurr);}

			String grammar = Constants.EMPTY_STRING;
			boolean moreOption = false;
			//Need to handle the Dynamic phrase list and Mannual Grammar portions
			dynamicValueArray.add(tprSelectedCurr+Constants.WAV_EXTENSION);

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Generated dynamic phrase list is"+dynamicValueArray);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Formed dynamic grammar for application layer is" + grammar);}

			String languageKey  = util.getLanguageKey((String) callInfo.getField(Field.LANGUAGE));
			Locale locale = util.getLocale((String) callInfo.getField(Field.LANGUAGE));

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Language Key value is"+ languageKey);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Locale selected is "+ locale);}

			String menuID = MenuIDMap.getMenuID("THIRD_PARTY_REMITTANCE_ENTER_AMT");
			//String anncID = AnncIDMap.getAnncID("");
			String featureID = FeatureIDMap.getFeatureID("Third_Party_Remittance");
			String combinedKey = languageKey+Constants.UNDERSCORE+featureID+Constants.UNDERSCORE+menuID;

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Combined Key is "+ combinedKey);}

			Object[] object = new Object[dynamicValueArray.size()];
			for(int count=0; count<dynamicValueArray.size();count++){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Adding "+ count +"element: "+dynamicValueArray.get(count) +"into Object array ");}
				object[count] = dynamicValueArray.get(count);

			}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"objArray  is :" + object);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Default wave file  is :" + DynaPhraseConstants.SILENCE_PHRASE);}


			str_GetMessage =  this.messageSource.getMessage(combinedKey, object, DynaPhraseConstants.SILENCE_PHRASE, locale );
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The property value for the get Message method is " + str_GetMessage);}

			if(str_GetMessage.equalsIgnoreCase(DynaPhraseConstants.SILENCE_PHRASE)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Assigning Silence phrase as result");}
				return (DynaPhraseConstants.SILENCE_PHRASE);
			}

			int totalPrompt = util.getTotalPromptCount(str_GetMessage);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Total Prompt received from the dynaproperty file is "+totalPrompt);}

			//Overriding the total prompts, received from the property file
			//No Need

			//To have the property file grammar, need to call that util method here
			//No Need

			callInfo.setField(Field.DYNAMICLIST, grammar);
			callInfo.setField(Field.MOREOPTION, moreOption);

			String dynamicPhraseKey = util.getDynaPhraseKey(str_GetMessage);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The dynamic phrase key is "+dynamicPhraseKey);}
			String dynamicMessageValue = util.getDynaPhraseMessage(str_GetMessage);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The dynamic message value is"+dynamicMessageValue);}

			finalResult = util.callDynaPhraseGeneration(dynamicPhraseKey, dynamicMessageValue, totalPrompt);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The final string formation is :" + finalResult);}

			//Need to handle if we want to append pipe seperator sign
			//No Need

			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: ThirdPartyRemittanceImpl.getTPAmountEnterPhrases()");}

		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.ERROR,session_ID_,"There was an error at ThirdPartyRemittanceImpl.getTPAmountEnterPhrases() "	+ e.getMessage());}
			throw new ServiceException(e);
		}
		return finalResult;
	}

	@Override
	public String getTPCurrencyPhrases(CallInfo callInfo) throws ServiceException {

		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: ThirdPartyRemittanceImpl.getTPCurrencyPhrases()");}
		String str_GetMessage, finalResult;

		try{
			ArrayList<Object> dynamicValueArray = new ArrayList<Object>();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
			ICEFeatureData ivr_ICEFeatureData = (ICEFeatureData) callInfo.getICEFeatureData();

			ArrayList<String> currList = null;
			currList = (ArrayList<String>) ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_TPR_CURRENCY_LIST);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The TPR Currency list retrieved is :" + currList);}

			String moreCount = (String)callInfo.getField(Field.MORECOUNT);
			int int_moreCount = util.isNullOrEmpty(moreCount)?Constants.GL_FIVE:Integer.parseInt(moreCount);

			String grammar = Constants.EMPTY_STRING;
			boolean moreOption = false;
			String temp_Str = Constants.EMPTY_STRING;

			/**
			 * Note temp_Str is nothing but the product name.  The wave file also should recorded in the same product name
			 * 
			 * eg OMR --> OMR.wav
			 * 
			 */
			int temp_MoreCount = int_moreCount - 1;

			for(int count=Constants.GL_ZERO;count<currList.size();count++){
				temp_Str = currList.get(count);
				dynamicValueArray.add((temp_Str+Constants.WAV_EXTENSION).trim());

				if(count == temp_MoreCount){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Adding the more option count");}
					moreOption = true;
					callInfo.setField(Field.MOREOPTIONCOUNT, int_moreCount);
				}

				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Added Product type "+temp_Str);}

				if(util.isNullOrEmpty(grammar)){
					grammar = temp_Str;
				}else{
					grammar = grammar + Constants.COMMA + temp_Str;
				}
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Adding the grammar value" + grammar);}

			}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Generated dynamic phrase list is"+dynamicValueArray);}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Formed dynamic grammar for application layer is" + grammar);}

			String languageKey  = util.getLanguageKey((String) callInfo.getField(Field.LANGUAGE));
			Locale locale = util.getLocale((String) callInfo.getField(Field.LANGUAGE));

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Language Key value is"+ languageKey);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Locale selected is "+ locale);}

			String menuID = MenuIDMap.getMenuID("THIRDPARTY_REMITTANCE_CURRENCY_SELECTION");
			//String anncID = AnncIDMap.getAnncID("Account_Balance_Message");
			String featureID = FeatureIDMap.getFeatureID("Third_Party_Remittance");
			String combinedKey = languageKey+Constants.UNDERSCORE+featureID+Constants.UNDERSCORE+menuID;

			/**
			 * Following are the modification done for configuring the more option of menus
			 */
			combinedKey = combinedKey + Constants.UNDERSCORE + int_moreCount;
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Combined key along with more count option is "+ combinedKey);}
			//END - Vinoth


			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Combined Key is "+ combinedKey);}

			Object[] object = new Object[dynamicValueArray.size()];
			for(int count=0; count<dynamicValueArray.size();count++){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Adding "+ count +"element: "+dynamicValueArray.get(count) +"into Object array ");}
				object[count] = dynamicValueArray.get(count);

			}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"objArray  is :" + object);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Default wave file  is :" + DynaPhraseConstants.SILENCE_PHRASE);}

			str_GetMessage =  this.messageSource.getMessage(combinedKey, object, DynaPhraseConstants.SILENCE_PHRASE, locale );
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The property value for the get Message method is " + str_GetMessage);}

			if(str_GetMessage.equalsIgnoreCase(DynaPhraseConstants.SILENCE_PHRASE)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Assigning Silence phrase as result");}
				return (DynaPhraseConstants.SILENCE_PHRASE);
			}

			int totalPrompt = util.getTotalPromptCount(str_GetMessage);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Total Prompt received from the dynaproperty file is "+totalPrompt);}

			//Overriding the total prompts, received from the property file
			if(currList.size()>int_moreCount){
				totalPrompt = Constants.GL_THREE * currList.size();
				totalPrompt = totalPrompt + Constants.GL_ONE;

				/**
				 * Added to fix the issue
				 */
				int temp1 = currList.size() / int_moreCount;
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Quotient value is "+temp1);}

				//				int temp2 =  currList.size() % int_moreCount;
				//				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Remainder value is "+temp2);}
				//				if(temp2 > 0){
				//					temp1++;
				//					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Quotient increased by one "+temp1);}
				//				}
				totalPrompt = totalPrompt + (temp1*Constants.GL_TWO);
				//END Vinoth


			}
			else{
				totalPrompt = Constants.GL_THREE * currList.size();
				totalPrompt = totalPrompt + Constants.GL_ONE;
			}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Total Prompt is "+totalPrompt);}

			callInfo.setField(Field.DYNAMICLIST, grammar);
			callInfo.setField(Field.MOREOPTION, moreOption);

			String dynamicPhraseKey = util.getDynaPhraseKey(str_GetMessage);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The dynamic phrase key is "+dynamicPhraseKey);}
			String dynamicMessageValue = util.getDynaPhraseMessage(str_GetMessage);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The dynamic message value is"+dynamicMessageValue);}

			finalResult = util.callDynaPhraseGeneration(dynamicPhraseKey, dynamicMessageValue, totalPrompt);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The final string formation is :" + finalResult);}
			//"CP_1033.wav*OMR.wav*CP_1019.wav*CP_1033.wav*INR.wav*CP_1020.wav*CP_1033.wav*USD.wav*CP_1021.wav";

			if(!util.isNullOrEmpty(finalResult)){
				temp_MoreCount = int_moreCount + 1;
				if(finalResult.contains(((DynaPhraseConstants.PHRASE_PRESS_)+temp_MoreCount+Constants.WAV_EXTENSION).trim())){
					finalResult = finalResult.replaceAll(((DynaPhraseConstants.PHRASE_PRESS_)+temp_MoreCount+Constants.WAV_EXTENSION),(((DynaPhraseConstants.PHRASE_PRESS_)+temp_MoreCount+Constants.WAV_EXTENSION)+Constants.PIPE));
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The Final Result string is after apending pipe seperator is "+finalResult);}
				}
			}

			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: ThirdPartyRemittanceImpl.getTPCurrencyPhrases()");}

		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at ThirdPartyRemittanceImpl.getTPCurrencyPhrases() "	+ e.getMessage());}
			throw new ServiceException(e);
		}
		return finalResult;
	}

	@Override
	public String getTPTransactionSuccessPhrases(CallInfo callInfo)
			throws ServiceException {

		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: FundsTransferConfirmationImpl.getFTSuccessAnnouncement()");}
		String str_GetMessage, finalResult;

		try{
			ArrayList<Object> dynamicValueArray = new ArrayList<Object>();

			String transRefID = Constants.EMPTY_STRING;
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Feature name is " + (String)callInfo.getField(Field.FEATURENAME));}



			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Transfer ID is " + transRefID);}
			//Setting this reference id in the callinfo
			transRefID=(String)callInfo.getField(Field.Transaction_Ref_No);

			String grammar = Constants.EMPTY_STRING;
			boolean moreOption = false;
			//Need to handle the Dynamic phrase list and Mannual Grammar portions
			if(!util.isNullOrEmpty(transRefID)){
				dynamicValueArray.add(DynaPhraseConstants.PHRASE_TRANS_REF_NO);	
				dynamicValueArray.add(transRefID);
			}else{
				dynamicValueArray.add(DynaPhraseConstants.SILENCE_PHRASE);	
				dynamicValueArray.add(Constants.EMPTY_STRING);
			}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Generated dynamic phrase list is"+dynamicValueArray);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Formed dynamic grammar for application layer is" + grammar);}

			String languageKey  = util.getLanguageKey((String) callInfo.getField(Field.LANGUAGE));
			Locale locale = util.getLocale((String) callInfo.getField(Field.LANGUAGE));

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Language Key value is"+ languageKey);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Locale selected is "+ locale);}

			//String menuID = MenuIDMap.getMenuID("");
			String anncID = AnncIDMap.getAnncID("Transaction_Success_Message");
			String featureID = FeatureIDMap.getFeatureID("Third_Party_Remittance");
			String combinedKey = languageKey+Constants.UNDERSCORE+featureID+Constants.UNDERSCORE+anncID;

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Combined Key is "+ combinedKey);}

			Object[] object = new Object[dynamicValueArray.size()];
			for(int count=0; count<dynamicValueArray.size();count++){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Adding "+ count +"element: "+dynamicValueArray.get(count) +"into Object array ");}
				object[count] = dynamicValueArray.get(count);
			}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"objArray  is :" + object);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Default wave file  is :" + DynaPhraseConstants.SILENCE_PHRASE);}

			str_GetMessage =  this.messageSource.getMessage(combinedKey, object, DynaPhraseConstants.SILENCE_PHRASE, locale );
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The property value for the get Message method is " + str_GetMessage);}

			if(str_GetMessage.equalsIgnoreCase(DynaPhraseConstants.SILENCE_PHRASE)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Assigning Silence phrase as result");}
				return (DynaPhraseConstants.SILENCE_PHRASE);
			}

			int totalPrompt = 0;
			if(!util.isNullOrEmpty(transRefID)){
				totalPrompt = util.getTotalPromptCount(str_GetMessage);
			}else{
				totalPrompt =Constants.GL_ONE;
			}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Total Prompt received from the dynaproperty file is "+totalPrompt);}


			//Overriding the total prompts, received from the property file

			//To have the property file grammar, need to call that util method here


			callInfo.setField(Field.DYNAMICLIST, grammar);
			callInfo.setField(Field.MOREOPTION, moreOption);

			String dynamicPhraseKey = util.getDynaPhraseKey(str_GetMessage);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The dynamic phrase key is "+dynamicPhraseKey);}
			String dynamicMessageValue = util.getDynaPhraseMessage(str_GetMessage);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The dynamic message value is"+dynamicMessageValue);}

			finalResult = util.callDynaPhraseGeneration(dynamicPhraseKey, dynamicMessageValue, totalPrompt);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The final string formation is :" + finalResult);}


			//Need to handle if we want to append pipeseperator sign

			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: ThirdPartyRemittanceImpl.getFTSuccessAnnouncement()");}

		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.ERROR,session_ID_,"There was an error at FundsTransferConfirmationImpl.getFTSuccessAnnouncement() "	+ e.getMessage());}
			throw new ServiceException(e);
		}
		return finalResult;
	}

	@Override
	public boolean isDebitCardActive(CallInfo arg0) throws ServiceException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getTPRBeneficiaryAccNoPhrases(CallInfo callInfo)
			throws ServiceException {

		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, session_ID_,"ENTER: ThirdPartyRemittanceImpl.getTPRBeneficiaryAccNoPhrases()");}
		String str_GetMessage, finalResult;

		try{
			ArrayList<Object> dynamicValueArray = new ArrayList<Object>();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
			ICEFeatureData ivr_ICEFeatureData = (ICEFeatureData) callInfo.getICEFeatureData();

			if(util.isNullOrEmpty(ivr_ICEFeatureData)){
				throw new ServiceException("ICEFeatureData object is null / empty");
			}
			//Need to get the FeatureConfig Data
			//			String selectedServiceProvider = (String)callInfo.getField(Field.SELECTEDSERVICEPROVIDER);
			//			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG, "Selected service provider is  "+selectedServiceProvider);}


			String noOfBenefCanBePlayed = util.isNullOrEmpty(ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_NO_OF_BENEF_CANBE_PLAYED))?Constants.TWO : (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_NO_OF_BENEF_CANBE_PLAYED);
			int int_NoOfBenefCanBePlayed = Integer.parseInt(noOfBenefCanBePlayed);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Total Number of beneficiary details can be playes are " + int_NoOfBenefCanBePlayed);}

			HashMap<String, TPR_BeneficiaryDetails> beneficiaryDetailMap = null;

			//Calling the beneficiary Details host service to get all beneficiary details of the available utility type beneficiary ids
			String beneficiaryDetlHostCode = getTPRBeneficiaryDetails(callInfo);

			if(Constants.ONE.equalsIgnoreCase(beneficiaryDetlHostCode)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Failed while calling Beneficiary details host service");}
				throw new ServiceException("FT_BenfPayeeDetails_HostRes object is null / Empty");
			}

			if(!util.isNullOrEmpty(callInfo.getTPR_BenfPayeeDetails_HostRes())){
				if(!util.isNullOrEmpty(callInfo.getTPR_BenfPayeeDetails_HostRes().getTPR_BeneficiaryDetailsMap())){
					beneficiaryDetailMap = callInfo.getTPR_BenfPayeeDetails_HostRes().getTPR_BeneficiaryDetailsMap();
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Beneficiary Detail map retrieved from host is "  + beneficiaryDetailMap);}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Total no Of Beneficiary details  available is"  + beneficiaryDetailMap.size());}
				}else{
					throw new ServiceException("beneficiaryDetailMap object is null / Empty");
				}
			}else{
				throw new ServiceException("FT_BenfPayeeDetails_HostRes  object is null / Empty");
			}

			/**
			 * Following are the changes done to fix the issue on 29-Nov-2014
			 */
			String moreCount = noOfBenefCanBePlayed;//(String)callInfo.getField(Field.MORECOUNT);
			int int_moreCount = util.isNullOrEmpty(moreCount)?Constants.GL_FIVE:Integer.parseInt(moreCount);

			String grammar = Constants.EMPTY_STRING;
			boolean moreOption = false;
			String temp_Str = Constants.EMPTY_STRING;
			TPR_BeneficiaryDetails temp_benefDetail = null;
			int validPayeeCount = Constants.GL_ZERO;

			//Need to handle the Dynamic phrase list and Mannual Grammar portions
			int temp_MoreCount = int_moreCount - 1;


			Iterator iter = beneficiaryDetailMap.keySet().iterator();
			String benefID = Constants.EMPTY_STRING;
			int count = Constants.GL_ZERO;
			int noOfBenefPlayedCount = Constants.GL_ZERO;
			String temp_DynaPhrases = Constants.EMPTY_STRING;
			while(iter.hasNext()) {
				noOfBenefPlayedCount++;
				if(noOfBenefPlayedCount > int_NoOfBenefCanBePlayed){

					dynamicValueArray.add(DynaPhraseConstants.Funds_Transfer_1019);
					dynamicValueArray.add(Constants.NA);
					temp_DynaPhrases = DynaPhraseConstants.PHRASE_PRESS_+noOfBenefPlayedCount;
					temp_DynaPhrases = temp_DynaPhrases + Constants.WAV_EXTENSION;
					dynamicValueArray.add(temp_DynaPhrases);

					if(util.isNullOrEmpty(grammar)){
						grammar = Constants.GRAMMAR_AGENT;
					}else{
						grammar = grammar + Constants.COMMA + Constants.GRAMMAR_AGENT;
					}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Adding the grammar value for AGENT and the frammed grammar is " + grammar);}
					break;
				}

				benefID = (String)iter.next();    
				temp_benefDetail  = (TPR_BeneficiaryDetails)beneficiaryDetailMap.get(benefID); 

				if(!util.isNullOrEmpty(temp_benefDetail) && !util.isNullOrEmpty(temp_benefDetail.getBenefAccountNo())){
					validPayeeCount++;

					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG, session_ID_,"The "+count+" beneficiary account number is " +temp_benefDetail.getBenefAccountNo());}
					temp_Str = temp_benefDetail.getBenefAccountNo();
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG, session_ID_,"The "+ benefID + "account number is"+temp_Str);}

					dynamicValueArray.add(DynaPhraseConstants.Funds_Transfer_1004);
					dynamicValueArray.add((temp_Str));
					temp_DynaPhrases = DynaPhraseConstants.PHRASE_PRESS_+noOfBenefPlayedCount;
					temp_DynaPhrases = temp_DynaPhrases + Constants.WAV_EXTENSION;
					dynamicValueArray.add(temp_DynaPhrases);

					if(count == temp_MoreCount){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Adding the more option count");}
						moreOption = true;
						callInfo.setField(Field.MOREOPTIONCOUNT, int_moreCount);
					}

					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Added Beneficiary number in the grammar list "+temp_Str);}
					if(util.isNullOrEmpty(grammar)){
						grammar = temp_Str;
					}else{
						grammar = grammar + Constants.COMMA + temp_Str;
					}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Adding the grammar value" + grammar);}
				}

				count++;
			}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Valid payee account number total count is "+validPayeeCount);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG, session_ID_,"Generated dynamic phrase list is"+dynamicValueArray);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG, session_ID_,"Formed dynamic grammar for application layer is" + grammar);}

			String languageKey  = util.getLanguageKey((String) callInfo.getField(Field.LANGUAGE));
			Locale locale = util.getLocale((String) callInfo.getField(Field.LANGUAGE));

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG, session_ID_,"Language Key value is"+ languageKey);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Locale selected is "+ locale);}

			String menuID = MenuIDMap.getMenuID("TPR_BENEFICIARY_ACCOUNT_SELECTION_BM");
			//String anncID = AnncIDMap.getAnncID("");
			String featureID = FeatureIDMap.getFeatureID("Third_Party_Remittance");
			String combinedKey = languageKey+Constants.UNDERSCORE+featureID+Constants.UNDERSCORE+menuID;

			/**
			 * Following are the modification done for configuring the more option of menus
			 */
			combinedKey = combinedKey + Constants.UNDERSCORE + int_moreCount;
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Combined key along with more count option is "+ combinedKey);}
			//END - Vinoth


			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Combined Key is "+ combinedKey);}

			Object[] object = new Object[dynamicValueArray.size()];
			for(int i=0; i<dynamicValueArray.size();i++){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Adding "+ i +"element: "+dynamicValueArray.get(i) +"into Object array ");}
				object[i] = dynamicValueArray.get(i);

			}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"objArray  is :" + object);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Default wave file  is :" + DynaPhraseConstants.SILENCE_PHRASE);}


			str_GetMessage =  this.messageSource.getMessage(combinedKey, object, DynaPhraseConstants.SILENCE_PHRASE, locale );
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The property value for the get Message method is " + str_GetMessage);}

			if(str_GetMessage.equalsIgnoreCase(DynaPhraseConstants.SILENCE_PHRASE)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Assigning Silence phrase as result");}
				return (DynaPhraseConstants.SILENCE_PHRASE);
			}

			int totalPrompt = util.getTotalPromptCount(str_GetMessage);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Total Prompt received from the dynaproperty file is "+totalPrompt);}


			//Overriding the total prompts, received from the property file
			//			if(validPayeeCount >int_moreCount){
			//				totalPrompt = Constants.GL_THREE * int_moreCount;
			//				totalPrompt = totalPrompt + Constants.GL_TWO;
			//			}
			//			else{
			//				totalPrompt = Constants.GL_THREE * validPayeeCount;
			//			}


			//Overriding the total prompts, received from the property file
			if(validPayeeCount >int_moreCount){
				totalPrompt = Constants.GL_THREE * validPayeeCount;
				//totalPrompt = totalPrompt + Constants.GL_TWO;

				/**
				 * Added to fix the issue
				 */
				int temp1 = validPayeeCount / int_moreCount;
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Quotient value is "+temp1);}

				int temp2 =  validPayeeCount % int_moreCount;
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Remainder value is "+temp2);}
				if(temp2 == 0){
					temp1--;
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Quotient decreased by one "+temp1);}
				}
				totalPrompt = totalPrompt + (temp1*Constants.GL_TWO);
				//END Vinoth
			}
			else{
				totalPrompt = Constants.GL_THREE * validPayeeCount;
			}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"Total Prompt is "+totalPrompt);}

			//To have the property file grammar, need to call that util method here
			//No need

			callInfo.setField(Field.DYNAMICLIST, grammar);
			callInfo.setField(Field.MOREOPTION, moreOption);

			String dynamicPhraseKey = util.getDynaPhraseKey(str_GetMessage);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The dynamic phrase key is "+dynamicPhraseKey);}
			String dynamicMessageValue = util.getDynaPhraseMessage(str_GetMessage);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The dynamic message value is"+dynamicMessageValue);}

			finalResult = util.callDynaPhraseGeneration(dynamicPhraseKey, dynamicMessageValue, totalPrompt);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_,"The final string formation is :" + finalResult);}


			//Need to handle if we want to append pipeseperator sign
			//			if(!util.isNullOrEmpty(finalResult)){
			//				temp_MoreCount = int_moreCount + 1;
			//				if(finalResult.contains(((DynaPhraseConstants.PHRASE_PRESS_)+temp_MoreCount).trim())){
			//					finalResult.replaceAll(((DynaPhraseConstants.PHRASE_PRESS_)+temp_MoreCount).trim(),(((DynaPhraseConstants.PHRASE_PRESS_)+temp_MoreCount)+Constants.PIPESEPERATOR).trim());
			//					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,"The Final Result string is after apending pipe seperator is "+finalResult);}
			//				}
			//			}

			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: ThirdPartyRemittanceImpl.getFTOutsideBMBeneficiaryAccNoPhrases()");}

		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at FundsTransferThirdPartyWithinBMImpl.getFTWithinBMBeneficiaryAccNoPhrases() "	+ e.getMessage());}
			throw new ServiceException(e);
		}
		return finalResult;

	}

}
