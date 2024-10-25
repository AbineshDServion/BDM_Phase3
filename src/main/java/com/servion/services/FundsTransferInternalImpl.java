package com.servion.services;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.servion.common.util.LoggerObject;
import com.servion.common.util.WriteLog;
import com.servion.common.util.util;
import com.servion.exception.ServiceException;
import com.servion.ice.RuleEngine.ICEFeatureData;
import com.servion.model.CallInfo;
import com.servion.model.Constants;
import com.servion.model.Field;
import com.servion.model.callerIdentification.AcctInfo;

public class FundsTransferInternalImpl implements IFundsTransferInternal{
	
	private static Logger logger = LoggerObject.getLogger();

	public void getConfigurationParam(CallInfo callInfo)throws ServiceException{
		//Since its a setting configuration param to call info session  variable don't throw any new exception
		String session_ID_ = "";try{session_ID_ = (String)callInfo.getField(Field.SESSIONID);logger = (Logger)callInfo.getField(Field.LOGGER);WriteLog.loggerInit(logger, session_ID_);}catch(Exception e){}
		try{
			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "ENTER: FundsTransferInternalImpl.getConfigurationParam()");}

			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Fetching the Feature Object values");}
			ICEFeatureData ivr_ICEFeatureData = (ICEFeatureData) callInfo.getICEFeatureData();

			ArrayList<String>ValidAcctTypesForTrans = new ArrayList<String>();
			String TransactionFee = Constants.ZERO;
			double PerDayTransAmtLimit = Constants.GL_ZERO;
			double PerTransactionAmtLimit = Constants.GL_ZERO;
			boolean EnableCrossCurrencyTrans = false;
			ArrayList<String> FromCurrencyTypes = new ArrayList<String>();
			String DefaultToCurrencyType = Constants.EMPTY_STRING;
			HashMap<String, ArrayList<String>> ToCurrSpecificToFromCurr = new HashMap<String, ArrayList<String>>();
			
			if(!util.isNullOrEmpty(ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_ValidAcctTypesForTrans))){
				ValidAcctTypesForTrans = (ArrayList<String>) ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_ValidAcctTypesForTrans);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting Valid Acct Types For Internal Trans fields and Order to the a local variable");}
			}
			
			if(!util.isNullOrEmpty(ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_FromCurrencyTypes))){
				FromCurrencyTypes = (ArrayList<String>) ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_FromCurrencyTypes);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting From Currency Types to the a local variable");}
			}
			
			if(!util.isNullOrEmpty(ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_ToCurrSpecificToFromCurr))){
				ToCurrSpecificToFromCurr = (HashMap<String, ArrayList<String>>) ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_ToCurrSpecificToFromCurr);
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Setting To Currency Types to the a local variable");}
			}
			
			
			/**
			 * Getting the customer category type from the accountt detail map object
			 */
			String sourceNumber = util.isNullOrEmpty(callInfo.getField(Field.SRCNO))?Constants.EMPTY : (String)callInfo.getField(Field.SRCNO);
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
			
			TransactionFee = util.isNullOrEmpty(ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_TransactionFee + Constants.UNDERSCORE+ customerCategoryType))?
					Constants.ZERO : (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_TransactionFee + Constants.UNDERSCORE+ customerCategoryType);
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Configured Transaction fee amount is "+TransactionFee);}
			
			
//			if(!util.isNullOrEmpty(ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_TransactionFee))){
//				TransactionFee = (double) ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_TransactionFee);
//			}

			if(!util.isNullOrEmpty(ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_PerDayTransAmtLimit))){
				PerDayTransAmtLimit = (double) ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_PerDayTransAmtLimit);
			}

			if(!util.isNullOrEmpty(ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_PerTransactionAmtLimit))){
				PerTransactionAmtLimit = (double) ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_PerTransactionAmtLimit);
			}
			
			if(!util.isNullOrEmpty(ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_EnableCrossCurrencyTrans))){
				EnableCrossCurrencyTrans = (boolean) ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_EnableCrossCurrencyTrans);
			}
			
			if(!util.isNullOrEmpty(ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_DefaultToCurrencyType))){
				DefaultToCurrencyType = (String)ivr_ICEFeatureData.getConfig().getParamValue(Constants.CUI_DefaultToCurrencyType);
			}

			if(!util.isNullOrEmpty(ValidAcctTypesForTrans)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "Valid Acct Types For Trans size is" + ValidAcctTypesForTrans.size());}
			}
			if(!util.isNullOrEmpty(FromCurrencyTypes)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "From Currency Types size is"+FromCurrencyTypes.size());}
			}
			if(!util.isNullOrEmpty(ToCurrSpecificToFromCurr)){
				if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "To Curr SpecificTo From Curr size is"+ToCurrSpecificToFromCurr.size());}
			}
			
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "TransactionFee of CUI is" + TransactionFee);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "PerTransactionAmtLimit of CUI is" + PerTransactionAmtLimit);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "EnableCrossCurrencyTrans of CUI is" + EnableCrossCurrencyTrans);}
			if(logger.isDebugEnabled()){WriteLog.write(WriteLog.DEBUG,session_ID_, "DefaultToCurrencyType of CUI is" + DefaultToCurrencyType);}
			
			
			callInfo.setField(Field.ValidAcctTypesForTrans, ValidAcctTypesForTrans);
			callInfo.setField(Field.TransactionFee, TransactionFee);
			callInfo.setField(Field.PerDayTransAmtLimit, PerDayTransAmtLimit);
			callInfo.setField(Field.PerTransactionAmtLimit, PerTransactionAmtLimit);
			callInfo.setField(Field.EnableCrossCurrencyTrans, EnableCrossCurrencyTrans);
			callInfo.setField(Field.FromCurrencyTypes, FromCurrencyTypes);
			callInfo.setField(Field.DefaultToCurrencyType, DefaultToCurrencyType);
			callInfo.setField(Field.ToCurrSpecificToFromCurr, ToCurrSpecificToFromCurr);

			if(logger.isInfoEnabled()){WriteLog.write(WriteLog.INFO,session_ID_, "EXIT: FundsTransferInternalImpl.getConfigurationParam()");}
		}catch(Exception e){
			if(logger.isDebugEnabled()){WriteLog.writeError(WriteLog.ERROR, e,  "There was an error at FundsTransferInternalImpl.getConfigurationParam() "	+ e.getMessage());
			throw new ServiceException(e);
			}
		}

	}

}
