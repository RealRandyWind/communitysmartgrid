package com.nativedevelopment.smartgrid.connection;

import com.nativedevelopment.smartgrid.Connection;
import com.nativedevelopment.smartgrid.Data;
import com.nativedevelopment.smartgrid.IAction;
import com.nativedevelopment.smartgrid.IData;

import java.io.Serializable;
import java.util.AbstractMap;
import java.util.Queue;
import java.util.UUID;

public class CommDeviceConnection extends Connection{
	protected Queue<Serializable> a_lToQueue = null;
	protected Queue<Serializable> a_lFromQueue = null;
	protected AbstractMap<UUID,Serializable> a_lActionMap = null;
	protected UUID a_iDevice = null;
	protected String a_lAttributes[] = null;

	public CommDeviceConnection(UUID oIdentifier, UUID iDevice, String[] lAttributes,
								AbstractMap<UUID, Serializable> lActionMap,
								Queue<Serializable> lToQueue, Queue<Serializable> lFromQueue,
								Queue<Serializable> lToLogQueue) {
		super(oIdentifier, lToLogQueue);
		a_lToQueue = lToQueue;
		a_lFromQueue = lFromQueue;
		a_lActionMap = lActionMap;
		a_iDevice = iDevice;
		a_lAttributes = lAttributes;
	}

	@Override
	public void Run() {
		System.out.printf("_WARNING: [CommDeviceConnection.Run] not yet implemented\n");
	}
}
