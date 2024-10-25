package com.servion.services;

import java.util.HashMap;

import org.apache.log4j.Logger;

import com.db.dataservices.DataServices;
import com.db.resource.DBConstants;
import com.db.resource.VRUDBDataServicesInstance;
import com.servion.common.util.LoggerObject;
import com.servion.common.util.WriteLog;
import com.servion.common.util.util;
import com.servion.exception.ServiceException;
import com.servion.ice.RuleEngine.ICERuleParam;
import com.servion.model.CallInfo;
import com.servion.model.Constants;
import com.servion.model.Field;

public class CallerTransferOnAbandonImpl implements ICallerTransferOnAbandon{
	private static Logger logger = LoggerObject.getLogger();


	@Override
	public String updateCallerPresentedRule(CallInfo callInfo)
			throws ServiceException {

		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		try {
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "A: CallerTransferOnAbandonImpl.updateCallerPresentedRule()");}

			String code = Constants.ONE;
			
			/**
			 * Following are the code handling for call on abandon feature condition check
			 */
			/**
			 * Rule engine update
			 */
			ICERuleParam ruleParamObj = (ICERuleParam)callInfo.getICERuleParam();

			if(util.isNullOrEmpty(ruleParamObj)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "***********Rule Engine Object is null or empty*************" + ruleParamObj);}
			}
			
			String lastSelectedValue = (String)callInfo.getField(Field.LASTSELECTEDVALUE);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "The Last selected value is "+ lastSelectedValue);}
			
			if(Constants.FT_OBM_TRANSFER_BENEFICIARY.equalsIgnoreCase(lastSelectedValue)){
				
			
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting the ruleParamObj variable value of IsCallOnAbandonExpired as TRUE");}
				ruleParamObj.setIVRParam(Constants.RULE_ISCALLONABANDONEXPIRED, Constants.TRUE);
				
				//END Rule Engine Updation


			}else{
				
//				/**
//				 * Rule engine update
//				 */
//				ICERuleParam ruleParamObj = (ICERuleParam)callInfo.getICERuleParam();
//
//				if(util.isNullOrEmpty(ruleParamObj)){
//					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "***********Rule Engine Object is null or empty*************" + ruleParamObj);}
//				}
//				
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting the ruleParamObj variable value of IsCallOnAbandonExpired as false");}
				ruleParamObj.setIVRParam(Constants.RULE_ISCALLONABANDONEXPIRED, "false");
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting the ruleParamObj variable value of IsCallAbandon as false");}
				ruleParamObj.setIVRParam(Constants.RULE_ISCALLONABANDON, "false");
				//END Rule Engine Updation


//				ruleParamObj.updateIVRFields();
//				//End Rule Engine Update
			}
			
			ruleParamObj.updateIVRFields();
			//End Rule Engine Update
			
			String sessionId = util.isNullOrEmpty(callInfo.getField(Field.SESSIONID)) ? Constants.EMPTY_STRING :  (String)callInfo.getField(Field.SESSIONID);
			String cli = util.isNullOrEmpty(callInfo.getField(Field.ANI)) ? Constants.EMPTY_STRING : (String)callInfo.getField(Field.ANI);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "CLI "+ cli);}
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Calling the DB Method ");}

			HashMap<String, Object> configMap = new HashMap<String, Object>();
			configMap.put(DBConstants.CLI,cli);

			String uui = (String)callInfo.getField(Field.UUI);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "UUI of the call is " + uui);}


			DataServices dataServices = VRUDBDataServicesInstance.getInstance();
			try {
				code = dataServices.updateCallAbandon(logger, sessionId, uui, configMap);

			} catch (com.db.exception.ServiceException e) {
				if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ERROR:  CallerTransferOnAbandonImpl.updateCallerPresentedRule()");}
				code = Constants.ONE;
				//e.printStackTrace();
			}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Final Result of the DB method call is " + code );}
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "Exit:  CallerTransferOnAbandonImpl.updateCallerPresentedRule()");}

			return code;
		} catch (Exception e) {
			WriteLog.writeError(WriteLog.ERROR, e,   "EXCEPTION at:   CallerTransferOnAbandonImpl.updateCallerPresentedRule()" + e.getMessage());
			throw new ServiceException(e);
		}
	}

}
