package cm.homeautomation.dashbutton;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;

//import com.github.shynixn.dashbutton.DashButtonListener;

public class DashButtonService {

	/*public DashButtonService() {
		String dashButtonIp = "192.168.1.90"; //Your static dashButton ip
		DashButtonListener listener = DashButtonListener.fromIpAddress(dashButtonIp);
		listener.register(new Runnable() {
		        @Override
		        public void run() {
		            //Gets called when the dashButton with the given ip in the local network is pressed
		        	System.out.println("Button pressed");
		        }
		});
		
		
	}*/
	
	public static void main(String[] args) {
		/*new DashButtonService();
		
		while (true) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}*/
		int listenPort=67;
		int MAX_BUFFER_SIZE = 1024;
		
		 DatagramSocket socket = null;
         try {
                 
                 socket = new DatagramSocket(listenPort);  // ipaddress? throws socket exception

                 byte[] payload = new byte[MAX_BUFFER_SIZE];
                 int length = 6;
                 DatagramPacket p = new DatagramPacket(payload, length);
                 //System.out.println("Success! Now listening on port " + listenPort + "...");
                 System.out.println("Listening on port " + listenPort + "...");
                 
                 //server is always listening
                 boolean listening = true;
                 while (listening) {
                	 
                         socket.receive(p); //throws i/o exception
                         
                         System.out.println("Connection established from " + p.getAddress());
                 
                         System.out.println("Data Received: " + Arrays.toString(p.getData()));
                 }
         } catch (SocketException e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
         }
         catch (IOException e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
         }

	}
	
}
