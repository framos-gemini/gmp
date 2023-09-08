package edu.gemini.aspen.gmp.tcsoffset.model;



import edu.gemini.epics.api.EpicsClient;
import edu.gemini.aspen.gmp.tcsoffset.model.PtrMethod;
import java.util.List;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;



public class ChannelAccessSubscribe2 implements EpicsClient{

    PtrMethod _ptrMethod;

    public ChannelAccessSubscribe2( PtrMethod ptr) {
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