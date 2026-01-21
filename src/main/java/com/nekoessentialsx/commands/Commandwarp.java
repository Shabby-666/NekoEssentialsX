package com.nekoessentialsx.commands;

import com.nekoessentialsx.NekoEssentialX;
import com.nekoessentialsx.catstyle.CatChatProcessor;
import com.nekoessentialsx.warp.WarpManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;

public class Commandwarp implements CommandExecutor, TabCompleter {
    private final NekoEssentialX plugin;
    private final WarpManager warpManager;

    public Commandwarp(NekoEssentialX plugin) {
        this.plugin = plugin;
        this.warpManager = plugin.getWarpManager();
        // 注册Tab补全器到warp命令
        plugin.getCommand("warp").setTabCompleter(this);
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
            
            if (args.length < 1) {
                // 显示可用传送点列表
                showWarpList(player, processor);
                return true;
            }

            String warpName = args[0];
            
            // 检查传送点是否存在
            if (!warpManager.warpExists(warpName)) {
                String message = "§c找不到名为 '" + warpName + "' 的传送点！喵~";
                if (processor != null) {
                    processor.sendCatStyleMessage(player, message);
                } else {
                    player.sendMessage(message);
                }
                return true;
            }
            
            // 传送到传送点
            boolean success = warpManager.teleportToWarp(player, warpName);
            
            if (success) {
                String message = "§a已成功传送到传送点 '§6" + warpName + "§a'！喵~";
                if (processor != null) {
                    processor.sendCatStyleMessage(player, message);
                } else {
                    player.sendMessage(message);
                }
            } else {
                String message = "§c传送失败！请检查传送点是否有效！喵~";
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
            plugin.getLogger().severe("执行warp命令时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
        return true;
    }

    /**
     * 显示可用传送点列表
     */
    private void showWarpList(Player player, CatChatProcessor processor) {
        List<String> warpNames = warpManager.getWarpNames();
        
        if (warpNames.isEmpty()) {
            String message = "§c当前没有可用的传送点！喵~";
            if (processor != null) {
                processor.sendCatStyleMessage(player, message);
            } else {
                player.sendMessage(message);
            }
            return;
        }
        
        StringBuilder message = new StringBuilder("§6===== §e可用传送点列表 §6=====\n");
        
        for (String warpName : warpNames) {
            message.append("§a- §6").append(warpName).append("§a喵~\n");
        }
        
        message.append("§a使用 /warp <传送点名称> 传送到指定传送点！喵~");
        
        if (processor != null) {
            processor.sendCatStyleMessage(player, message.toString());
        } else {
            player.sendMessage(message.toString());
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            String partialName = args[0];
            List<String> warpNames = warpManager.getWarpNames();
            
            for (String warpName : warpNames) {
                if (warpName.startsWith(partialName)) {
                    completions.add(warpName);
                }
            }
        }
        
        return completions;
    }
}