package cm.homeautomation.jeromq.server;

import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;



public class JeroMQServerThread implements Runnable {

	private Socket frontend;
	private Socket backend;

	@Override
	public void run() {
		ZContext ctx = new ZContext();

        frontend = ctx.createSocket(ZMQ.ROUTER);
        frontend.bind("tcp://*:5570");

        backend = ctx.createSocket(ZMQ.DEALER);
        backend.bind("inproc://backend");

        //  Launch pool of worker threads, precise number is not critical
        for (int threadNbr = 0; threadNbr < 5; threadNbr++) {
            new Thread(new JeroMQServerWorker(ctx)).start();
        }

        //  Connect backend to frontend via a proxy
        ZMQ.proxy(frontend, backend, null);

        ctx.destroy();

	}
	
	public void stop() {
		frontend.close();
		backend.close();
	}

}
