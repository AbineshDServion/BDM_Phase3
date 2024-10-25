package com.servion.services;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.db.dataservices.DataServices;
import com.db.resource.DBConstants;
import com.db.resource.VRUDBDataServicesInstance;
import com.reportbean.IvrData;
import com.servion.common.util.LoggerObject;
import com.servion.common.util.WriteLog;
import com.servion.common.util.util;
import com.servion.exception.ServiceException;
import com.servion.ice.RuleEngine.ICERuleParam;
import com.servion.model.CallInfo;
import com.servion.model.Constants;
import com.servion.model.Field;
import com.servion.model.reporting.HostReportDetails;

public class PromotionsImpl implements IPromotions {
	private static Logger logger = LoggerObject.getLogger();

	@Override
	public String UpdatePromotionOffered(CallInfo callInfo) throws ServiceException {

		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		try {
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: PromotionsImpl.UpdatePromotionOffered()");}

			String code = Constants.ONE;
			String sessionId = (String)callInfo.getField(Field.SESSIONID);
			String customerID =(String)callInfo.getField(Field.CUSTOMERID);
			Date date =new Date();
			SimpleDateFormat spdf = new SimpleDateFormat(Constants.DATEFORMAT_YYYYMMDDHHMMSS);
			String currDate =spdf.format(date);
			String promotionID=(String)callInfo.getField(Field.PromotionID);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Calling the DB Method ");}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "customer ID :"+customerID);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "current Date :"+currDate);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "promotion ID :"+promotionID);}

			HashMap<String, Object> configMap = new HashMap<String, Object>();

			String mobileNo = util.isNullOrEmpty(callInfo.getField(Field.ANI)) ? Constants.EMPTY_STRING : (String)callInfo.getField(Field.ANI); 
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "ANI is "+mobileNo);}
			
			configMap.put(DBConstants.MOBILE_NO,mobileNo);
			configMap.put(DBConstants.DATETIME,currDate);
			configMap.put(DBConstants.PROMOTIONID,promotionID);

			String uui = (String)callInfo.getField(Field.UUI);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "UUI of the call is " + uui);}


			/**
			 * For Reporting Purpose
			 */
			HostReportDetails hostReportDetails = new HostReportDetails();
			
			String featureId = (String)callInfo.getField(Field.FEATUREID);
			hostReportDetails.setHostActiveMenu(featureId);
			//hostReportDetails.setHostCounter(hostCounter);
			//hostReportDetails.setHostEndTime(hostEndTime);
//			String sourceNo = util.isNullOrEmpty(callInfo.getField(Field.SRCNO))? Constants.NA : (String)callInfo.getField(Field.SRCNO);
			
			String strHostInParam = Constants.HOST_INPUT_PARAM_CUSTOMERID+ Constants.EQUALTO + customerID 
					+Constants.COMMA + Constants.HOST_INPUT_PARAM_PROMOTION_TYPE + Constants.EQUALTO + promotionID
					+Constants.COMMA + Constants.HOST_INPUT_PARAM_IS_COMPLETED + Constants.EQUALTO + Constants.PRMOTION_TYPE_COMPLETED
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBREQREFNUM +Constants.EQUALTO +( util.isNullOrEmpty(callInfo.getField(Field.ESBREQREFNUM)) ?"NA" :callInfo.getField(Field.ESBREQREFNUM)) ;
//					+Constants.COMMA + Constants.HOST_INPUT_PARAM_SOURCE_NO + Constants.EQUALTO + callInfo.getField(Field.SRCNO);
			hostReportDetails.setHostInParams(strHostInParam);
			hostReportDetails.setHostMethod(Constants.HOST_METHOD_DB_PROMOTION);
			//hostReportDetails.setHostOutParams(hostOutParams);
			hostReportDetails.setHostReserve1(Constants.NO_RESPONSE_FROM_HOST);
			hostReportDetails.setHostReserve2(Constants.NO_RESPONSE_FROM_HOST);
			hostReportDetails.setHostReserve3(Constants.NO_RESPONSE_FROM_HOST);
			hostReportDetails.setHostReserve4(Constants.NO_RESPONSE_FROM_HOST);
			
			hostReportDetails.setHostStartTime(util.getCurrentDateTime()); //It should be in the format of 31/07/2013 18:11:11
			hostReportDetails.setHostType(Constants.HOST_TYPE_DATABASE);
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
			
			DataServices dataServices = VRUDBDataServicesInstance.getInstance();
			try {
				code = dataServices.updatePromotion(logger, sessionId, uui, configMap);

			} catch (com.db.exception.ServiceException e) {
				if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ERROR:  PromotionsImpl.UpdatePromotionOffered()");}
				code = Constants.ONE;
				//e.printStackTrace();
			}
			
			/****Duplicate RRN Fix 25012016 *****/
			strHostInParam = Constants.HOST_INPUT_PARAM_CUSTOMERID+ Constants.EQUALTO + customerID 
					+Constants.COMMA + Constants.HOST_INPUT_PARAM_PROMOTION_TYPE + Constants.EQUALTO + promotionID
					+Constants.COMMA + Constants.HOST_INPUT_PARAM_IS_COMPLETED + Constants.EQUALTO + Constants.PRMOTION_TYPE_COMPLETED
					+ Constants.COMMA + Constants.HOST_INPUT_PARAM_ESBREQREFNUM +Constants.EQUALTO +( util.isNullOrEmpty(callInfo.getField(Field.ESBREQREFNUM)) ?"NA" :callInfo.getField(Field.ESBREQREFNUM)) ;
			hostReportDetails.setHostInParams(strHostInParam);
			/************************************/

			String hostOutputParam = Constants.HOST_OUTPUT_PARAM_RESPONSEDESC + Constants.EQUALTO + code +Constants.COMMA + Constants.HOST_OUTPUT_PARAM_RESPONSECODE
					+ Constants.EQUALTO + code;
				
			String hostEndTime = util.getCurrentDateTime();
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host End time is is " + hostEndTime);}
			hostReportDetails.setHostEndTime(hostEndTime);
			
			String hostResCode = code;
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Actual Host response code is " + hostResCode);}
			hostReportDetails.setHostResponse(hostResCode);
			hostReportDetails.setHostOutParams(hostOutputParam);
			
			callInfo.setHostReportDetails(hostReportDetails);
			ivrdata = (IvrData)callInfo.getField(Field.IVRDATA);
			
			callInfo.updateHostDetails(ivrdata);
			
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Final Result of the DB method call is " + code );}
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "Exit:  PromotionsImpl.UpdatePromotionOffered()");}

			return code;
		} catch (Exception e) {
			WriteLog.writeError(WriteLog.ERROR, e,   "EXCEPTION at:  PromotionsImpl.UpdatePromotionOffered" + e.getMessage());
			throw new ServiceException(e);
		}
	}

	@Override
	public String getPromotions(CallInfo callInfo) throws ServiceException {

		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: PromotionsImpl.getPromotions()");}
		String finalResult=Constants.ONE;

		try{
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Rule Object values");}
			//ICEFeatureData ivr_ICEFeatureData = (ICEFeatureData) callInfo.getICEFeatureData();

			ICERuleParam iceRuleParam = (ICERuleParam) callInfo.getICERuleParam();

			if(util.isNullOrEmpty(iceRuleParam)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "***********Rule Engine Object is null or empty*************" + iceRuleParam);}
			}

			String isafterMainMenu  =  util.isNullOrEmpty(callInfo.getField(Field.isAfterMainMenu))? Constants.FALSE : (String)callInfo.getField(Field.isAfterMainMenu);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Is After Main Menu" + isafterMainMenu);}
			
			
			List<String> promotionType = null;
			List<String> promotionID = null;
			List<String> agentVDN = null;
			List<String> promotionPhrase = null;
			
			if(Constants.FALSE.equalsIgnoreCase(isafterMainMenu)){
				promotionType = (List<String>)iceRuleParam.getParam(Constants.RULE_ENGINE_PROMOTIONTYPE_ALERT);
				promotionID = (List<String>)iceRuleParam.getParam(Constants.RULE_ENGINE_PROMOTIONID_ALERT);
				agentVDN = (List<String>)iceRuleParam.getParam(Constants.RULE_ENGINE_AGENTVDNNO_ALERT);
				promotionPhrase = (List<String>)iceRuleParam.getParam(Constants.RULE_ENGINE_PHRASEFILE_ALERT);
			}
			else{
				
				promotionType = (List<String>)iceRuleParam.getParam(Constants.RULE_ENGINE_PROMOTIONTYPE);
				promotionID = (List<String>)iceRuleParam.getParam(Constants.RULE_ENGINE_PROMOTIONID);
				agentVDN = (List<String>)iceRuleParam.getParam(Constants.RULE_ENGINE_AGENT_VDNNO);
				promotionPhrase = (List<String>)iceRuleParam.getParam(Constants.RULE_ENGINE_PHRASEFILE);
			}

			//Collected values from Rule 
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Promotion Type :" +promotionType);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "promotionID :" +promotionID);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "agentVDN : " +agentVDN);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Promotion Phrases : " +promotionPhrase);}


			String str_PromotionType = Constants.EMPTY;
			String strPromotionId = Constants.EMPTY;
			String str_AgentVdn = Constants.EMPTY;
			String str_PromotionPhrase = Constants.EMPTY;

			if(promotionType!=null && promotionType.size() > Constants.GL_ZERO){
				str_PromotionType = promotionType.get(Constants.GL_ZERO);
			}

			if(promotionID!=null && promotionID.size() > Constants.GL_ZERO){
				strPromotionId = promotionID.get(Constants.GL_ZERO);
			}

			if(agentVDN!=null && agentVDN.size() > Constants.GL_ZERO){
				str_AgentVdn = agentVDN.get(Constants.GL_ZERO);
			}
			
			if(promotionPhrase!=null && promotionPhrase.size() > Constants.GL_ZERO){
				str_PromotionPhrase = promotionPhrase.get(Constants.GL_ZERO);
			}


			callInfo.setField(Field.PromotionType,str_PromotionType);
			callInfo.setField(Field.PromotionTypePhrases,str_PromotionPhrase);
			callInfo.setField(Field.PromotionID,strPromotionId);
			callInfo.setField(Field.PromotionVDN, str_AgentVdn);

			finalResult =Constants.ZERO;
			
		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.ERROR,session_ID_,"There was an error at  Promotional message : "	+ e.getMessage());}
			throw new ServiceException(e);
		}
		return finalResult;
	}

}
