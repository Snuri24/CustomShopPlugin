package com.snuri.customshopplugin.network.customshophud;

import java.nio.charset.StandardCharsets;

import org.bukkit.entity.Player;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.snuri.customshopplugin.CustomShopPlugin;

public class Packet {
	
	protected byte type;
	protected StringBuilder builder;
	
	public Packet(byte type) {
		this.type = type;
		builder = new StringBuilder();
	}
	
	public void sendTo(Player player) {
		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		byte[] bytes = builder.toString().getBytes(StandardCharsets.UTF_8);
		out.writeByte(type);
		out.writeInt(bytes.length);
		out.write(bytes);
		player.sendPluginMessage(CustomShopPlugin.getInstance(), "customshop:channel2", out.toByteArray());
	}
}
