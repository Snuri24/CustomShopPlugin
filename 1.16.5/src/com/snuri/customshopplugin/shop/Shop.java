package com.snuri.customshopplugin.shop;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;

public abstract class Shop {
	
	protected String name;
	protected Map<String, ShopData> shopDataMap;
	
	public Shop(String name) {
		this.name = name;
		this.shopDataMap = new HashMap<String, ShopData>();
	}
	
	public String getName() {
		return name;
	}
	
	public Map<String, ShopData> getShopDataMap() {
		return shopDataMap;
	}
	
	public abstract List<Item> getItemList();
	
	public abstract void open(Player player);
	
	public abstract void close(Player player);
	
	public abstract void update(Player player);
	
	public abstract void onMouseEvent(Player player, String id, int mouseButton, boolean isCtrlKeyDown, boolean isAltKeyDown, boolean isShiftKeyDown);
	
	public abstract void onKeyEvent(Player player, int key);
	
}