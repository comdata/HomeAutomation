package cm.homeautomation.services.networkmonitor;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.apache.log4j.Logger;

import cm.homeautomation.db.EntityManagerService;
import cm.homeautomation.entities.NetworkDevice;
import cm.homeautomation.networkMonitor.NetworkScanner;
import cm.homeautomation.services.base.BaseService;
import cm.homeautomation.services.base.GenericStatus;

/** 
 * service to get all hosts from the {@link NetworkScanner} internal list
 * 
 * @author christoph
 *
 */
@Path("networkdevices")
public class NetworkDevicesService extends BaseService {

    private final int PORT = 9;    
    private final String broadcastIpAddress="192.168.1.255";
	
	@Path("getAll")
	@GET
	public List<NetworkDevice> readAll() {
		EntityManager em = EntityManagerService.getNewManager();
		@SuppressWarnings("unchecked")
		List<NetworkDevice> resultList = (List<NetworkDevice>)em.createQuery("select n from NetworkDevice n").getResultList();
	
		if (resultList==null) {
			resultList=new ArrayList<NetworkDevice>();
		}
		em.close();
		return resultList;
	}
	
	@GET
	@Path("delete/{name}/{ip}/{mac}")
	public GenericStatus delete(@PathParam("name") String name, @PathParam("ip") String ip, @PathParam("mac") String macStsr) {
		// TODO perform delete
		return new GenericStatus(true);
	}
	

    @GET
    @Path("wake/{mac}")
    public GenericStatus wakeUp(@PathParam("mac") String macStr) {
        try {
            byte[] macBytes = getMacBytes(macStr);
            byte[] bytes = new byte[6 + 16 * macBytes.length];
            for (int i = 0; i < 6; i++) {
                bytes[i] = (byte) 0xff;
            }
            for (int i = 6; i < bytes.length; i += macBytes.length) {
                System.arraycopy(macBytes, 0, bytes, i, macBytes.length);
            }
            
            InetAddress address = InetAddress.getByName(broadcastIpAddress);
            DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, PORT);
            DatagramSocket socket = new DatagramSocket();
            socket.send(packet);
            socket.close();
            
            Logger.getLogger(this.getClass()).info("Wake-on-LAN packet sent.");
            return new GenericStatus(true);
        }
        catch (Exception e) {
            Logger.getLogger(this.getClass()).info("Failed to send Wake-on-LAN packet: + e");
            return new GenericStatus(false);
        }
        
    }
    
    private byte[] getMacBytes(String macStr) throws IllegalArgumentException {
        byte[] bytes = new byte[6];
        String[] hex = macStr.split("(\\:|\\-)");
        if (hex.length != 6) {
            throw new IllegalArgumentException("Invalid MAC address.");
        }
        try {
            for (int i = 0; i < 6; i++) {
                bytes[i] = (byte) Integer.parseInt(hex[i], 16);
            }
        }
        catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid hex digit in MAC address.");
        }
        return bytes;
    }
	
}
