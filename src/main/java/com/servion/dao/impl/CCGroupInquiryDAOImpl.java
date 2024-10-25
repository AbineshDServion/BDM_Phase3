package com.servion.dao.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.bankmuscat.esb.cardmanagementservice.CCGroupAuthInfoType;
import com.bankmuscat.esb.cardmanagementservice.CCGrpInqResType;
import com.bankmuscat.esb.cardmanagementservice.CardGrpInfoType;
import com.servion.common.util.LoggerObject;
import com.servion.common.util.WriteLog;
import com.servion.common.util.util;
import com.servion.dao.CCGroupInquiryDAO;
import com.servion.exception.ServiceException;
import com.servion.ice.RuleEngine.ICEFeatureData;
import com.servion.ice.RuleEngine.ICEGlobalConfig;
import com.servion.model.CallInfo;
import com.servion.model.Constants;
import com.servion.model.Field;
import com.servion.model.creditCardBalance.CreditCardGroupAuthDetails;
import com.servion.model.creditCardBalance.CreditCardGroupInfoDetails;
import com.servion.model.creditCardBalance.CreditCardGroupInq_HostRes;
import com.servion.ws.exception.DaoException;
import com.servion.ws.exception.PersistenceException;
import com.servion.ws.header.WS_ResponseHeader;
import com.servion.ws.header.impl.WS_ResponseHeaderImpl;
import com.servion.ws.service.CCGroupInquiryService;
import com.servion.ws.util.DAOLayerUtils;

public class CCGroupInquiryDAOImpl implements CCGroupInquiryDAO{
	private static Logger logger = LoggerObject.getLogger();

	@Autowired
	CCGroupInquiryService ccGroupInquiryService;


	public CCGroupInquiryService getCcGroupInquiryService() {
		return ccGroupInquiryService;
	}

	public void setCcGroupInquiryService(CCGroupInquiryService ccGroupInquiryService) {
		this.ccGroupInquiryService = ccGroupInquiryService;
	}

	public WS_ResponseHeader getWs_ResponseHeader() {
		return ws_ResponseHeader;
	}

	public void setWs_ResponseHeader(WS_ResponseHeader ws_ResponseHeader) {
		this.ws_ResponseHeader = ws_ResponseHeader;
	}

	//Not initializing through BEAN Injection
	private WS_ResponseHeader ws_ResponseHeader = new WS_ResponseHeaderImpl();


	@Override
	public CreditCardGroupInq_HostRes getCCAvailableBalanceHostRes(CallInfo callInfo,
			String cardEmbossNum, String reference, String extraOption, String maxNoAuth, String requestType) throws DaoException {
		

		try{logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, (String)callInfo.getField(Field.SESSIONID));}catch(Exception e){}
		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** ENTER - DISPACHING WS RES ***: CCGroupInquiryDAOImpl.getCCAvailableBalanceHostRes()");}

		CreditCardGroupInq_HostRes beanResponse = new CreditCardGroupInq_HostRes();

		try{
			String sessionID = (String)callInfo.getField(Field.SESSIONID);

			if(util.isNullOrEmpty(sessionID))
				throw new DaoException("Session ID is null / empty");

			CCGrpInqResType response = null;

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Calling the DAO Layer method callHostForCCAvailableBalance host");}

			String str_UUI = util.isNullOrEmpty(callInfo.getField(Field.UUI)) ? Constants.DEFAULT : (String)callInfo.getField(Field.UUI);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The UUI value is "+str_UUI);}
			
			//Newly added to disable or enable host response files
			String generateXML = util.isNullOrEmpty(callInfo.getField(Field.GENERATEHOSTXML)) ? Constants.N : (String)callInfo.getField(Field.GENERATEHOSTXML);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Is to generate host xml files ? "+generateXML);}
			//END
			
			response = ccGroupInquiryService.callHostForCCAvailableBalance(logger, sessionID, cardEmbossNum, reference, extraOption, maxNoAuth, requestType, str_UUI, generateXML, callInfo);

			ws_ResponseHeader = DAOLayerUtils.getWsResponseStatus(sessionID,response, ws_ResponseHeader);
			String code = ws_ResponseHeader.getEsbErrCode();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Host error code of callHostForCCAvailableBalance is : "+code);}
			WriteLog.hostLogWrite(sessionID, (String)callInfo.getField(Field.HOST_SERVICE_NAME), code);
			
			//Setting the ESB request reference number for reporting
			String esbReqRefNum = util.isNullOrEmpty(ws_ResponseHeader.getReqRefNum()) ? Constants.NA :  ws_ResponseHeader.getReqRefNum();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The ESB Request Reference number is "+ws_ResponseHeader.getReqRefNum());}
			callInfo.setField(Field.ESBREQREFNUM, esbReqRefNum);
			//END
			
			ICEGlobalConfig ivr_ICEGlobalConfig = (ICEGlobalConfig) callInfo.getICEGlobalConfig();
			if(util.isNullOrEmpty(ivr_ICEGlobalConfig)){
				throw new ServiceException("ICEGlobalConfig object is null");
			}

			beanResponse.setHostResponseCode(code);

			beanResponse.setErrorDesc(ws_ResponseHeader.getEsbErrDesc());
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Host error Short description for getCCAvailableBalanceHostRes is : "+ws_ResponseHeader.getEsbErrDesc());}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID,"### CCGrpInq HOST RESPONSE DETAILS ###" +"CALL ID = "+str_UUI+"### RESPONSE CODE = "+beanResponse.getHostResponseCode() + "### RESPONSE DESC = " + beanResponse.getErrorDesc());}
			
			ICEFeatureData ivr_ICEFeatureData = util.isNullOrEmpty(callInfo.getICEFeatureData())? null : (ICEFeatureData)callInfo.getICEFeatureData();
			if(util.isNullOrEmpty(ivr_ICEFeatureData)){
				throw new ServiceException("ivr_ICEFeatureData object is null");
			}
			
			String hostErrorCodeList = Constants.EMPTY_STRING;
			
			if(!util.isNullOrEmpty(ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_CCGroupInquiry_Succ_ErrorCode))){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Success error code received from the Feature ");}
				hostErrorCodeList = (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_CCGroupInquiry_Succ_ErrorCode); 
			}
			else{
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Success error code received from the Global ");}
				hostErrorCodeList = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_CCGroupInquiry_Succ_ErrorCode);
			}

			code = util.isCodePresentInTheList(code, hostErrorCodeList,callInfo)?Constants.WS_SUCCESS_CODE:Constants.WS_FAILURE_CODE;
			beanResponse.setErrorCode(code);
			String hostEndTime = util.getCurrentDateTime();
			beanResponse.setHostEndTime(hostEndTime);

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The host response error code for Application layer is "+code);}

			if(Constants.WS_SUCCESS_CODE.equals(code)){
				if(!util.isNullOrEmpty(response)){

					HashMap<String, ArrayList<CreditCardGroupInfoDetails>> ccGroupInfoDetailsMap = new HashMap<String, ArrayList<CreditCardGroupInfoDetails>>();
					ArrayList<CreditCardGroupInfoDetails> ccGroupDetailList = new ArrayList<CreditCardGroupInfoDetails>();
					//Changed as on 23-08-2015
					CreditCardGroupInfoDetails creditCardGroupInfoDetails = null;
					
					HashMap<String, ArrayList<CreditCardGroupAuthDetails>> ccGroupAuthDetailsMap = new HashMap<String, ArrayList<CreditCardGroupAuthDetails>>();
					ArrayList<CreditCardGroupAuthDetails> ccGroupAuthDetailList = new ArrayList<CreditCardGroupAuthDetails>();
					//Changed as on 23-08-2015
					CreditCardGroupAuthDetails creditCardGroupAuthDetails = null;
					
					beanResponse.setCardNumber(response.getCardNumber());
					
					List<CCGroupAuthInfoType> cardAuthInfoTypeList = response.getCardAuthInfo();
					CCGroupAuthInfoType ccGroupAuthInfoType = null;
					
					List<CardGrpInfoType> cardGrpInfoTypeList = response.getCardGrpInfo();
					CardGrpInfoType cardGrpInfoType = null;
					
					if(!util.isNullOrEmpty(cardAuthInfoTypeList)){
						for(int i = 0; i < cardAuthInfoTypeList.size(); i++){
							ccGroupAuthInfoType = cardAuthInfoTypeList.get(i);
							
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, i + " Object of ccGroupAuthInfoType is "+ ccGroupAuthInfoType);}
							creditCardGroupAuthDetails = new CreditCardGroupAuthDetails();
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Auth Limit Period is "+ ccGroupAuthInfoType.getAuthLimitPeriod());}
							creditCardGroupAuthDetails.setAuthLimitPeriod(ccGroupAuthInfoType.getAuthLimitPeriod()+ Constants.EMPTY_STRING);
							
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Auth  Cash Period is "+ ccGroupAuthInfoType.getAuthLimitPeriod());}
							creditCardGroupAuthDetails.setAuthCashPeriod(ccGroupAuthInfoType.getAuthCashPeriod()+ Constants.EMPTY_STRING);
							
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Cash Trxn is "+ ccGroupAuthInfoType.getAuthLimitPeriod());}
							creditCardGroupAuthDetails.setAuthCashTrxn(ccGroupAuthInfoType.getAuthCashTrxn()+ Constants.EMPTY_STRING);
							
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Auth CCY is "+ ccGroupAuthInfoType.getAuthLimitPeriod());}
							creditCardGroupAuthDetails.setAuthCcy(ccGroupAuthInfoType.getAuthCcy()+ Constants.EMPTY_STRING);
							
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Last Balance is "+ ccGroupAuthInfoType.getAuthLimitPeriod());}
							creditCardGroupAuthDetails.setLastBalance(ccGroupAuthInfoType.getLastBalance()+ Constants.EMPTY_STRING);
							
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Auth Status is "+ ccGroupAuthInfoType.getAuthLimitPeriod());}
							creditCardGroupAuthDetails.setAuthStatus(ccGroupAuthInfoType.getAuthStatus()+ Constants.EMPTY_STRING);
							
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Outstanding Auth Trxn Period is "+ ccGroupAuthInfoType.getAuthLimitPeriod());}
							creditCardGroupAuthDetails.setAuthTrxnPeriod(ccGroupAuthInfoType.getAuthTrxnPeriod()+ Constants.EMPTY_STRING);
							
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Outstanding Auth Amt is "+ ccGroupAuthInfoType.getAuthLimitPeriod());}
							creditCardGroupAuthDetails.setOutstandingAuthAmount(ccGroupAuthInfoType.getOutStandingAuthAmt()+ Constants.EMPTY_STRING);
							
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Adding the creditCardGroupAuthDetails object "+ creditCardGroupAuthDetails +" in a ArrayList");}
							ccGroupAuthDetailList.add(creditCardGroupAuthDetails);
						}
						
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Inserting into the hash map object of ccGroupAuthDetailsMap");}
						ccGroupAuthDetailsMap.put(beanResponse.getCardNumber(), ccGroupAuthDetailList);
					}
					
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Size of ccGroupAuthDetailsMap object is  " + ccGroupAuthDetailsMap.size());}
					beanResponse.setCreditCardGrpAuthDetailMap(ccGroupAuthDetailsMap);
					
					if(!util.isNullOrEmpty(cardGrpInfoTypeList)){
						
						for(int j = 0; j < cardGrpInfoTypeList.size(); j++){
							cardGrpInfoType = cardGrpInfoTypeList.get(j);
							
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, j + " Object of CardGrpInfoType is "+ cardGrpInfoType);}
							creditCardGroupInfoDetails = new CreditCardGroupInfoDetails();
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Group Number  is "+ cardGrpInfoType.getGroupnumber()+ Constants.EMPTY_STRING);}
							creditCardGroupInfoDetails.setGroupnumber(cardGrpInfoType.getGroupnumber() + Constants.EMPTY_STRING);
							
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Credit Limit  is "+ cardGrpInfoType.getCreditLimit()+ Constants.EMPTY_STRING);}
							creditCardGroupInfoDetails.setCreditLimit(cardGrpInfoType.getCreditLimit() + Constants.EMPTY_STRING);
							
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Total OTB is "+ cardGrpInfoType.getTotalOTB()+ Constants.EMPTY_STRING);}
							creditCardGroupInfoDetails.setTotalOTB(cardGrpInfoType.getTotalOTB() + Constants.EMPTY_STRING);
							
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Total Auth Limit is "+ cardGrpInfoType.getNumTotalAuthsLimit()+ Constants.EMPTY_STRING);}
							creditCardGroupInfoDetails.setNumTotalAuthsLimit(cardGrpInfoType.getNumTotalAuthsLimit() + Constants.EMPTY_STRING);
							
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Total Auth  is "+ cardGrpInfoType.getNumTotalAuths()+ Constants.EMPTY_STRING);}
							creditCardGroupInfoDetails.setNumTotalAuths(cardGrpInfoType.getNumTotalAuths() + Constants.EMPTY_STRING);
							
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Cash Limit is "+ cardGrpInfoType.getCashLimit()+ Constants.EMPTY_STRING);}
							creditCardGroupInfoDetails.setCashLimit(cardGrpInfoType.getCashLimit() + Constants.EMPTY_STRING);
							
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Cash OTB  is "+ cardGrpInfoType.getCashOTB()+ Constants.EMPTY_STRING);}
							creditCardGroupInfoDetails.setCashOTB(cardGrpInfoType.getCashOTB() + Constants.EMPTY_STRING);
							
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Cash Auth is "+ cardGrpInfoType.getNumCashAuths()+ Constants.EMPTY_STRING);}
							creditCardGroupInfoDetails.setNumCashAuths(cardGrpInfoType.getNumCashAuths() + Constants.EMPTY_STRING);
							
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Ledger OTB  is "+ cardGrpInfoType.getLedgerOTB()+ Constants.EMPTY_STRING);}
							creditCardGroupInfoDetails.setLedgerOTB(cardGrpInfoType.getLedgerOTB() + Constants.EMPTY_STRING);
							
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Max Loan Period is "+ cardGrpInfoType.getMaxLoanPeriod()+ Constants.EMPTY_STRING);}
							creditCardGroupInfoDetails.setMaxLoanPeriod(cardGrpInfoType.getMaxLoanPeriod() + Constants.EMPTY_STRING);
							
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ##  Max Loan Amt is "+ cardGrpInfoType.getMaxLoanAmt()+ Constants.EMPTY_STRING);}
							creditCardGroupInfoDetails.setMaxLoanAmt(cardGrpInfoType.getMaxLoanAmt() + Constants.EMPTY_STRING);
							
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Response field ## Min Loan Amt  is "+ cardGrpInfoType.getMinLoanAmt()+ Constants.EMPTY_STRING);}
							creditCardGroupInfoDetails.setMinLoanAmt(cardGrpInfoType.getMinLoanAmt() + Constants.EMPTY_STRING);
							
							if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Adding the creditCardGroupInfoDetails object "+ creditCardGroupInfoDetails +" in a ArrayList");}
							ccGroupDetailList.add(creditCardGroupInfoDetails);
						}
						
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Inserting into the hash map object of ccGroupAuthDetailsMap");}
						ccGroupInfoDetailsMap.put(beanResponse.getCardNumber(), ccGroupDetailList);
					}
					
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Size of ccGroupInfoDetailsMap object is  " + ccGroupInfoDetailsMap.size());}
					beanResponse.setCreditCardGrpInfoDetailMap(ccGroupInfoDetailsMap);
				}else{
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## CCGrpInqResType Response field Received null / empty so setting error code as 1");}

					beanResponse.setErrorCode(Constants.WS_FAILURE_CODE);
				}
			}else{
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## CCGrpInqResType Response field Received null / empty so setting error code as 1");}

				beanResponse.setErrorCode(Constants.WS_FAILURE_CODE);
			}

		}catch(PersistenceException pe){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, pe,  "There was an error at CCGroupInquiryDAOImpl.getCCAvailableBalanceHostRes() "	+ pe.getMessage());}
			throw new DaoException(pe);
		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at CCGroupInquiryDAOImpl.getCCAvailableBalanceHostRes() "	+ e.getMessage());}
			throw new DaoException(e);
		}

		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** EXIT - DISPACHING WS RES ***: CCGroupInquiryDAOImpl.getCCAvailableBalanceHostRes()");}
		return beanResponse;
	}

}
