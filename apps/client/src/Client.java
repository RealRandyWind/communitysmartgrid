package com.nativedevelopment.smartgrid.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;

import com.nativedevelopment.smartgrid.*;

public class Client extends Main implements IClient {
	private MLogManager mLogManager = MLogManager.GetInstance();
	private MConnectionManager mConnectionMannager = MConnectionManager.GetInstance();
	private UUID uuid;

	protected InetAddress ip;

	private Map<UUID,IDevice> kvDevices = new HashMap<UUID,IDevice>();

	protected Client() {
		this.uuid = UUID.randomUUID();
	}

	public void ShutDown() {
		mLogManager.ShutDown();

		System.out.printf("_SUCCESS: [Client.ShutDown] Thread from RMI still active in background.\n");
	}

	public void SetUp() {
		mLogManager.SetUp();

		try {
			IClient stub = (IClient) UnicastRemoteObject.exportObject(this, 0);

			// Bind the remote object's stub in the registry
			Registry registry = LocateRegistry.getRegistry();
			registry.bind("Client" + this.getIdentifier(), stub); // todo allow multiple clients by appending "Client" with identifier

			mLogManager.Success("[Client.SetUp] Server ready, bound in registry with name Client"+this.getIdentifier(), 0);
		} catch (Exception e) {
			mLogManager.Error("Server exception: " + e.toString(),0);
			e.printStackTrace();
		}

		mLogManager.Success("[Client.SetUp]",0);
	}

	public void Run() {
		mLogManager.Log("[Client.Run] running test",0);
		Data d = new Data();
		d.clientId = UUID.fromString(Config.TestClientUUID);
		d.clientIp = this.ip;
		d.deviceId = UUID.randomUUID();
		d.predictedProduction = 100.0;
		d.potentialProduction = 100.0;
		this.sendRealTimeData(d);
		this.sendRealTimeData(d);
	}

	public void AddDevice(IDevice oDevice) {
		if(oDevice == null) {
			mLogManager.Warning("[Client.AddDevice] device to add is null",0);
			return;
		}

		kvDevices.put(oDevice.getIdentifier(),oDevice);
	}

	public IDevice GetDevice(UUID idDevice) {
		if(idDevice == null) {
			mLogManager.Warning("[Client.GetDevice] device id is null",0);
			return null;
		}

		return kvDevices.get(idDevice);
	}

	public static Main GetInstance() {
		if(a_oInstance != null) { return a_oInstance; }
		a_oInstance = new Client();
		return a_oInstance;
	}

	public static void main(String[] arguments) {
		Client oApplication = (Client) Client.GetInstance();
		try {
			oApplication.ip = InetAddress.getByName(arguments[0]);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		int iEntryReturn = oApplication.Entry();
	}

    @Override
    public void passActionToDevice(Action action) throws RemoteException {
		mLogManager.Debug("[Client.passActionToDevice] called", 0);
		mLogManager.Info("[Client.passActionToDevice] Received action for device " + action.deviceId, 0);
    }

	@Override
	public UUID getIdentifier() {
		// TODO hardcoded
		return UUID.fromString("3b287567-0813-4903-b7d6-e23bf5402c01");
		//return this.uuid;
	}

	public void sendRealTimeData(Data data) {
		DatagramSocket socket = null;
		try {
			socket = new DatagramSocket();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(data);
			oos.flush();
			byte[] databytes = baos.toByteArray();

			DatagramPacket msg = new DatagramPacket(databytes, databytes.length, InetAddress.getByName(Config.IP_DataCollection), Config.Port_DataCollection);
			socket.send(msg);
			mLogManager.Log("Send Real-Time data to CollectionServer. Bytes " + databytes.length,0);
		} catch (SocketException e) {
			mLogManager.Error("SocketException: " + e.getMessage(), 0);
		} catch (IOException e) {
			mLogManager.Error("IOException: " + e.getMessage(), 0);
		}
	}
}
