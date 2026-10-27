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

import com.iemr.common.data.videocall.VideoCallParameters;
import com.iemr.common.mapper.videocall.VideoCallMapper;
import com.iemr.common.model.videocall.UpdateCallRequest;
import com.iemr.common.model.videocall.VideoCallRequest;
import com.iemr.common.repository.videocall.VideoCallParameterRepository;
import com.iemr.common.utils.JitsiJwtUtil;
import com.iemr.common.utils.config.ConfigProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VideoCallServiceImplTest {
    @InjectMocks
    VideoCallServiceImpl service;

    @Mock
    VideoCallParameterRepository videoCallRepository;
    @Mock
    VideoCallMapper videoCallMapper;
    @Mock
    VideoCallRequest videoCallRequest;
    @Mock
    UpdateCallRequest updateCallRequest;
    @Mock
    VideoCallParameters videoCallParameters;
    @Mock
    JitsiJwtUtil jitsiJwtUtil;

    @BeforeEach
    public void setup() throws Exception {
        ReflectionTestUtils.setField(service, "jitsiLink", "https://meet.jit.si/");
        ReflectionTestUtils.setField(service, "jitsiDomain", "meet.jit.si");
        ReflectionTestUtils.setField(service, "roomPrefix", "piramal-meeting-");
        ReflectionTestUtils.setField(service, "defaultUserEmail", "admin@piramalswasthya.org");
    }

    @Test
    public void testConstructor() {
        VideoCallServiceImpl instance = new VideoCallServiceImpl();
        assertNotNull(instance);
    }

    @Test
    public void testGenerateMeetingLink() {
        String link = service.generateMeetingLink();
        assertNotNull(link);
        assertTrue(link.startsWith("https://meet.jit.si/m="));
    }

    @Test
    public void testSendMeetingLink_success() throws Exception {
        ReflectionTestUtils.setField(service, "meetingLink", "https://meet.jit.si/m=ABCDEFGH");
        when(videoCallMapper.videoCallToEntity(any())).thenReturn(videoCallParameters);
        when(videoCallMapper.videoCallToRequest(any())).thenReturn(videoCallRequest);
        when(videoCallRequest.toJson()).thenReturn("{\"meetingLink\":\"https://meet.jit.si/m=ABCDEFGH\"}");
        when(videoCallRepository.save(any())).thenReturn(videoCallParameters);
        String result = service.sendMeetingLink(videoCallRequest);
        assertTrue(result.contains("meetingLink"));
        verify(videoCallRepository).save(any());
        verify(videoCallMapper).videoCallToEntity(any());
        verify(videoCallMapper).videoCallToRequest(any());
    }

    @Test
    public void testSendMeetingLink_meetingLinkNotGenerated() {
        ReflectionTestUtils.setField(service, "meetingLink", null);
        Exception ex = assertThrows(Exception.class, () -> service.sendMeetingLink(videoCallRequest));
        assertEquals("Meeting link not generated yet.", ex.getMessage());
    }

    @Test
    public void testUpdateCallStatus_success() throws Exception {
        VideoCallParameters updated = mock(VideoCallParameters.class);
        when(updateCallRequest.getMeetingLink()).thenReturn("https://meet.jit.si/m=Ab3xQ9pK");
        when(updateCallRequest.getCallStatus()).thenReturn("COMPLETED");
        when(updateCallRequest.getCallDuration()).thenReturn("00:12:30");
        when(updateCallRequest.getModifiedBy()).thenReturn("agent1");
        when(updateCallRequest.getIsLinkUsed()).thenReturn(true);
        when(videoCallRepository.findByMeetingLink("https://meet.jit.si/m=Ab3xQ9pK"))
                .thenReturn(videoCallParameters, updated);
        when(videoCallRepository.updateCallStatusAndRecording("https://meet.jit.si/m=Ab3xQ9pK", "COMPLETED",
                "00:12:30", "agent1", true, "piramal-meeting-Ab3xQ9pK/piramal-meeting-Ab3xQ9pK.mp4")).thenReturn(1);
        when(videoCallMapper.videoCallToResponse(updated))
                .thenReturn(new com.iemr.common.model.videocall.UpdateCallResponse());

        String result = service.updateCallStatus(updateCallRequest);

        assertNotNull(result);
        // The response reflects the row re-read after the update, not the one read before it.
        verify(videoCallMapper).videoCallToResponse(updated);
    }

    @Test
    public void testUpdateCallStatus_unknownMeetingLink() {
        when(updateCallRequest.getMeetingLink()).thenReturn("https://meet.jit.si/m=missing");
        when(videoCallRepository.findByMeetingLink("https://meet.jit.si/m=missing")).thenReturn(null);

        Exception ex = assertThrows(Exception.class, () -> service.updateCallStatus(updateCallRequest));
        assertEquals("No meeting found for link: https://meet.jit.si/m=missing", ex.getMessage());
    }

    @Test
    public void testUpdateCallStatus_failure() {
        when(updateCallRequest.getMeetingLink()).thenReturn("https://meet.jit.si/m=Ab3xQ9pK");
        when(videoCallRepository.findByMeetingLink("https://meet.jit.si/m=Ab3xQ9pK")).thenReturn(videoCallParameters);
        when(videoCallRepository.updateCallStatusAndRecording(anyString(), any(), any(), any(), anyBoolean(), any()))
                .thenReturn(0);

        Exception ex = assertThrows(Exception.class, () -> service.updateCallStatus(updateCallRequest));
        assertEquals("Failed to update the call status — 0 rows affected", ex.getMessage());
    }

    @Test
    public void testUpdateCallStatus_omittedLinkUsedDefaultsToUsed() throws Exception {
        when(updateCallRequest.getMeetingLink()).thenReturn("https://meet.jit.si/m=Ab3xQ9pK");
        when(updateCallRequest.getIsLinkUsed()).thenReturn(null);
        when(videoCallRepository.findByMeetingLink("https://meet.jit.si/m=Ab3xQ9pK")).thenReturn(videoCallParameters);
        when(videoCallRepository.updateCallStatusAndRecording(anyString(), any(), any(), any(), anyBoolean(), any()))
                .thenReturn(1);
        when(videoCallMapper.videoCallToResponse(any(VideoCallParameters.class)))
                .thenReturn(new com.iemr.common.model.videocall.UpdateCallResponse());

        service.updateCallStatus(updateCallRequest);

        verify(videoCallRepository).updateCallStatusAndRecording(anyString(), any(), any(), any(), eq(true), any());
    }

    @Test
    public void testUpdateCallStatus_meetingLinkWithoutSlugMarkerHasNoRecording() throws Exception {
        when(updateCallRequest.getMeetingLink()).thenReturn("https://meet.jit.si/plain-room");
        when(videoCallRepository.findByMeetingLink("https://meet.jit.si/plain-room")).thenReturn(videoCallParameters);
        when(videoCallRepository.updateCallStatusAndRecording(anyString(), any(), any(), any(), anyBoolean(), any()))
                .thenReturn(1);
        when(videoCallMapper.videoCallToResponse(any(VideoCallParameters.class)))
                .thenReturn(new com.iemr.common.model.videocall.UpdateCallResponse());

        service.updateCallStatus(updateCallRequest);

        verify(videoCallRepository).updateCallStatusAndRecording(anyString(), any(), any(), any(), anyBoolean(),
                isNull());
    }

    @Test
    public void testResolveMeetingLink_success() throws Exception {
        when(videoCallRepository.findByMeetingLink("https://meet.jit.si/m=Ab3xQ9pK"))
                .thenReturn(videoCallParameters);
        when(videoCallParameters.getAgentName()).thenReturn("Dr. Asha");
        when(jitsiJwtUtil.generateRoomToken(
                eq("piramal-meeting-Ab3xQ9pK"),
                eq("Dr. Asha"),
                eq("admin@piramalswasthya.org"),
                eq(false))).thenReturn("FAKE.JWT.TOKEN");

        String result = service.resolveMeetingLink("Ab3xQ9pK");

        assertEquals(
                "https://meet.jit.si/piramal-meeting-Ab3xQ9pK?jwt=FAKE.JWT.TOKEN",
                result);
        verify(jitsiJwtUtil).generateRoomToken(
                "piramal-meeting-Ab3xQ9pK", "Dr. Asha", "admin@piramalswasthya.org", false);
    }

    @Test
    public void testResolveMeetingLink_emptySlug() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.resolveMeetingLink(""));
        assertEquals("Meeting slug is required", ex.getMessage());
    }

    @Test
    public void testResolveMeetingLink_nullSlug() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.resolveMeetingLink(null));
        assertEquals("Meeting slug is required", ex.getMessage());
    }

    @Test
    public void testResolveMeetingLink_notFound() {
        when(videoCallRepository.findByMeetingLink("https://meet.jit.si/m=missing"))
                .thenReturn(null);

        Exception ex = assertThrows(
                Exception.class,
                () -> service.resolveMeetingLink("missing"));
        assertTrue(ex.getMessage().contains("No meeting found"));
    }

    @Test
    public void testResolveMeetingLink_fallbackUserNameWhenAgentMissing() throws Exception {
        when(videoCallRepository.findByMeetingLink("https://meet.jit.si/m=Ab3xQ9pK"))
                .thenReturn(videoCallParameters);
        when(videoCallParameters.getAgentName()).thenReturn(null);
        when(jitsiJwtUtil.generateRoomToken(
                eq("piramal-meeting-Ab3xQ9pK"),
                eq("Guest"),
                eq("admin@piramalswasthya.org"),
                eq(false))).thenReturn("FAKE.JWT.TOKEN");

        String result = service.resolveMeetingLink("Ab3xQ9pK");

        assertTrue(result.endsWith("?jwt=FAKE.JWT.TOKEN"));
        verify(jitsiJwtUtil).generateRoomToken(
                "piramal-meeting-Ab3xQ9pK", "Guest", "admin@piramalswasthya.org", false);
    }

}
