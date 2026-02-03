package com.nekoessentialsx.commands;

import com.nekoessentialsx.NekoEssentialX;
import com.nekoessentialsx.catstyle.CatChatProcessor;
import com.nekoessentialsx.database.DatabaseManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import java.util.Collections;
import java.util.List;

public class Commanddelhome implements CommandExecutor, TabCompleter {
    private final NekoEssentialX plugin;
    private final DatabaseManager databaseManager;

    public Commanddelhome(NekoEssentialX plugin) {
        this.plugin = plugin;
        this.databaseManager = plugin.getDatabaseManager();
        // 注册Tab补全器到delhome命令
        plugin.getCommand("delhome").setTabCompleter(this);
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
            String playerId = player.getName();
            
            if (args.length < 1) {
                String message = "§c用法: /delhome <家名称>喵~";
                if (processor != null) {
                    processor.sendCatStyleMessage(player, message);
                } else {
                    player.sendMessage(message);
                }
                return true;
            }

            String homeName = args[0];
            
            // 检查玩家是否有该名称的家
            if (!databaseManager.hasPlayerHome(playerId, homeName)) {
                String message = "§c你没有名为 '" + homeName + "' 的家！喵~";
                if (processor != null) {
                    processor.sendCatStyleMessage(player, message);
                } else {
                    player.sendMessage(message);
                }
                return true;
            }

            // 删除玩家的家
            boolean success = databaseManager.deletePlayerHome(playerId, homeName);

            if (success) {
                String message = "§a家 '" + homeName + "' 已成功删除！喵~";
                if (processor != null) {
                    processor.sendCatStyleMessage(player, message);
                } else {
                    player.sendMessage(message);
                }
            } else {
                String message = "§c删除家失败！喵~";
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
            plugin.getLogger().severe("执行delhome命令时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return Collections.emptyList();
        }

        Player player = (Player) sender;
        if (args.length == 1) {
            // 补全已有的家名称
            return databaseManager.getPlayerHomeNames(player.getName());
        }
        return Collections.emptyList();
    }
}