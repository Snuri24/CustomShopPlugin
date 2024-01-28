package com.snuri.customshopplugin.network.customshophud;

public class ServerPacketUpdateHUD extends Packet {

	public ServerPacketUpdateHUD(int money) {
		super(PacketType.SERVER_UPDATE_HUD);
		builder.append(money);
	}
	
}
