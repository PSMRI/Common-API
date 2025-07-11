/*
* AMRIT – Accessible Medical Records via Integrated Technology 
* Integrated EHR (Electronic Health Records) Solution 
*
* Copyright (C) "Piramal Swasthya Management and Research Institute" 
*
* This file is part of AMRIT.
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program.  If not, see https://www.gnu.org/licenses/.
*/
package com.iemr.common.notification.agent;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonElement;
// import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.iemr.common.notification.agent.DTO.AlertAndNotificationChangeStatusDTO;
import com.iemr.common.notification.agent.DTO.AlertAndNotificationCountDTO;
import com.iemr.common.notification.agent.DTO.AlertAndNotificationSetDeleteDTO;
import com.iemr.common.notification.agent.DTO.SuccessObjectDTO;
import com.iemr.common.notification.agent.DTO.UserNotificationDisplayMaxDTO;
import com.iemr.common.notification.agent.DTO.UserNotificationDisplayMinDTO;
import com.iemr.common.notification.util.InputMapper;

import com.iemr.common.utils.response.OutputResponse;

import io.lettuce.core.dynamic.annotation.Param;



@RequestMapping(value = "/notification")
@RestController
public class UserNotificationMappingController
{

	private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	private OutputResponse response = new OutputResponse();

	@Autowired
	UserNotificationMappingService notificationService;

	@RequestMapping(value = "/getAlertsAndNotificationCount", method = RequestMethod.POST,
			produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE,
			headers = "Authorization")
	public @ResponseBody String
			getAlertsAndNotificationCount(@Param(value = "{" + "\"userID\":\"Integer\"\n"
					+ "\"roleID\":\"Integer\"\n" + "\"providerServiceMapID\":\"Integer\"\n"
					+ "\"workingLocationID\":\"Integer\"\n" + "}") @RequestBody String getNotificationCountFilter)
	{

		logger.info("UserNotificationMappingController.getAlertsAndNotificationCount - start");

		try
		{
			JsonElement json = new JsonParser().parse(getNotificationCountFilter);
			logger.info("UserNotificationMappingController.getAlertsAndNotificationCount : json");
			UserNotificationDisplayMinDTO inputData =
					InputMapper.getInstance().gson().fromJson(json, UserNotificationDisplayMinDTO.class);

			// Send the captured data to Service & get Aggregated Output List
			AlertAndNotificationCountDTO dtoOut = notificationService.getAlertAndNotificationCount(inputData);

			// Generate the response
			// String response = getSuccessResponseString(dtoOut, 200, "Success", "getAlertsAndNotificationCount");

			response.setResponse(InputMapper.getInstance().gson().toJson(dtoOut));

			logger.info("UserNotificationMappingController.getAlertsAndNotificationCount: success - finish");
			return response.toString();
		} catch (Exception e)
		{
			//e.printStackTrace();
			// String response = getErrorResponseString(e.getLocalizedMessage(), 200, "success",
			// "getAlertsAndNotificationCount");
			response.setError(e);
			logger.error("UserNotificationMappingController.getAlertsAndNotificationCount: failure - finish",e.getMessage());
			return response.toString();
		}
	}

	@RequestMapping(value = "/getAlertsAndNotificationDetail", method = RequestMethod.POST,
			produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE,
			headers = "Authorization")
	public @ResponseBody String
			getAlertsAndNotificationDetail(@Param(value = "{" + "\"userID\":\"Integer\"\n"
					+ "\"roleID\":\"Integer\"\n" + "\"notificationTypeID\":\"Integer\"\n"
					+ "\"providerServiceMapID\":\"Integer\"\n" + "\"workingLocationID\":\"Integer\"\n"
					+ "}") @RequestBody String getNotificationDetailFilter)
	{
		OutputResponse output = new OutputResponse();
		logger.info("UserNotificationMappingController.getAlertsAndNotificationDetail - start");
		try
		{
			JsonElement json = new JsonParser().parse(getNotificationDetailFilter);
			UserNotificationDisplayMaxDTO inputData =
					InputMapper.getInstance().gson().fromJson(json, UserNotificationDisplayMaxDTO.class);

			// Send the captured data to Service to get Aggregated Output List
			List<UserNotificationMapping> list = notificationService.getAlertAndNotificationDetail(inputData);
			// AlertAndNotificationDetailDTO list = notificationService.getAlertAndNotificationDetail(inputData);
			System.out.println("hello");

			// Generate the response
			// String response = getSuccessResponseString(list, 200, "success", "getAlertsAndNotificationDetail");
			// output.setResponse(OutputMapper.gsonWithoutExposeRestriction().toJson(list));
			output.setResponse(list.toString());
			// response.setResponse(InputMapper.getInstance().gson().toJson(list));
			// response.setResponse(OutputMapper.gsonWithoutExposeRestriction().toJson(list));
			logger.info("UserNotificationMappingController.getAlertsAndNotificationDetail: success - finish");
			// return response.toString();

		} catch (Exception e)
		{
			// e.printStackTrace();
			// String response = getErrorResponseString(e.getLocalizedMessage(), 200, "success",
			// "getAlertsAndNotificationDetail");

			// response.setError(e);
			output.setError(e);
			// logger.info("UserNotificationMappingController.getAlertsAndNotificationDetail: failure - finish");
			// return response.toString();
		}
		return output.toString();
	}

	@RequestMapping(value = "/changeNotificationStatus", method = RequestMethod.POST,
			produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE,
			headers = "Authorization")
	public @ResponseBody String changeNotificationStatus(@Param(value = "{" + "\"notficationStatus\":\"String\"\n"
			+ "\"notificationMapIDList\":\"List<Integer>\"\n" + "}") @RequestBody String changeNotificationStatusFilter)
	{

		logger.info("UserNotificationMappingController.changeNotificationStatus - start");

		try
		{
			JsonElement json = new JsonParser().parse(changeNotificationStatusFilter);
			AlertAndNotificationChangeStatusDTO dto =
					InputMapper.getInstance().gson().fromJson(json, AlertAndNotificationChangeStatusDTO.class);

			if (dto.getUserNotificationMapIDList().size() == 1)
			{
				notificationService.markNotificationSingle(dto.getNotficationStatus(),
						dto.getUserNotificationMapIDList().get(0));
			} else if (dto.getUserNotificationMapIDList().size() > 1)
			{
				notificationService.markNotificationList(dto.getNotficationStatus(),
						dto.getUserNotificationMapIDList());
			} else
			{
				// SUNIL TODO: alternatively throw missing mandatory parameter exception
				// String response = getErrorResponseString("Missing mandatory Parameter - at least 1 NotificationMapId
				// needed.", 200, "success", "changeNotificationStatus");

				response.setError(new Throwable("Missing mandatory Parameter - at least 1 NotificationMapId needed."));
				logger.info("UserNotificationMappingController.changeNotificationStatus: failure - finish");
				return response.toString();
			}

			// String response = getSuccessResponseString("Success", 200, "success", "changeNotificationStatus");

			SuccessObjectDTO obj = new SuccessObjectDTO();
			obj.setOperation(dto.getNotficationStatus());
			obj.setStatus("success");
			response.setResponse(InputMapper.getInstance().gson().toJson(obj));
			logger.info("UserNotificationMappingController.changeNotificationStatus: success - finish");
			return response.toString();
		} catch (Exception e)
		{
			// String response = getErrorResponseString(e.getLocalizedMessage(), 200, "success",
			// "changeNotificationStatus");

			response.setError(e);
			logger.error("UserNotificationMappingController.changeNotificationStatus: failure - finish");
			return response.toString();
		}
	}

	@RequestMapping(value = "/markDelete", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE,
			consumes = MediaType.APPLICATION_JSON_VALUE, headers = "Authorization")
	public @ResponseBody String markDelete(@Param(value = "{" + "\"isDeleted\":\"Boolean\"\n"
			+ "\"userNotificationMapIDList\":\"List<Integer>\"\n" + "}") @RequestBody String markDeleteFilter)
	{

		logger.info("UserNotificationMappingController.markDelete - start");

		try
		{
			JsonElement json = new JsonParser().parse(markDeleteFilter);
			AlertAndNotificationSetDeleteDTO dto =
					InputMapper.getInstance().gson().fromJson(json, AlertAndNotificationSetDeleteDTO.class);

			if (dto.getUserNotificationMapIDList().size() == 1)
			{
				notificationService.deleteNotificationSingle(dto.getIsDeleted(),
						dto.getUserNotificationMapIDList().get(0));
			} else if (dto.getUserNotificationMapIDList().size() > 1)
			{
				notificationService.deleteNotificationList(dto.getIsDeleted(), dto.getUserNotificationMapIDList());
			} else
			{
				// SUNIL TODO: alternatively throw missing mandatory parameter exception
				// String response = getErrorResponseString("Missing mandatory Parameter - at least 1 NotificationMapId
				// needed.", 200, "success", "markDelete");

				response.setError(new Throwable("Missing mandatory Parameter - at least 1 NotificationMapId needed."));
				logger.info("UserNotificationMappingController.markDelete: failure - finish");
				return response.toString();
			}

			// String response = getSuccessResponseString("Success", 200, "success", "markDelete");

			SuccessObjectDTO obj = new SuccessObjectDTO();
			obj.setOperation("isDeleted = " + dto.getIsDeleted().toString());
			obj.setStatus("success");
			response.setResponse(InputMapper.getInstance().gson().toJson(obj));
			logger.info("UserNotificationMappingController.markDelete: success - finish");
			return response.toString();

		} catch (Exception e)
		{
			// String response = getErrorResponseString(e.getLocalizedMessage(), 200, "success", "markDelete");

			response.setError(e);
			logger.info("UserNotificationMappingController.markDelete: failure - finish");
			return response.toString();
		}
	}

	// @ApiOperation(value = "stores scheme data", consumes = "application/json", produces = "application/json")
	// @RequestMapping(
	// value = "/createDetailForUsers", method = RequestMethod.POST,
	// produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE,
	// headers = "Authorization")
	// public String createAlertDetails(
	// @Param(
	// value = "{\"providerServiceMapID\": \"integer-provider service map id\", "
	// + "\"schemeName\": \"Name of Scheme\", " + "\"schemeDesc\": \"Description of Scheme\", "
	// + "\"createdBy\": \"User name of the supervisor creating scheme\", "
	// + "\"kmFileManager\":{\"fileName\":\"String: name of file\", " + "\"fileExtension\":\"String: file extension\", "
	// + "\"providerServiceMapID\":\"Integer: service provider map ID\", "
	// + "\"userID\":\"Integer: user ID of the supervisor\", "
	// + "\"fileContent\":\"String: base64encoded binary file content\", "
	// + "\"createdBy\":\"String: username of supervisor\"}}") @RequestBody String request)
	// {
	// logger.info("UserNotificationMappingController.createAlertDetails - start");
	//
	//
	// logger.info("UserNotificationMappingController.createAlertDetails - finish");
	// return "";
	// }
	//
	// private String getSuccessResponseString(String message, Integer statusCode, String statusMsg, String methodName)
	// {
	// String data = OutputMapper.getInstance().gson().toJson(message);
	// logger.info("data: " + data);
	// logger.info("data.toStr: " + data.toString());
	// OutputResponse response = new OutputResponse.Builder()
	// .setDataJsonType("JsonObject.class")
	// .setStatusCode(statusCode)
	// .setStatusMessage(statusMsg)
	// .setDataObjectType(this.getClass().getSimpleName())
	// .setMethodName(methodName)
	// .setData(data)
	// .build();
	// return OutputMapper.getInstance().gson().toJson(response);
	// }
	//
	// private String getSuccessResponseString(AlertAndNotificationCountDTO dto, Integer statusCode, String statusMsg,
	// String methodName) {
	// String data = OutputMapper.getInstance().gson().toJson(dto);
	// logger.info("data: " + data);
	// logger.info("data.toStr: " + data.toString());
	// OutputResponse response = new OutputResponse.Builder()
	// .setDataJsonType("JsonObject.class")
	// .setStatusCode(statusCode)
	// .setStatusMessage(statusMsg)
	// .setDataObjectType(this.getClass().getSimpleName())
	// .setMethodName(methodName)
	// .setData(data)
	// .build();
	// return OutputMapper.getInstance().gson().toJson(response);
	// }
	//
	// private String getSuccessResponseString(AlertAndNotificationDetailDTO dto, Integer statusCode, String statusMsg,
	// String methodName) {
	// //Type typeOfSrc = new TypeToken<List<UserNotificationMapping>>(){private static final long serialVersionUID =
	// 1L;}.getType();
	// //String data = OutputMapper.getInstance().gson().toJson(list, typeOfSrc);
	//
	// String data = OutputMapper.getInstance().gson().toJson(dto);
	// logger.info("data: " + data);
	// logger.info("data.toStr: " + data.toString());
	// OutputResponse response = new OutputResponse.Builder()
	// .setDataJsonType("JsonObject.class")
	// .setStatusCode(statusCode)
	// .setStatusMessage(statusMsg)
	// .setDataObjectType(this.getClass().getSimpleName())
	// .setMethodName(methodName)
	// .setData(data)
	// .build();
	// return OutputMapper.getInstance().gson().toJson(response);
	// }
	//
	// private String getErrorResponseString(String errorMsg, Integer statusCode, String statusMsg, String methodName) {
	// JsonObject dta = new JsonObject();
	// dta.addProperty("error", errorMsg);
	// String data = OutputMapper.getInstance().gson().toJson(errorMsg).toString();
	// logger.info("data: " + data);
	//
	// OutputResponse response = new OutputResponse.Builder()
	// .setDataJsonType("JsonObject.class")
	// .setStatusCode(statusCode)
	// .setStatusMessage(statusMsg)
	// .setDataObjectType(this.getClass().getSimpleName())
	// .setMethodName(methodName)
	// .setData(data)
	// .build();
	//
	// logger.info("IdentityController.getResponseString - end");
	//
	// return OutputMapper.getInstance().gson().toJson(response);
	// }
	//
	//
	// ------------------------------------- //
	// @RequestMapping(
	// value = "/getAlertsAndNotificationCountAPI", method = RequestMethod.POST,
	// produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE,
	// headers = "Authorization")
	// public String getAlertsAndNotificationCountAPI(@Param(value = "{"
	// + "\"userID\":\"Integer\""
	// + "\"roleID\":\"Integer\""
	// + "\"providerServiceMapID\":\"Integer\""
	// + "\"workingLocationID\":\"Integer\""
	// + "}") @RequestBody String createRequest) {
	//
	// logger.info("UserNotificationMappingController.getAlertsAndNotificationCount - start");
	// OutputResponse output = new OutputResponse();
	//
	// try {
	// @SuppressWarnings("static-access")
	// UserNotificationDisplayMinDTO inputData = mapper.gson().fromJson(createRequest,
	// UserNotificationDisplayMinDTO.class);
	//
	// // Send the captured data to Service to get Aggregated Output List
	// AlertAndNotificationCountDTO dtoOut = notificationService.getAlertAndNotificationCount(inputData);
	//
	// // Send the output DTO
	// output.setResponse(dtoOut.toString());
	// } catch (IEMRException e) {
	// e.printStackTrace();
	// output.setError(5000, e.getMessage());
	// }
	//
	// logger.info("UserNotificationMappingController.getAlertsAndNotificationCount - finish");
	// return "";
	// }
}
