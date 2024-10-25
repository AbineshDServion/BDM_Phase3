package com.servion.dao.impl;

import java.math.BigDecimal;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.bankmuscat.esb.financialtxnservice.CreditCardAuthInfoType;
import com.bankmuscat.esb.financialtxnservice.UtilityBillPaymentCCResType;
import com.servion.common.util.LoggerObject;
import com.servion.common.util.WriteLog;
import com.servion.common.util.util;
import com.servion.dao.UtilityBillPaymentCCDAO;
import com.servion.exception.ServiceException;
import com.servion.ice.RuleEngine.ICEFeatureData;
import com.servion.ice.RuleEngine.ICEGlobalConfig;
import com.servion.model.CallInfo;
import com.servion.model.Constants;
import com.servion.model.Field;
import com.servion.model.billPayment.UpdatePaymentDetailsCC_HostRes;
import com.servion.ws.exception.DaoException;
import com.servion.ws.exception.PersistenceException;
import com.servion.ws.header.WS_ResponseHeader;
import com.servion.ws.header.impl.WS_ResponseHeaderImpl;
import com.servion.ws.service.UtilityBillPaymentCCService;
import com.servion.ws.util.DAOLayerUtils;

public class UtilityBillPaymentCCDAOImpl implements UtilityBillPaymentCCDAO{
	private static Logger logger = LoggerObject.getLogger();

	@Autowired
	UtilityBillPaymentCCService utilityBillPaymentCCService;

	public UtilityBillPaymentCCService getUtilityBillPaymentCCService() {
		return utilityBillPaymentCCService;
	}

	public void setUtilityBillPaymentCCService(
			UtilityBillPaymentCCService utilityBillPaymentCCService) {
		this.utilityBillPaymentCCService = utilityBillPaymentCCService;
	}

	//Not initializing through BEAN Injection
	private WS_ResponseHeader ws_ResponseHeader = new WS_ResponseHeaderImpl();


	@Override
	public UpdatePaymentDetailsCC_HostRes getCCUtilityBillUpdPaymentHostRes(
			CallInfo callInfo, String paymentType, String utilityCode,
			String serviceProviderCode, String contractNo, String billNo,
			int msisdn, BigDecimal payAmt, BigDecimal bonusRechrgAmt,
			String cardNumber, String traceNo, String merchantCategoryCode,
			String acquiringInstitutionCountry, String merchantID,
			String retrievalReference, String terminalID,
			String transactionCCY, String addlPOSInfo, String requestType) throws DaoException {

		try{logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, (String)callInfo.getField(Field.SESSIONID));}catch(Exception e){}
		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** ENTER - DISPACHING WS RES ***: UtilityBillPaymentCCDAOImpl.getCCSchoolBillUpdPaymentHostRes()");}

		UpdatePaymentDetailsCC_HostRes beanResponse = new UpdatePaymentDetailsCC_HostRes();

		try{
			String sessionID = (String)callInfo.getField(Field.SESSIONID);

			if(util.isNullOrEmpty(sessionID))
				throw new DaoException("Session ID is null / empty");

			UtilityBillPaymentCCResType response = null;

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Calling the DAO Layer method callUtilityBillPaymentCCHost host");}

			String str_UUI = util.isNullOrEmpty(callInfo.getField(Field.UUI)) ? Constants.DEFAULT : (String)callInfo.getField(Field.UUI);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The UUI value is "+str_UUI);}
			
			//Newly added to disable or enable host response files
			String generateXML = util.isNullOrEmpty(callInfo.getField(Field.GENERATEHOSTXML)) ? Constants.N : (String)callInfo.getField(Field.GENERATEHOSTXML);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Is to generate host xml files ? "+generateXML);}
			//END
			
			
			response = utilityBillPaymentCCService.callUtilityBillPaymentCCHost(logger, sessionID, paymentType, utilityCode, serviceProviderCode, contractNo, 
					billNo, msisdn, payAmt, bonusRechrgAmt, cardNumber, traceNo, merchantCategoryCode, 
					acquiringInstitutionCountry, merchantID, retrievalReference, terminalID, transactionCCY,
					addlPOSInfo, requestType, str_UUI, generateXML, callInfo);

			ws_ResponseHeader = DAOLayerUtils.getWsResponseStatus(sessionID,response, ws_ResponseHeader);
			String code = ws_ResponseHeader.getEsbErrCode();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Host error code of callUtilityBillPaymentCCHost is : "+code);}
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
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Host error Short description for getAPINValCustProfDetailsHostRes is : "+ws_ResponseHeader.getEsbErrDesc());}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID,"### UtilitybillPayment HOST RESPONSE DETAILS ###" +"CALL ID = "+str_UUI+"### RESPONSE CODE = "+beanResponse.getHostResponseCode() + "### RESPONSE DESC = " + beanResponse.getErrorDesc());}

			ICEFeatureData ivr_ICEFeatureData = util.isNullOrEmpty(callInfo.getICEFeatureData())? null : (ICEFeatureData)callInfo.getICEFeatureData();
			if(util.isNullOrEmpty(ivr_ICEFeatureData)){
				throw new ServiceException("ivr_ICEFeatureData object is null");
			}
			
			String hostErrorCodeList = Constants.EMPTY_STRING;
			
			if(!util.isNullOrEmpty(ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_UtilityBillPaymentCC_Succ_ErrorCode))){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Success error code received from the Feature ");}
				hostErrorCodeList = (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_UtilityBillPaymentCC_Succ_ErrorCode); 
			}
			else{
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Success error code received from the Global ");}
				hostErrorCodeList = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_UtilityBillPaymentCC_Succ_ErrorCode);
			}

			
			code = util.isCodePresentInTheList(code, hostErrorCodeList ,callInfo)?Constants.WS_SUCCESS_CODE:Constants.WS_FAILURE_CODE;
			beanResponse.setErrorCode(code);
			String hostEndTime = util.getCurrentDateTime();
			beanResponse.setHostEndTime(hostEndTime);

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The host response error code for Application layer is "+code);}

			if(Constants.WS_SUCCESS_CODE.equals(code)){
				if(!util.isNullOrEmpty(response)){
					
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Bill Reference Number"+ response.getBillerRefNum());}
					beanResponse.setBillerRefNo(response.getBillerRefNum());
					
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Payment Trans ID"+response.getBillerPaymentTransId());}
					beanResponse.setBillerPaymentTransID(response.getBillerPaymentTransId());
					
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Account Balance"+response.getActBal() + Constants.EMPTY_STRING);}
					beanResponse.setActBal(response.getActBal() + Constants.EMPTY_STRING);
					
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## UCIPRET code"+response.getUCIPRetCode() + Constants.EMPTY_STRING);}
					beanResponse.setUcipRetCode(response.getUCIPRetCode() + Constants.EMPTY_STRING);
					
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Command Name"+response.getCommandName() + Constants.EMPTY_STRING);}
					beanResponse.setCommandName(response.getCommandName() + Constants.EMPTY_STRING);
					
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Valid For Resubmit"+response.isValidForResubmit() + Constants.EMPTY_STRING);}
					beanResponse.setValidForResubmit(response.isValidForResubmit() + Constants.EMPTY_STRING);
					
					CreditCardAuthInfoType creditCardAuthInfoType = response.getCreditCardAuthInfo();
					
					if(!util.isNullOrEmpty(creditCardAuthInfoType)){
						
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Credit Card Auth InfoType"+creditCardAuthInfoType.getCardNumber());}
						beanResponse.setCardNumber(creditCardAuthInfoType.getCardNumber());
						
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Credit Card Auth Info Type"+ creditCardAuthInfoType.getTraceNumber());}
						beanResponse.setTraceNumber(creditCardAuthInfoType.getTraceNumber());
						
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Merchant Category Code"+creditCardAuthInfoType.getMerchantCategoryCode());}
						beanResponse.setMerchantCategoryCode(creditCardAuthInfoType.getMerchantCategoryCode());
						
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Acquiring Institution country"+creditCardAuthInfoType.getAcquiringInstitutionCountry());};
						beanResponse.setAcquiringInstitutionCountry(creditCardAuthInfoType.getAcquiringInstitutionCountry());
						
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Merchant ID "+creditCardAuthInfoType.getMerchantID());}
						beanResponse.setMerchantID(creditCardAuthInfoType.getMerchantID());
						
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Retrieval reference"+creditCardAuthInfoType.getRetrievalReference());}
						beanResponse.setRetrievalReference(creditCardAuthInfoType.getRetrievalReference());
						
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Terminal id"+creditCardAuthInfoType.getTerminalID());}
						beanResponse.setTerminalID(creditCardAuthInfoType.getTerminalID());
						
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Transaction CCY"+creditCardAuthInfoType.getTransactionCcy());}
						beanResponse.setTransactionCcy(creditCardAuthInfoType.getTransactionCcy());
						
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## AddIPOSInfo"+ creditCardAuthInfoType.getAddlPOSInfo());}
						beanResponse.setAdditionalPOSInfo(creditCardAuthInfoType.getAddlPOSInfo());
					}
				}
			}else{
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## UtilityBillPaymentCC service Response field Received null / empty so setting error code as 1");}

				beanResponse.setErrorCode(Constants.WS_FAILURE_CODE);
			}
		}catch(PersistenceException pe){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, pe,  "There was an error at UtilityBillPaymentCCDAOImpl.getCCSchoolBillUpdPaymentHostRes() "	+ pe.getMessage());}
			throw new DaoException(pe);
		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at UtilityBillPaymentCCDAOImpl.getCCSchoolBillUpdPaymentHostRes() "	+ e.getMessage());}
			throw new DaoException(e);
		}

		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** EXIT - DISPACHING WS RES ***: UtilityBillPaymentCCDAOImpl.getCCSchoolBillUpdPaymentHostRes()");}
		return beanResponse;
	}

}
