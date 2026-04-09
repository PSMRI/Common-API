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
import com.iemr.common.utils.JitsiJwtUtil;
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

    @Autowired
    private JitsiJwtUtil jitsiJwtUtil;

    private String meetingLink;

    private boolean isLinkSent = false;

    @Value("${videocall.url}")
    private String jitsiLink;

    // Fallback chains let either dot-form or JITSI_*-form work in any property
    // source (.properties files do NOT get Spring relaxed binding for @Value).
    @Value("${jitsi.domain:${JITSI_DOMAIN:vc.piramalswasthya.org}}")
    private String jitsiDomain;

    @Value("${jitsi.room.prefix:${JITSI_ROOM_PREFIX:piramal-meeting-}}")
    private String roomPrefix;

    @Value("${jitsi.default.user.email:${JITSI_DEFAULT_USER_EMAIL:admin@piramalswasthya.org}}")
    private String defaultUserEmail;

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

@Override
public String resolveMeetingLink(String slug) throws Exception {
    if (slug == null || slug.isEmpty()) {
        throw new IllegalArgumentException("Meeting slug is required");
    }

    // The persisted meetingLink is the short URL produced by generateMeetingLink(),
    // i.e. "<videocall.url>m=<slug>". Reconstruct it to look up the row.
    String shortLink = jitsiLink + "m=" + slug;
    VideoCallParameters params = videoCallRepository.findByMeetingLink(shortLink);

    if (params == null) {
        throw new Exception("No meeting found for slug: " + slug);
    }

    // Note: we deliberately do NOT block on linkUsed=true here, because real
    // calls drop and the agent/beneficiary often have to rejoin. The linkUsed
    // flag is for reporting in updateCallStatus, not access control. Access
    // control comes from the JWT exp + the room claim.

    String roomName = roomPrefix + slug;
    String userName = params.getAgentName() != null && !params.getAgentName().isEmpty()
            ? params.getAgentName()
            : "Guest";

    String token = jitsiJwtUtil.generateRoomToken(roomName, userName, defaultUserEmail);

    String redirectUrl = "https://" + jitsiDomain + "/" + roomName + "?jwt=" + token;
    logger.info("Resolved slug {} -> room {}", slug, roomName);
    return redirectUrl;
}

}


