package com.servion.services;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.db.dataservices.DataServices;
import com.db.resource.DBConstants;
import com.db.resource.VRUDBDataServicesInstance;
import com.servion.common.util.LoggerObject;
import com.servion.common.util.WriteLog;
import com.servion.common.util.util;
import com.servion.exception.ServiceException;
import com.servion.ice.RuleEngine.ICEGlobalConfig;
import com.servion.model.CallInfo;
import com.servion.model.Constants;
import com.servion.model.Field;

public class TransferToAgentImpl implements ITransferToAgent{
	private static Logger logger = LoggerObject.getLogger();

	@Override
	public String logMobileNumber(CallInfo arg0) throws ServiceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isANIAMobileNo(CallInfo arg0) throws ServiceException {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public String insertCallAbandon(CallInfo callInfo) throws ServiceException {

		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		try {
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: TransferToAgentImpl.insertCallAbandon()");}

			/**
			 * Following are condition handling for the CR "Call On Abondan time range check"
			 */
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG, session_ID_,"Fetching the Feature Object values");}
			ICEGlobalConfig ivr_ICEGlobalConfig = (ICEGlobalConfig)callInfo.getICEGlobalConfig();

			if(util.isNullOrEmpty(ivr_ICEGlobalConfig)){
				throw new ServiceException("ICEGlobalConfig object is null or empty");
			}
			else{
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG, session_ID_,"Retrieved Object value from ICEGlobalConfig");}
			}


			/***Declaration ***/

			//The sample value would be like "0300~0500|1200~2000|2100~2400"

//			if(util.isNullOrEmpty(ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CALLONABONDANTIMERANGE))){
//				throw new ServiceException("ICE Global Config object is null or empty");
//			}

			String timeLimits= util.isNullOrEmpty(ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CALLONABONDANTIMERANGE))? Constants.DEFAULT_CALLONABANDONT_TIME : 
				(String)ivr_ICEGlobalConfig.getConfig().getParamValue(Constants.CUI_CALLONABONDANTIMERANGE);
			
			String currentHour = "",startTime="",endTime="";
			int intCurrentTime,intStartTime,intEndTime;
			Date currentDate = null;
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Configured time slot is "+ timeLimits);}
			
			if(!util.isNullOrEmpty(timeLimits)){

				/****Calculating Current Hour****/	
				currentDate = new Date();
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "currentDate :"+currentDate);}
				SimpleDateFormat spdf = new SimpleDateFormat("HHmm");
				currentHour = spdf.format(currentDate);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "currentHour :"+currentHour);}

				/****Getting the time limit to provide call on abandon*****/
				String[] timeSlot = timeLimits.split("\\|");
				for(int i = 0; i<timeSlot.length;i++){
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "timeSlot[i] :"+timeSlot[i]);}
					String[] startEndTime = timeSlot[i].split("\\*");

					startTime=startEndTime[0];
					endTime=startEndTime[1];

					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "startTime :"+ startTime);}
					if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "endTime :" + endTime);}
					intCurrentTime = Integer.parseInt(currentHour);
					intStartTime  = Integer.parseInt(startTime);
					intEndTime  = Integer.parseInt(endTime);

					if(intCurrentTime>intStartTime && intEndTime>intCurrentTime){
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Time range are within the limit starting the DB process");}

						String code = Constants.ONE;

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
							code = dataServices.insertCallAbandon(logger, sessionId, uui, configMap);

						} catch (com.db.exception.ServiceException e) {
							if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ERROR: TransferToAgentImpl.insertCallAbandon()");}
							code = Constants.ONE;
							//e.printStackTrace();
						}

						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Final Result of the DB method call is " + code );}
						if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "Exit:  CallerTransferOnAbandonImpl.updateCallerPresentedRule()");}

						return code;

					}else{
						if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG, session_ID_,"Time range are out of the limit hence skipping DB process");}
					}
				}
			}

		} catch (Exception e) {
			WriteLog.writeError(WriteLog.ERROR, e,   "EXCEPTION at:  TransferToAgentImpl.insertCallAbandon()" + e.getMessage());
			throw new ServiceException(e);
		}
		
		return Constants.ONE;
	}


}
