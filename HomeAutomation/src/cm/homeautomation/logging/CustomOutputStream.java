package cm.homeautomation.logging;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import org.apache.log4j.Logger;

import cm.homeautomation.services.base.AutoCreateInstance;

@AutoCreateInstance
class CustomOutputStream extends OutputStream {
	
	public CustomOutputStream() {
		System.setOut(new PrintStream(this));
	}
	 
	@Override
	public final void write(int b) throws IOException {
		// the correct way of doing this would be using a buffer
		// to store characters until a newline is encountered,
		// this implementation is for illustration only
		Logger.getLogger(CustomOutputStream.class).info((char) b);
	}
}
