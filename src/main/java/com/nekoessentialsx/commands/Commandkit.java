package com.nekoessentialsx.commands;

import com.nekoessentialsx.NekoEssentialX;
import com.nekoessentialsx.catstyle.CatChatProcessor;
import com.nekoessentialsx.kits.KitManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;

public class Commandkit implements CommandExecutor, TabCompleter {
    private final NekoEssentialX plugin;
    private final KitManager kitManager;

    public Commandkit(NekoEssentialX plugin) {
        this.plugin = plugin;
        this.kitManager = plugin.getKitManager();
        // 注册Tab补全器到kit命令
        plugin.getCommand("kit").setTabCompleter(this);
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
                // 显示可用工具包列表
                showKitList(player, processor);
                return true;
            }

            String kitName = args[0];
            
            // 领取工具包
            kitManager.giveKit(player, kitName);
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
            plugin.getLogger().severe("执行kit命令时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
        return true;
    }

    /**
     * 显示可用工具包列表
     */
    private void showKitList(Player player, CatChatProcessor processor) {
        List<String> kitNames = kitManager.getKitNames();
        
        if (kitNames.isEmpty()) {
            String message = "§c当前没有可用的工具包！喵~";
            if (processor != null) {
                processor.sendCatStyleMessage(player, message);
            } else {
                player.sendMessage(message);
            }
            return;
        }
        
        StringBuilder message = new StringBuilder("§6===== §e可用工具包列表 §6=====\n");
        
        for (String kitName : kitNames) {
            String canClaim = kitManager.canClaimKit(player, kitName);
            if (canClaim == null) {
                message.append("§a- §6").append(kitName).append("§a喵~\n");
            } else {
                message.append("§c- §6").append(kitName).append(" §c(不可用)喵~\n");
            }
        }
        
        message.append("§a使用 /kit <工具包名称> 领取工具包！喵~");
        
        if (processor != null) {
            processor.sendCatStyleMessage(player, message.toString());
        } else {
            player.sendMessage(message.toString());
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (sender instanceof Player && args.length == 1) {
            Player player = (Player) sender;
            List<String> kitNames = kitManager.getKitNames();
            String partialName = args[0];
            
            for (String kitName : kitNames) {
                if (kitName.startsWith(partialName)) {
                    // 只显示玩家可以领取的工具包
                    if (kitManager.canClaimKit(player, kitName) == null) {
                        completions.add(kitName);
                    }
                }
            }
        }
        
        return completions;
    }
}