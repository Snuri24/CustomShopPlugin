package com.snuri.customshopplugin.shop;

import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.server.v1_16_R3.MojangsonParser;
import net.minecraft.server.v1_16_R3.NBTTagCompound;

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
			this.itemStack = CraftItemStack.asBukkitCopy(net.minecraft.server.v1_16_R3.ItemStack.a(MojangsonParser.parse(data)));
		} catch(CommandSyntaxException e) {
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
