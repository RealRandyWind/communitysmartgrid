package com.nativedevelopment.smartgrid.server.management;

import com.nativedevelopment.smartgrid.Main;
import com.nativedevelopment.smartgrid.MLogManager;
import com.nativedevelopment.smartgrid.MConnectionManager;

public class ManagementServer extends Main {
	private MLogManager mLogManager = MLogManager.GetInstance();
	private MConnectionManager mConnectionMannager = MConnectionManager.GetInstance();
	
	protected ManagementServer() {

	}

	public void ShutDown() {
		mConnectionMannager.ShutDown();
		mLogManager.ShutDown();

		System.out.printf("_SUCCESS: [ManagementServer.ShutDown]\n");
	}

	public void SetUp() {
		mLogManager.SetUp();
		mConnectionMannager.SetUp();

		mLogManager.Success("[ManagementServer.SetUp]",0);
	}

	public static Main GetInstance() {
		if(a_oInstance != null) { return a_oInstance; }
		a_oInstance = new ManagementServer();
		return a_oInstance;
	}

	public static void main(String[] arguments)
	{
		Main oApplication = ManagementServer.GetInstance();
		int iEntryReturn = oApplication.Entry();
		System.exit(iEntryReturn);
	}
}