package com.nekoessentialsx.commands;

import com.nekoessentialsx.NekoEssentialX;
import com.nekoessentialsx.catstyle.CatChatProcessor;
import com.nekoessentialsx.warp.WarpManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import java.util.List;

public class Commandsetwarp implements CommandExecutor, TabCompleter {
    private final NekoEssentialX plugin;
    private final WarpManager warpManager;

    public Commandsetwarp(NekoEssentialX plugin) {
        this.plugin = plugin;
        this.warpManager = plugin.getWarpManager();
        // 注册Tab补全器到setwarp命令
        plugin.getCommand("setwarp").setTabCompleter(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§c只有玩家可以使用这个命令！喵~");
                return true;
            }

            Player player = (Player) sender;
            CatChatProcessor processor = CatChatProcessor.getInstance();
            
            // 检查权限
            if (!player.hasPermission("nekoessentialsx.warp.admin")) {
                String message = "§c你没有权限使用这个命令！喵~";
                if (processor != null) {
                    processor.sendCatStyleMessage(player, message);
                } else {
                    player.sendMessage(message);
                }
                return true;
            }

            if (args.length < 1) {
                String message = "§c用法: /setwarp <传送点名称>喵~";
                if (processor != null) {
                    processor.sendCatStyleMessage(player, message);
                } else {
                    player.sendMessage(message);
                }
                return true;
            }

            String warpName = args[0];
            
            // 创建传送点
            boolean success = warpManager.createWarp(warpName, player.getLocation(), player.getName());
            
            if (success) {
                String message = "§a传送点 '§6" + warpName + "§a' 已成功创建！喵~";
                if (processor != null) {
                    processor.sendCatStyleMessage(player, message);
                } else {
                    player.sendMessage(message);
                }
            } else {
                String message = "§c创建传送点失败！喵~";
                if (processor != null) {
                    processor.sendCatStyleMessage(player, message);
                } else {
                    player.sendMessage(message);
                }
            }
        } catch (Exception e) {
            String errorMessage = "§c执行命令时发生错误！喵~";
            if (sender instanceof Player) {
                CatChatProcessor processor = CatChatProcessor.getInstance();
                if (processor != null) {
                    processor.sendCatStyleMessage((Player) sender, errorMessage);
                } else {
                    sender.sendMessage(errorMessage);
                }
            } else {
                sender.sendMessage(errorMessage);
            }
            plugin.getLogger().severe("执行setwarp命令时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        // setwarp命令不需要Tab补全，返回空列表
        return List.of();
    }
}