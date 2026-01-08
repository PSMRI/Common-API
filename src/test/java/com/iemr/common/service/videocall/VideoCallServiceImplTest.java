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

    @BeforeEach
    public void setup() throws Exception {
        ReflectionTestUtils.setField(service, "jitsiLink", "https://meet.jit.si/");
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
        when(videoCallMapper.updateRequestToVideoCall(any(UpdateCallRequest.class))).thenReturn(videoCallParameters);
        when(videoCallParameters.getMeetingLink()).thenReturn("link123");
        when(videoCallRepository.findByMeetingLink("link123")).thenReturn(videoCallParameters);
        when(videoCallRepository.updateCallStatusByMeetingLink(eq("link123"), isNull(), isNull(), isNull())).thenReturn(1);
        when(videoCallMapper.videoCallToResponse(any(VideoCallParameters.class))).thenReturn(new com.iemr.common.model.videocall.UpdateCallResponse());
        when(videoCallRepository.save(any())).thenReturn(videoCallParameters);
        String result = service.updateCallStatus(updateCallRequest);
        assertNotNull(result);
        verify(videoCallRepository).save(any());
    }

    @Test
    public void testUpdateCallStatus_failure() {
        when(videoCallMapper.updateRequestToVideoCall(any(UpdateCallRequest.class))).thenReturn(videoCallParameters);
        when(videoCallParameters.getMeetingLink()).thenReturn("link123");
        when(videoCallRepository.findByMeetingLink("link123")).thenReturn(videoCallParameters);
        when(videoCallRepository.updateCallStatusByMeetingLink(eq("link123"), isNull(), isNull(), isNull())).thenReturn(0);
        Exception ex = assertThrows(Exception.class, () -> service.updateCallStatus(updateCallRequest));
        assertEquals("Failed to update the call status", ex.getMessage());
    }

    @Test
    public void testSaveRecordingFile_success() throws Exception {
        try (MockedStatic<ConfigProperties> configMock = mockStatic(ConfigProperties.class);
             MockedStatic<Files> filesMock = mockStatic(Files.class)) {
            configMock.when(() -> ConfigProperties.getPropertyByName("jibri.output.path")).thenReturn("/tmp/jibri");
            configMock.when(() -> ConfigProperties.getPropertyByName("video.recording.path")).thenReturn("/tmp/recordings");
            try (MockedConstruction<File> fileConstruction = mockConstruction(File.class, (mock, context) -> {
                if (context.arguments().size() > 0 && "/tmp/jibri".equals(context.arguments().get(0))) {
                    File matchingFile = mock(File.class);
            
                    when(matchingFile.toPath()).thenReturn(Path.of("/tmp/jibri/meeting123.mp4"));
                    when(mock.exists()).thenReturn(true);
                    when(mock.isDirectory()).thenReturn(true);
                    when(mock.listFiles(any(java.io.FilenameFilter.class))).then(invocation -> {
                        java.io.FilenameFilter filter = invocation.getArgument(0);
                        if (filter.accept(mock, "meeting123.mp4")) {
                            return new File[]{matchingFile};
                        }
                        return new File[0];
                    });
                }
            })) {
                filesMock.when(() -> Files.copy(any(Path.class), any(Path.class), any(java.nio.file.CopyOption[].class)))
                        .thenReturn(Path.of("/tmp/recordings/meeting123.mp4"));
                ReflectionTestUtils.invokeMethod(service, "saveRecordingFile", "meeting123");
            }
        }
    }

    @Test
    public void testSaveRecordingFile_noMatchingFile() throws Exception {
        try (MockedStatic<ConfigProperties> configMock = mockStatic(ConfigProperties.class)) {
            configMock.when(() -> ConfigProperties.getPropertyByName("jibri.output.path")).thenReturn("/tmp/jibri");
            configMock.when(() -> ConfigProperties.getPropertyByName("video.recording.path")).thenReturn("/tmp/recordings");
            try (MockedConstruction<File> fileConstruction = mockConstruction(File.class, (mock, context) -> {
                if (context.arguments().size() > 0 && "/tmp/jibri".equals(context.arguments().get(0))) {
                    File nonMatchingFile = mock(File.class);
                    when(mock.exists()).thenReturn(true);
                    when(mock.isDirectory()).thenReturn(true);
                    when(mock.listFiles(any(java.io.FilenameFilter.class))).then(invocation -> {
                        java.io.FilenameFilter filter = invocation.getArgument(0);
                        if (filter.accept(mock, "otherfile.mp4")) {
                            return new File[]{nonMatchingFile};
                        }
                        return new File[0];
                    });
                }
            })) {
                ReflectionTestUtils.invokeMethod(service, "saveRecordingFile", "meeting123");
            }
        }
    }

    @Test
    public void testSaveRecordingFile_ioException() throws Exception {
        try (MockedStatic<ConfigProperties> configMock = mockStatic(ConfigProperties.class);
             MockedStatic<Files> filesMock = mockStatic(Files.class)) {
            configMock.when(() -> ConfigProperties.getPropertyByName("jibri.output.path")).thenReturn("/tmp/jibri");
            configMock.when(() -> ConfigProperties.getPropertyByName("video.recording.path")).thenReturn("/tmp/recordings");
            try (MockedConstruction<File> fileConstruction = mockConstruction(File.class, (mock, context) -> {
                if (context.arguments().size() > 0 && "/tmp/jibri".equals(context.arguments().get(0))) {
                    File matchingFile = mock(File.class);
                   
                    when(matchingFile.toPath()).thenReturn(Path.of("/tmp/jibri/meeting123.mp4"));
                    when(mock.exists()).thenReturn(true);
                    when(mock.isDirectory()).thenReturn(true);
                    when(mock.listFiles(any(java.io.FilenameFilter.class))).then(invocation -> {
                        java.io.FilenameFilter filter = invocation.getArgument(0);
                        if (filter.accept(mock, "meeting123.mp4")) {
                            return new File[]{matchingFile};
                        }
                        return new File[0];
                    });
                }
            })) {
                filesMock.when(() -> Files.copy(any(Path.class), any(Path.class), any(java.nio.file.CopyOption[].class)))
                        .thenThrow(new IOException("fail"));
                ReflectionTestUtils.invokeMethod(service, "saveRecordingFile", "meeting123");
            }
        }
    }
}
