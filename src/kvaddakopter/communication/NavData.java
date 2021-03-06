package kvaddakopter.communication;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import kvaddakopter.interfaces.MainBusCommInterface;

/**
 * NavData class handles the receiving of data sent from the quad over UDP
 *
 */
public class NavData implements Runnable {

	private volatile MainBusCommInterface mMainbus;

	private DatagramSocket socket_nav;
	private InetAddress inet_addr;

	private Communication comm;
	private boolean NavDataTimeOut = false;
	private boolean mIsInitiated = false;
	private int counter = 0;

	// Container class for sensor data
	QuadData mQuadData = new QuadData();

	public NavData(int threadid, MainBusCommInterface mainbus, String name,
			Communication comm) {

		mMainbus = mainbus;
		this.comm = comm;
		try {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			socket_nav = new DatagramSocket(Communication.NAV_PORT);
			socket_nav.setSoTimeout(3000);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Initializes the UDP socket
	 */
	public void init() {
		try {
			this.inet_addr = comm.getInetAddr();
		} catch (Exception ex1) {
			ex1.printStackTrace();
		}
		System.out.println("Init NavData");
	}

	/**
	 * Checks if the communication unit is started
	 */

	public void checkIsCommRunning() {
		while (!comm.isRunning() && !comm.isInitiated()) {
			synchronized (comm) {
				try {
					comm.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			System.out.println("NavData: Stopped waiting!");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		if (!mIsInitiated) {
			mIsInitiated = true;
			NavDataTimeOut = false;
			init();
		}
	}
	
	public void checkShouldStart(){
		while (!mMainbus.shouldStart() && !mMainbus.isStarted()) {
			synchronized (mMainbus) {
				try {
					mIsInitiated = false;
					mMainbus.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			System.out.println("NavData: Stopped waiting on shouldStart!");
		}
	}
	
	public void handleTimeOut(){
		if (!comm.getIsFlying())
		{
			System.err.println("While stationary");
			mMainbus.setShouldStart(false);
			NavDataTimeOut = true;
			mMainbus.setWifiFixOk(false);
			comm.reset();
			mIsInitiated = false;
		}
		else{
			System.err.println("While flying");
			mMainbus.setControlSignal(new float[]{1,0,0,0,0});
			mMainbus.setRunController(false);
			mMainbus.setWifiFixOk(false);
			//comm.reset();
			NavDataTimeOut = true;
			mIsInitiated = false;
		}
	}

	/**
	 * Sends what information to get with AT.CONFIG messages and receives
	 * messages with {@link socket_nav.receive(packet_rcv)}
	 * 
	 * Received data is read in order with {@link NavReader class} Processes
	 * different header opts and data types
	 */
	public void run() {
		while (true) {
			checkShouldStart();
			checkIsCommRunning();
			try {
				Thread.sleep(3000);

				byte[] buf_snd = { 0x01, 0x00, 0x00, 0x00 };
				DatagramPacket packet_snd = new DatagramPacket(buf_snd,
						buf_snd.length, this.inet_addr, Communication.NAV_PORT);
				socket_nav.send(packet_snd);

				System.out.println("Sent trigger flag to UDP port "
						+ Communication.NAV_PORT);

				comm.send_at_cmd("AT*CONFIG=" + comm.get_seq()
						+ ",\"general:navdata_demo\",\"FALSE\"");
				comm.send_at_cmd("AT*CONFIG=" + comm.get_seq() + ",777060865");

				byte[] buf_rcv = new byte[10240];
				DatagramPacket packet_rcv = new DatagramPacket(buf_rcv,
						buf_rcv.length);

				while (!NavDataTimeOut) {
					try {
						// checkIsCommRunning();
						socket_nav.receive(packet_rcv);
						NavDataTimeOut = false;

					} catch (SocketTimeoutException ex3) {
						System.err.println("socket_nav.receive(): Timeout");
						handleTimeOut();
						continue;	
					} catch (Exception ex1) {
						ex1.printStackTrace();
					}
					

					NavReader reader = new NavReader(packet_rcv.getData());

					// Get header information
					long header = reader.uint32();
					boolean[] droneStates = reader.droneStates32();
					long sequenceNumber = reader.uint32();
					long visionFlag = reader.uint32();

					if (droneStates[NavReader.FLYING]) {
						comm.setIsFlying(true);
					}else{
						comm.setIsFlying(false);
					}
					
					// Run until checksum
					boolean finnished = false;
					while (!finnished) {
						int optionId = reader.uint16();
						int length = reader.uint16();

						byte[] content;
						// length includes 4 byte header
						content = reader.getSubArray(length - 4);
						NavReader contentReader = new NavReader(content);

						switch (optionId) {
						case NavReader.DEMO:
							int flyState = contentReader.uint16();
							int controlState = contentReader.uint16();

							mQuadData.setBatteryLevel(contentReader.uint32());
							mQuadData.setPitch(contentReader.float32() / 1000);
							mQuadData.setRoll(contentReader.float32() / 1000);
							mQuadData.setYaw(contentReader.float32() / 1000);
							mQuadData.setAltitude((float) (contentReader
									.int32()) / 1000);
							mQuadData.setVx(contentReader.float32() / 1000);
							mQuadData.setVy(contentReader.float32() / 1000);
							mQuadData.setVz(contentReader.float32());

							long frameIndex = contentReader.uint32();
							break;

						case NavReader.WIFI:
							mQuadData.setLinkQuality(1 - contentReader
									.float32());
							break;

						case NavReader.GPS:
							mQuadData.setGPSLat(contentReader.double64());
							mQuadData.setGPSLong(contentReader.double64());
							mQuadData.setNGPSSatelites(contentReader
									.uint32(164));
							break;
						// Checksum used to determine if message received
						// properly
						case NavReader.CHECKSUM:

							long expectedChecksum = 0;
							for (int i = 0; i < buf_rcv.length - length; i++) {
								expectedChecksum += buf_rcv[i];
							}

							long checksum = contentReader.uint32();

							if (checksum != expectedChecksum) {

								// System.err.println("Checksum fail, expected: "
								// + expectedChecksum + ", got: " + checksum);

							}

							// checksum is the last option
							finnished = true;
							// Thread.sleep(50); //TODO REFRESH TIME
							break;
						}
					}
					mMainbus.setQuadData(mQuadData);
					checkStartConditions();
				}
			} catch (Exception ex2) {
				ex2.printStackTrace();
			}
		}

	}

	public void checkStartConditions() {
		boolean wifi = false, gps = false;
		if (mQuadData.getNGPSSatelites() > 2) {
			mMainbus.setGpsFixOk(true);
			gps = true;
		}
		if (mQuadData.getLinkQuality() > 0.8) {
			mMainbus.setWifiFixOk(true);
			wifi = true;
		}

		if ( gps) {		
		if (!mMainbus.getIsArmed()) {
			mMainbus.setIsArmed(true);
			synchronized (mMainbus) {
				mMainbus.notifyAll();
				System.out.println("Communication: Armed, notifyall!");
			}
		}
		}
	}

}