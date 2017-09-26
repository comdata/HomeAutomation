package cm.homeautomation.logging;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import org.apache.logging.log4j.LogManager;

import cm.homeautomation.services.base.AutoCreateInstance;

@AutoCreateInstance
public class CustomOutputStream extends OutputStream {
	
	private static CustomOutputStream instance;

	public CustomOutputStream() {
		System.setOut(new PrintStream(this));
		instance=this;
	}
	 
	@Override
	public final void write(int b) throws IOException {
		// the correct way of doing this would be using a buffer
		// to store characters until a newline is encountered,
		// this implementation is for illustration only
		LogManager.getLogger(this.getClass()).info((char) b);
	}
	
	public static CustomOutputStream getInstance() {
		return instance;
	}
}
