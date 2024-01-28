package com.snuri.customshopplugin.shop;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.snuri.customshopplugin.CustomShopPlugin;
import com.snuri.customshopplugin.network.ServerPacketGuiOpen;
import com.snuri.customshopplugin.network.ServerPacketUpdate;
import com.snuri.customshopplugin.network.UpdateType;

import net.md_5.bungee.api.ChatColor;

public class ShopSell extends Shop {

	private List<Item> itemList;
	
	public ShopSell(String name) {
		super(name);
		itemList = new ArrayList<Item>();
	}
	
	@Override
	public List<Item> getItemList() {
		return itemList;
	}
	
	@Override
	public void open(Player player) {
		shopDataMap.put(player.getName(), new ShopData());
		
		ServerPacketGuiOpen packet = new ServerPacketGuiOpen("shop_sell", false);
		
		int size = itemList.size();
		int maxPage = size / 8;
		if(size == 0) {
			maxPage ++;
		} else if(size > (8 * maxPage)) {
			maxPage ++;
		}
		
		packet.append("t00", UpdateType.TEXT, "1 / " + maxPage);
		packet.append("t01", UpdateType.TEXT, String.format("%,d", CustomShopPlugin.getMoney(player.getName())));
		
		for(int i = 1; i <= 8; i ++) {
			int idx = i - 1;
			if(idx < size) {
				Item item = itemList.get(idx);
				packet.append("v0" + i, UpdateType.VISIBLE, true);
				packet.append("i0" + i, UpdateType.ITEM, item.getData());
				packet.append("ta" + i, UpdateType.TEXT, item.getName());
				packet.append("tb" + i, UpdateType.TEXT, String.format("%,d", item.getPrice()));
				packet.append("b0" + i, UpdateType.VISIBLE, true);
			} else {
				packet.append("v0" + i, UpdateType.VISIBLE, false);
				packet.append("i0" + i, UpdateType.ITEM, "NULL");
				packet.append("ta" + i, UpdateType.TEXT, "NULL");
				packet.append("tb" + i, UpdateType.TEXT, "NULL");
				packet.append("b0" + i, UpdateType.VISIBLE, false);
			}
		}
		
		packet.sendTo(player);
	}
	
	@Override
	public void close(Player player) {
		shopDataMap.remove(player.getName());
	}
	
	@Override
	public void update(Player player) {
		int size = itemList.size();
		int page = shopDataMap.get(player.getName()).getPage();
		int maxPage = size / 8;
		if(size == 0) {
			maxPage ++;
		} else if(size > (8 * maxPage)) {
			maxPage ++;
		}
		
		ServerPacketUpdate packet = new ServerPacketUpdate("t00", UpdateType.TEXT, page + " / " + maxPage);
		packet.append("t01", UpdateType.TEXT, String.format("%,d", CustomShopPlugin.getMoney(player.getName())));
		
		for(int i = 1; i <= 8; i ++) {
			int idx = (page - 1) * 8 + i - 1;
			if(idx < size) {
				Item item = itemList.get(idx);
				packet.append("v0" + i, UpdateType.VISIBLE, true);
				packet.append("i0" + i, UpdateType.ITEM, item.getData());
				packet.append("ta" + i, UpdateType.TEXT, item.getName());
				packet.append("tb" + i, UpdateType.TEXT, String.format("%,d", item.getPrice()));
				packet.append("b0" + i, UpdateType.VISIBLE, true);
			} else {
				packet.append("v0" + i, UpdateType.VISIBLE, false);
				packet.append("i0" + i, UpdateType.ITEM, "NULL");
				packet.append("ta" + i, UpdateType.TEXT, "NULL");
				packet.append("tb" + i, UpdateType.TEXT, "NULL");
				packet.append("b0" + i, UpdateType.VISIBLE, false);
			}
		}
		
		packet.sendTo(player);
	}
	
	@Override
	public void onMouseEvent(Player player, String id, int mouseButton, boolean isCtrlKeyDown, boolean isAltKeyDown, boolean isShiftKeyDown) {
		if(mouseButton != 0)
			return;
		
		if(id.equals("ba1")) { // PREV_PAGE
			ShopData data = shopDataMap.get(player.getName());
			int page = data.getPage();
			if(page > 1) {
				data.setPage(page - 1);
				update(player);
			}
			return;
		}
		
		if(id.equals("ba2")) { // NEXT_PAGE
			ShopData data = shopDataMap.get(player.getName());
			int page = data.getPage();
			int maxPage = itemList.size() / 8 + 1;
			if(page < maxPage) {
				data.setPage(page + 1);
				update(player);
			}
			return;
		}
		
		if(id.startsWith("b0")) { // SELL
			int i = Integer.parseInt(id.substring(2));
			
			ShopData data = shopDataMap.get(player.getName());
			int idx = (data.getPage() - 1) * 8 + i - 1;
			
			if(idx >= itemList.size())
				return;
			
			Item item = itemList.get(idx);
			
			int amount = isShiftKeyDown ? 64 : 1;
			int a = amount;
			
			for(int j = 0; j < 36; j ++) {
				ItemStack t = player.getInventory().getItem(j);
				if(t != null && t.isSimilar(item.getItemStack())) {
					if(t.getAmount() > a) {
						t.setAmount(t.getAmount() - a);
						a = 0;
			    		break;
			    	} else {
			    		a -= t.getAmount();
			    		player.getInventory().clear(j);
			    		if(a == 0) break;
			    	}
				}
			}
			
			int q = amount - a;
			if(q > 0) {
				CustomShopPlugin.addMoney(player.getName(), item.getPrice() * q);
				updateMoney(player);
			} else {
				player.sendMessage(ChatColor.GOLD + "판매할 물건이 없습니다.");
			}
		}
	}
	
	@Override
	public void onKeyEvent(Player player, int key) {
		
	}
	
	private void updateMoney(Player player) {
		ServerPacketUpdate packet = new ServerPacketUpdate("t01", UpdateType.TEXT, String.format("%,d", CustomShopPlugin.getMoney(player.getName())));
		packet.sendTo(player);
	}
}
