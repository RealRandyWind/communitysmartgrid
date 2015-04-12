package com.nativedevelopment.smartgrid;


import com.nativedevelopment.smartgrid.client.Client;

import java.util.UUID;

public class DummyDevice implements IDevice {
	private UUID id;
	private double currentUsage;
	private double potentialProduction;
	private Location location;

	public DummyDevice(Location location) {
		this.id = UUID.randomUUID();
		this.location = location;
	}

	public void setPotentialProduction(double potentialProduction) {
		this.potentialProduction = potentialProduction;
	}

	public void setCurrentUsage(double currentUsage) {
		this.currentUsage = currentUsage;
	}

	@Override
	public UUID getIdentifier() {
		return this.id;
	}

	@Override
	public Data getData() {
		Data d = new Data();
		d.deviceId = this.id;
		d.potentialProduction = potentialProduction;
		d.usage = this.currentUsage;
		d.location = this.location;
		d.unixTimestamp = System.currentTimeMillis()/1000;
		return d;
	}

	@Override
	public void performAction(Action oAction) {
		this.currentUsage = oAction.newProduction * -1;
	}
}
