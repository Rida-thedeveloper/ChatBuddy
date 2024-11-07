package com.example.chat_buddy.client;

import com.example.chat_buddy.Message;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class MyStompSessionHandler extends StompSessionHandlerAdapter {
    private String username;
  

    public MyStompSessionHandler(String username){
        this.username = username;
       
    }

    @Override
    public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
        System.out.println("Client Connected");
        session.send("/app/connect",username);

        session.subscribe("/topic/messages", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return Message.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                try {
                    if (payload instanceof Message) {
                        Message message = (Message) payload;
                       
                        System.out.println("Received message: " + message.getUser() + ": " + message.getMessage());
                    } else {
                        System.out.println("Received unexpected payload type: " + payload.getClass());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        System.out.println("Client Subscribe to /topic/messages");

        session.subscribe("/topic/users", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return new ArrayList<String>().getClass();
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                try{
                    if(payload instanceof ArrayList){
                        ArrayList<String> activeUsers = (ArrayList<String>) payload;
                        
                        System.out.println("Received active users: " + activeUsers);
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        });
        System.out.println("Subscribed to /topic/users");

        session.send("/app/connect", username);
        session.send("/app/request-users", "");
    }

    @Override
    public void handleTransportError(StompSession session, Throwable exception) {
        exception.printStackTrace();
    }
}