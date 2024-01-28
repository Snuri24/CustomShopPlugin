package com.snuri.customshopplugin.network;

public class PacketType {
	
	public static final byte SERVER_GUI_OPEN         = 0x00;
	public static final byte SERVER_GUI_CLOSE        = 0x01;
	public static final byte SERVER_UPDATE           = 0x02;
	
	public static final byte CLIENT_GUI_CLOSE        = 0x03;
	public static final byte CLIENT_MOUSE_EVENT      = 0x04;
	public static final byte CLIENT_KEY_EVENT        = 0x05;
	
}
