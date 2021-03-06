package com.nativedevelopment.smartgrid.tests;

import com.nativedevelopment.smartgrid.*;
import com.nativedevelopment.smartgrid.connection.RMIControllerCallerConnection;
import com.nativedevelopment.smartgrid.connection.RMIControllerListenerConnection;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.junit.Assert.*;

public class RMIConnectionTest implements ITestCase {
	static final String SETTINGS_VALUE_EXCHANGE = "StubController";
	static final String SETTINGS_VALUE_HOST = "localhost";
	static final int SETTINGS_VALUE_CHECKTIME = 400;
	static final int SETTINGS_VALUE_PORT = 0;
	static final String SERIALIZABLE_OBJECT = "Serializable";


	MLogManager a_mLogManager = null;

	IController a_oController = null;
	Queue<Serializable> a_oLogQueue = null;

	ISettings a_oCallerConfiguration = null;
	ISettings a_oListenerConfiguration = null;

	Queue<Serializable> a_lQueue = null;
	HashMap<Serializable, Serializable> a_lSerializables = null;
	Serializable a_oSerializable = null;

	@Before
	public void setUp() throws Exception {
		a_mLogManager = MLogManager.GetInstance();
		a_mLogManager.SetUp();

		a_lQueue = new ConcurrentLinkedQueue<>();
		a_lSerializables = new HashMap<>();
		a_oSerializable = SERIALIZABLE_OBJECT;

		a_lSerializables.put(a_oSerializable, a_oSerializable);

		a_oController = new StubController(a_lQueue, a_oSerializable, a_lSerializables);

		a_oCallerConfiguration = new Settings(null);
		a_oListenerConfiguration = new Settings(null);

		a_oCallerConfiguration.Set(RMIControllerCallerConnection.SETTINGS_KEY_EXCHANGE,SETTINGS_VALUE_EXCHANGE);
		a_oCallerConfiguration.Set(RMIControllerCallerConnection.SETTINGS_KEY_REMOTEADDRESS,SETTINGS_VALUE_HOST);
		a_oCallerConfiguration.Set(RMIControllerCallerConnection.SETTING_KEY_ISREBIND,false);
		a_oCallerConfiguration.Set(RMIControllerCallerConnection.SETTINGS_KEY_CHECKTIME,SETTINGS_VALUE_CHECKTIME);
		a_oListenerConfiguration.Set(RMIControllerListenerConnection.SETTINGS_KEY_EXCHANGE,SETTINGS_VALUE_EXCHANGE);
		a_oListenerConfiguration.Set(RMIControllerListenerConnection.SETTING_KEY_ISREBIND,false);
		a_oListenerConfiguration.Set(RMIControllerListenerConnection.SETTINGS_KEY_CHECKTIME,SETTINGS_VALUE_CHECKTIME);
		a_oListenerConfiguration.Set(RMIControllerListenerConnection.SETTINGS_KEY_LOCALPORT,SETTINGS_VALUE_PORT);
		a_oListenerConfiguration.Set(RMIControllerListenerConnection.SETTING_KEY_ISFORCEUNEXPORT,true);
	}

	@After
	public void tearDown() throws Exception {
		a_mLogManager.ShutDown();
	}

	@Test
	public void testRun() throws Exception {
		a_mLogManager.Test("[RMIConnectionTest.testRun] begin", 0);
		IPromise oPromise = new Promise();
		int nTryCount = 6;
		IStubController oStubController = null;

		IConnection oCaller = new RMIControllerCallerConnection(null,a_oLogQueue, oPromise);
		IConnection oListener = new RMIControllerListenerConnection(null, a_oController, a_oLogQueue);

		a_mLogManager.Test("[RMIConnectionTest.testRun] Configure",0);
		oCaller.Configure(a_oCallerConfiguration);
		oListener.Configure(a_oListenerConfiguration);

		a_mLogManager.Test("[RMIConnectionTest.testRun] Open",0);
		oListener.Open();
		oCaller.Open();
		Thread.sleep(500);

		a_mLogManager.Test("[RMIConnectionTest.testRun] Run",0);
		while(!oPromise.IsDone()) {
			if (nTryCount <= 0) { fail(); }
			Thread.sleep(200);
			nTryCount--;
		}
		oStubController = (IStubController)oPromise.Get();
		assertTrue(a_lQueue.isEmpty());
		oStubController.ProcedureNoArguments();
		Thread.sleep(150);
		oStubController.ProcedureArgumentsSerializable(a_oSerializable, a_lSerializables);
		Thread.sleep(150);
		assertEquals(a_oSerializable, oStubController.FunctionReturnNoArgumentsSerializable());
		Thread.sleep(150);
		assertEquals(a_oSerializable, oStubController.FunctionReturnArgumentsSerializable(a_oSerializable, a_lSerializables));
		Thread.sleep(150);
		assertEquals(new LinkedList<>(a_lSerializables.values()), oStubController.FunctionReturnListNoArgumentsSerializable());
		Thread.sleep(150);
		assertEquals(a_lSerializables, oStubController.FunctionReturnMapNoArgumentsSerializable());
		Thread.sleep(500);

		assertFalse(a_lQueue.isEmpty());
		assertTrue(a_lQueue.contains(StubController.EMethods.PROCEDURENOARGUMENTS));
		assertTrue(a_lQueue.contains(StubController.EMethods.PROCEDUREARGUMENTSSERIALIZABLE));
		assertTrue(a_lQueue.contains(StubController.EMethods.FUNCTIONRETURNNOARGUMENTSSERIALIZABLE));
		assertTrue(a_lQueue.contains(StubController.EMethods.FUNCTIONRETURNARGUMENTSSERIALIZABLE));
		assertTrue(a_lQueue.contains(StubController.EMethods.FUNCTIONRETURNLISTNOARGUMENTSSERIALIZABLE));
		assertTrue(a_lQueue.contains(StubController.EMethods.FUNCTIONRETURNMAPNOARGUMENTSSERIALIZABLE));


		a_mLogManager.Test("[RMIConnectionTest.testRun] Close",0);
		oListener.Close();
		oCaller.Close();
		Thread.sleep(200);

		a_mLogManager.Test("[RMIConnectionTest.testRun] end",0);
	}
}