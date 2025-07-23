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

package com.iemr.common.service.videocall;

import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.sql.Timestamp;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.io.IOException;
import com.iemr.common.data.videocall.VideoCallParameters;
import com.iemr.common.mapper.videocall.VideoCallMapper;
import com.iemr.common.model.videocall.UpdateCallRequest;
import com.iemr.common.model.videocall.VideoCallRequest;
import com.iemr.common.repository.videocall.VideoCallParameterRepository;
import com.iemr.common.utils.config.ConfigProperties;
import com.iemr.common.utils.mapper.OutputMapper;
import com.iemr.common.utils.response.OutputResponse;
import org.springframework.beans.factory.annotation.Value;

@Service
public class VideoCallServiceImpl implements VideoCallService {
	final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	@Autowired
    private VideoCallParameterRepository videoCallRepository;
    
	@Autowired
    private VideoCallMapper videoCallMapper;

    private String meetingLink;

    private boolean isLinkSent = false;

    @Value("${videocall.url}")
    private String jitsiLink;

    public VideoCallServiceImpl() {
        // Default constructor
        this.meetingLink = null;
        this.isLinkSent = false;
    }

    @Override
    public String generateMeetingLink() {
        logger.info("Jitsi Link: " + jitsiLink);
        meetingLink=jitsiLink+"m="+RandomStringUtils.randomAlphanumeric(8);
        logger.info("Meeting link: " + meetingLink);
        return meetingLink;
    }

    @Override
    public String sendMeetingLink(VideoCallRequest request) throws Exception {
    OutputResponse response = new OutputResponse();

    if (meetingLink == null || meetingLink.isEmpty()) {
        throw new Exception("Meeting link not generated yet.");
    }

    isLinkSent = true;

    VideoCallParameters videoCallEntity = videoCallMapper.videoCallToEntity(request);
    videoCallEntity.setMeetingLink(meetingLink);
    videoCallEntity.setLinkGeneratedAt(Timestamp.valueOf(LocalDateTime.now()));
    videoCallEntity.setLinkUsed(false);

    videoCallRepository.save(videoCallEntity);

    VideoCallRequest responseData = videoCallMapper.videoCallToRequest(videoCallEntity);
    response.setResponse(responseData.toJson());

    return OutputMapper.gsonWithoutExposeRestriction()
        .toJson(response) .replace("\\u003d", "=")
    .replace("\\u003c", "<")
    .replace("\\u003e", ">")
    .replace("\\u0026", "&");
    }

@Override
public String updateCallStatus(UpdateCallRequest callRequest) throws Exception {
    VideoCallParameters videoCall = null;

    VideoCallParameters requestEntity = videoCallMapper.updateRequestToVideoCall(callRequest);

    videoCall = videoCallRepository.findByMeetingLink(requestEntity.getMeetingLink());

    int updateCount = videoCallRepository.updateCallStatusByMeetingLink(
        requestEntity.getMeetingLink(),
        requestEntity.getCallStatus(),
        requestEntity.getCallDuration(),
        requestEntity.getModifiedBy()
    );

    if (updateCount > 0) {
        videoCall.setLinkUsed(true);
        videoCallRepository.save(videoCall); 
      
    } else {
        throw new Exception("Failed to update the call status");
    }

    return OutputMapper.gsonWithoutExposeRestriction()
        .toJson(videoCallMapper.videoCallToResponse(videoCall));
}

}


