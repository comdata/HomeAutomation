package cm.homeautomation.logging;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import org.apache.log4j.Logger;

import cm.homeautomation.services.base.AutoCreateInstance;

@AutoCreateInstance
public class CustomOutputStream extends OutputStream {

	private static CustomOutputStream instance;
	private Logger logger = Logger.getLogger(this.getClass());

	public CustomOutputStream() {
		System.setOut(new PrintStream(this));
	}

	@Override
	public final void write(int b) throws IOException {
		// the correct way of doing this would be using a buffer
		// to store characters until a newline is encountered,
		// this implementation is for illustration only
		Runnable writer = () -> {
			logger.info((char) b);
		};
		new Thread(writer).start();
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		Runnable writer = () -> {
			if (b == null) {
				throw new NullPointerException();
			} else if ((off < 0) || (off > b.length) || (len < 0) || ((off + len) > b.length) || ((off + len) < 0)) {
				throw new IndexOutOfBoundsException();
			} else if (len == 0) {
				return;
			}
			byte[] pb = new byte[len];
			for (int i = 0; i < len; i++) {
				pb[i] = (b[off + i]);
			}
			String str = new String(pb);
			logger.info(str);
		};
		new Thread(writer).start();
	}

	public static CustomOutputStream getInstance() {
		if (instance == null) {
			instance = new CustomOutputStream();
		}

		return instance;
	}
}
