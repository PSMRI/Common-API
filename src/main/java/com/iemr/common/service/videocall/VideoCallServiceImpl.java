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

    @Value("${jitsi.domain}")
    private String jitsiDomain;

    @Value("${jitsi.room.prefix}")
    private String roomPrefix;

    @Value("${jitsi.default.user.email}")
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

// @Override
// public String updateCallStatus(UpdateCallRequest callRequest) throws Exception {
//     VideoCallParameters videoCall = null;

//     VideoCallParameters requestEntity = videoCallMapper.updateRequestToVideoCall(callRequest);

//     videoCall = videoCallRepository.findByMeetingLink(requestEntity.getMeetingLink());

//     int updateCount = videoCallRepository.updateCallStatusByMeetingLink(
//         requestEntity.getMeetingLink(),
//         requestEntity.getCallStatus(),
//         requestEntity.getCallDuration(),
//         requestEntity.getModifiedBy()
//     );

//     if (updateCount > 0) {
//         // End-consultation: UI sends isLinkUsed=true; fall back to true for
//         // backwards compatibility with older callers that didn't send the flag.
//         boolean linkUsed = callRequest.getIsLinkUsed() == null || callRequest.getIsLinkUsed();
//         videoCall.setLinkUsed(linkUsed);
//         videoCall.setRecordingFileName(buildRecordingFileName(requestEntity.getMeetingLink()));
//         videoCallRepository.save(videoCall);

//     } else {
//         throw new Exception("Failed to update the call status");
//     }

//     return OutputMapper.gsonWithoutExposeRestriction()
//         .toJson(videoCallMapper.videoCallToResponse(videoCall));
// }


@Override
public String updateCallStatus(UpdateCallRequest callRequest) throws Exception {
    String meetingLink = callRequest.getMeetingLink();
    logger.info("[updateCallStatus] START — meetingLink={}, callStatus={}, callDuration={}, modifiedBy={}, isLinkUsed={}",
        meetingLink,
        callRequest.getCallStatus(),
        callRequest.getCallDuration(),
        callRequest.getModifiedBy(),
        callRequest.getIsLinkUsed());

    // 1. Verify the row actually exists before attempting update
    VideoCallParameters existing = videoCallRepository.findByMeetingLink(meetingLink);
    if (existing == null) {
        logger.error("[updateCallStatus] No row found in t_videocallparameter for meetingLink={}", meetingLink);
        throw new Exception("No meeting found for link: " + meetingLink);
    }
    logger.info("[updateCallStatus] Found existing row — meetingID={}, currentStatus={}, currentLinkUsed={}, currentRecording={}",
        existing.getMeetingID(),
        existing.getCallStatus(),
        existing.isLinkUsed(),
        existing.getRecordingFileName());

    // 2. Derive the two fields the old query was missing
    boolean linkUsed = callRequest.getIsLinkUsed() == null || callRequest.getIsLinkUsed();
    String recordingFileName = buildRecordingFileName(meetingLink);
    logger.info("[updateCallStatus] Computed — linkUsed={}, recordingFileName={}", linkUsed, recordingFileName);

    // 3. Single atomic JPQL UPDATE — sets ALL five fields in one DB round-trip
    int updateCount = videoCallRepository.updateCallStatusAndRecording(
        meetingLink,
        callRequest.getCallStatus(),
        callRequest.getCallDuration(),
        callRequest.getModifiedBy(),
        linkUsed,
        recordingFileName
    );
    logger.info("[updateCallStatus] JPQL updateCallStatusAndRecording affected {} row(s)", updateCount);

    if (updateCount == 0) {
        logger.error("[updateCallStatus] Update affected 0 rows — possible meetingLink mismatch. meetingLink={}", meetingLink);
        throw new Exception("Failed to update the call status — 0 rows affected");
    }

    // 4. Re-fetch AFTER the update so the returned JSON reflects what is now in the DB
    VideoCallParameters updated = videoCallRepository.findByMeetingLink(meetingLink);
    logger.info("[updateCallStatus] Post-update state — callStatus={}, callDuration={}, linkUsed={}, recordingFileName={}",
        updated.getCallStatus(),
        updated.getCallDuration(),
        updated.isLinkUsed(),
        updated.getRecordingFileName());

    return OutputMapper.gsonWithoutExposeRestriction()
        .toJson(videoCallMapper.videoCallToResponse(updated));
}

/**
 * Jibri records each Jitsi room into a directory named after the room, with
 * the MP4 file sharing the same name — e.g. piramal-meeting-Ab3xQ9pK/piramal-meeting-Ab3xQ9pK.mp4.
 * The short SMS link is "<videocall.url>m=<slug>", so derive the room from the slug.
 */
private String buildRecordingFileName(String meetingLink) {
    logger.info("[buildRecordingFileName] Input meetingLink={}", meetingLink);

    if (meetingLink == null) {
        logger.warn("[buildRecordingFileName] meetingLink is null — returning null");
        return null;
    }

    int idx = meetingLink.lastIndexOf("m=");
    if (idx < 0) {
        logger.warn("[buildRecordingFileName] 'm=' marker not found in meetingLink={} — returning null", meetingLink);
        return null;
    }

    String slug = meetingLink.substring(idx + 2);
    if (slug.isEmpty()) {
        logger.warn("[buildRecordingFileName] slug is empty after 'm=' in meetingLink={} — returning null", meetingLink);
        return null;
    }

    String roomName = roomPrefix + slug;
    String fileName = roomName + "/" + roomName + ".mp4";
    logger.info("[buildRecordingFileName] slug={}, roomName={}, fileName={}", slug, roomName, fileName);
    return fileName;
}

// @Override
// public String resolveMeetingLink(String slug) throws Exception {
//     if (slug == null || slug.isEmpty()) {
//         throw new IllegalArgumentException("Meeting slug is required");
//     }

//     // The persisted meetingLink is the short URL produced by generateMeetingLink(),
//     // i.e. "<videocall.url>m=<slug>". Reconstruct it to look up the row.
//     String shortLink = jitsiLink + "m=" + slug;
//     VideoCallParameters params = videoCallRepository.findByMeetingLink(shortLink);

//     if (params == null) {
//         throw new Exception("No meeting found for slug: " + slug);
//     }

//     // Note: we deliberately do NOT block on linkUsed=true here, because real
//     // calls drop and the agent/beneficiary often have to rejoin. The linkUsed
//     // flag is for reporting in updateCallStatus, not access control. Access
//     // control comes from the JWT exp + the room claim.

//     String roomName = roomPrefix + slug;
//     String userName = params.getAgentName() != null && !params.getAgentName().isEmpty()
//             ? params.getAgentName()
//             : "Guest";

//     String token = jitsiJwtUtil.generateRoomToken(roomName, userName, defaultUserEmail);

//     String redirectUrl = "https://" + jitsiDomain + "/" + roomName + "?jwt=" + token;
//     logger.info("Resolved slug {} -> room {}", slug, roomName);
//     return redirectUrl;
// }

@Override
public String resolveMeetingLink(String slug) throws Exception {
    if (slug == null || slug.isEmpty()) {
        throw new IllegalArgumentException("Meeting slug is required");
    }

    String shortLink = jitsiLink + "m=" + slug;
    VideoCallParameters params = videoCallRepository.findByMeetingLink(shortLink);

    if (params == null) {
        throw new Exception("No meeting found for slug: " + slug);
    }

    // ✅ ADD THIS — block re-entry after call ends
    if (params.isLinkUsed()) {
        throw new Exception("This meeting link has already been used and is no longer active.");
    }

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


