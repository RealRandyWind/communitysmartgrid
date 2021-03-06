package com.nativedevelopment.smartgrid;

import java.io.Serializable;
import java.util.Queue;
import java.util.UUID;

public class Connection implements IConnection {
    protected Thread a_oThread = null;
    protected Queue<Serializable> a_lToLogQueue = null;
    private UUID a_oIdentifier = null;
    volatile private boolean a_isClose = false;

    public Connection(UUID oIdentifier, Queue<Serializable> lToLogQueue) {
        a_oThread = new Thread(this);
        if(oIdentifier == null) { oIdentifier = UUID.randomUUID(); }
        a_oIdentifier = oIdentifier;
        a_lToLogQueue = lToLogQueue;
    }

    @Override
    public UUID GetIdentifier() {
        return a_oIdentifier;
    }

    @Override
    public void Open() {
        a_oThread.start();
    }

    @Override
    public void Close() {
        a_isClose = true;
    }

    @Override
    public void ForceClose() {
        a_oThread.interrupt();
        a_isClose = false;
    }

    @Override
    public void Join(long nTimeout) throws Exception {
        a_oThread.join(nTimeout);
    }

    @Override
    public boolean IsClose() {
        return a_isClose;
    }

    @Override
    public boolean IsActive() {
        return a_oThread.isAlive();
    }

    @Override
    public void Run() {
        System.out.printf("_WARNING: [Connection.Run] not yet implemented\n");
    }

    @Override
    public void run() {
        Run();
        a_isClose = false;
    }

    @Override
    public void Configure(ISettings oConfigurations) {
        System.out.printf("_WARNING: [Connection.Configure] nothing to configure\n");
    }
}

