package com.example.chat_buddy.client;

import com.example.chat_buddy.Message;
import java.util.concurrent.ExecutionException;
import jdk.jshell.execution.Util;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class ClientGUI {
	public static void main(String args[])throws ExecutionException, InterruptedException   {
		MyStompClient myStompClient=new MyStompClient("Rida");
		MyStompClient.sendMessage(new Message("Tap Tap","Hello"))
	}
}

