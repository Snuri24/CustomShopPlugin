package com.snuri.customshopplugin;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.snuri.customshopplugin.command.CommandCs;
import com.snuri.customshopplugin.command.CommandMoney;
import com.snuri.customshopplugin.network.MessageListener;
import com.snuri.customshopplugin.network.PacketType;
import com.snuri.customshopplugin.network.customshophud.ServerPacketUpdateHUD;
import com.snuri.customshopplugin.shop.Item;
import com.snuri.customshopplugin.shop.Shop;
import com.snuri.customshopplugin.shop.ShopBuy;
import com.snuri.customshopplugin.shop.ShopSell;

public class CustomShopPlugin extends JavaPlugin {
	
	private static CustomShopPlugin instance;
	public final static Logger log = Logger.getLogger("Minecraft");
	
	public static Map<String, Integer> moneyMap = new HashMap<String, Integer>();
	public static Map<String, Shop> shopMap = new HashMap<String, Shop>();
	public static Map<String, Shop> playerShopMap = new HashMap<String, Shop>();
	public static Map<String, String> npcShopMap = new HashMap<String, String>();
	
	public void onEnable() {
		instance = this;
		
		this.getServer().getPluginManager().registerEvents(new CustomShopListener(), this);
		
		this.getServer().getMessenger().registerOutgoingPluginChannel(this, "customshop:channel");
		this.getServer().getMessenger().registerIncomingPluginChannel(this, "customshop:channel", new MessageListener());
		this.getServer().getMessenger().registerOutgoingPluginChannel(this, "customshop:channel2");
		
		getCommand("cs").setExecutor(new CommandCs());
		getCommand("money").setExecutor(new CommandMoney());
		
		log("CustomShopPlugin has been enabled", Level.INFO);
		
		try {
			File pluginFolder = getDataFolder();
			if(!pluginFolder.exists()) {
				pluginFolder.mkdirs();
				
				try {
					String[] fileArr = { "shop_buy.yml", "shop_sell.yml" };
					for(String file : fileArr) {
						Files.copy(getClass().getResourceAsStream("/" + file), new File(pluginFolder, file).toPath());
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
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
						shopMap.put(shopName, shop);
					} else if(type.equals("sell")) {
						ShopSell shop = new ShopSell(shopName);
						Set<String> keySet = config.getKeys(false);
						keySet.remove("type");
						for(String key : keySet) {
							shop.getItemList().add(new Item(config.getString(key + ".item"), config.getString(key + ".name"), config.getInt(key + ".price")));
						}
						shopMap.put(shopName, shop);
					}
				}
			}
			
			File dataFile = new File(pluginFolder, "player.yml");
			if(!dataFile.exists())
				dataFile.createNewFile();
			
			YamlConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
			Set<String> playerSet = config.getKeys(false);
			for(String player : playerSet) {
				moneyMap.put(player, Integer.parseInt(config.getString(player)));
			}
			
			dataFile = new File(pluginFolder, "npc.yml");
			if(!dataFile.exists())
				dataFile.createNewFile();
			
			config = YamlConfiguration.loadConfiguration(dataFile);
			Set<String> npcSet = config.getKeys(false);
			for(String npc : npcSet) {
				npcShopMap.put(npc, config.getString(npc));
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void onDisable() {
		log("CustomShopPlugin has been disabled", Level.INFO);
		
		try {
			File dataFile = new File(getDataFolder(), "player.yml");
			if(!dataFile.exists())
				dataFile.createNewFile();
			
			YamlConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
			Set<String> playerSet = moneyMap.keySet();
			for(String player : playerSet) {
				config.set(player, moneyMap.get(player));
			}
			config.save(dataFile);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static CustomShopPlugin getInstance() {
		return instance;
	}
	
	public static void log(String string, Level level) {
		log.log(level, "[CustomShopPlugin] " + string);
	}
	
	public static int getMoney(String playerName) {
		return moneyMap.get(playerName);
	}
	
	public static void setMoney(String playerName, int money) {
		if(!moneyMap.containsKey(playerName)) 
			return;

		moneyMap.put(playerName, money);
		
		Player player = Bukkit.getPlayer(playerName);
		if(player != null) {
			ServerPacketUpdateHUD packet = new ServerPacketUpdateHUD(money);
			packet.sendTo(player);
		}
	}
	
	public static void addMoney(String playerName, int amount) {
		if(!moneyMap.containsKey(playerName)) 
			return;
		
		int money = moneyMap.get(playerName) + amount;
		moneyMap.put(playerName, money);
		
		Player player = Bukkit.getPlayer(playerName);
		if(player != null) {
			ServerPacketUpdateHUD packet = new ServerPacketUpdateHUD(money);
			packet.sendTo(player);
		}
	}
	
	// PacketHandle
	public void handleMessage(Player player, byte type, String data) {
		if(type == PacketType.CLIENT_GUI_CLOSE) {
			Shop shop = playerShopMap.get(player.getName());
			if(shop != null) {
				shop.close(player);
				playerShopMap.remove(player.getName());
			}
		} else if(type == PacketType.CLIENT_MOUSE_EVENT) {
			Shop shop = playerShopMap.get(player.getName());
			if(shop != null) {
				shop.onMouseEvent(player, data.substring(0, 3), Integer.parseInt(data.substring(3, 4)), data.substring(4, 5).equals("1"), data.substring(5, 6).equals("1"), data.substring(6, 7).equals("1"));
			}
		} else if(type == PacketType.CLIENT_KEY_EVENT) {
			Shop shop = playerShopMap.get(player.getName());
			if(shop != null) {
				shop.onKeyEvent(player, Integer.parseInt(data));
			}
		}
	}
}