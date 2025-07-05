// package com.iemr.common.controller.carestream;

// import com.google.gson.JsonSyntaxException;
// import com.iemr.common.data.carestream.CreateOrderData;
// import com.iemr.common.utils.mapper.InputMapper;
// import com.iemr.common.utils.response.OutputResponse;

// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.mockito.MockedStatic;
// import org.slf4j.Logger;

// import java.io.ByteArrayInputStream;
// import java.io.ByteArrayOutputStream;
// import java.io.IOException;
// import java.io.InputStream;
// import java.io.OutputStream;
// import java.lang.reflect.Field;
// import java.net.Socket;
// import java.net.UnknownHostException;

// import static org.junit.jupiter.api.Assertions.assertTrue;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.ArgumentMatchers.anyInt;
// import static org.mockito.ArgumentMatchers.anyString;
// import static org.mockito.Mockito.doThrow;
// import static org.mockito.Mockito.mock;
// import static org.mockito.Mockito.mockStatic;
// import static org.mockito.Mockito.never;
// import static org.mockito.Mockito.times;
// import static org.mockito.Mockito.verify;
// import static org.mockito.Mockito.when;
// import static org.mockito.Mockito.verifyNoInteractions;

// class CareStreamCreateOrderControllerTest {

//     private CareStreamCreateOrderController controller;
//     private Logger mockLogger;
//     private Socket mockSocket;
//     private InputStream mockInputStream;
//     private OutputStream mockOutputStream;

//     // Test data for CreateOrderData
//     private final String validJsonInput = "{\"firstName\":\"John\",\"lastName\":\"Doe\",\"patientID\":\"P123\",\"dob\":\"1990-01-01\",\"gender\":\"M\",\"acc\":\"ACC123\"}";

//     @BeforeEach
//     void setUp() throws NoSuchFieldException, IllegalAccessException, IOException {
//         controller = new CareStreamCreateOrderController();

//         // Mock Logger and inject it using reflection
//         mockLogger = mock(Logger.class);
//         Field loggerField = CareStreamCreateOrderController.class.getDeclaredField("logger");
//         loggerField.setAccessible(true);
//         loggerField.set(controller, mockLogger);

//         // Set @Value fields using reflection for createOrder method
//         Field ipField = CareStreamCreateOrderController.class.getDeclaredField("carestreamSocketIP");
//         ipField.setAccessible(true);
//         ipField.set(controller, "127.0.0.1");

//         Field portField = CareStreamCreateOrderController.class.getDeclaredField("carestreamSocketPort");
//         portField.setAccessible(true);
//         portField.set(controller, 12345);

//         // Mock Socket and its streams
//         mockSocket = mock(Socket.class);
//         mockInputStream = mock(InputStream.class);
//         mockOutputStream = mock(OutputStream.class);

//         when(mockSocket.getInputStream()).thenReturn(mockInputStream);
//         when(mockSocket.getOutputStream()).thenReturn(mockOutputStream);
//     }

//     @Test
//     void testCreateOrder_Success() throws IOException {
//         try (MockedStatic<Socket> mockedSocket = mockStatic(Socket.class)) {
//             // Mock the Socket constructor call
//             mockedSocket.when(() -> new Socket(anyString(), anyInt())).thenReturn(mockSocket);

//             // Simulate a successful response from the server
//             String serverResponse = "ACK";
//             when(mockInputStream.read(any(byte[].class))).thenAnswer(invocation -> {
//                 byte[] buffer = invocation.getArgument(0);
//                 System.arraycopy(serverResponse.getBytes(), 0, buffer, 0, serverResponse.length());
//                 return serverResponse.length();
//             });

//             String result = controller.createOrder(validJsonInput);

//             // Verify socket interactions
//             verify(mockOutputStream).write(any(byte[].class));
//             verify(mockInputStream).read(any(byte[].class));
//             verify(mockSocket, times(1)).close(); // Only one close in finally for createOrder

//             // Verify response
//             assertTrue(result.contains("Order successfully created"));
//             verifyNoInteractions(mockLogger); // No errors should be logged on success
//         }
//     }

//     @Test
//     void testCreateOrder_SocketCreationFails() throws IOException {
//         try (MockedStatic<Socket> mockedSocket = mockStatic(Socket.class)) {
//             // Simulate Socket constructor throwing an IOException
//             mockedSocket.when(() -> new Socket(anyString(), anyInt())).thenThrow(new IOException("Connection refused"));

//             String result = controller.createOrder(validJsonInput);

//             // Verify no socket interactions beyond creation attempt
//             verify(mockOutputStream, never()).write(any(byte[].class));
//             verify(mockInputStream, never()).read(any(byte[].class));
//             verify(mockSocket, never()).close(); // Socket was never successfully created

//             // Verify error logging and response
//             verify(mockLogger).error(anyString(), any(IOException.class));
//             assertTrue(result.contains("Connection refused"));
//         }
//     }

//     @Test
//     void testCreateOrder_WriteToOutputStreamFails() throws IOException {
//         try (MockedStatic<Socket> mockedSocket = mockStatic(Socket.class)) {
//             mockedSocket.when(() -> new Socket(anyString(), anyInt())).thenReturn(mockSocket);

//             // Simulate IOException when writing to output stream
//             doThrow(new IOException("Write error")).when(mockOutputStream).write(any(byte[].class));

//             String result = controller.createOrder(validJsonInput);

//             // Verify socket interactions
//             verify(mockOutputStream).write(any(byte[].class)); // Attempted write
//             verify(mockInputStream, never()).read(any(byte[].class)); // Read should not happen
//             verify(mockSocket, times(1)).close(); // Socket should still be closed in finally block

//             // Verify error logging and response
//             verify(mockLogger).error(anyString(), any(IOException.class));
//             assertTrue(result.contains("Write error"));
//         }
//     }

//     @Test
//     void testCreateOrder_ReadFromInputStreamFails() throws IOException {
//         try (MockedStatic<Socket> mockedSocket = mockStatic(Socket.class)) {
//             mockedSocket.when(() -> new Socket(anyString(), anyInt())).thenReturn(mockSocket);

//             // Simulate IOException when reading from input stream
//             doThrow(new IOException("Read error")).when(mockInputStream).read(any(byte[].class));

//             String result = controller.createOrder(validJsonInput);

//             // Verify socket interactions
//             verify(mockOutputStream).write(any(byte[].class));
//             verify(mockInputStream).read(any(byte[].class)); // Attempted read
//             verify(mockSocket, times(1)).close(); // Socket should still be closed

//             // Verify error logging and response
//             verify(mockLogger).error(anyString(), any(IOException.class));
//             assertTrue(result.contains("Read error"));
//         }
//     }

//     @Test
//     void testCreateOrder_ServerReturnsNoBytes() throws IOException {
//         try (MockedStatic<Socket> mockedSocket = mockStatic(Socket.class)) {
//             mockedSocket.when(() -> new Socket(anyString(), anyInt())).thenReturn(mockSocket);

//             // Simulate server returning 0 bytes (or -1 for EOF)
//             when(mockInputStream.read(any(byte[].class))).thenReturn(0); // Or -1 for EOF

//             String result = controller.createOrder(validJsonInput);

//             // Verify socket interactions
//             verify(mockOutputStream).write(any(byte[].class));
//             verify(mockInputStream).read(any(byte[].class));
//             verify(mockSocket, times(1)).close();

//             // The response will be default if no bytes are read and the if block is skipped.
//             assertTrue(result.contains("\"response\":null"));
//             assertTrue(result.contains("\"error\":null"));
//             verifyNoInteractions(mockLogger); // No exception, so no error logged
//         }
//     }

//     @Test
//     void testCreateOrder_InvalidJsonInput() throws IOException {
//         String invalidJson = "{invalid json}";
//         String result = controller.createOrder(invalidJson);

//         // Verify no socket interactions as parsing fails before socket creation
//         verify(mockSocket, never()).getInputStream();
//         verify(mockSocket, never()).getOutputStream();
//         verify(mockSocket, never()).close();
//         verify(mockOutputStream, never()).write(any(byte[].class));
//         verify(mockInputStream, never()).read(any(byte[].class));

//         // Verify error logging and response for JSON parsing error
//         verify(mockLogger).error(anyString(), any(JsonSyntaxException.class));
//         assertTrue(result.contains("JsonSyntaxException"));
//     }

//     // --- Tests for updateOrder ---

//     @Test
//     void testUpdateOrder_Success() throws IOException {
//         try (MockedStatic<Socket> mockedSocket = mockStatic(Socket.class)) {
//             // Mock the Socket constructor call with hardcoded IP/Port
//             mockedSocket.when(() -> new Socket("192.168.1.101", 1235)).thenReturn(mockSocket);

//             // Simulate a successful response from the server
//             String serverResponse = "ACK_UPDATE";
//             when(mockInputStream.read(any(byte[].class))).thenAnswer(invocation -> {
//                 byte[] buffer = invocation.getArgument(0);
//                 System.arraycopy(serverResponse.getBytes(), 0, buffer, 0, serverResponse.length());
//                 return serverResponse.length();
//             });

//             String result = controller.updateOrder(validJsonInput);

//             // Verify socket interactions
//             verify(mockOutputStream).write(any(byte[].class));
//             verify(mockInputStream).read(any(byte[].class));
//             verify(mockSocket, times(2)).close(); // Socket is closed inside the if block AND in finally

//             // Verify response
//             assertTrue(result.contains("Receiver from server: ACK_UPDATE"));
//             verifyNoInteractions(mockLogger);
//         }
//     }

//     @Test
//     void testUpdateOrder_SocketCreationFails() throws IOException {
//         try (MockedStatic<Socket> mockedSocket = mockStatic(Socket.class)) {
//             mockedSocket.when(() -> new Socket("192.168.1.101", 1235)).thenThrow(new IOException("Update connection refused"));

//             String result = controller.updateOrder(validJsonInput);

//             verify(mockSocket, never()).close();
//             verify(mockLogger).error(anyString(), any(IOException.class));
//             assertTrue(result.contains("Update connection refused"));
//         }
//     }

//     @Test
//     void testUpdateOrder_ReadFromInputStreamFails() throws IOException {
//         try (MockedStatic<Socket> mockedSocket = mockStatic(Socket.class)) {
//             mockedSocket.when(() -> new Socket("192.168.1.101", 1235)).thenReturn(mockSocket);
//             doThrow(new IOException("Update read error")).when(mockInputStream).read(any(byte[].class));

//             String result = controller.updateOrder(validJsonInput);

//             verify(mockOutputStream).write(any(byte[].class));
//             verify(mockInputStream).read(any(byte[].class));
//             verify(mockSocket, times(1)).close(); // Only finally block close
//             verify(mockLogger).error(anyString(), any(IOException.class));
//             assertTrue(result.contains("Update read error"));
//         }
//     }

//     @Test
//     void testUpdateOrder_NoBytesRead() throws IOException {
//         try (MockedStatic<Socket> mockedSocket = mockStatic(Socket.class)) {
//             mockedSocket.when(() -> new Socket("192.168.1.101", 1235)).thenReturn(mockSocket);
//             when(mockInputStream.read(any(byte[].class))).thenReturn(0); // No bytes read

//             String result = controller.updateOrder(validJsonInput);

//             verify(mockOutputStream).write(any(byte[].class));
//             verify(mockInputStream).read(any(byte[].class));
//             verify(mockSocket, times(1)).close(); // Only finally block close

//             assertTrue(result.contains("\"response\":null"));
//             assertTrue(result.contains("\"error\":null"));
//             verifyNoInteractions(mockLogger);
//         }
//     }

//     // --- Tests for deleteOrder ---

//     @Test
//     void testDeleteOrder_Success() throws IOException {
//         try (MockedStatic<Socket> mockedSocket = mockStatic(Socket.class)) {
//             // Mock the Socket constructor call with hardcoded IP/Port
//             mockedSocket.when(() -> new Socket("192.168.1.101", 1235)).thenReturn(mockSocket);

//             // Simulate a successful response from the server
//             String serverResponse = "ACK_DELETE";
//             when(mockInputStream.read(any(byte[].class))).thenAnswer(invocation -> {
//                 byte[] buffer = invocation.getArgument(0);
//                 System.arraycopy(serverResponse.getBytes(), 0, buffer, 0, serverResponse.length());
//                 return serverResponse.length();
//             });

//             String result = controller.deleteOrder(validJsonInput);

//             // Verify socket interactions
//             verify(mockOutputStream).write(any(byte[].class));
//             verify(mockInputStream).read(any(byte[].class));
//             verify(mockSocket, times(2)).close(); // Socket is closed inside the if block AND in finally

//             // Verify response
//             assertTrue(result.contains("Receiver from server: ACK_DELETE"));
//             verifyNoInteractions(mockLogger);
//         }
//     }

//     @Test
//     void testDeleteOrder_SocketCreationFails() throws IOException {
//         try (MockedStatic<Socket> mockedSocket = mockStatic(Socket.class)) {
//             mockedSocket.when(() -> new Socket("192.168.1.101", 1235)).thenThrow(new IOException("Delete connection refused"));

//             String result = controller.deleteOrder(validJsonInput);

//             verify(mockSocket, never()).close();
//             verify(mockLogger).error(anyString(), any(IOException.class));
//             assertTrue(result.contains("Delete connection refused"));
//         }
//     }

//     @Test
//     void testDeleteOrder_ReadFromInputStreamFails() throws IOException {
//         try (MockedStatic<Socket> mockedSocket = mockStatic(Socket.class)) {
//             mockedSocket.when(() -> new Socket("192.168.1.101", 1235)).thenReturn(mockSocket);
//             doThrow(new IOException("Delete read error")).when(mockInputStream).read(any(byte[].class));

//             String result = controller.deleteOrder(validJsonInput);

//             verify(mockOutputStream).write(any(byte[].class));
//             verify(mockInputStream).read(any(byte[].class));
//             verify(mockSocket, times(1)).close(); // Only finally block close
//             verify(mockLogger).error(anyString(), any(IOException.class));
//             assertTrue(result.contains("Delete read error"));
//         }
//     }

//     @Test
//     void testDeleteOrder_NoBytesRead() throws IOException {
//         try (MockedStatic<Socket> mockedSocket = mockStatic(Socket.class)) {
//             mockedSocket.when(() -> new Socket("192.168.1.101", 1235)).thenReturn(mockSocket);
//             when(mockInputStream.read(any(byte[].class))).thenReturn(0); // No bytes read

//             String result = controller.deleteOrder(validJsonInput);

//             verify(mockOutputStream).write(any(byte[].class));
//             verify(mockInputStream).read(any(byte[].class));
//             verify(mockSocket, times(1)).close(); // Only finally block close

//             assertTrue(result.contains("\"response\":null"));
//             assertTrue(result.contains("\"error\":null"));
//             verifyNoInteractions(mockLogger);
//         }
//     }
// }