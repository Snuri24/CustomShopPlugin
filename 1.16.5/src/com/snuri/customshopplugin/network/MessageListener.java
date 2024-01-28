package com.snuri.customshopplugin.network;

import java.nio.charset.StandardCharsets;

import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.snuri.customshopplugin.CustomShopPlugin;

public class MessageListener implements PluginMessageListener {
	
	public MessageListener() {
		
	}
	
	@Override
	public void onPluginMessageReceived(String channel, Player player, byte[] message) {
		ByteArrayDataInput in = ByteStreams.newDataInput(message);
		byte type = in.readByte();
		int len = in.readInt();
		byte[] bytes = new byte[len];
		in.readFully(bytes);
		
		CustomShopPlugin.getInstance().handleMessage(player, type, new String(bytes, StandardCharsets.UTF_8));
	}
}
