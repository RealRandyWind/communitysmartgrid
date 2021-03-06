package com.nativedevelopment.smartgrid.client.device;

import com.nativedevelopment.smartgrid.*;
import com.nativedevelopment.smartgrid.connection.UDPProducerConnection;

import java.io.Serializable;
import java.net.SocketAddress;
import java.util.AbstractMap;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DeviceClient extends Main implements IDeviceClient, IConfigurable {
	public static final String SETTINGS_KEY_IDENTIFIER = "identifier";

	public static final String SETTINGS_KEYPREFIX_DATAREALTIME = "data.realtime.";
	public static final String SETTINGS_KEYPREFIX_ACTIONCONTROLL = "action.control.";
	public static final String SETTINGS_KEYPREFIX_MONITORING = "monitoring.";
	public static final String SETTINGS_KEYPREFIX_BROADCAST = "broadcast.";

	public static final String APP_SETTINGS_DEFAULT_PATH = "client.device.settings";

	private MLogManager a_mLogManager = null;
	private MSettingsManager a_mSettingsManager = null;
	private MConnectionManager a_mConnectionManager = null;

	private UUID a_oIdentifier = null;

	private Queue<Serializable> a_lDataQueue = null;
	private Queue<Serializable> a_lActionQueue = null;
	private AbstractMap<Serializable,SocketAddress> a_lConnectionRegistry = null;

	protected DeviceClient() {
		a_mLogManager = MLogManager.GetInstance();
		a_mSettingsManager = MSettingsManager.GetInstance();
		a_mConnectionManager = MConnectionManager.GetInstance();
	}

	public void ShutDown() {
		a_mConnectionManager.ShutDown();
		a_mSettingsManager.ShutDown();
		a_mLogManager.ShutDown();

		System.out.printf("_SUCCESS: [DeviceClient.ShutDown]\n");
	}

	public void SetUp() {
		a_mLogManager.SetUp();
		a_mSettingsManager.SetUp();
		a_mConnectionManager.SetUp();

		ISettings oDeviceClientSettings = a_mSettingsManager.LoadSettingsFromFile(APP_SETTINGS_DEFAULT_PATH);
		Configure(oDeviceClientSettings);

		// TODO establish device connection
		// TODO establish realtime data connection
		// TODO establish monitoring connection
		// TODO establish broadcast connection

		a_lDataQueue = new ConcurrentLinkedQueue<>();
		a_lActionQueue = new ConcurrentLinkedQueue<>();
		a_lConnectionRegistry = new ConcurrentHashMap<>();

		Fx_EstablishMainConnection(null);

		a_mLogManager.Success("[DeviceClient.SetUp]",0);
	}

	public static Main GetInstance() {
		if(a_oInstance != null) { return a_oInstance; }
		a_oInstance = new DeviceClient();
		return a_oInstance;
	}

	private UUID Fx_EstablishMainConnection(UUID iConnection) {
		IConnection oConnection = null;

		a_mLogManager.Warning("[DeviceClient.Fx_EstablishMainConnection] not yet implemented",0);
		return null; //oConnection.GetIdentifier();
	}

	private UUID Fx_EstablishConnection(IConnection oConnection) {
		a_mConnectionManager.AddConnection(oConnection);
		return oConnection.GetIdentifier();
	}

	private void Fx_TerminateConnection(UUID iConnection) {
		a_mConnectionManager.RemoveConnection(iConnection);
	}

	@Override
	public void Configure(ISettings oConfigurations) {
		Serializable oIdentifier = oConfigurations.Get(SETTINGS_KEY_IDENTIFIER);
		a_oIdentifier = oIdentifier == null ? UUID.randomUUID() : UUID.fromString((String)oIdentifier);
		a_mLogManager.Success("[DeviceClient.Configure] configured",0);
	}

	@Override
	public void Run() {
		while(!IsClosing()) {
			// TODO handel incoming actions for the server itself.
			Exit();
		}
	}

	public static void main(String[] arguments)
	{
		Main oApplication = DeviceClient.GetInstance();
		int iEntryReturn = oApplication.Entry();
		System.exit(iEntryReturn);
	}
}