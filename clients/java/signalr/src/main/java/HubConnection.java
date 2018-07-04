// Copyright (c) .NET Foundation. All rights reserved.
// Licensed under the Apache License, Version 2.0. See License.txt in the project root for license information.

import com.google.gson.Gson;
import com.google.gson.JsonArray;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class HubConnection {
    private String url;
    private Transport transport;
    private OnReceiveCallBack callback;
    private CallbackMap handlers = new CallbackMap();
    private HubProtocol protocol;
    private Gson gson = new Gson();

    public Boolean connected = false;

    public HubConnection(String url, Transport transport){
        this.url = url;
        this.protocol = new JsonHubProtocol();
        this.callback = (payload) -> {

            InvocationMessage[] messages = protocol.parseMessages(payload);

            // message will be null if we receive any message other than an invocation.
            // Adding this to avoid getting error messages on pings for now.
            for (InvocationMessage message : messages) {
                if (message != null && handlers.containsKey(message.target)) {
                    ArrayList<Object> args = gson.fromJson((JsonArray)message.arguments[0], (new ArrayList<Object>()).getClass());
                    List<ActionBase> actions = handlers.get(message.target);
                    for (ActionBase action: actions) {
                        action.invoke(args.toArray());
                    }
                }
            }
        };

        if (transport == null){
            try {
                this.transport = new WebSocketTransport(this.url);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        } else {
            this.transport = transport;
        }
    }

    public HubConnection(String url) {
        this(url, null);
    }

    public void start() throws InterruptedException {
        transport.setOnReceive(this.callback);
        transport.start();
        connected = true;
    }

    public void stop(){
        transport.stop();
        connected = false;
    }

    public void send(String method, Object... args) throws Exception {
        InvocationMessage invocationMessage = new InvocationMessage(method, args);
        String message = protocol.writeMessage(invocationMessage);
        transport.send(message);
    }

    public void on(String target, Action callback) {
        ActionBase ac = args -> callback.invoke();
        handlers.put(target, ac);
    }

    public <T1> void on(String target, Action1<T1> callback, Class<T1> param1) {
        ActionBase ac = params -> callback.invoke(param1.cast(params[0]));
        handlers.put(target, ac);
    }

    public <T1, T2> void on(String target, Action2<T1,T2> callback, Class<T1> param1, Class<T2> param2) {
        ActionBase action = params -> {
            callback.invoke(param1.cast(params[0]), param2.cast(params[1]));
        };
        handlers.put(target, action);
    }

    public <T1, T2, T3> void on(String target, Action3<T1,T2, T3> callback, Class<T1> param1, Class<T2> param2, Class<T3> param3) {
        ActionBase action = params -> {
            callback.invoke(param1.cast(params[0]), param2.cast(params[1]), param3.cast(params[2]));
        };
        handlers.put(target, action);
    }

    public <T1, T2, T3, T4> void on(String target, Action4<T1,T2, T3, T4> callback, Class<T1> param1, Class<T2> param2, Class<T3> param3, Class<T4> param4) {
        ActionBase action = params -> {
            callback.invoke(param1.cast(params[0]), param2.cast(params[1]), param3.cast(params[2]), param4.cast(params[3]));
        };
        handlers.put(target, action);
    }

    public <T1, T2, T3, T4,T5> void on(String target, Action5<T1,T2, T3, T4, T5> callback, Class<T1> param1, Class<T2> param2, Class<T3> param3, Class<T4> param4, Class<T5> param5) {
        ActionBase action = params -> {
            callback.invoke(param1.cast(params[0]), param2.cast(params[1]), param3.cast(params[2]), param4.cast(params[3]),
                    param5.cast(params[4]));
        };
        handlers.put(target, action);
    }
}