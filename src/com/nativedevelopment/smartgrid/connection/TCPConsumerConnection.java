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

public class TCPConsumerConnection extends Connection {
	public static final String SETTINGS_KEY_LOCALADDRESS = "local.address";
	public static final String SETTINGS_KEY_LOCALPORT = "local.port";
	public static final String SETTINGS_KEY_BUFFERCAPACITY = "buffer.capacity";

	public static final String SETTINGS_KEY_CHECKTIMELOWERBOUND = "checktime.lowerbound";
	public static final String SETTINGS_KEY_CHECKTIMEUPPERBOUND = "checktime.upperbound";
	public static final String SETTINGS_KEY_DELTACHECKUPPERBOUND = "checktime.delta";

	private Queue<Serializable> a_lToQueue = null;
	private Queue<SocketAddress> a_lRemotes = null;
	private Set<SocketChannel> a_lChannels = null;

	private String a_sLocalAddress = null;
	private int a_nLocalPort = 0;
	private int a_nBufferCapacity = 0;

	private int a_nCheckTime = 0;
	private int a_nCheckTimeLowerBound = 0;
	private int a_nCheckTimeUpperBound = 0;
	private int a_nDeltaCheckTime = 0;

	public TCPConsumerConnection(UUID oIdentifier, Queue<Serializable> lToQueue, Queue<Serializable> lToLogQueue,
								 Queue<SocketAddress> lRemotes) {
		super(oIdentifier, lToLogQueue);
		a_lToQueue = lToQueue;
		a_lRemotes = lRemotes;
		a_lChannels = new HashSet<>();
	}

	private void Fx_AcceptConnection(SocketChannel oChannel) throws Exception {
		if(oChannel == null || !oChannel.isConnected()) {
			if (a_lChannels.isEmpty()) {
				Thread.sleep(a_nCheckTime);
				a_nCheckTime += a_nDeltaCheckTime;
				a_nCheckTime = a_nCheckTime >= a_nCheckTimeUpperBound ? a_nCheckTimeUpperBound : a_nCheckTime;
			}
			return;
		}
		a_nCheckTime = a_nCheckTimeLowerBound;

		//TODO use lRemotes to set up connection requested by queue
		oChannel.shutdownOutput();
		a_lChannels.add(oChannel);

		System.out.printf("_DEBUG: [TCPConsumerConnection.Fx_AcceptConnection] connection accepted \"%s\" through \"%s\"\n"
				,String.valueOf(oChannel.getRemoteAddress())
				,String.valueOf(oChannel.getLocalAddress()));
	}

	private boolean Fx_CheckConnection(SocketChannel oChannel) throws Exception {
		if(oChannel.isConnected()) {
			return true;
		}
		a_lChannels.remove(oChannel);
		System.out.printf("_WARNING: [TCPConsumerConnection.Fx_CheckConnection] lost connection \"%s\""
				,String.valueOf(oChannel.getRemoteAddress()));
		return false;
	}

	private void Fx_Consume(byte[] rawBytes) throws Exception {
		a_lToQueue.offer(Serializer.Deserialize(rawBytes,a_nBufferCapacity));
	}

	@Override
	public void Configure(ISettings oConfigurations) {
		a_nBufferCapacity = (int)oConfigurations.Get(SETTINGS_KEY_BUFFERCAPACITY);
		a_sLocalAddress = oConfigurations.GetString(SETTINGS_KEY_LOCALADDRESS);
		a_nLocalPort = (int)oConfigurations.Get(SETTINGS_KEY_LOCALPORT);

		a_nCheckTimeLowerBound = (int)oConfigurations.Get(SETTINGS_KEY_CHECKTIMELOWERBOUND);
		a_nCheckTimeUpperBound = (int)oConfigurations.Get(SETTINGS_KEY_CHECKTIMEUPPERBOUND);
		a_nDeltaCheckTime = (int)oConfigurations.Get(SETTINGS_KEY_DELTACHECKUPPERBOUND);

		a_nCheckTime = a_nCheckTimeLowerBound;
	}

	@Override
	public void Run() {
		try {
			SocketAddress oLocal = new InetSocketAddress(a_sLocalAddress, a_nLocalPort);
			ServerSocketChannel oServerChannel = ServerSocketChannel.open();
			ByteBuffer oByteBuffer = ByteBuffer.allocate(a_nBufferCapacity);

			oServerChannel.bind(oLocal);
			oServerChannel.configureBlocking(false);

			while(!IsClose()) {
				Fx_AcceptConnection(oServerChannel.accept());
				for (SocketChannel oChannel: a_lChannels) {
					if(!Fx_CheckConnection(oChannel)) { continue; }
					oByteBuffer.clear();
					oChannel.read(oByteBuffer);
					oByteBuffer.flip();
					System.out.printf("_DEBUG: [TCPConsumerConnection.Run] bytes read \"%s\"\n",String.valueOf(oByteBuffer.remaining()));
					if(!oByteBuffer.hasRemaining()) { continue; }
					byte[] rawBytes = new byte[oByteBuffer.remaining()];
					oByteBuffer.get(rawBytes,0,rawBytes.length);
					System.out.printf("_DEBUG: [TCPConsumerConnection.Run] bytes \"%s\"\n",Arrays.toString(rawBytes));
					Fx_Consume(rawBytes);
				}
			}

			oServerChannel.close();
			for (SocketChannel oChannel: a_lChannels) {
				oChannel.close();
			}
			a_lChannels.clear();
		} catch (Exception oException) {
			System.out.printf("_WARNING: [TCPConsumerConnection.Run] %s \"%s\"\n"
					,oException.getClass().getCanonicalName(),oException.getMessage());
		}
	}
}
