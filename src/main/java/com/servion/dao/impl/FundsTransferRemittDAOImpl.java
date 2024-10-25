package com.servion.dao.impl;

import java.math.BigDecimal;

import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.bankmuscat.esb.commontypes.DebitInfoType;
import com.bankmuscat.esb.commontypes.XferRecType;
import com.bankmuscat.esb.financialtxnservice.XferRemittResType;
import com.servion.common.util.LoggerObject;
import com.servion.common.util.WriteLog;
import com.servion.common.util.util;
import com.servion.dao.FundsTransferRemittDAO;
import com.servion.exception.ServiceException;
import com.servion.ice.RuleEngine.ICEFeatureData;
import com.servion.ice.RuleEngine.ICEGlobalConfig;
import com.servion.model.CallInfo;
import com.servion.model.Constants;
import com.servion.model.Field;
import com.servion.model.fundsTransfer.UpdateFTRemittPayment_HostRes;
import com.servion.model.thirdPartyRemittance.TPR_UpdatePaymentDetails_HostRes;
import com.servion.ws.exception.DaoException;
import com.servion.ws.exception.PersistenceException;
import com.servion.ws.header.WS_ResponseHeader;
import com.servion.ws.header.impl.WS_ResponseHeaderImpl;
import com.servion.ws.service.FundsTransferRemittService;
import com.servion.ws.util.DAOLayerUtils;

public class FundsTransferRemittDAOImpl implements FundsTransferRemittDAO{
	private static Logger logger = LoggerObject.getLogger();

	@Autowired
	FundsTransferRemittService fundsTransferRemittService;

	public FundsTransferRemittService getFundsTransferRemittService() {
		return fundsTransferRemittService;
	}

	public void setFundsTransferRemittService(
			FundsTransferRemittService fundsTransferRemittService) {
		this.fundsTransferRemittService = fundsTransferRemittService;
	}


	//Not initializing through BEAN Injection
	private WS_ResponseHeader ws_ResponseHeader = new WS_ResponseHeaderImpl();


	@Override
	public UpdateFTRemittPayment_HostRes getFTTOBMUpdatePaymentOHostRes(
			CallInfo callInfo, String xferID, BigDecimal debitAmt,
			BigDecimal creditAmt, XMLGregorianCalendar debitValueDate,
			String acctID, String accWithBank, String bankCode,
			BigDecimal customerRate, String paymentDetails, String purposeCode,
			int txnCode, String fullName, String nostroBankName, String gsmNo,
			String benfCode, String benfCustomer, String purchaseBenfAcctNo,
			String benfLocation, String benfBranch, String benfAcctID,
			BigDecimal ccyRate, String requestType) throws DaoException {

		try{logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, (String)callInfo.getField(Field.SESSIONID));}catch(Exception e){}
		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** ENTER - DISPACHING WS RES ***: FundsTransferRemittDAOImpl.getFTTOBMUpdatePaymentOHostRes()");}

		UpdateFTRemittPayment_HostRes beanResponse = new UpdateFTRemittPayment_HostRes();

		try{
			String sessionID = (String)callInfo.getField(Field.SESSIONID);

			if(util.isNullOrEmpty(sessionID))
				throw new DaoException("Session ID is null / empty");

			XferRemittResType response = null;

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Calling the DAO Layer method callCCEntityInquiry host");}

			String str_UUI = util.isNullOrEmpty(callInfo.getField(Field.UUI)) ? Constants.DEFAULT : (String)callInfo.getField(Field.UUI);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The UUI value is "+str_UUI);}
			
			
			//Newly added to disable or enable host response files
			String generateXML = util.isNullOrEmpty(callInfo.getField(Field.GENERATEHOSTXML)) ? Constants.N : (String)callInfo.getField(Field.GENERATEHOSTXML);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Is to generate host xml files ? "+generateXML);}
			//END
			
			response = fundsTransferRemittService.callFundsTransferRemittHost(logger, sessionID, xferID, debitAmt, creditAmt, debitValueDate, acctID, 
					accWithBank, bankCode, customerRate, paymentDetails,  purposeCode,  txnCode,
					fullName, nostroBankName, gsmNo, benfCode,  benfCustomer,  purchaseBenfAcctNo, 
					benfLocation, benfBranch, benfAcctID, ccyRate, requestType, str_UUI, generateXML, callInfo);   

			ws_ResponseHeader = DAOLayerUtils.getWsResponseStatus(sessionID,response, ws_ResponseHeader);
			String code = ws_ResponseHeader.getEsbErrCode();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Host error code of callCreditCardBalanceHost is : "+code);}
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
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Host error Short description for getFTTOBMUpdatePaymentOHostRes is : "+ws_ResponseHeader.getEsbErrDesc());}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID,"### FundsTransferRemitt HOST RESPONSE DETAILS ###" +"CALL ID = "+str_UUI+"### RESPONSE CODE = "+beanResponse.getHostResponseCode() + "### RESPONSE DESC = " + beanResponse.getErrorDesc());}

			ICEFeatureData ivr_ICEFeatureData = util.isNullOrEmpty(callInfo.getICEFeatureData())? null : (ICEFeatureData)callInfo.getICEFeatureData();
			if(util.isNullOrEmpty(ivr_ICEFeatureData)){
				throw new ServiceException("ivr_ICEFeatureData object is null");
			}
			
			String hostErrorCodeList = Constants.EMPTY_STRING;
			
			if(!util.isNullOrEmpty(ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_FundTransferRemitt_Succ_ErrorCode))){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Success error code received from the Feature ");}
				hostErrorCodeList = (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_FundTransferRemitt_Succ_ErrorCode); 
			}
			else{
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Success error code received from the Global ");}
				hostErrorCodeList = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_FundTransferRemitt_Succ_ErrorCode);
			}
			
			
			
			
			code = util.isCodePresentInTheList(code, hostErrorCodeList,callInfo)?Constants.WS_SUCCESS_CODE:Constants.WS_FAILURE_CODE;
			beanResponse.setErrorCode(code);
			String hostEndTime = util.getCurrentDateTime();
			beanResponse.setHostEndTime(hostEndTime);

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The host response error code for Application layer is "+code);}

			if(Constants.WS_SUCCESS_CODE.equals(code)){
				if(!util.isNullOrEmpty(response)){
					DebitInfoType debitInfoType = response.getDebitInfo();
					
					if(!util.isNullOrEmpty(debitInfoType)){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Debit Ccy Mkt is "+debitInfoType.getCcyMktDr());}
						beanResponse.setDebitCcyMkt(debitInfoType.getCcyMktDr());
					}
					
					XferRecType xferRecType = response.getXferRec();
					if(!util.isNullOrEmpty(xferRecType)){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## XFER ID is "+xferRecType.getXferId());}
						beanResponse.setXferID(xferRecType.getXferId());
					}
				}
			}else{
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## XferRemitt Service Response field Received null / empty so setting error code as 1");}

				beanResponse.setErrorCode(Constants.WS_FAILURE_CODE);
			}
		}catch(PersistenceException pe){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, pe,  "There was an error at FundsTransferRemittDAOImpl.getFTTOBMUpdatePaymentOHostRes() "	+ pe.getMessage());}
			throw new DaoException(pe);
		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at FundsTransferRemittDAOImpl.getFTTOBMUpdatePaymentOHostRes() "	+ e.getMessage());}
			throw new DaoException(e);
		}

		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** EXIT - DISPACHING WS RES ***: FundsTransferRemittDAOImpl.getFTTOBMUpdatePaymentOHostRes()");}
		return beanResponse;
	}

	@Override
	public TPR_UpdatePaymentDetails_HostRes getTPRUpdatePaymentHostRes(
			CallInfo callInfo, String xferID, BigDecimal debitAmt,
			BigDecimal creditAmt, XMLGregorianCalendar debitValueDate,
			String acctID, String accWithBank, String bankCode,
			BigDecimal customerRate, String paymentDetails, String purposeCode,
			int txnCode, String fullName, String nostroBankName, String gsmNo,
			String benfCode, String benfCustomer, String purchaseBenfAcctNo,
			String benfLocation, String benfBranch, String benfAcctID,
			BigDecimal ccyRate, String requestType) throws DaoException {

		try{logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, (String)callInfo.getField(Field.SESSIONID));}catch(Exception e){}
		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** ENTER - DISPACHING WS RES ***: FundsTransferRemittDAOImpl.getTPRUpdatePaymentHostRes()");}

		TPR_UpdatePaymentDetails_HostRes beanResponse = new TPR_UpdatePaymentDetails_HostRes();

		try{
			String sessionID = (String)callInfo.getField(Field.SESSIONID);

			if(util.isNullOrEmpty(sessionID))
				throw new DaoException("Session ID is null / empty");

			XferRemittResType response = null;

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Calling the DAO Layer method callFundsTransferRemittHost host");}

			String str_UUI = util.isNullOrEmpty(callInfo.getField(Field.UUI)) ? Constants.DEFAULT : (String)callInfo.getField(Field.UUI);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The UUI value is "+str_UUI);}
			
			//Newly added to disable or enable host response files
			String generateXML = util.isNullOrEmpty(callInfo.getField(Field.GENERATEHOSTXML)) ? Constants.N : (String)callInfo.getField(Field.GENERATEHOSTXML);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Is to generate host xml files ? "+generateXML);}
			//END
			
			response = fundsTransferRemittService.callFundsTransferRemittHost(logger, sessionID, xferID, debitAmt, creditAmt, debitValueDate, acctID, 
					accWithBank, bankCode, customerRate, paymentDetails,  purposeCode,  txnCode,
					fullName, nostroBankName, gsmNo, benfCode,  benfCustomer,  purchaseBenfAcctNo, 
					benfLocation, benfBranch, benfAcctID, ccyRate, requestType, str_UUI, generateXML, callInfo);   

			ws_ResponseHeader = DAOLayerUtils.getWsResponseStatus(sessionID,response, ws_ResponseHeader);
			String code = ws_ResponseHeader.getEsbErrCode();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Host error code of callFundsTransferRemittHost is : "+code);}
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
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Host error Short description for getTPRUpdatePaymentHostRes is : "+ws_ResponseHeader.getEsbErrDesc());}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID,"### FundsTransferRemitt HOST RESPONSE DETAILS ###" +"CALL ID = "+str_UUI+"### RESPONSE CODE = "+beanResponse.getHostResponseCode() + "### RESPONSE DESC = " + beanResponse.getErrorDesc());}
			
			ICEFeatureData ivr_ICEFeatureData = util.isNullOrEmpty(callInfo.getICEFeatureData())? null : (ICEFeatureData)callInfo.getICEFeatureData();
			if(util.isNullOrEmpty(ivr_ICEFeatureData)){
				throw new ServiceException("ivr_ICEFeatureData object is null");
			}
			
			String hostErrorCodeList = Constants.EMPTY_STRING;
			
			if(!util.isNullOrEmpty(ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_FundTransferRemitt_Succ_ErrorCode))){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Success error code received from the Feature ");}
				hostErrorCodeList = (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_FundTransferRemitt_Succ_ErrorCode); 
			}
			else{
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Success error code received from the Global ");}
				hostErrorCodeList = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_FundTransferRemitt_Succ_ErrorCode);
			}
			
			
			
			code = util.isCodePresentInTheList(code, hostErrorCodeList,callInfo)?Constants.WS_SUCCESS_CODE:Constants.WS_FAILURE_CODE;
			beanResponse.setErrorCode(code);
			String hostEndTime = util.getCurrentDateTime();
			beanResponse.setHostEndTime(hostEndTime);

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The host response error code for Application layer is "+code);}

			if(Constants.WS_SUCCESS_CODE.equals(code)){
				if(!util.isNullOrEmpty(response)){
					
					XferRecType xferRecType = response.getXferRec();
					if(!util.isNullOrEmpty(xferRecType)){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## XFER ID is "+xferRecType.getXferId());}
						beanResponse.setXferID(xferRecType.getXferId());
						
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Credited Amount is "+xferRecType.getAmtCredited());}
						beanResponse.setCreditedAmt(xferRecType.getAmtCredited());
						
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Debited Amount is "+xferRecType.getAmtDebited());}
						beanResponse.setDebitedAmt(xferRecType.getAmtDebited());
					}
				}
			}else{
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## XferRemitt Service Response field Received null / empty so setting error code as 1");}

				beanResponse.setErrorCode(Constants.WS_FAILURE_CODE);
			}
		}catch(PersistenceException pe){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, pe,  "There was an error at FundsTransferRemittDAOImpl.getTPRUpdatePaymentHostRes() "	+ pe.getMessage());}
			throw new DaoException(pe);
		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at FundsTransferRemittDAOImpl.getTPRUpdatePaymentHostRes() "	+ e.getMessage());}
			throw new DaoException(e);
		}

		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** EXIT - DISPACHING WS RES ***: FundsTransferRemittDAOImpl.getTPRUpdatePaymentHostRes()");}
		return beanResponse;
	}

}
