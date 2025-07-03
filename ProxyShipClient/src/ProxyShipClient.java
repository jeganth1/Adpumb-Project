import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProxyShipClient {
	public static void main(String[] args) {
		try (ServerSocket shipServerSoket = new ServerSocket(8080)) {
			System.out.println("Client Ship running on port 8080");
			try (Socket connector = new Socket("proxy-server", 9090)) {
				System.out.println("ship and server are connected");
				BufferedReader serverOutput = new BufferedReader(new InputStreamReader(connector.getInputStream()));
				BufferedWriter serverInput = new BufferedWriter(new OutputStreamWriter(connector.getOutputStream()));
				ExecutorService queue = Executors.newSingleThreadExecutor();

				while (true) {
					Socket shipSocket = shipServerSoket.accept();
					queue.submit(() -> processRequest(shipSocket, serverOutput, serverInput));
				}
			}
		} catch (Exception e) {
			System.out.println("Exception Occured " + e);
		}
	}

	private static void processRequest(Socket shipSocket, BufferedReader output, BufferedWriter input) {
		try (shipSocket) {
			BufferedReader clientInputt = new BufferedReader(new InputStreamReader(shipSocket.getInputStream()));
			BufferedWriter clientOutput = new BufferedWriter(new OutputStreamWriter(shipSocket.getOutputStream()));

			String line;
			StringBuilder builder = new StringBuilder();
			while ((line = clientInputt.readLine()) != null && !line.isEmpty()) {
				
					builder.append(line).append("\r\n");
			
				
			}
			builder.append("\r\n");
			input.write(builder.toString());
			input.flush();

			String responseLine;
			while ((responseLine = output.readLine()) != null) {
				clientOutput.write(responseLine + "\r\n");
				if (responseLine.isEmpty())
					break;
			}

			char[] buffer = new char[8192];
			int len;
			while ((len = output.read(buffer)) != -1) {
				clientOutput.write(buffer, 0, len);
				if (len < 8192)
					break;
			}
			clientOutput.flush();
		}

		catch (Exception e) {
			System.out.println("Exception occured in process" + e);
		}
	}
}