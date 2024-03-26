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
package com.iemr.common.controller.carestream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.iemr.common.data.carestream.CreateOrderData;
import com.iemr.common.utils.mapper.InputMapper;
import com.iemr.common.utils.response.OutputResponse;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
@RequestMapping(value = "/carestream")
public class CareStreamCreateOrderController {
	private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	private static final char END_OF_BLOCK = '\u001c';
	private static final char START_OF_BLOCK = '\u000b';
	private static final char CARRIAGE_RETURN = 13;

	@Value("${carestream_socket_ip}")
	private String carestreamSocketIP;

	@Value("${carestream_socket_port}")
	private int carestreamSocketPort;

	@CrossOrigin()
	@ApiOperation(value = "Create order")
	@RequestMapping(value = "/createOrder", headers = "Authorization", method = { RequestMethod.POST }, produces = {
			"application/json" })
	public String createOrder(@ApiParam("{\"firstName\":\"String\",\"middleName\":\"String\",\"LastName\":\"String\","
			+ "\"gender\":\"String\",\"dob\":\"String\",\"patientID\":\"String\",\"acc\":\"String\"}") @RequestBody String createOrder)
			throws UnknownHostException, IOException {
		OutputResponse response = new OutputResponse();
		Socket socket = null;
		try {

			CreateOrderData benificiaryDetails = InputMapper.gson().fromJson(createOrder, CreateOrderData.class);

			String firstName = benificiaryDetails.getFirstName();
			String lastName = benificiaryDetails.getLastName();
			String patientID = benificiaryDetails.getPatientID();
			String dob = benificiaryDetails.getDob();
			String gender = benificiaryDetails.getGender();
			String acc = benificiaryDetails.getAcc();

			socket = new Socket(carestreamSocketIP, carestreamSocketPort);

			StringBuffer testHL7MassageToTransmit = new StringBuffer();
			testHL7MassageToTransmit.append(START_OF_BLOCK)
					.append("MSH|^~"
							+ "\"&|ImageSuite||||20120730000000.996||ORM^O01|MSG02010101_1|P|2.5|||AL||USA|ASCII||||")
					.append(CARRIAGE_RETURN)
					.append("PID|||" + patientID + "||" + firstName + "^" + lastName + "^||" + dob + "|" + gender
							+ "|||||||||||||||||||||||||||Species Code|Breed Code")
					.append(CARRIAGE_RETURN)
					.append("ORC|NW||" + acc + "||||20110621|||||Referring Physician Felix||||||||||||||||||")
					.append(CARRIAGE_RETURN)
					.append("OBR|0001|||CR_CHEST_PA|||20110622000000.996|||||||||Referring Physician Felix|||||||||||20110624|||||||||20110624||||||||CR_CHEST_PA|||||")
					.append(END_OF_BLOCK).append(CARRIAGE_RETURN);

			InputStream in = socket.getInputStream();
			OutputStream out = socket.getOutputStream();
			out.write(testHL7MassageToTransmit.toString().getBytes());

			byte[] byteBuffer = new byte[50];
			if (in.read(byteBuffer) > 0) {

				String resultCS = new String(byteBuffer);
				if (resultCS != null && resultCS.length() > 0) {
					response.setResponse("Order successfully created");
				} else {
					response.setResponse("Error in order creation");
				}
			}

		} catch (Exception e) {
			logger.error(e.getMessage());
			response.setError(e);

		} finally {
			if (socket != null)
				socket.close();
		}

		return response.toString();
	}

	@CrossOrigin()
	@ApiOperation(value = "Update order")
	@RequestMapping(value = "/UpdateOrder", headers = "Authorization", method = { RequestMethod.POST }, produces = {
			"application/json" })
	public String updateOrder(@ApiParam("{\"firstName\":\"String\",\"middleName\":\"String\",\"LastName\":\"String\","
			+ "\"gender\":\"String\",\"dob\":\"String\",\"patientID\":\"String\",\"acc\":\"String\"}") @RequestBody String UpdateOrder)
			throws UnknownHostException, IOException {
		OutputResponse response = new OutputResponse();
		Socket socket = null;

		try {

			CreateOrderData benificiaryDetails = InputMapper.gson().fromJson(UpdateOrder, CreateOrderData.class);

			String firstName = benificiaryDetails.getFirstName();
			String lastName = benificiaryDetails.getLastName();
			String patientID = benificiaryDetails.getPatientID();
			String dob = benificiaryDetails.getDob();
			String gender = benificiaryDetails.getGender();
			String acc = benificiaryDetails.getAcc();

			socket = new Socket("192.168.1.101", 1235);

			StringBuffer testHL7MassageToTransmit = new StringBuffer();
			testHL7MassageToTransmit.append(START_OF_BLOCK)
					.append("MSH|^~"
							+ "\"&|ImageSuite||||20120730000000.996||ORM^O01|MSG02010101_1|P|2.5|||AL||USA|ASCII||||")
					.append(CARRIAGE_RETURN)
					.append("PID|||" + patientID + "||" + firstName + "^" + lastName + "^||" + dob + "|" + gender
							+ "|||||||||||||||||||||||||||Species Code|Breed Code")
					.append(CARRIAGE_RETURN)
					.append("ORC|XO||" + acc + "||||20110621|||||Referring Physician Felix||||||||||||||||||")
					.append(CARRIAGE_RETURN)
					.append("OBR|0001|||CR_CHEST_PA|||20110622000000.996|||||||||Referring Physician Felix|||||||||||20110624|||||||||20110624||||||||CR_CHEST_PA|||||")
					.append(END_OF_BLOCK).append(CARRIAGE_RETURN);

			InputStream in = socket.getInputStream();
			OutputStream out = socket.getOutputStream();
			// Send the MLLP -wrapped HL7 message to the server
			out.write(testHL7MassageToTransmit.toString().getBytes());

			byte[] byteBuffer = new byte[200];
			if (in.read(byteBuffer) > 0) {

				response.setResponse("Receiver from server: " + new String(byteBuffer));
				// Close the socket and its streams

				socket.close();
			}

		} catch (Exception e) {
			logger.error(e.getMessage());
			response.setError(e);

		} finally {
			if (socket != null)
				socket.close();
		}

		return response.toString();
	}

	@CrossOrigin()
	@ApiOperation(value = "Delete order")
	@RequestMapping(value = "/deleteOrder", headers = "Authorization", method = { RequestMethod.POST }, produces = {
			"application/json" })
	public String deleteOrder(@ApiParam("{\"firstName\":\"String\",\"middleName\":\"String\",\"LastName\":\"String\","
			+ "\"gender\":\"String\",\"dob\":\"String\",\"patientID\":\"String\",\"acc\":\"String\"}") @RequestBody String deleteOrder)
			throws UnknownHostException, IOException {
		OutputResponse response = new OutputResponse();
		Socket socket = null;
		try {

			CreateOrderData benificiaryDetails = InputMapper.gson().fromJson(deleteOrder, CreateOrderData.class);

			String firstName = benificiaryDetails.getFirstName();
			String lastName = benificiaryDetails.getLastName();
			String patientID = benificiaryDetails.getPatientID();
			String dob = benificiaryDetails.getDob();
			String gender = benificiaryDetails.getGender();
			String acc = benificiaryDetails.getAcc();

			socket = new Socket("192.168.1.101", 1235);

			StringBuffer testHL7MassageToTransmit = new StringBuffer();
			testHL7MassageToTransmit.append(START_OF_BLOCK)
					.append("MSH|^~"
							+ "\"&|ImageSuite||||20120730000000.996||ORM^O01|MSG02010101_1|P|2.5|||AL||USA|ASCII||||")
					.append(CARRIAGE_RETURN)
					.append("PID|||" + patientID + "||" + firstName + "^" + lastName + "^||" + dob + "|" + gender
							+ "|||||||||||||||||||||||||||Species Code|Breed Code")
					.append(CARRIAGE_RETURN)
					.append("ORC|CA||" + acc + "||||20110621|||||Referring Physician Felix||||||||||||||||||")
					.append(CARRIAGE_RETURN)
					.append("OBR|0001|||CR_CHEST_PA|||20110622000000.996|||||||||Referring Physician Felix|||||||||||20110624|||||||||20110624||||||||CR_CHEST_PA|||||")
					.append(END_OF_BLOCK).append(CARRIAGE_RETURN);

			InputStream in = socket.getInputStream();
			OutputStream out = socket.getOutputStream();
			// Send the MLLP -wrapped HL7 message to the server
			out.write(testHL7MassageToTransmit.toString().getBytes());

			byte[] byteBuffer = new byte[200];
			if (in.read(byteBuffer) > 0) {

				response.setResponse("Receiver from server: " + new String(byteBuffer));

			}

		} catch (Exception e) {
			logger.error(e.getMessage());
			response.setError(e);

		} finally {
			if (socket != null)
				socket.close();
		}

		return response.toString();
	}

}
