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
package com.iemr.common.service.users;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.iemr.common.data.users.EmployeeSignature;
import com.iemr.common.repository.users.EmployeeSignatureRepo;

@ExtendWith(MockitoExtension.class)
public class EmployeeSignatureServiceImplTest {

    @Mock
    private EmployeeSignatureRepo employeeSignatureRepo;

    @InjectMocks
    private EmployeeSignatureServiceImpl employeeSignatureService;

    private EmployeeSignature testEmployeeSignature;
    private Long testUserID;

    @BeforeEach
    void setUp() {
        testUserID = 1L;
        testEmployeeSignature = createTestEmployeeSignature();
    }

    private EmployeeSignature createTestEmployeeSignature() {
        EmployeeSignature signature = new EmployeeSignature();
        signature.setUserSignatureID(1L);
        signature.setUserID(testUserID);
        signature.setFileName("test_signature.png");
        signature.setFileType("image/png");
        signature.setFileContent("base64encodedcontent");
        signature.setSignature("test signature bytes".getBytes());
        signature.setDeleted(false);
        signature.setCreatedBy("test_user");
        signature.setCreatedDate(Timestamp.valueOf(LocalDateTime.now()));
        signature.setModifiedBy("test_user");
        signature.setLastModDate(Timestamp.valueOf(LocalDateTime.now()));
        return signature;
    }

    @Test
    void testFetchSignature_Success() {
        // Given
        when(employeeSignatureRepo.findOneByUserID(testUserID)).thenReturn(testEmployeeSignature);

        // When
        EmployeeSignature result = employeeSignatureService.fetchSignature(testUserID);

        // Then
        assertNotNull(result);
        assertEquals(testUserID, result.getUserID());
        assertEquals("test_signature.png", result.getFileName());
        assertEquals("image/png", result.getFileType());
        assertEquals("base64encodedcontent", result.getFileContent());
        assertArrayEquals("test signature bytes".getBytes(), result.getSignature());
        assertEquals(false, result.getDeleted());
        assertEquals("test_user", result.getCreatedBy());
        assertNotNull(result.getCreatedDate());
        
        verify(employeeSignatureRepo).findOneByUserID(testUserID);
    }

    @Test
    void testFetchSignature_NotFound() {
        // Given
        when(employeeSignatureRepo.findOneByUserID(testUserID)).thenReturn(null);

        // When
        EmployeeSignature result = employeeSignatureService.fetchSignature(testUserID);

        // Then
        assertNull(result);
        verify(employeeSignatureRepo).findOneByUserID(testUserID);
    }

    @Test
    void testFetchSignature_WithNullUserID() {
        // Given
        Long nullUserID = null;
        when(employeeSignatureRepo.findOneByUserID(nullUserID)).thenReturn(null);

        // When
        EmployeeSignature result = employeeSignatureService.fetchSignature(nullUserID);

        // Then
        assertNull(result);
        verify(employeeSignatureRepo).findOneByUserID(nullUserID);
    }

    @Test
    void testFetchSignature_WithDifferentUserID() {
        // Given
        Long differentUserID = 999L;
        EmployeeSignature differentSignature = createTestEmployeeSignature();
        differentSignature.setUserID(differentUserID);
        differentSignature.setFileName("different_signature.jpg");
        
        when(employeeSignatureRepo.findOneByUserID(differentUserID)).thenReturn(differentSignature);

        // When
        EmployeeSignature result = employeeSignatureService.fetchSignature(differentUserID);

        // Then
        assertNotNull(result);
        assertEquals(differentUserID, result.getUserID());
        assertEquals("different_signature.jpg", result.getFileName());
        verify(employeeSignatureRepo).findOneByUserID(differentUserID);
    }

    @Test
    void testExistSignature_SignatureExists() {
        // Given
        Long countResult = 1L;
        when(employeeSignatureRepo.countByUserIDAndSignatureNotNull(testUserID)).thenReturn(countResult);

        // When
        Boolean result = employeeSignatureService.existSignature(testUserID);

        // Then
        assertTrue(result);
        verify(employeeSignatureRepo).countByUserIDAndSignatureNotNull(testUserID);
    }

    @Test
    void testExistSignature_SignatureDoesNotExist() {
        // Given
        Long countResult = 0L;
        when(employeeSignatureRepo.countByUserIDAndSignatureNotNull(testUserID)).thenReturn(countResult);

        // When
        Boolean result = employeeSignatureService.existSignature(testUserID);

        // Then
        assertFalse(result);
        verify(employeeSignatureRepo).countByUserIDAndSignatureNotNull(testUserID);
    }

    @Test
    void testExistSignature_MultipleSignaturesExist() {
        // Given
        Long countResult = 3L;
        when(employeeSignatureRepo.countByUserIDAndSignatureNotNull(testUserID)).thenReturn(countResult);

        // When
        Boolean result = employeeSignatureService.existSignature(testUserID);

        // Then
        assertTrue(result);
        verify(employeeSignatureRepo).countByUserIDAndSignatureNotNull(testUserID);
    }

    @Test
    void testExistSignature_WithNullUserID() {
        // Given
        Long nullUserID = null;
        Long countResult = 0L;
        when(employeeSignatureRepo.countByUserIDAndSignatureNotNull(nullUserID)).thenReturn(countResult);

        // When
        Boolean result = employeeSignatureService.existSignature(nullUserID);

        // Then
        assertFalse(result);
        verify(employeeSignatureRepo).countByUserIDAndSignatureNotNull(nullUserID);
    }

    @Test
    void testExistSignature_WithDifferentUserID() {
        // Given
        Long differentUserID = 999L;
        Long countResult = 2L;
        when(employeeSignatureRepo.countByUserIDAndSignatureNotNull(differentUserID)).thenReturn(countResult);

        // When
        Boolean result = employeeSignatureService.existSignature(differentUserID);

        // Then
        assertTrue(result);
        verify(employeeSignatureRepo).countByUserIDAndSignatureNotNull(differentUserID);
    }

    @Test
    void testExistSignature_EdgeCaseCountEqualsOne() {
        // Given - Edge case where exactly one signature exists
        Long countResult = 1L;
        when(employeeSignatureRepo.countByUserIDAndSignatureNotNull(testUserID)).thenReturn(countResult);

        // When
        Boolean result = employeeSignatureService.existSignature(testUserID);

        // Then
        assertTrue(result);
        verify(employeeSignatureRepo).countByUserIDAndSignatureNotNull(testUserID);
    }

    @Test
    void testFetchSignature_WithDeletedSignature() {
        // Given
        EmployeeSignature deletedSignature = createTestEmployeeSignature();
        deletedSignature.setDeleted(true);
        when(employeeSignatureRepo.findOneByUserID(testUserID)).thenReturn(deletedSignature);

        // When
        EmployeeSignature result = employeeSignatureService.fetchSignature(testUserID);

        // Then
        assertNotNull(result);
        assertEquals(true, result.getDeleted());
        verify(employeeSignatureRepo).findOneByUserID(testUserID);
    }

    @Test
    void testFetchSignature_WithNullSignatureBytes() {
        // Given
        EmployeeSignature signatureWithNullBytes = createTestEmployeeSignature();
        signatureWithNullBytes.setSignature(null);
        when(employeeSignatureRepo.findOneByUserID(testUserID)).thenReturn(signatureWithNullBytes);

        // When
        EmployeeSignature result = employeeSignatureService.fetchSignature(testUserID);

        // Then
        assertNotNull(result);
        assertNull(result.getSignature());
        verify(employeeSignatureRepo).findOneByUserID(testUserID);
    }

    @Test
    void testFetchSignature_WithEmptySignatureBytes() {
        // Given
        EmployeeSignature signatureWithEmptyBytes = createTestEmployeeSignature();
        signatureWithEmptyBytes.setSignature(new byte[0]);
        when(employeeSignatureRepo.findOneByUserID(testUserID)).thenReturn(signatureWithEmptyBytes);

        // When
        EmployeeSignature result = employeeSignatureService.fetchSignature(testUserID);

        // Then
        assertNotNull(result);
        assertNotNull(result.getSignature());
        assertEquals(0, result.getSignature().length);
        verify(employeeSignatureRepo).findOneByUserID(testUserID);
    }

    @Test
    void testFetchSignature_WithLargeUserID() {
        // Given
        Long largeUserID = Long.MAX_VALUE;
        when(employeeSignatureRepo.findOneByUserID(largeUserID)).thenReturn(null);

        // When
        EmployeeSignature result = employeeSignatureService.fetchSignature(largeUserID);

        // Then
        assertNull(result);
        verify(employeeSignatureRepo).findOneByUserID(largeUserID);
    }

    @Test
    void testExistSignature_WithLargeUserID() {
        // Given
        Long largeUserID = Long.MAX_VALUE;
        Long countResult = 0L;
        when(employeeSignatureRepo.countByUserIDAndSignatureNotNull(largeUserID)).thenReturn(countResult);

        // When
        Boolean result = employeeSignatureService.existSignature(largeUserID);

        // Then
        assertFalse(result);
        verify(employeeSignatureRepo).countByUserIDAndSignatureNotNull(largeUserID);
    }

    @Test
    void testFetchSignature_RepositoryThrowsException() {
        // Given
        when(employeeSignatureRepo.findOneByUserID(testUserID))
            .thenThrow(new RuntimeException("Database connection error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            employeeSignatureService.fetchSignature(testUserID);
        });
        
        verify(employeeSignatureRepo).findOneByUserID(testUserID);
    }

    @Test
    void testExistSignature_RepositoryThrowsException() {
        // Given
        when(employeeSignatureRepo.countByUserIDAndSignatureNotNull(testUserID))
            .thenThrow(new RuntimeException("Database connection error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            employeeSignatureService.existSignature(testUserID);
        });
        
        verify(employeeSignatureRepo).countByUserIDAndSignatureNotNull(testUserID);
    }

    @Test
    void testFetchSignature_WithCompleteSignatureData() {
        // Given
        EmployeeSignature completeSignature = createTestEmployeeSignature();
        completeSignature.setFileName("complete_signature.pdf");
        completeSignature.setFileType("application/pdf");
        completeSignature.setFileContent("base64PDFContent");
        completeSignature.setModifiedBy("admin_user");
        
        when(employeeSignatureRepo.findOneByUserID(testUserID)).thenReturn(completeSignature);

        // When
        EmployeeSignature result = employeeSignatureService.fetchSignature(testUserID);

        // Then
        assertNotNull(result);
        assertEquals("complete_signature.pdf", result.getFileName());
        assertEquals("application/pdf", result.getFileType());
        assertEquals("base64PDFContent", result.getFileContent());
        assertEquals("admin_user", result.getModifiedBy());
        verify(employeeSignatureRepo).findOneByUserID(testUserID);
    }

    @Test
    void testExistSignature_BoundaryValueZero() {
        // Given - Testing boundary value where count is exactly 0
        Long countResult = 0L;
        when(employeeSignatureRepo.countByUserIDAndSignatureNotNull(testUserID)).thenReturn(countResult);

        // When
        Boolean result = employeeSignatureService.existSignature(testUserID);

        // Then
        assertFalse(result);
        verify(employeeSignatureRepo).countByUserIDAndSignatureNotNull(testUserID);
    }

    @Test
    void testExistSignature_BoundaryValueOne() {
        // Given - Testing boundary value where count is exactly 1
        Long countResult = 1L;
        when(employeeSignatureRepo.countByUserIDAndSignatureNotNull(testUserID)).thenReturn(countResult);

        // When
        Boolean result = employeeSignatureService.existSignature(testUserID);

        // Then
        assertTrue(result);
        verify(employeeSignatureRepo).countByUserIDAndSignatureNotNull(testUserID);
    }

    @Test
    void testFetchSignature_WithMinimalData() {
        // Given
        EmployeeSignature minimalSignature = new EmployeeSignature();
        minimalSignature.setUserID(testUserID);
        when(employeeSignatureRepo.findOneByUserID(testUserID)).thenReturn(minimalSignature);

        // When
        EmployeeSignature result = employeeSignatureService.fetchSignature(testUserID);

        // Then
        assertNotNull(result);
        assertEquals(testUserID, result.getUserID());
        assertNull(result.getFileName());
        assertNull(result.getFileType());
        assertNull(result.getSignature());
        verify(employeeSignatureRepo).findOneByUserID(testUserID);
    }
}
