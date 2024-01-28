package com.snuri.customshopplugin.network;

public class ServerPacketUpdate extends Packet {

	public ServerPacketUpdate(String id, String updateType, String value) {
		super(PacketType.SERVER_UPDATE);
		builder.append(id).append(updateType).append(value);
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
