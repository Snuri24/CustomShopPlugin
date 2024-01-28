package com.snuri.customshopplugin.network;

public class ServerPacketGuiOpen extends Packet {

	public ServerPacketGuiOpen(String guiName, boolean drawBackground) {
		super(PacketType.SERVER_GUI_OPEN);
		builder.append(guiName).append(drawBackground ? "//1" : "//0");
	}
	
	public void append(String id, String updateType, String value) {
		builder.append("//").append(id).append(updateType).append(value);
	}
	
	public void append(String id, String updateType, int value) {
		builder.append("//").append(id).append(updateType).append(value);
	}
	
	public void append(String id, String updateType, boolean bool) {
		builder.append("//").append(id).append(updateType).append(bool ? "1" : "0");
	}
	
}
