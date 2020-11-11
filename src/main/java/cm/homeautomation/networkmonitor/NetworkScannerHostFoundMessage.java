package cm.homeautomation.networkmonitor;

import javax.xml.bind.annotation.XmlRootElement;

import cm.homeautomation.entities.NetworkDevice;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@XmlRootElement
public class NetworkScannerHostFoundMessage {

	private NetworkDevice host;

}
