package com.servion.dao.impl;


import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.bankmuscat.esb.miscellaneousservice.ExchgRateInfoType;
import com.bankmuscat.esb.miscellaneousservice.ExchgRateInqResType;
import com.servion.common.util.LoggerObject;
import com.servion.common.util.WriteLog;
import com.servion.common.util.util;
import com.servion.dao.ExchngRateInqDAO;
import com.servion.exception.ServiceException;
import com.servion.ice.RuleEngine.ICEFeatureData;
import com.servion.ice.RuleEngine.ICEGlobalConfig;
import com.servion.model.CallInfo;
import com.servion.model.Constants;
import com.servion.model.Field;
import com.servion.model.exchangeRates.ExchangeRateInquiryDtls;
import com.servion.model.exchangeRates.ExchangeRateInquiry_HostRes;
import com.servion.model.fundsTransfer.FT_ExchangeRateDetails;
import com.servion.model.fundsTransfer.FT_ExchangeRateDetails_HostRes;
import com.servion.model.thirdPartyRemittance.TPR_ExchangeRateDetails_HostRes;
import com.servion.model.thirdPartyRemittance.TPR_ExchangeRateInquiryDtls;
import com.servion.ws.exception.DaoException;
import com.servion.ws.exception.PersistenceException;
import com.servion.ws.header.WS_ResponseHeader;
import com.servion.ws.header.impl.WS_ResponseHeaderImpl;
import com.servion.ws.service.ExchngRateInqService;
import com.servion.ws.util.DAOLayerUtils;

public class ExchngRateInqDAOImpl implements ExchngRateInqDAO{
	private static Logger logger = LoggerObject.getLogger();

	@Autowired
	ExchngRateInqService exchngRateInqService;

	public ExchngRateInqService getExchngRateInqService() {
		return exchngRateInqService;
	}

	public void setExchngRateInqService(ExchngRateInqService exchngRateInqService) {
		this.exchngRateInqService = exchngRateInqService;
	}

	//Not initializing through BEAN Injection
	private WS_ResponseHeader ws_ResponseHeader = new WS_ResponseHeaderImpl();

	@Override
	public FT_ExchangeRateDetails_HostRes getFTExchangeRateHostRes(
			CallInfo callInfo, String currencyCode, String customerID, String ccyMarket,String requestType) throws DaoException {

		try{logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, (String)callInfo.getField(Field.SESSIONID));}catch(Exception e){}
		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** ENTER - DISPACHING WS RES ***: ExchngRateInqDAOImpl.FT_ExchangeRateDetails_HostRes()");}

		FT_ExchangeRateDetails_HostRes beanResponse = new FT_ExchangeRateDetails_HostRes();
		FT_ExchangeRateDetails ft_ExchangeRateDetails = null;
		try{
			String sessionID = (String)callInfo.getField(Field.SESSIONID);

			if(util.isNullOrEmpty(sessionID))
				throw new DaoException("Session ID is null / empty");

			ExchgRateInqResType	response = null;

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Currency code is " + currencyCode);}
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Customer id is " + customerID);}
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "CCY Market value  is " + ccyMarket);}
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Calling the DAO Layer method callCCEntityInquiry host");}

			String str_UUI = util.isNullOrEmpty(callInfo.getField(Field.UUI)) ? Constants.DEFAULT : (String)callInfo.getField(Field.UUI);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The UUI value is "+str_UUI);}
			
			//Newly added to disable or enable host response files
			String generateXML = util.isNullOrEmpty(callInfo.getField(Field.GENERATEHOSTXML)) ? Constants.N : (String)callInfo.getField(Field.GENERATEHOSTXML);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Is to generate host xml files ? "+generateXML);}
			//END
			
			response = exchngRateInqService.callExchangeRateHost(logger, sessionID, currencyCode, customerID, ccyMarket, requestType, str_UUI, generateXML, callInfo);

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
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Host error Short description for getFTExchangeRateHostRes is : "+ws_ResponseHeader.getEsbErrDesc());}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID,"### ExchngRateInq HOST RESPONSE DETAILS ###" +"CALL ID = "+str_UUI+"### RESPONSE CODE = "+beanResponse.getHostResponseCode() + "### RESPONSE DESC = " + beanResponse.getErrorDesc());}
			
			ICEFeatureData ivr_ICEFeatureData = util.isNullOrEmpty(callInfo.getICEFeatureData())? null : (ICEFeatureData)callInfo.getICEFeatureData();
			if(util.isNullOrEmpty(ivr_ICEFeatureData)){
				throw new ServiceException("ivr_ICEFeatureData object is null");
			}
			
			String hostErrorCodeList = Constants.EMPTY_STRING;
			
			if(!util.isNullOrEmpty(ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_ExchngRateInq_Succ_ErrorCode))){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Success error code received from the Feature ");}
				hostErrorCodeList = (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_ExchngRateInq_Succ_ErrorCode); 
			}
			else{
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Success error code received from the Global ");}
				hostErrorCodeList = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_ExchngRateInq_Succ_ErrorCode);
			}
			
			
			
			code = util.isCodePresentInTheList(code, hostErrorCodeList,callInfo)?Constants.WS_SUCCESS_CODE:Constants.WS_FAILURE_CODE;
			beanResponse.setErrorCode(code);
			String hostEndTime = util.getCurrentDateTime();
			beanResponse.setHostEndTime(hostEndTime);

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The host response error code for Application layer is "+code);}

			if(Constants.WS_SUCCESS_CODE.equals(code)){
				if(!util.isNullOrEmpty(response)){

					List<ExchgRateInfoType> exchgRateInfoTypeList = response.getExchgRateInfo();
					ExchgRateInfoType exchgRateInfoType = null;
					HashMap<String, FT_ExchangeRateDetails> exchangeCurrencyMap = new HashMap<String, FT_ExchangeRateDetails>();
					
					if(exchgRateInfoTypeList != null){
						for(int count = 0; count < exchgRateInfoTypeList.size() ; count++){
							ft_ExchangeRateDetails = new FT_ExchangeRateDetails();
							exchgRateInfoType = exchgRateInfoTypeList.get(count);
							
							if(!util.isNullOrEmpty(exchgRateInfoType)){
								
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "GETTING THE EXCHANGE RATE VALUES FOR THE CURRENCY "+exchgRateInfoType.getCcy() + Constants.EMPTY_STRING);}

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## BUY Rate value is "+exchgRateInfoType.getBuyRate() + Constants.EMPTY_STRING);}
								ft_ExchangeRateDetails.setBuyRate(exchgRateInfoType.getBuyRate() + Constants.EMPTY_STRING);

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## CCy Market "+exchgRateInfoType.getCcyMarket());}
								ft_ExchangeRateDetails.setCcyMarket(exchgRateInfoType.getCcyMarket());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Mid Reval Rate "+exchgRateInfoType.getMidRevalRate()+ Constants.EMPTY_STRING);}
								ft_ExchangeRateDetails.setMidRevalRate(exchgRateInfoType.getMidRevalRate()+ Constants.EMPTY_STRING);

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Exchange Rate Info Type "+exchgRateInfoType.getCcy());}
								ft_ExchangeRateDetails.setRequestedCCY(exchgRateInfoType.getCcy());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Sell Rate "+exchgRateInfoType.getSellRate()+ Constants.EMPTY_STRING);}
								ft_ExchangeRateDetails.setSellRate(exchgRateInfoType.getSellRate()+ Constants.EMPTY_STRING);
							}
							
							exchangeCurrencyMap.put(exchgRateInfoType.getCcy(), ft_ExchangeRateDetails);
						}
					}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Formed Exchage Currency Map is "+ ft_ExchangeRateDetails);}
					
					beanResponse.setExchangeRateCurrMap(exchangeCurrencyMap);
				
				}
			}else{
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Exchange Rate Service Response field Received null / empty so setting error code as 1");}

				beanResponse.setErrorCode(Constants.WS_FAILURE_CODE);
			}


		}catch(PersistenceException pe){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, pe,  "There was an error at ExchngRateInqDAOImpl.FT_ExchangeRateDetails_HostRes() "	+ pe.getMessage());}
			throw new DaoException(pe);
		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at ExchngRateInqDAOImpl.FT_ExchangeRateDetails_HostRes() "	+ e.getMessage());}
			throw new DaoException(e);
		}

		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** EXIT - DISPACHING WS RES ***: ExchngRateInqDAOImpl.FT_ExchangeRateDetails_HostRes()");}
		return beanResponse;
	}

	@Override
	public TPR_ExchangeRateDetails_HostRes getTPRemittanceExchangeHostRes(
			CallInfo callInfo, String currencyCode, String customerID, String ccyMarket,String requestType) throws DaoException {

		try{logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, (String)callInfo.getField(Field.SESSIONID));}catch(Exception e){}
		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** ENTER - DISPACHING WS RES ***: ExchngRateInqDAOImpl.getTPRemittanceExchangeHostRes()");}

		TPR_ExchangeRateDetails_HostRes beanResponse = new TPR_ExchangeRateDetails_HostRes();
		TPR_ExchangeRateInquiryDtls exchangeRateInquiryDtls = null;
		try{
			String sessionID = (String)callInfo.getField(Field.SESSIONID);

			if(util.isNullOrEmpty(sessionID))
				throw new DaoException("Session ID is null / empty");

			ExchgRateInqResType	response = null;

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Calling the DAO Layer method callCCEntityInquiry host");}
			String str_UUI = util.isNullOrEmpty(callInfo.getField(Field.UUI)) ? Constants.DEFAULT : (String)callInfo.getField(Field.UUI);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The UUI value is "+str_UUI);}
			
			//Newly added to disable or enable host response files
			String generateXML = util.isNullOrEmpty(callInfo.getField(Field.GENERATEHOSTXML)) ? Constants.N : (String)callInfo.getField(Field.GENERATEHOSTXML);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Is to generate host xml files ? "+generateXML);}
			//END
			
			response = exchngRateInqService.callExchangeRateHost(logger, sessionID, currencyCode, customerID, ccyMarket, requestType, str_UUI, generateXML, callInfo);

			ws_ResponseHeader = DAOLayerUtils.getWsResponseStatus(sessionID,response, ws_ResponseHeader);
			String code = ws_ResponseHeader.getEsbErrCode();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Host error code of callCreditCardBalanceHost is : "+code);}

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
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Host error Short description for getFTExchangeRateHostRes is : "+ws_ResponseHeader.getEsbErrDesc());}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID,"### ExchngRateInq HOST RESPONSE DETAILS ###" +"CALL ID = "+str_UUI+"### RESPONSE CODE = "+beanResponse.getHostResponseCode() + "### RESPONSE DESC = " + beanResponse.getErrorDesc());}
			
			ICEFeatureData ivr_ICEFeatureData = util.isNullOrEmpty(callInfo.getICEFeatureData())? null : (ICEFeatureData)callInfo.getICEFeatureData();
			if(util.isNullOrEmpty(ivr_ICEFeatureData)){
				throw new ServiceException("ivr_ICEFeatureData object is null");
			}
			
			String hostErrorCodeList = Constants.EMPTY_STRING;
			
			if(!util.isNullOrEmpty(ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_ExchngRateInq_Succ_ErrorCode))){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Success error code received from the Feature ");}
				hostErrorCodeList = (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_ExchngRateInq_Succ_ErrorCode); 
			}
			else{
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Success error code received from the Global ");}
				hostErrorCodeList = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_ExchngRateInq_Succ_ErrorCode);
			}
			
			
			code = util.isCodePresentInTheList(code, hostErrorCodeList,callInfo)?Constants.WS_SUCCESS_CODE:Constants.WS_FAILURE_CODE;
			beanResponse.setErrorCode(code);
			String hostEndTime = util.getCurrentDateTime();
			beanResponse.setHostEndTime(hostEndTime);

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The host response error code for Application layer is "+code);}

			if(Constants.WS_SUCCESS_CODE.equals(code)){
				if(!util.isNullOrEmpty(response)){

					List<ExchgRateInfoType> exchgRateInfoTypeList = response.getExchgRateInfo();
					ExchgRateInfoType exchgRateInfoType = null;
					HashMap<String, TPR_ExchangeRateInquiryDtls> exchangeCurrencyMap = new HashMap<String, TPR_ExchangeRateInquiryDtls>();
					
					if(exchgRateInfoTypeList != null){
						for(int count = 0; count < exchgRateInfoTypeList.size() ; count++){
							exchangeRateInquiryDtls = new TPR_ExchangeRateInquiryDtls();
							exchgRateInfoType = exchgRateInfoTypeList.get(count);
							String exchgRateCcy = "";
							String exchgSellRate = "";
							
							if(!util.isNullOrEmpty(exchgRateInfoType)){
								
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "GETTING THE EXCHANGE RATE VALUES FOR THE CURRENCY "+exchgRateInfoType.getCcy() + Constants.EMPTY_STRING);}
								exchgRateCcy = exchgRateInfoType.getCcy();

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## BUY Rate value is "+exchgRateInfoType.getBuyRate() + Constants.EMPTY_STRING);}
								exchangeRateInquiryDtls.setBuyRate(exchgRateInfoType.getBuyRate() + Constants.EMPTY_STRING);

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## CCy Market "+exchgRateInfoType.getCcyMarket());}
								exchangeRateInquiryDtls.setCcyMarket(exchgRateInfoType.getCcyMarket());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Mid Reval Rate "+exchgRateInfoType.getMidRevalRate()+ Constants.EMPTY_STRING);}
								exchangeRateInquiryDtls.setMidRevalRate(exchgRateInfoType.getMidRevalRate()+ Constants.EMPTY_STRING);

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Exchange Rate Info Type "+exchgRateInfoType.getCcy());}
								exchangeRateInquiryDtls.setRequestedCCY(exchgRateInfoType.getCcy());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Sell Rate "+exchgRateInfoType.getSellRate()+ Constants.EMPTY_STRING);}
								
								/****As per vijay comment. If sell rate is null and currency is OMR, then we have to return sell rate as 1 15-03-2015 ***/
								exchgSellRate = exchgRateInfoType.getSellRate()+Constants.EMPTY_STRING;
								if((exchgSellRate == null || exchgSellRate.equalsIgnoreCase(Constants.NULL)) && exchgRateCcy.equalsIgnoreCase(Constants.CURR_TYPE_OMR) ){
									if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Exchange Rate value is null. So reset to 1 "+exchgRateInfoType.getSellRate()+ Constants.EMPTY_STRING);}
									exchgSellRate = Constants.ONE;
								}
								
								exchangeRateInquiryDtls.setSellRate(exchgSellRate);
								/***********************************************************************************************************/
							}
							
							exchangeCurrencyMap.put(exchgRateInfoType.getCcy(), exchangeRateInquiryDtls);
						}
				}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Formed Exchage Currency Map is "+ exchangeRateInquiryDtls);}
					
					beanResponse.setTpr_ExchangeRateCurrMap(exchangeCurrencyMap);
				
				}
			}else{
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Exchange Rate Service Response field Received null / empty so setting error code as 1");}

				beanResponse.setErrorCode(Constants.WS_FAILURE_CODE);
			}


		}catch(PersistenceException pe){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, pe,  "There was an error at ExchngRateInqDAOImpl.getExchangeRatesHostRes() "	+ pe.getMessage());}
			throw new DaoException(pe);
		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at ExchngRateInqDAOImpl.getExchangeRatesHostRes() "	+ e.getMessage());}
			throw new DaoException(e);
		}

		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** EXIT - DISPACHING WS RES ***: ExchngRateInqDAOImpl.getTPRemittanceExchangeHostRes()");}
		return beanResponse;
	}

	@Override
	public ExchangeRateInquiry_HostRes getExchangeRatesHostRes(
			CallInfo callInfo, String currencyCode, String customerID, String ccyMarket, String requestType) throws DaoException {

		try{logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, (String)callInfo.getField(Field.SESSIONID));}catch(Exception e){}
		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** ENTER - DISPACHING WS RES ***: ExchngRateInqDAOImpl.getExchangeRatesHostRes()");}

		ExchangeRateInquiry_HostRes beanResponse = new ExchangeRateInquiry_HostRes();
		ExchangeRateInquiryDtls exchangeRateInquiryDtls = null;
		try{
			String sessionID = (String)callInfo.getField(Field.SESSIONID);

			if(util.isNullOrEmpty(sessionID))
				throw new DaoException("Session ID is null / empty");

			ExchgRateInqResType	response = null;

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Calling the DAO Layer method callCCEntityInquiry host");}
			String str_UUI = util.isNullOrEmpty(callInfo.getField(Field.UUI)) ? Constants.DEFAULT : (String)callInfo.getField(Field.UUI);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The UUI value is "+str_UUI);}
			
			//Newly added to disable or enable host response files
			String generateXML = util.isNullOrEmpty(callInfo.getField(Field.GENERATEHOSTXML)) ? Constants.N : (String)callInfo.getField(Field.GENERATEHOSTXML);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Is to generate host xml files ? "+generateXML);}
			//END
			
			response = exchngRateInqService.callExchangeRateHost(logger, sessionID, currencyCode, customerID, ccyMarket, requestType, str_UUI, generateXML, callInfo);

			ws_ResponseHeader = DAOLayerUtils.getWsResponseStatus(sessionID,response, ws_ResponseHeader);
			String code = ws_ResponseHeader.getEsbErrCode();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Host error code of callCreditCardBalanceHost is : "+code);}

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
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The Host error Short description for getFTExchangeRateHostRes is : "+ws_ResponseHeader.getEsbErrDesc());}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID,"### ExchngRateInq HOST RESPONSE DETAILS ###" +"CALL ID = "+str_UUI+"### RESPONSE CODE = "+beanResponse.getHostResponseCode() + "### RESPONSE DESC = " + beanResponse.getErrorDesc());}

			ICEFeatureData ivr_ICEFeatureData = util.isNullOrEmpty(callInfo.getICEFeatureData())? null : (ICEFeatureData)callInfo.getICEFeatureData();
			if(util.isNullOrEmpty(ivr_ICEFeatureData)){
				throw new ServiceException("ivr_ICEFeatureData object is null");
			}
			
			String hostErrorCodeList = Constants.EMPTY_STRING;
			
			if(!util.isNullOrEmpty(ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_ExchngRateInq_Succ_ErrorCode))){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Success error code received from the Feature ");}
				hostErrorCodeList = (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_CI_ExchngRateInq_Succ_ErrorCode); 
			}
			else{
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Success error code received from the Global ");}
				hostErrorCodeList = (String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CI_ExchngRateInq_Succ_ErrorCode);
			}
			
			
			code = util.isCodePresentInTheList(code, hostErrorCodeList,callInfo)?Constants.WS_SUCCESS_CODE:Constants.WS_FAILURE_CODE;
			beanResponse.setErrorCode(code);
			String hostEndTime = util.getCurrentDateTime();
			beanResponse.setHostEndTime(hostEndTime);

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "The host response error code for Application layer is "+code);}

			if(Constants.WS_SUCCESS_CODE.equals(code)){
				if(!util.isNullOrEmpty(response)){

					List<ExchgRateInfoType> exchgRateInfoTypeList = response.getExchgRateInfo();
					ExchgRateInfoType exchgRateInfoType = null;
					HashMap<String, ExchangeRateInquiryDtls> exchangeCurrencyMap = new HashMap<String, ExchangeRateInquiryDtls>();
					
					if(exchgRateInfoTypeList != null){
						for(int count = 0; count < exchgRateInfoTypeList.size() ; count++){
							exchangeRateInquiryDtls = new ExchangeRateInquiryDtls();
							exchgRateInfoType = exchgRateInfoTypeList.get(count);
							
							if(!util.isNullOrEmpty(exchgRateInfoType)){
								
								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "GETTING THE EXCHANGE RATE VALUES FOR THE CURRENCY "+exchgRateInfoType.getCcy() + Constants.EMPTY_STRING);}

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## BUY Rate value is "+exchgRateInfoType.getBuyRate() + Constants.EMPTY_STRING);}
								exchangeRateInquiryDtls.setBuyRate(exchgRateInfoType.getBuyRate() + Constants.EMPTY_STRING);

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## CCy Market "+exchgRateInfoType.getCcyMarket());}
								exchangeRateInquiryDtls.setCcyMarket(exchgRateInfoType.getCcyMarket());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Mid Reval Rate "+exchgRateInfoType.getMidRevalRate()+ Constants.EMPTY_STRING);}
								exchangeRateInquiryDtls.setMidRevalRate(exchgRateInfoType.getMidRevalRate()+ Constants.EMPTY_STRING);

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Exchange Rate Info Type "+exchgRateInfoType.getCcy());}
								exchangeRateInquiryDtls.setRequestedCCY(exchgRateInfoType.getCcy());

								if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "## RESPONSE FIELD ## Sell Rate "+exchgRateInfoType.getSellRate()+ Constants.EMPTY_STRING);}
								exchangeRateInquiryDtls.setSellRate(exchgRateInfoType.getSellRate()+ Constants.EMPTY_STRING);
							}
							
							exchangeCurrencyMap.put(exchgRateInfoType.getCcy(), exchangeRateInquiryDtls);
						}
					}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, "Formed Exchage Currency Map is "+ exchangeRateInquiryDtls);}
					
					beanResponse.setExchangeRateCurrMap(exchangeCurrencyMap);
				
				}
			}else{
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,sessionID, " ## Exchange Rate Service Response field Received null / empty so setting error code as 1");}

				beanResponse.setErrorCode(Constants.WS_FAILURE_CODE);
			}


		}catch(PersistenceException pe){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, pe,  "There was an error at ExchngRateInqDAOImpl.getExchangeRatesHostRes() "	+ pe.getMessage());}
			throw new DaoException(pe);
		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at ExchngRateInqDAOImpl.getExchangeRatesHostRes() "	+ e.getMessage());}
			throw new DaoException(e);
		}

		//if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO, "*** EXIT - DISPACHING WS RES ***: ExchngRateInqDAOImpl.getExchangeRatesHostRes()");}
		return beanResponse;
	}

}
