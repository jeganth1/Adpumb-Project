import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ProxyOffShoreServer{
	public static void main(String [] args) {
		try (ServerSocket offshoreServerSocket = new ServerSocket(9090)) {
			System.out.println("Server running on port 9090");
			Socket socket=offshoreServerSocket.accept();
			System.out.println("Ship connected to server");
			BufferedReader serverinput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			BufferedWriter serveroutput = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			HttpClient httpClient=HttpClient.newHttpClient();
			
			while(true) {
			String line;
			StringBuilder builder=new StringBuilder();
			while ((line=serverinput.readLine())!=null && !line.isEmpty()) {
			
					builder.append(line).append("\r\n");
			
				
			}
			if (builder.length()==0)
				continue;
			
			
			String request = builder.toString();
			String[] lines = request.split("\r\n");
			String urlLine = lines[0]; 
			String url = urlLine.split(" ")[1];

			HttpRequest httpRequest = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
			HttpResponse<String> response;
			
			response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
			//serveroutput.write(response.body());
			serveroutput.write("HTTP/1.1 " + response.statusCode() + " OK\r\n");
			serveroutput.write("Content-Length: " + response.body().length() + "\r\n");
			serveroutput.write("Content-Type: text/html\r\n");
			serveroutput.write("\r\n");
			serveroutput.write(response.body());
			serveroutput.flush();
		}
		}
		catch(Exception e) {
			System.out.println("Exception occured in Server "+e);
		}
	}
}