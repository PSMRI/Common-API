package com.iemr.common.utils.km.openkm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.springframework.test.util.ReflectionTestUtils;

import com.openkm.sdk4j.OKMWebservices;
import com.openkm.sdk4j.bean.Document;
import com.openkm.sdk4j.bean.Folder;
import com.openkm.sdk4j.exception.PathNotFoundException;
import com.openkm.sdk4j.exception.RepositoryException;

/** Covers the OpenKM document-repository gateway used for KM file storage. */
class OpenKMServiceImplTest {

	private OKMWebservices connector;
	private MockedStatic<OpenKMConnector> connectorFactory;
	private OpenKMServiceImpl service;

	@BeforeEach
	void setUp() {
		connector = mock(OKMWebservices.class);
		connectorFactory = mockStatic(OpenKMConnector.class);
		connectorFactory.when(() -> OpenKMConnector.initialize(anyString(), anyString(), anyString()))
				.thenReturn(connector);

		service = new OpenKMServiceImpl();
		ReflectionTestUtils.setField(service, "url", "http://km.test");
		ReflectionTestUtils.setField(service, "username", "km-user");
		ReflectionTestUtils.setField(service, "password", "km-password");
		ReflectionTestUtils.setField(service, "kmRootPath", "/okm:root");
		ReflectionTestUtils.setField(service, "guestUser", "guest");
		ReflectionTestUtils.setField(service, "guestPassword", "guest");
	}

	@AfterEach
	void tearDown() {
		connectorFactory.close();
	}

	@Test
	@DisplayName("the document root is read from the repository's root folder")
	void documentRootIsTheRootFolderPath() throws Exception {
		Folder root = new Folder();
		root.setPath("/okm:root");
		when(connector.getRootFolder()).thenReturn(root);

		assertThat(service.getDocumentRoot()).isEqualTo("/okm:root");
	}

	@Test
	@DisplayName("a repository failure while reading the root yields no path rather than an exception")
	void documentRootSwallowsRepositoryFailure() throws Exception {
		when(connector.getRootFolder()).thenThrow(new RepositoryException("repository offline"));

		assertThat(service.getDocumentRoot()).isNull();
	}

	@Test
	@DisplayName("creating a document returns the uuid OpenKM assigned it")
	void createDocumentReturnsUuid(@TempDir Path tempDir) throws Exception {
		File file = tempDir.resolve("scan.pdf").toFile();
		Files.writeString(file.toPath(), "content");
		Document document = new Document();
		document.setUuid("uuid-42");
		when(connector.createDocumentSimple(eq("/okm:root/beneficiary/scan.pdf"), any(InputStream.class)))
				.thenReturn(document);

		assertThat(service.createDocument("/beneficiary/scan.pdf", file.getAbsolutePath())).isEqualTo("uuid-42");
	}

	@Test
	@DisplayName("a missing folder is created and the upload retried")
	void createDocumentCreatesMissingFolderAndRetries(@TempDir Path tempDir) throws Exception {
		File file = tempDir.resolve("scan.pdf").toFile();
		Files.writeString(file.toPath(), "content");
		Document document = new Document();
		document.setUuid("uuid-7");
		when(connector.createDocumentSimple(anyString(), any(InputStream.class)))
				.thenThrow(new PathNotFoundException("/okm:root/beneficiary/scan.pdf")).thenReturn(document);
		when(connector.getFolderChildren(anyString())).thenReturn(List.of());

		assertThat(service.createDocument("/beneficiary/scan.pdf", file.getAbsolutePath())).isEqualTo("uuid-7");
		verify(connector, times(2)).createDocumentSimple(anyString(), any(InputStream.class));
	}

	@Test
	@DisplayName("a source file that does not exist yields no uuid")
	void createDocumentWithMissingSourceFile() {
		assertThat(service.createDocument("/beneficiary/scan.pdf", "/no/such/file.pdf")).isNull();
	}

	@Test
	@DisplayName("only the folders that are missing are created")
	void createFolderSkipsExistingFolders() throws Exception {
		Folder existing = new Folder();
		existing.setPath("/okm:root/beneficiary");
		when(connector.getFolderChildren("/okm:root")).thenReturn(List.of(existing));
		when(connector.getFolderChildren("/okm:root/beneficiary")).thenReturn(List.of());

		service.createFolder("/okm:root/beneficiary/2024/scan.pdf");

		verify(connector, never()).createFolderSimple("/okm:root/beneficiary");
		verify(connector).createFolderSimple("/okm:root/beneficiary/2024");
	}

	@Test
	@DisplayName("a repository failure while walking the tree is logged, not propagated")
	void createFolderSwallowsRepositoryFailure() throws Exception {
		when(connector.getFolderChildren(anyString())).thenThrow(new RepositoryException("repository offline"));

		assertThat(service.createFolder("/okm:root/beneficiary/scan.pdf")).isNull();
	}

	@Test
	@DisplayName("deleting a document reports success")
	void deleteDocumentReportsSuccess() throws Exception {
		assertThat(service.deleteDocument("uuid-42")).isEqualTo("success");

		verify(connector).deleteDocument("uuid-42");
	}

	@Test
	@DisplayName("a delete the repository rejects reports failure")
	void deleteDocumentReportsFailure() throws Exception {
		doThrow(new PathNotFoundException("uuid-42")).when(connector).deleteDocument("uuid-42");

		assertThat(service.deleteDocument("uuid-42")).isEqualTo("failure");
	}

	@Test
	@DisplayName("the placeholder upload helper reports no document rather than failing")
	void uploadFileReturnsNothing() {
		assertThat(service.uploadFile()).isNull();
	}
}
