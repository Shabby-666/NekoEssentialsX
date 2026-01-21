package com.nekoessentialsx.commands;

import com.nekoessentialsx.NekoEssentialX;
import com.nekoessentialsx.catstyle.CatChatProcessor;
import com.nekoessentialsx.database.DatabaseManager;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import java.util.Collections;
import java.util.List;

public class Commandhome implements CommandExecutor, TabCompleter {
    private final NekoEssentialX plugin;
    private final DatabaseManager databaseManager;

    public Commandhome(NekoEssentialX plugin) {
        this.plugin = plugin;
        this.databaseManager = plugin.getDatabaseManager();
        // 注册Tab补全器到home命令
        plugin.getCommand("home").setTabCompleter(this);
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
            
            if (args.length > 0) {
                String homeName = args[0];
                teleportToHome(player, homeName, processor);
            } else {
                // 如果没有提供家名称，显示家列表或传送到默认家
                List<String> homeNames = databaseManager.getPlayerHomeNames(playerId);
                
                if (homeNames.isEmpty()) {
                    String message = "§c你还没有设置家！使用 /sethome 设置你的第一个家吧！喵~";
                    if (processor != null) {
                        processor.sendCatStyleMessage(player, message);
                    } else {
                        player.sendMessage(message);
                    }
                } else if (homeNames.size() == 1 || homeNames.contains("default")) {
                    // 如果只有一个家或有家名为default，直接传送到default家
                    teleportToHome(player, "default", processor);
                } else {
                    // 显示家列表
                    showHomeList(player, homeNames, processor);
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
            plugin.getLogger().severe("执行home命令时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
        return true;
    }

    /**
     * 传送到指定家
     */
    private void teleportToHome(Player player, String homeName, CatChatProcessor processor) {
        String playerId = player.getName();
        
        if (!databaseManager.hasPlayerHome(playerId, homeName)) {
            String message = "§c你没有名为 '" + homeName + "' 的家！喵~";
            if (processor != null) {
                processor.sendCatStyleMessage(player, message);
            } else {
                player.sendMessage(message);
            }
            return;
        }

        // 获取家的位置信息
        Object[] homeInfo = databaseManager.getPlayerHome(playerId, homeName);
        if (homeInfo == null) {
            String message = "§c获取家位置失败！喵~";
            if (processor != null) {
                processor.sendCatStyleMessage(player, message);
            } else {
                player.sendMessage(message);
            }
            return;
        }

        // 解析位置信息
        String worldName = (String) homeInfo[0];
        double x = (double) homeInfo[1];
        double y = (double) homeInfo[2];
        double z = (double) homeInfo[3];
        float yaw = (float) homeInfo[4];
        float pitch = (float) homeInfo[5];

        // 获取世界对象
        World world = plugin.getServer().getWorld(worldName);
        if (world == null) {
            String message = "§c找不到对应的世界！喵~";
            if (processor != null) {
                processor.sendCatStyleMessage(player, message);
            } else {
                player.sendMessage(message);
            }
            return;
        }

        // 创建位置对象
        Location homeLocation = new Location(world, x, y, z, yaw, pitch);

        // 传送玩家到家中
        player.teleport(homeLocation);

        // 发送成功消息
        String message = "§a已成功传送到你的家 '" + homeName + "'！喵~";
        if (processor != null) {
            processor.sendCatStyleMessage(player, message);
        } else {
            player.sendMessage(message);
        }
    }

    /**
     * 显示玩家所有家列表
     */
    private void showHomeList(Player player, List<String> homeNames, CatChatProcessor processor) {
        StringBuilder message = new StringBuilder("§6===== §e你的家列表 §6=====\n");
        
        for (String homeName : homeNames) {
            message.append("§a- §6").append(homeName).append("§a喵~\n");
        }
        
        message.append("§a使用 /home <家名称> 传送到指定家！喵~");
        
        if (processor != null) {
            processor.sendCatStyleMessage(player, message.toString());
        } else {
            player.sendMessage(message.toString());
        }
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