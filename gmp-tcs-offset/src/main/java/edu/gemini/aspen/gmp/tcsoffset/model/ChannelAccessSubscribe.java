package edu.gemini.aspen.gmp.tcsoffset.model;



import edu.gemini.epics.api.EpicsClient;

import java.util.List;


public class ChannelAccessSubscribe implements EpicsClient{

    PtrMethod _ptrMethod;

    public ChannelAccessSubscribe(PtrMethod ptr) {
        _ptrMethod = ptr;
    }

    public <T> void valueChanged(String channel, List<T> values) {

        System.out.println("the new " + channel + " value is: " + values);
        _ptrMethod.func(values);

    }

    public void connected() {
        System.out.println("Connected Channel");
    }

    public void disconnected() {
        System.out.println("Disconnected Channel");
    }

}