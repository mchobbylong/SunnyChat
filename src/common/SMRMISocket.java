package common;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.server.RMISocketFactory;

public class SMRMISocket extends RMISocketFactory {
	private int dataPort = 0;

	public SMRMISocket() {
		super();
	}

	public SMRMISocket(int dataPort) {
		this.dataPort = dataPort;
	}

	@Override
	public Socket createSocket(String host, int port) throws IOException {
		return new Socket(host, port);
	}

	@Override
	public ServerSocket createServerSocket(int port) throws IOException {
		if (port == 0 && dataPort != 0)
			port = dataPort;
		return new ServerSocket(port);
	}
}
