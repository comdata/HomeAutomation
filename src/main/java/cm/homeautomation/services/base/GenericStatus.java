package cm.homeautomation.services.base;

import javax.xml.bind.annotation.XmlRootElement;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Generic REST status response
 * 
 * @author christoph
 *
 */
@XmlRootElement
@AllArgsConstructor
@NoArgsConstructor

@Getter
@Setter
public class GenericStatus {

	private Object object;

	private boolean success = false;

	public GenericStatus(boolean success) {
		this.success = success;

	}
}
