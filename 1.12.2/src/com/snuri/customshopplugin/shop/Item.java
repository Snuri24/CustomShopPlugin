package com.snuri.customshopplugin.shop;

import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import net.minecraft.server.v1_12_R1.MojangsonParseException;
import net.minecraft.server.v1_12_R1.MojangsonParser;
import net.minecraft.server.v1_12_R1.NBTTagCompound;

public class Item {
	
	private String data;
	private String name;
	private int price;
	private ItemStack itemStack;
	
	public Item(String data, String name, int price) {
		this.data = data;
		this.name = name;
		this.price = price;
		try {
			this.itemStack = CraftItemStack.asBukkitCopy(new net.minecraft.server.v1_12_R1.ItemStack(MojangsonParser.parse(data)));
		} catch (MojangsonParseException e) {
			e.printStackTrace();
		}
	}
	
	public Item(ItemStack itemStack, String name, int price) {
		this.data = CraftItemStack.asNMSCopy(itemStack).save(new NBTTagCompound()).toString();
		this.name = name;
		this.price = price;
		this.itemStack = itemStack;
	}
	
	public String getData() {
		return data;
	}
	
	public String getName() {
		return name;
	}
	
	public int getPrice() {
		return price;
	}
	
	public ItemStack getItemStack() {
		return itemStack;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setPrice(int price) {
		this.price = price;
	}
}
