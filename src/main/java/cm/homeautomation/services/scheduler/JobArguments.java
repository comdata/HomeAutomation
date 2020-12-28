package cm.homeautomation.services.scheduler;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JobArguments {

	private List<String> argumentList;

	public JobArguments(List<String> argumentList) {
		this.argumentList = argumentList;
	}

}
