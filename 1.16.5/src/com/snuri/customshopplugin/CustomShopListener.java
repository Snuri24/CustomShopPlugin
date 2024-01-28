package com.snuri.customshopplugin;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;

import com.snuri.customshopplugin.network.customshophud.ServerPacketUpdateHUD;
import com.snuri.customshopplugin.shop.Shop;

public class CustomShopListener implements Listener {

	public CustomShopListener() {
		
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {		
		Player player = event.getPlayer();
		if(!CustomShopPlugin.moneyMap.containsKey(player.getName())) {
			CustomShopPlugin.moneyMap.put(player.getName(), 50000);
		}
		
		Bukkit.getScheduler().scheduleSyncDelayedTask(CustomShopPlugin.getInstance(), () -> {
			ServerPacketUpdateHUD packet = new ServerPacketUpdateHUD(CustomShopPlugin.getMoney(player.getName()));
			packet.sendTo(event.getPlayer());
		}, 20L);
		
		Bukkit.getScheduler().scheduleSyncDelayedTask(CustomShopPlugin.getInstance(), () -> {
			ServerPacketUpdateHUD packet = new ServerPacketUpdateHUD(CustomShopPlugin.getMoney(player.getName()));
			packet.sendTo(event.getPlayer());
		}, 100L);
	}
	
	@EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
		if(event.getHand() != EquipmentSlot.HAND)
    		return;
		if(event.getPlayer().isSneaking())
    		return;
        
    	String npcShop = null;
    	Entity entity = event.getRightClicked();
    	if(entity.getCustomName() != null)
    		npcShop = CustomShopPlugin.npcShopMap.get(entity.getCustomName());
    	if(npcShop == null)
    		npcShop = CustomShopPlugin.npcShopMap.get(entity.getName());
    	if(npcShop == null)
    		return;
    	
    	event.setCancelled(true);
    	
    	Player player = event.getPlayer();
    	Shop shop = CustomShopPlugin.shopMap.get(npcShop);
    	if(shop != null) {
    		Bukkit.getScheduler().scheduleSyncDelayedTask(CustomShopPlugin.getInstance(), () -> {
    			shop.open(player);
            	CustomShopPlugin.playerShopMap.put(player.getName(), shop);
			}, 5L);
    	}
    }
}