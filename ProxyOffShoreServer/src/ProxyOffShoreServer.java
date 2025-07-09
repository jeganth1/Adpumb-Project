import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProxyOffShoreServer{
	public static void main(String [] args) {
		try (ServerSocket offshoreServerSocket = new ServerSocket(9090)) {
			System.out.println("Server running on port 9090");
			Socket socket = offshoreServerSocket.accept();
			System.out.println("Ship connected to server");
			BufferedReader serverinput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			BufferedWriter serveroutput = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			HttpClient httpClient = HttpClient.newHttpClient();
			OutputStream toShipOut = socket.getOutputStream();

			while (true) {
				System.out.println(" Waiting for new request from ship...");
				String line;
				StringBuilder builder = new StringBuilder();
				while ((line = serverinput.readLine()) != null && !line.isEmpty()) {
					builder.append(line).append("\r\n");
				}
				if (builder.length() == 0) {
					System.out.println("Empty request header, skipping.");
					continue;
				}

				String request = builder.toString();
				String[] lines = request.split("\r\n");
				String urlLine = lines[0];
				String[] requestPart = urlLine.split(" ");
				String method = requestPart[0];
				String url = requestPart[1];
				System.out.println("Method: " + method + ", URL: " + url);
				Map<String, String> headers = new HashMap<>();
				int contentLength = 0;

				for (int i = 1; i < lines.length; i++) {
					int colon = lines[i].indexOf(":");
					if (colon != -1) {
						String key = lines[i].substring(0, colon).trim();
						String value = lines[i].substring(colon + 1).trim();
						headers.put(key, value);
						if (key.equalsIgnoreCase("Content-Length")) {
							contentLength = Integer.parseInt(value);
						}
					}
				}

				char[] body = new char[contentLength];
				if (contentLength > 0) {
					serverinput.read(body, 0, contentLength);
					builder.append(body);
				}
				byte[] bodyBytes = new String(body).getBytes(StandardCharsets.UTF_8);
				System.out.println("Headers: " + headers);
				System.out.println("Body: " + new String(body));
				HttpRequest.Builder builders = HttpRequest.newBuilder().uri(URI.create(url)).method(method,
						contentLength > 0 ? HttpRequest.BodyPublishers.ofByteArray(bodyBytes)
								: HttpRequest.BodyPublishers.noBody());
				List<String> restrictedHeaders = List.of("host", "content-length", "transfer-encoding", "connection",
						"expect", "upgrade");

				for (Map.Entry<String, String> entry : headers.entrySet()) {
					String key = entry.getKey().toLowerCase();
					if (!restrictedHeaders.contains(key)) {
						builders.header(entry.getKey(), entry.getValue());
					}
				}
				HttpRequest requests = builders.build();
				HttpResponse<InputStream> response = httpClient.send(requests,
						HttpResponse.BodyHandlers.ofInputStream());
				serveroutput.write("HTTP/1.1 " + response.statusCode() + " OK\r\n");
				for (Map.Entry<String, List<String>> header : response.headers().map().entrySet()) {
					for (String value : header.getValue()) {
						serveroutput.write(header.getKey() + ": " + value + "\r\n");
					}
				}
				serveroutput.write("\r\n");
				serveroutput.flush();

				response.body().transferTo(toShipOut);
				toShipOut.flush();
			}
		}
		catch(Exception e) {
			System.out.println("Exception occured in Server "+e);
		}
	}
}