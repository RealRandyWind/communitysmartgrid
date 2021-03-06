package com.nativedevelopment.smartgrid.connection;

import com.nativedevelopment.smartgrid.Connection;
import com.nativedevelopment.smartgrid.ISettings;
import com.nativedevelopment.smartgrid.Serializer;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;

public class TCPProducerConnection extends Connection {
	public static final String SETTINGS_KEY_BUFFERCAPACITY = "buffer.capacity";

	public static final String SETTINGS_KEY_CHECKTIMELOWERBOUND = "checktime.lowerbound";
	public static final String SETTINGS_KEY_CHECKTIMEUPPERBOUND = "checktime.upperbound";
	public static final String SETTINGS_KEY_DELTACHECKUPPERBOUND = "checktime.delta";

	public static final String SETTINGS_KEY_DELTACONNECTIONS = "connections.delta";

	private Queue<Serializable> a_lFromQueue = null;
	private Queue<SocketAddress> a_lRemotes = null;
	private Set<SocketChannel> a_lChannels = null;

	private int a_nBufferCapacity = 0;

	private int a_nCheckTime = 0;
	private int a_nCheckTimeLowerBound = 0;
	private int a_nCheckTimeUpperBound = 0;
	private int a_nDeltaCheckTime = 0;

	private int a_nDeltaConnections = 0;

	public TCPProducerConnection(UUID oIdentifier,
								 Queue<Serializable> lFromQueue, Queue<Serializable> lToLogQueue,
								 Queue<SocketAddress> lRemotes) {
		super(oIdentifier, lToLogQueue);
		if(lFromQueue == null) {
			System.out.printf("_WARNING: [TCPProducerConnection] no queue to produce from\n");
		}
		a_lFromQueue = lFromQueue;
		a_lRemotes = lRemotes;
		a_lChannels = new HashSet<>();
	}

	private void Fx_EstablishConnections() throws Exception {
		SocketAddress oRemote = a_lRemotes.poll();
		int iRemote = a_nDeltaConnections;
		while (oRemote!=null && 0 < iRemote) {
			SocketChannel oChannel = SocketChannel.open(oRemote);
			oChannel.shutdownInput();
			a_lChannels.add(oChannel);
			oRemote = a_lRemotes.poll();
			iRemote--;
			System.out.printf("_DEBUG: [TCPProducerConnection.Fx_EstablishConnections] new connection to \"%s\" through \"%s\"\n"
					,String.valueOf(oChannel.getRemoteAddress())
					,String.valueOf(oChannel.getLocalAddress()));
		}
	}

	private boolean Fx_CheckConnection(SocketChannel oChannel) throws Exception {
		if(oChannel.isConnected()) {
			return true;
		}
		a_lChannels.remove(oChannel);
		System.out.printf("_WARNING: [TCPProducerConnection.Fx_CheckConnection] lost connection \"%s\"\n"
				,String.valueOf(oChannel.getRemoteAddress()));
		return false;
	}

	private byte[] Fx_Produce() throws Exception {
		Serializable oSerializable = a_lFromQueue.poll();
		if (oSerializable==null){
			Thread.sleep(a_nCheckTime);
			a_nCheckTime += a_nDeltaCheckTime;
			a_nCheckTime = a_nCheckTime >= a_nCheckTimeUpperBound ? a_nCheckTimeUpperBound : a_nCheckTime;
			return null;
		}
		a_nCheckTime = a_nCheckTimeLowerBound;
		return Serializer.Serialize(oSerializable,a_nBufferCapacity);
	}

	@Override
	public void Configure(ISettings oConfigurations) {
		a_nBufferCapacity = (int)oConfigurations.Get(SETTINGS_KEY_BUFFERCAPACITY);

		a_nCheckTimeLowerBound = (int)oConfigurations.Get(SETTINGS_KEY_CHECKTIMELOWERBOUND);
		a_nCheckTimeUpperBound = (int)oConfigurations.Get(SETTINGS_KEY_CHECKTIMEUPPERBOUND);
		a_nDeltaCheckTime = (int)oConfigurations.Get(SETTINGS_KEY_DELTACHECKUPPERBOUND);

		a_nDeltaConnections = (int)oConfigurations.Get(SETTINGS_KEY_DELTACONNECTIONS);

		a_nCheckTime = a_nCheckTimeLowerBound;
	}

	@Override
	public void Run() {
		try {
			ByteBuffer oByteBuffer = ByteBuffer.allocate(a_nBufferCapacity);

			while(!IsClose()) {
				Fx_EstablishConnections();

				oByteBuffer.clear();
				byte[] rawBytes = Fx_Produce();
				if(rawBytes == null) { continue; }
				oByteBuffer.put(rawBytes,0,rawBytes.length);

				for (SocketChannel oChannel: a_lChannels) {
					if(!Fx_CheckConnection(oChannel)) { continue; }
					oByteBuffer.flip();
					oChannel.write(oByteBuffer);
				}
			}

			for (SocketChannel oChannel: a_lChannels) {
				oChannel.close();
			}
			a_lChannels.clear();
		} catch (Exception oException) {
			System.out.printf("_WARNING: [TCPProducerConnection.Run] %s \"%s\"\n"
					,oException.getClass().getCanonicalName(),oException.getMessage());
		}
	}
}
