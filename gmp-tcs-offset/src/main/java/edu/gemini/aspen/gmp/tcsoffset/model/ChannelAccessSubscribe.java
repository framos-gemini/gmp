package edu.gemini.aspen.gmp.tcsoffset.model;

import edu.gemini.epics.api.EpicsClient;
import java.util.List;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;


public class ChannelAccessSubscribe implements EpicsClient{

    private Class<?> _objClass;

    private String _methodName;

    private Object _obj;

    private Method _method;

    public ChannelAccessSubscribe(Class<?> objClass, String methodName, Object obj) {
        _objClass = objClass;
        _methodName = methodName;
        _obj = obj;
        try {
            _method = _objClass.getDeclaredMethod(_methodName, List.class);
        }catch (NoSuchMethodException e) {
            _method = null;
            System.out.println("method name is: "+ _methodName);
            e.printStackTrace();
        }
    }

    public <T> void valueChanged(String channel, List<T> values) {

        System.out.println("the new " + channel + " value is: " + values);
        try {
            for (T e : values)
               System.out.println("class: "+ e.getClass());
            _method.invoke(_obj, values);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

    }

    public void connected() {
        System.out.println("Connected Channel");
    }

    public void disconnected() {
        System.out.println("Disconnected Channel");
    }

}