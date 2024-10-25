package com.servion.dao;

import javax.xml.datatype.XMLGregorianCalendar;

import com.servion.model.CallInfo;
import com.servion.model.sendSMS.SendingSMS_HostRes;
import com.servion.ws.exception.DaoException;

public interface SMSNotificationDAO {
	/**
	 * @param userID
	 * @param password
	 * @param deptID
	 * @param langID
	 * @param transactionDate
	 * @param sendAsChannel
	 * @param smsTemplateID
	 * @param gsmNo
	 * @param msgTxt
	 * @return
	 * @throws DaoException
	 */
	
	SendingSMS_HostRes getSendSMSHostRes(CallInfo callInfo, String userID, String password, String appID, String deptID, int langID, 
			XMLGregorianCalendar transactionDate, String sendAsChannel, int smsTemplateID, long gsmNo, String msgTxt, String requestType)throws DaoException;

}
