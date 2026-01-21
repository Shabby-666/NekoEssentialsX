package com.nekoessentialsx.commands;

import com.nekoessentialsx.NekoEssentialX;
import com.nekoessentialsx.catstyle.CatChatProcessor;
import com.nekoessentialsx.gui.GUIManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import java.util.List;

public class Commandmainmenu implements CommandExecutor, TabCompleter {
    private final NekoEssentialX plugin;
    private final GUIManager guiManager;

    public Commandmainmenu(NekoEssentialX plugin) {
        this.plugin = plugin;
        this.guiManager = plugin.getGuiManager();
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
            
            // 打开主菜单
            guiManager.openMainGUI(player);
            
            // 发送猫娘风格的消息
            if (processor != null) {
                processor.sendCatStyleMessage(player, "§a正在为您打开主菜单的说~喵~");
            } else {
                player.sendMessage("§a正在为您打开主菜单的说~喵~");
            }
            
            plugin.getLogger().info("玩家 " + player.getName() + " 打开了主菜单！喵~");
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
            plugin.getLogger().severe("执行mainmenu命令时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        // mainmenu命令不需要Tab补全，返回空列表
        return List.of();
    }
}