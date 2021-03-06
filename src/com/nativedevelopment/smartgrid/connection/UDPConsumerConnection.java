package com.nativedevelopment.smartgrid.connection;

import com.nativedevelopment.smartgrid.Connection;
import com.nativedevelopment.smartgrid.ISettings;
import com.nativedevelopment.smartgrid.Serializer;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.*;

public class UDPConsumerConnection extends Connection {
	public static final String SETTINGS_KEY_LOCALADDRESS = "local.address";
	public static final String SETTINGS_KEY_LOCALPORT = "local.port";
	public static final String SETTINGS_KEY_BUFFERCAPACITY = "buffer.capacity";

	private Queue<Serializable> a_lToQueue = null;
	private Queue<SocketAddress> a_lRemotes = null;
	private Set<DatagramChannel> a_lChannels = null;

	private String a_sLocalAddress = null;
	private int a_nLocalPort = 0;
	private int a_nBufferCapacity = 0;

	public UDPConsumerConnection(UUID oIdentifier, Queue<Serializable> lToQueue, Queue<Serializable> lToLogQueue,
								 Queue<SocketAddress> lRemotes) {
		super(oIdentifier, lToLogQueue);
		a_lToQueue = lToQueue;
		a_lRemotes = lRemotes;
		a_lChannels = new HashSet<>();
	}

	private void Fx_Consume(byte[] rawBytes) throws Exception {
		a_lToQueue.offer(Serializer.Deserialize(rawBytes,a_nBufferCapacity));
	}

	@Override
	public void Configure(ISettings oConfigurations) {
		a_nBufferCapacity = (int)oConfigurations.Get(SETTINGS_KEY_BUFFERCAPACITY);
		a_sLocalAddress = oConfigurations.GetString(SETTINGS_KEY_LOCALADDRESS);
		a_nLocalPort = (int)oConfigurations.Get(SETTINGS_KEY_LOCALPORT);
	}

	@Override
	public void Run() {
		try {
			SocketAddress oLocal = new InetSocketAddress(a_sLocalAddress, a_nLocalPort);
			DatagramChannel oServerChannel = DatagramChannel.open();
			ByteBuffer oByteBuffer = ByteBuffer.allocate(a_nBufferCapacity);

			oServerChannel.bind(oLocal);

			while (!IsClose()) {
				oByteBuffer.clear();
				oServerChannel.receive(oByteBuffer);
				oByteBuffer.flip();
				if(!oByteBuffer.hasRemaining()) { continue; }
				byte[] rawBytes = new byte[oByteBuffer.remaining()];
				oByteBuffer.get(rawBytes,0,rawBytes.length);
				Fx_Consume(rawBytes);
			}

			oServerChannel.disconnect();
			oServerChannel.close();
		} catch (Exception oException) {
			System.out.printf("_WARNING: [UDPConsumerConnection.Run] %s \"%s\"\n"
					,oException.getClass().getCanonicalName(),oException.getMessage());
		}
	}
}
