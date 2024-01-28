package com.snuri.customshopplugin.command;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.snuri.customshopplugin.CustomShopPlugin;
import com.snuri.customshopplugin.network.ServerPacketGuiClose;
import com.snuri.customshopplugin.shop.Item;
import com.snuri.customshopplugin.shop.Shop;
import com.snuri.customshopplugin.shop.ShopBuy;
import com.snuri.customshopplugin.shop.ShopSell;

public class CommandCs implements CommandExecutor {
	
	@Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    	if(!(sender instanceof Player)) {
			sender.sendMessage("§c콘솔에서 입력할 수 없는 명령어입니다.");
			return true;
		}
    	
		if(args.length == 0)  {
			sender.sendMessage("/cs open <shop name>");
			sender.sendMessage("/cs shop add <shop name> <type>");
			sender.sendMessage("/cs shop remove <shop name>");
			sender.sendMessage("/cs shop list (page)");
			sender.sendMessage("/cs shop reload");
			sender.sendMessage("/cs item add <shop name> <name> <price> (index)");
			sender.sendMessage("/cs item remove <shop name> <index>");
			sender.sendMessage("/cs item name <shop name> <index> <name...>");
			sender.sendMessage("/cs item price <shop name> <index> <price>");
			sender.sendMessage("/cs npc add <npc name> <shop name>");
			sender.sendMessage("/cs npc remove <npc name>");
			sender.sendMessage("/cs npc list (page)");
			sender.sendMessage("/cs npc reload");
			sender.sendMessage("/cs customitem name <display name...>");
			sender.sendMessage("/cs customitem lore <lore...>");
			return true;
		}
		
		if(args[0].equals("open")) {
			if(args.length == 1) {
				sender.sendMessage("/cs open <shop name>");
				return true;
			}
			
			Player player = (Player) sender;
			Shop shop = CustomShopPlugin.shopMap.get(args[1]);
			if(shop == null) {
				player.sendMessage("§c해당 상점을 찾을 수 없습니다.");
				return true;
			}
			
			shop.open(player);
			CustomShopPlugin.playerShopMap.put(player.getName(), shop);
			return true;
		}
		
		if(args[0].equals("shop")) {
			if(!sender.isOp()) {
				sender.sendMessage("§c권한이 없습니다.");
				return true;
			}
			
			if(args.length == 1) {
				sender.sendMessage("/cs shop add <shop name> <type>");
				sender.sendMessage("/cs shop remove <shop name>");
				sender.sendMessage("/cs shop list (page)");
				sender.sendMessage("/cs shop reload");
				return true;
			}
			
			if(args[1].equals("add")) {
				if(args.length < 4) {
					sender.sendMessage("/cs shop add <shop name> <type>");
					return true;
				}
				
				Player player = (Player) sender;
				
				if(CustomShopPlugin.shopMap.containsKey(args[2])) {
					player.sendMessage("§c해당 이름의 상점이 이미 존재합니다.");
					return true;
				}
				
				if(!args[3].equals("buy") && !args[3].equals("sell")) {
					player.sendMessage("§ctype은 buy 또는 sell 이어야 합니다.");
					return true;
				}
				
				if(args[3].equals("buy")) {
					try {
						File dataFile = new File(CustomShopPlugin.getInstance().getDataFolder(), args[2] + ".yml");
						if(!dataFile.exists())
							dataFile.createNewFile();
						
						YamlConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
						config.set("type", "buy");
						config.save(dataFile);
					} catch(Exception e) {
						e.printStackTrace();
					}
					
					ShopBuy shop = new ShopBuy(args[2]);
					CustomShopPlugin.shopMap.put(args[2], shop);
					player.sendMessage(String.format("§6%s 상점이 추가되었습니다.", args[2]));
					return true;
				}
				
				if(args[3].equals("sell")) {
					try {
						File dataFile = new File(CustomShopPlugin.getInstance().getDataFolder(), args[2] + ".yml");
						if(!dataFile.exists())
							dataFile.createNewFile();
						
						YamlConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
						config.set("type", "sell");
						config.save(dataFile);
					} catch(Exception e) {
						e.printStackTrace();
					}
					
					ShopSell shop = new ShopSell(args[2]);
					CustomShopPlugin.shopMap.put(args[2], shop);
					player.sendMessage(String.format("§6%s 상점이 추가되었습니다.", args[2]));
					return true;
				}
				
				player.sendMessage("§ctype은 buy 또는 sell 이어야 합니다.");
				return true;
			}
			
			if(args[1].equals("remove")) {
				if(args.length < 3) {
					sender.sendMessage("/cs shop remove <shop name>");
					return true;
				}
				
				Player player = (Player) sender;
				
				Shop shop = CustomShopPlugin.shopMap.get(args[2]);
				if(shop == null) {
					player.sendMessage("§c해당 상점을 찾을 수 없습니다.");
					return true;
				}
				
				ServerPacketGuiClose packet = new ServerPacketGuiClose();
				for(String n : shop.getShopDataMap().keySet()) {
					CustomShopPlugin.playerShopMap.remove(n);
					Player p = Bukkit.getPlayer(n);
					if(p != null) {
						packet.sendTo(p);
						p.sendMessage("§c운영자에 의해 상점이 종료되었습니다.");
					}
				}
				shop.getShopDataMap().clear();
				
				try {
					File dataFile = new File(CustomShopPlugin.getInstance().getDataFolder(), args[2] + ".yml");
					if(dataFile.exists()) {
						dataFile.delete();
					}
				} catch(Exception e) {
					e.printStackTrace();
				}
				
				CustomShopPlugin.shopMap.remove(args[2]);
				player.sendMessage(String.format("§6%s 상점이 제거되었습니다.", args[2]));
				return true;
			}
			
			if(args[1].equals("list")) {
				int size = CustomShopPlugin.shopMap.size();
				if(size == 0) {
					sender.sendMessage("§c불러올 상점이 없습니다.");
					return true;
				}
				
				if(args.length == 2) {
					int page = 1;
					int maxPage = size / 8;
					if(size > (8 * maxPage)) {
						maxPage ++;
					}
					
					String[] shopArr = CustomShopPlugin.shopMap.keySet().toArray(new String[0]);
					int last = size > (page * 8) ? (page * 8) : size;
					
					sender.sendMessage(String.format("§e---------- §fShop List (%d/%d) §e----------", page, maxPage));
					sender.sendMessage("§7/cs shop list <page> 명령어를 입력하여 목록의 다른 쪽을 불러올 수 있습니다.");
					for(int i = ((page - 1) * 8); i < last; i ++) {
						sender.sendMessage("§6- " + shopArr[i]);
					}
					return true;
				}
				
				int page;
				try {
					page = Integer.parseInt(args[2]);
				} catch(NumberFormatException e) {
					sender.sendMessage("/cs shop list <page>");
					return true;
				}
				
				int maxPage = size / 8;
				if(size > (8 * maxPage)) {
					maxPage ++;
				}
				
				if(page < 1) {
					page = 1;
				}
				if(page > maxPage) {
					page = maxPage;
				}
				
				String[] shopArr = CustomShopPlugin.shopMap.keySet().toArray(new String[0]);
				int last = size > (page * 8) ? (page * 8) : size;
				
				sender.sendMessage(String.format("§e---------- §fShop List (%d/%d) §e----------", page, maxPage));
				for(int i = ((page - 1) * 8); i < last; i ++) {
					sender.sendMessage("§6- " + shopArr[i]);
				}
				
				return true;
			}
			
			if(args[1].equals("reload")) {
				ServerPacketGuiClose packet = new ServerPacketGuiClose();
				for(String s : CustomShopPlugin.playerShopMap.keySet()) {
					Player p = Bukkit.getPlayer(s);
					if(p != null) {
						packet.sendTo(p);
						p.sendMessage("§c운영자에 의해 상점이 종료되었습니다.");
					}
				}
				CustomShopPlugin.playerShopMap.clear();
				CustomShopPlugin.shopMap.clear();
				
				File pluginFolder = CustomShopPlugin.getInstance().getDataFolder();
				File[] fileArr = pluginFolder.listFiles(new FilenameFilter() {
					@Override
					public boolean accept(File dir, String n) {
						return n.endsWith(".yml") && !n.equals("player.yml") && !n.equals("npc.yml");
					}
				});
				if(fileArr != null) {
					for(File file : fileArr) {
						String shopName = file.getName().substring(0, file.getName().length() - 4);
						YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
						String type = config.getString("type");

						if(type.equals("buy")) {
							ShopBuy shop = new ShopBuy(shopName);
							Set<String> keySet = config.getKeys(false);
							keySet.remove("type");
							for(String key : keySet) {
								shop.getItemList().add(new Item(config.getString(key + ".item"), config.getString(key + ".name"), config.getInt(key + ".price")));
							}
							CustomShopPlugin.shopMap.put(shopName, shop);
						} else if(type.equals("sell")) {
							ShopSell shop = new ShopSell(shopName);
							Set<String> keySet = config.getKeys(false);
							keySet.remove("type");
							for(String key : keySet) {
								shop.getItemList().add(new Item(config.getString(key + ".item"), config.getString(key + ".name"), config.getInt(key + ".price")));
							}
							CustomShopPlugin.shopMap.put(shopName, shop);
						}
					}
				}
				
				sender.sendMessage("§a상점 데이터를 다시 불러왔습니다.");
				return true;
			}
			
			sender.sendMessage("/cs shop add <shop name> <type>");
			sender.sendMessage("/cs shop remove <shop name>");
			sender.sendMessage("/cs shop list (page)");
			sender.sendMessage("/cs shop reload");
			return true;
		}
		
		if(args[0].equals("item")) {
			if(!sender.isOp()) {
				sender.sendMessage("§c권한이 없습니다.");
				return true;
			}
			
			if(args.length == 1) {
				sender.sendMessage("/cs item add <shop name> <name> <price> (index)");
				sender.sendMessage("/cs item remove <shop name> <index>");
				sender.sendMessage("/cs item name <shop name> <index> <name...>");
				sender.sendMessage("/cs item price <shop name> <index> <price>");
				return true;
			}
			
			if(args[1].equals("add")) {
				if(args.length < 5) {
					sender.sendMessage("/cs item add <shop name> <name> <price> (index)");
					return true;
				}
				
				Player player = (Player) sender;
				
				Shop shop = CustomShopPlugin.shopMap.get(args[2]);
				if(shop == null) {
					player.sendMessage("§c해당 상점을 찾을 수 없습니다.");
					return true;
				}
				
				int price;
				try {
					price = Integer.parseInt(args[4]);
				} catch(NumberFormatException e) {
					player.sendMessage("/cs item add <shop name> <name> <price> (index)");
					return true;
				}
				
				if(price < 0) {
					player.sendMessage("§c가격은 음수일 수 없습니다.");
					return true;
				}
				
				ItemStack itemStack = player.getInventory().getItemInMainHand();
				if(itemStack.getType() == Material.AIR) {
					player.sendMessage("§c아이템을 들고있지 않습니다.");
					return true;
				}
				
				itemStack = itemStack.clone();
				itemStack.setAmount(1);
				
				if(args.length == 5) {
					ServerPacketGuiClose packet = new ServerPacketGuiClose();
					for(String n : shop.getShopDataMap().keySet()) {
						CustomShopPlugin.playerShopMap.remove(n);
						Player p = Bukkit.getPlayer(n);
						if(p != null) {
							packet.sendTo(p);
							p.sendMessage("§c운영자에 의해 상점이 종료되었습니다.");
						}
					}
					shop.getShopDataMap().clear();
					
					shop.getItemList().add(new Item(itemStack, args[3], price));
					
					try {
						File dataFile = new File(CustomShopPlugin.getInstance().getDataFolder(), args[2] + ".yml");
						if(dataFile.exists()) {
							dataFile.delete();
						}
						dataFile.createNewFile();
						YamlConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
						
						if(shop instanceof ShopBuy) {
							config.set("type", "buy");
						} else if(shop instanceof ShopSell) {
							config.set("type", "sell");
						}
						
						List<Item> itemList = shop.getItemList();
						int size = itemList.size();
						for(int i = 1; i <= size; i ++) {
							Item item = itemList.get(i - 1);
							
							config.set(i + ".item", item.getData());
							config.set(i + ".name", item.getName());
							config.set(i + ".price", item.getPrice());
						}
							
						config.save(dataFile);
					} catch(Exception e) {
						e.printStackTrace();
					}
					
					player.sendMessage(String.format("§6%s 상점에 아이템이 추가되었습니다.", args[2]));
					return true;
				}
				
				int index;
				try {
					index = Integer.parseInt(args[5]);
				} catch(NumberFormatException e) {
					player.sendMessage("/cs item add <shop name> <name> <price> (index)");
					return true;
				}
				
				if(index < 0 || index > shop.getItemList().size()) {
					player.sendMessage(String.format("§cindex는 0 이상 %d 이하여야 합니다.", shop.getItemList().size()));
					return true;
				}
				
				ServerPacketGuiClose packet = new ServerPacketGuiClose();
				for(String n : shop.getShopDataMap().keySet()) {
					CustomShopPlugin.playerShopMap.remove(n);
					Player p = Bukkit.getPlayer(n);
					if(p != null) {
						packet.sendTo(p);
						p.sendMessage("§c운영자에 의해 상점이 종료되었습니다.");
					}
				}
				shop.getShopDataMap().clear();
				
				shop.getItemList().add(index, new Item(itemStack, args[3], price));
				
				try {
					File dataFile = new File(CustomShopPlugin.getInstance().getDataFolder(), args[2] + ".yml");
					if(dataFile.exists()) {
						dataFile.delete();
					}
					dataFile.createNewFile();
					YamlConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
					
					if(shop instanceof ShopBuy) {
						config.set("type", "buy");
					} else if(shop instanceof ShopSell) {
						config.set("type", "sell");
					}
					
					List<Item> itemList = shop.getItemList();
					int size = itemList.size();
					for(int i = 1; i <= size; i ++) {
						Item item = itemList.get(i - 1);
						
						config.set(i + ".item", item.getData());
						config.set(i + ".name", item.getName());
						config.set(i + ".price", item.getPrice());
					}
						
					config.save(dataFile);
				} catch(Exception e) {
					e.printStackTrace();
				}
				
				player.sendMessage(String.format("§6%s 상점에 아이템이 추가되었습니다.", args[2]));
				return true;
			}
			
			if(args[1].equals("remove")) {
				if(args.length < 4) {
					sender.sendMessage("/cs item remove <shop name> <index>");
					return true;
				}
				
				Player player = (Player) sender;
				
				Shop shop = CustomShopPlugin.shopMap.get(args[2]);
				if(shop == null) {
					player.sendMessage("§c해당 상점을 찾을 수 없습니다.");
					return true;
				}
				
				int index;
				try {
					index = Integer.parseInt(args[3]);
				} catch(NumberFormatException e) {
					player.sendMessage("/cs item remove <shop name> <index>");
					return true;
				}
				
				if(index < 0 || index >= shop.getItemList().size()) {
					player.sendMessage(String.format("§cindex는 0 이상 %d 이하여야 합니다.", shop.getItemList().size() - 1));
					return true;
				}
				
				ServerPacketGuiClose packet = new ServerPacketGuiClose();
				for(String n : shop.getShopDataMap().keySet()) {
					CustomShopPlugin.playerShopMap.remove(n);
					Player p = Bukkit.getPlayer(n);
					if(p != null) {
						packet.sendTo(p);
						p.sendMessage("§c운영자에 의해 상점이 종료되었습니다.");
					}
				}
				shop.getShopDataMap().clear();
				
				shop.getItemList().remove(index);
				
				try {
					File dataFile = new File(CustomShopPlugin.getInstance().getDataFolder(), args[2] + ".yml");
					if(dataFile.exists()) {
						dataFile.delete();
					}
					dataFile.createNewFile();
					YamlConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
					
					if(shop instanceof ShopBuy) {
						config.set("type", "buy");
					} else if(shop instanceof ShopSell) {
						config.set("type", "sell");
					}
					
					List<Item> itemList = shop.getItemList();
					int size = itemList.size();
					for(int i = 1; i <= size; i ++) {
						Item item = itemList.get(i - 1);
						
						config.set(i + ".item", item.getData());
						config.set(i + ".name", item.getName());
						config.set(i + ".price", item.getPrice());
					}
						
					config.save(dataFile);
				} catch(Exception e) {
					e.printStackTrace();
				}
				
				player.sendMessage(String.format("§6%s 상점의 아이템이 제거되었습니다.", args[2]));
				return true;
			}
			
			if(args[1].equals("name")) {
				if(args.length < 5) {
					sender.sendMessage("/cs item name <shop name> <index> <name...>");
					return true;
				}
				
				Player player = (Player) sender;
				
				Shop shop = CustomShopPlugin.shopMap.get(args[2]);
				if(shop == null) {
					player.sendMessage("§c해당 상점을 찾을 수 없습니다.");
					return true;
				}
				
				int index;
				try {
					index = Integer.parseInt(args[3]);
				} catch(NumberFormatException e) {
					player.sendMessage("/cs item name <shop name> <index> <name...>");
					return true;
				}
				
				if(index < 0 || index >= shop.getItemList().size()) {
					player.sendMessage(String.format("§cindex는 0 이상 %d 이하여야 합니다.", shop.getItemList().size() - 1));
					return true;
				}
				
				ServerPacketGuiClose packet = new ServerPacketGuiClose();
				for(String n : shop.getShopDataMap().keySet()) {
					CustomShopPlugin.playerShopMap.remove(n);
					Player p = Bukkit.getPlayer(n);
					if(p != null) {
						packet.sendTo(p);
						p.sendMessage("§c운영자에 의해 상점이 종료되었습니다.");
					}
				}
				shop.getShopDataMap().clear();
				
				String name = args[4];
				int length = args.length;
				for(int i = 5; i < length; i ++) {
					name += (" " + args[i]);
				}
				
				shop.getItemList().get(index).setName(name);
				
				try {
					File dataFile = new File(CustomShopPlugin.getInstance().getDataFolder(), args[2] + ".yml");
					if(dataFile.exists()) {
						dataFile.delete();
					}
					dataFile.createNewFile();
					YamlConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
					
					if(shop instanceof ShopBuy) {
						config.set("type", "buy");
					} else if(shop instanceof ShopSell) {
						config.set("type", "sell");
					}
					
					List<Item> itemList = shop.getItemList();
					int size = itemList.size();
					for(int i = 1; i <= size; i ++) {
						Item item = itemList.get(i - 1);
						
						config.set(i + ".item", item.getData());
						config.set(i + ".name", item.getName());
						config.set(i + ".price", item.getPrice());
					}
						
					config.save(dataFile);
				} catch(Exception e) {
					e.printStackTrace();
				}
				
				player.sendMessage(String.format("§6%s 상점의 아이템 이름이 수정되었습니다.", args[2]));
				return true;
			}
			
			if(args[1].equals("price")) {
				if(args.length < 5) {
					sender.sendMessage("/cs item price <shop name> <index> <price>");
					return true;
				}
				
				Player player = (Player) sender;
				
				Shop shop = CustomShopPlugin.shopMap.get(args[2]);
				if(shop == null) {
					player.sendMessage("§c해당 상점을 찾을 수 없습니다.");
					return true;
				}
				
				int index;
				try {
					index = Integer.parseInt(args[3]);
				} catch(NumberFormatException e) {
					player.sendMessage("/cs item price <shop name> <index> <price>");
					return true;
				}
				
				if(index < 0 || index >= shop.getItemList().size()) {
					player.sendMessage(String.format("§cindex는 0 이상 %d 이하여야 합니다.", shop.getItemList().size() - 1));
					return true;
				}
				
				int price;
				try {
					price = Integer.parseInt(args[4]);
				} catch(NumberFormatException e) {
					player.sendMessage("/cs item price <shop name> <index> <price>");
					return true;
				}
				
				if(price < 0) {
					player.sendMessage("§c가격은 음수일 수 없습니다.");
					return true;
				}
				
				ServerPacketGuiClose packet = new ServerPacketGuiClose();
				for(String n : shop.getShopDataMap().keySet()) {
					CustomShopPlugin.playerShopMap.remove(n);
					Player p = Bukkit.getPlayer(n);
					if(p != null) {
						packet.sendTo(p);
						p.sendMessage("§c운영자에 의해 상점이 종료되었습니다.");
					}
				}
				shop.getShopDataMap().clear();
				
				shop.getItemList().get(index).setPrice(price);
				
				try {
					File dataFile = new File(CustomShopPlugin.getInstance().getDataFolder(), args[2] + ".yml");
					if(dataFile.exists()) {
						dataFile.delete();
					}
					dataFile.createNewFile();
					YamlConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
					
					if(shop instanceof ShopBuy) {
						config.set("type", "buy");
					} else if(shop instanceof ShopSell) {
						config.set("type", "sell");
					}
					
					List<Item> itemList = shop.getItemList();
					int size = itemList.size();
					for(int i = 1; i <= size; i ++) {
						Item item = itemList.get(i - 1);
						
						config.set(i + ".item", item.getData());
						config.set(i + ".name", item.getName());
						config.set(i + ".price", item.getPrice());
					}
						
					config.save(dataFile);
				} catch(Exception e) {
					e.printStackTrace();
				}
				
				player.sendMessage(String.format("§6%s 상점의 아이템 가격이 수정되었습니다.", args[2]));
				return true;
			}
			
			sender.sendMessage("/cs item add <shop name> <name> <price> (index)");
			sender.sendMessage("/cs item remove <shop name> <index>");
			sender.sendMessage("/cs item name <shop name> <index> <name...>");
			sender.sendMessage("/cs item price <shop name> <index> <price>");
			return true;
		}
		
		if(args[0].equals("npc")) {
			if(!sender.isOp()) {
				sender.sendMessage("§c권한이 없습니다.");
				return true;
			}
			
			if(args.length == 1) {
				sender.sendMessage("/cs npc add <npc name> <shop name>");
				sender.sendMessage("/cs npc remove <npc name>");
				sender.sendMessage("/cs npc list (page)");
				sender.sendMessage("/cs npc reload");
				return true;
			}
			
			if(args[1].equals("add")) {
				if(args.length < 4) {
					sender.sendMessage("/cs npc add <npc name> <shop name>");
					return true;
				}
				
				if(CustomShopPlugin.npcShopMap.containsKey(args[2])) {
					sender.sendMessage("§c해당 이름의 엔피시가 이미 존재합니다.");
					return true;
				}
				
				if(!CustomShopPlugin.shopMap.containsKey(args[3])) {
					sender.sendMessage("§c해당 상점을 찾을 수 없습니다.");
					return true;
				}
				
				try {
					File dataFile = new File(CustomShopPlugin.getInstance().getDataFolder(), "npc.yml");
					if(!dataFile.exists())
						dataFile.createNewFile();
					
					YamlConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
					config.set(args[2], args[3]);
					config.save(dataFile);
				} catch(Exception e) {
					e.printStackTrace();
				}
				
				CustomShopPlugin.npcShopMap.put(args[2], args[3]);
				sender.sendMessage(String.format("§6%s 엔피시가 추가되었습니다", args[2]));
				return true;
			}
			
			if(args[1].equals("remove")) {
				if(args.length < 3) {
					sender.sendMessage("/cs npc remove <npc name>");
					return true;
				}
				
				if(!CustomShopPlugin.npcShopMap.containsKey(args[2])) {
					sender.sendMessage("§c해당 엔피시를 찾을 수 없습니다.");
					return true;
				}
				
				try {
					File dataFile = new File(CustomShopPlugin.getInstance().getDataFolder(), "npc.yml");
					if(!dataFile.exists())
						dataFile.createNewFile();
					
					YamlConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
					config.set(args[2], null);
					config.save(dataFile);
				} catch(Exception e) {
					e.printStackTrace();
				}
				
				CustomShopPlugin.npcShopMap.remove(args[2]);
				sender.sendMessage(String.format("§6%s 엔피시가 제거되었습니다", args[2]));
				return true;
			}
			
			if(args[1].equals("list")) {
				int size = CustomShopPlugin.npcShopMap.size();
				if(size == 0) {
					sender.sendMessage("§c불러올 엔피시가 없습니다.");
					return true;
				}
				
				if(args.length == 2) {
					int page = 1;
					int maxPage = size / 8;
					if(size > (8 * maxPage)) {
						maxPage ++;
					}
					
					String[] npcArr = CustomShopPlugin.npcShopMap.keySet().toArray(new String[0]);
					int last = size > (page * 8) ? (page * 8) : size;
					
					sender.sendMessage(String.format("§e---------- §fNpc List (%d/%d) §e----------", page, maxPage));
					sender.sendMessage("§7/cs npc list <page> 명령어를 입력하여 목록의 다른 쪽을 불러올 수 있습니다.");
					for(int i = ((page - 1) * 8); i < last; i ++) {
						sender.sendMessage("§6" + npcArr[i] + " - " + CustomShopPlugin.npcShopMap.get(npcArr[i]));
					}
					return true;
				}
				
				int page;
				try {
					page = Integer.parseInt(args[2]);
				} catch(NumberFormatException e) {
					sender.sendMessage("/cs npc list <page>");
					return true;
				}
				
				int maxPage = size / 8;
				if(size > (8 * maxPage)) {
					maxPage ++;
				}
				
				if(page < 1) {
					page = 1;
				}
				if(page > maxPage) {
					page = maxPage;
				}
				
				String[] npcArr = CustomShopPlugin.npcShopMap.keySet().toArray(new String[0]);
				int last = size > (page * 8) ? (page * 8) : size;
				
				sender.sendMessage(String.format("§e---------- §fNpc List (%d/%d) §e----------", page, maxPage));
				for(int i = ((page - 1) * 8); i < last; i ++) {
					sender.sendMessage("§6" + npcArr[i] + " - " + CustomShopPlugin.npcShopMap.get(npcArr[i]));
				}
				
				return true;
			}
			
			if(args[1].equals("reload")) {
				CustomShopPlugin.npcShopMap.clear();
				
				try {
					File dataFile = new File(CustomShopPlugin.getInstance().getDataFolder(), "npc.yml");
					if(!dataFile.exists())
						dataFile.createNewFile();
					
					YamlConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
					Set<String> npcSet = config.getKeys(false);
					for(String npc : npcSet) {
						CustomShopPlugin.npcShopMap.put(npc, config.getString(npc));
					}
				} catch(Exception e) {
					e.printStackTrace();
				}
				
				sender.sendMessage("§a엔피시 데이터를 다시 불러왔습니다.");
				return true;
			}
			
			sender.sendMessage("/cs npc add <npc name> <shop name>");
			sender.sendMessage("/cs npc remove <npc name>");
			sender.sendMessage("/cs npc list (page)");
			sender.sendMessage("/cs npc reload");
			return true;
		}
		
		if(args[0].equals("customitem")) {
			if(!sender.isOp()) {
				sender.sendMessage("§c권한이 없습니다.");
				return true;
			}
			
			if(args.length == 1) {
				sender.sendMessage("/cs customitem name <display name...>");
				sender.sendMessage("/cs customitem lore <lore...>");
				return true;
			}
			
			Player player = (Player) sender;
			
			if(args[1].equals("name")) {
				if(args.length < 3) {
					player.sendMessage("/cs customitem name <display name...>");
					return true;
				}
				
				ItemStack itemStack = player.getInventory().getItemInMainHand();
				if(itemStack.getType() == Material.AIR) {
					player.sendMessage("§c아이템을 들고있지 않습니다.");
					return true;
				}
				
				String name = args[2];
				int length = args.length;
				for(int i = 3; i < length; i ++) {
					name += (" " + args[i]);
				}
				name = name.replaceAll("\\$", "§");
				
				ItemMeta itemMeta = itemStack.getItemMeta();
				itemMeta.setDisplayName(name);
				itemStack.setItemMeta(itemMeta);
				
				player.sendMessage("§6아이템의 이름이 수정되었습니다.");
				return true;
			}
			
			if(args[1].equals("lore")) {
				if(args.length < 3) {
					player.sendMessage("/cs customitem lore <lore...>");
					return true;
				}
				
				ItemStack itemStack = player.getInventory().getItemInMainHand();
				if(itemStack.getType() == Material.AIR) {
					player.sendMessage("§c아이템을 들고있지 않습니다.");
					return true;
				}
				
				String lore = args[2];
				int length = args.length;
				for(int i = 3; i < length; i ++) {
					lore += (" " + args[i]);
				}
				lore = lore.replaceAll("\\$", "§");
				
				ItemMeta itemMeta = itemStack.getItemMeta();
				itemMeta.setLore(Arrays.asList(lore.split("\\\\n")));
				itemStack.setItemMeta(itemMeta);
				
				player.sendMessage("§6아이템의 로어가 수정되었습니다.");
				return true;
			}
			
			sender.sendMessage("/cs customitem name <display name...>");
			sender.sendMessage("/cs customitem lore <lore...>");
			return true;
		}
		
		return true;
    }
}
