package com.snuri.customshopplugin.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.snuri.customshopplugin.CustomShopPlugin;

public class CommandMoney implements CommandExecutor {
	
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    	if(args.length == 0)  {
			sender.sendMessage("/돈 확인 <플레이어>");
			sender.sendMessage("/돈 추가 <플레이어> <금액>");
			sender.sendMessage("/돈 제거 <플레이어> <금액>");
			sender.sendMessage("/돈 설정 <플레이어> <금액>");
			sender.sendMessage("/돈 보내기 <플레이어> <금액>");
			return true;
		}
    	
		if(args[0].equals("확인")) {
			if(!sender.isOp()) {
				sender.sendMessage("§c권한이 없습니다.");
				return true;
			}
			
			if(args.length == 1) {
				sender.sendMessage("/돈 확인 <플레이어>");
				return true;
			}
			
			sender.sendMessage(String.format("§6%s님의 돈: %,d원", args[1], CustomShopPlugin.getMoney(args[1])));
			return true;
		}
		
		if(args[0].equals("추가")) {
			if(!sender.isOp()) {
				sender.sendMessage("§c권한이 없습니다.");
				return true;
			}
			
			if(args.length < 3) {
				sender.sendMessage("/돈 추가 <플레이어> <금액>");
				return true;
			}
			
			if(!CustomShopPlugin.moneyMap.containsKey(args[1])) {
				sender.sendMessage("§c해당 플레이어를 찾을 수 없습니다.");
				return true;
			}
			
			int amount;
			try {
				amount = Integer.parseInt(args[2]);
			} catch(NumberFormatException e) {
				sender.sendMessage("/돈 추가 <플레이어> <금액>");
				return true;
			}
			
			CustomShopPlugin.addMoney(args[1], amount);
			sender.sendMessage(String.format("§6%s님의 돈이 %,d원 추가되었습니다.", args[1], amount));
			
			Player p = Bukkit.getPlayer(args[1]);
			if(p != null) {
				p.sendMessage(String.format("§e운영자에 의해 %s님의 돈이 %,d원 추가되었습니다.", args[1], amount));
			}
			return true;
		}
		
		if(args[0].equals("제거")) {
			if(!sender.isOp()) {
				sender.sendMessage("§c권한이 없습니다.");
				return true;
			}
			
			if(args.length < 3) {
				sender.sendMessage("/돈 제거 <플레이어> <금액>");
				return true;
			}
			
			if(!CustomShopPlugin.moneyMap.containsKey(args[1])) {
				sender.sendMessage("§c해당 플레이어를 찾을 수 없습니다.");
				return true;
			}
			
			int amount;
			try {
				amount = Integer.parseInt(args[2]);
			} catch(NumberFormatException e) {
				sender.sendMessage("/돈 제거 <플레이어> <금액>");
				return true;
			}
			
			CustomShopPlugin.addMoney(args[1], -amount);
			sender.sendMessage(String.format("§6%s님의 돈이 %,d원 제거되었습니다.", args[1], amount));
			
			Player p = Bukkit.getPlayer(args[1]);
			if(p != null) {
				p.sendMessage(String.format("§e운영자에 의해 %s님의 돈이 %,d원 제거되었습니다.", args[1], amount));
			}
			return true;
		}
		
		if(args[0].equals("설정")) {
			if(!sender.isOp()) {
				sender.sendMessage("§c권한이 없습니다.");
				return true;
			}
			
			if(args.length < 3) {
				sender.sendMessage("/돈 설정 <플레이어> <금액>");
				return true;
			}
			
			if(!CustomShopPlugin.moneyMap.containsKey(args[1])) {
				sender.sendMessage("§c해당 플레이어를 찾을 수 없습니다.");
				return true;
			}
			
			int money;
			try {
				money = Integer.parseInt(args[2]);
			} catch(NumberFormatException e) {
				sender.sendMessage("/돈 설정 <플레이어> <금액>");
				return true;
			}
			
			CustomShopPlugin.setMoney(args[1], money);
			sender.sendMessage(String.format("§6%s님의 돈이 %,d원으로 설정되었습니다.", args[1], money));
			
			Player p = Bukkit.getPlayer(args[1]);
			if(p != null) {
				p.sendMessage(String.format("§e운영자에 의해 %s님의 돈이 %,d원으로 설정되었습니다.", args[1], money));
			}
			return true;
		}
		
		if(args[0].equals("보내기")) {
			if(!(sender instanceof Player)) {
				sender.sendMessage("§c콘솔에서 입력할 수 없는 명령어입니다.");
				return true;
			}
			
			if(args.length < 3) {
				sender.sendMessage("/돈 보내기 <플레이어> <금액>");
				return true;
			}
			
			if(!CustomShopPlugin.moneyMap.containsKey(args[1])) {
				sender.sendMessage("§c해당 플레이어를 찾을 수 없습니다.");
				return true;
			}
			
			int amount;
			try {
				amount = Integer.parseInt(args[2]);
			} catch(NumberFormatException e) {
				sender.sendMessage("/돈 보내기 <플레이어> <금액>");
				return true;
			}
			
			Player player = (Player) sender;
			if(CustomShopPlugin.getMoney(player.getName()) < amount) {
				sender.sendMessage("§c돈이 부족합니다.");
				return true;
			}
			
			CustomShopPlugin.addMoney(player.getName(), -amount);
			CustomShopPlugin.addMoney(args[1], amount);
			sender.sendMessage(String.format("§6%s님에게 %,d원을 송금했습니다.", args[1], amount));
			
			Player p = Bukkit.getPlayer(args[1]);
			if(p != null) {
				p.sendMessage(String.format("§e%s님이 %,d원을 송금했습니다.", player.getName(), amount));
			}
			return true;
		}
		
		return true;
    }
}
