package com.nekoessentialsx.gui;

import com.nekoessentialsx.NekoEssentialX;
import com.nekoessentialsx.economy.EconomyManager;
import com.nekoessentialsx.titles.TitleManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ChatInputListener implements Listener {
    private final NekoEssentialX plugin;
    private final GUIManager guiManager;
    private final TitleManager titleManager;
    private final EconomyManager economyManager;
    private final Map<Player, InputState> playerInputStates = new HashMap<>();

    public enum InputState {
        WAITING_FOR_TITLE_NAME,
        WAITING_FOR_ADMIN_TITLE_INFO,
        WAITING_FOR_ADMIN_EDIT_TITLE_INFO,
        WAITING_FOR_CURRENCY_NAME
    }

    public ChatInputListener(NekoEssentialX plugin, GUIManager guiManager) {
        this.plugin = plugin;
        this.guiManager = guiManager;
        this.titleManager = plugin.getTitleManager();
        this.economyManager = plugin.getEconomyManager();
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (playerInputStates.containsKey(player)) {
            event.setCancelled(true);
            String message = event.getMessage().trim();
            handleInput(player, message);
        }
    }

    private void handleInput(Player player, String input) {
        InputState state = playerInputStates.remove(player);
        if (state == null) return;

        switch (state) {
            case WAITING_FOR_TITLE_NAME:
                handleTitleNameInput(player, input);
                break;
            case WAITING_FOR_ADMIN_TITLE_INFO:
                handleAdminTitleInfoInput(player, input);
                break;
            case WAITING_FOR_ADMIN_EDIT_TITLE_INFO:
                handleAdminEditTitleInfoInput(player, input);
                break;
            case WAITING_FOR_CURRENCY_NAME:
                handleCurrencyNameInput(player, input);
                break;
        }
    }

    private void handleTitleNameInput(Player player, String input) {
        // 检查是否取消操作
        if (input.equalsIgnoreCase("cancel")) {
            player.sendMessage(ChatColor.YELLOW + "创建头衔操作已取消！");
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                plugin.getGuiManager().openMainGUI(player);
            });
            return;
        }
        
        // 检查是否确认创建
        if (input.equalsIgnoreCase("confirm")) {
            // 从会话中获取之前输入的头衔名称
            GUIManager.GUISession session = plugin.getGuiManager().getOrCreateSession(player);
            String titleName = (String) session.getData().get("previewTitleName");
            if (titleName == null || titleName.isEmpty()) {
                player.sendMessage(ChatColor.RED + "请先输入头衔名称！");
                // 重新设置输入状态
                setPlayerInputState(player, InputState.WAITING_FOR_TITLE_NAME);
                return;
            }
            
            // 使用主线程打开确认GUI
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                plugin.getGuiManager().openCreateTitleConfirmGUI(player, titleName);
            });
            return;
        }

        // 验证输入长度
        if (input.length() < 1) {
            player.sendMessage(ChatColor.RED + "头衔名称不能为空！");
            // 重新设置输入状态
            setPlayerInputState(player, InputState.WAITING_FOR_TITLE_NAME);
            return;
        }

        if (input.length() > 20) {
            player.sendMessage(ChatColor.RED + "头衔名称不能超过20个字符！");
            // 重新设置输入状态
            setPlayerInputState(player, InputState.WAITING_FOR_TITLE_NAME);
            return;
        }
        
        // 保存当前输入到会话，用于实时预览
        GUIManager.GUISession session = plugin.getGuiManager().getOrCreateSession(player);
        session.setData("previewTitleName", input);
        
        // 更新实时预览
        updateTitlePreview(player, input);
        
        // 继续保持输入状态
        setPlayerInputState(player, InputState.WAITING_FOR_TITLE_NAME);
    }
    
    /**
     * 更新头衔实时预览
     * @param player 玩家
     * @param titleName 头衔名称
     */
    private void updateTitlePreview(Player player, String titleName) {
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            // 获取当前打开的GUI
            org.bukkit.inventory.Inventory openInventory = player.getOpenInventory().getTopInventory();
            if (!(openInventory.getHolder() instanceof GUIManager.GUIHolder)) {
                return;
            }
            
            // 更新预览项
            ItemStack previewItem = openInventory.getItem(11);
            if (previewItem == null) {
                return;
            }
            
            ItemMeta previewMeta = previewItem.getItemMeta();
            if (previewMeta == null) {
                return;
            }
            
            String prefix = "[" + titleName + "] ";
            List<String> previewLore = new ArrayList<>();
            previewLore.add("§7当前预览效果：");
            previewLore.add("§7头衔名称：§b" + titleName);
            previewLore.add("§7头衔前缀：§b" + prefix);
            previewLore.add("§7价格：" + (player.hasPermission("nekoessentialx.title.admin") ? "§a免费" : "§6" + GUIManager.CUSTOM_TITLE_COST + " " + plugin.getEconomyManager().getCurrencyName()));
            previewLore.add("§a输入 'confirm' 确认创建");
            previewLore.add("§c输入 'cancel' 取消操作");
            previewMeta.setLore(previewLore);
            previewItem.setItemMeta(previewMeta);
            openInventory.setItem(11, previewItem);
            
            // 发送提示消息
            player.sendMessage("§a实时预览已更新！请查看GUI中的预览效果。");
            player.sendMessage("§a输入 'confirm' 确认创建，或 'cancel' 取消操作。");
        });
    }

    private void handleAdminTitleInfoInput(Player player, String input) {
        // 检查是否取消操作
        if (input.equalsIgnoreCase("cancel")) {
            player.sendMessage(ChatColor.YELLOW + "创建系统头衔操作已取消！");
            return;
        }

        // 解析输入，格式：<id> <name> <prefix>
        String[] parts = input.split("\s+", 3);
        if (parts.length < 3) {
            player.sendMessage(ChatColor.RED + "输入格式错误！请使用以下格式：");
            player.sendMessage(ChatColor.YELLOW + "<id> <name> <prefix>");
            player.sendMessage(ChatColor.YELLOW + "示例：vip 会员 [VIP] ");
            player.sendMessage(ChatColor.YELLOW + "输入 'cancel' 取消操作");
            // 重新设置输入状态，让玩家继续输入
            playerInputStates.put(player, InputState.WAITING_FOR_ADMIN_TITLE_INFO);
            return;
        }

        String titleId = parts[0];
        String titleName = parts[1];
        String prefix = parts[2];

        // 验证输入
        if (titleId.isEmpty() || titleName.isEmpty() || prefix.isEmpty()) {
            player.sendMessage(ChatColor.RED + "所有字段都不能为空！");
            player.sendMessage(ChatColor.YELLOW + "输入 'cancel' 取消操作");
            // 重新设置输入状态，让玩家继续输入
            playerInputStates.put(player, InputState.WAITING_FOR_ADMIN_TITLE_INFO);
            return;
        }

        if (titleName.length() > 20) {
            player.sendMessage(ChatColor.RED + "头衔名称不能超过20个字符！");
            player.sendMessage(ChatColor.YELLOW + "输入 'cancel' 取消操作");
            // 重新设置输入状态，让玩家继续输入
            playerInputStates.put(player, InputState.WAITING_FOR_ADMIN_TITLE_INFO);
            return;
        }

        if (prefix.length() > 30) {
            player.sendMessage(ChatColor.RED + "头衔前缀不能超过30个字符！");
            player.sendMessage(ChatColor.YELLOW + "输入 'cancel' 取消操作");
            // 重新设置输入状态，让玩家继续输入
            playerInputStates.put(player, InputState.WAITING_FOR_ADMIN_TITLE_INFO);
            return;
        }

        // 调用TitleManager创建系统头衔
        titleManager.createTitle(titleId, titleName, prefix, "", "nekoessentialx.titles." + titleId, 1, true);

        // 发送成功消息
        player.sendMessage(ChatColor.GREEN + "系统头衔创建成功！");
        player.sendMessage(ChatColor.GREEN + "头衔ID: " + ChatColor.AQUA + titleId);
        player.sendMessage(ChatColor.GREEN + "头衔名称: " + ChatColor.AQUA + titleName);
        player.sendMessage(ChatColor.GREEN + "头衔前缀: " + ChatColor.AQUA + prefix);

        // 使用主线程打开GUI，避免异步线程调用同步方法
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            guiManager.openAdminTitleManager(player, 1);
        });
    }

    private void handleAdminEditTitleInfoInput(Player player, String input) {
        // 检查是否取消操作
        if (input.equalsIgnoreCase("cancel")) {
            player.sendMessage(ChatColor.YELLOW + "编辑系统头衔操作已取消！");
            return;
        }

        // 解析输入，格式：<name> <prefix>
        String[] parts = input.split("\s+", 2);
        if (parts.length < 2) {
            player.sendMessage(ChatColor.RED + "输入格式错误！请使用以下格式：");
            player.sendMessage(ChatColor.YELLOW + "<name> <prefix>");
            player.sendMessage(ChatColor.YELLOW + "示例：超级VIP [超级VIP] ");
            player.sendMessage(ChatColor.YELLOW + "输入 'cancel' 取消操作");
            // 重新设置输入状态，让玩家继续输入
            playerInputStates.put(player, InputState.WAITING_FOR_ADMIN_EDIT_TITLE_INFO);
            return;
        }

        // 获取正在编辑的头衔ID
        GUIManager.GUISession session = guiManager.getOrCreateSession(player);
        String titleId = (String) session.getData().get("editingTitleId");
        if (titleId == null) {
            player.sendMessage(ChatColor.RED + "无法获取正在编辑的头衔ID！");
            return;
        }

        String titleName = parts[0];
        String prefix = parts[1];

        // 验证输入
        if (titleName.isEmpty() || prefix.isEmpty()) {
            player.sendMessage(ChatColor.RED + "所有字段都不能为空！");
            player.sendMessage(ChatColor.YELLOW + "输入 'cancel' 取消操作");
            // 重新设置输入状态，让玩家继续输入
            playerInputStates.put(player, InputState.WAITING_FOR_ADMIN_EDIT_TITLE_INFO);
            return;
        }

        if (titleName.length() > 20) {
            player.sendMessage(ChatColor.RED + "头衔名称不能超过20个字符！");
            player.sendMessage(ChatColor.YELLOW + "输入 'cancel' 取消操作");
            // 重新设置输入状态，让玩家继续输入
            playerInputStates.put(player, InputState.WAITING_FOR_ADMIN_EDIT_TITLE_INFO);
            return;
        }

        if (prefix.length() > 30) {
            player.sendMessage(ChatColor.RED + "头衔前缀不能超过30个字符！");
            player.sendMessage(ChatColor.YELLOW + "输入 'cancel' 取消操作");
            // 重新设置输入状态，让玩家继续输入
            playerInputStates.put(player, InputState.WAITING_FOR_ADMIN_EDIT_TITLE_INFO);
            return;
        }

        // 调用TitleManager编辑系统头衔
        titleManager.editTitle(titleId, titleName, prefix, "", "nekoessentialx.titles." + titleId, 1, true);

        // 发送成功消息
        player.sendMessage(ChatColor.GREEN + "系统头衔编辑成功！");
        player.sendMessage(ChatColor.GREEN + "头衔ID: " + ChatColor.AQUA + titleId);
        player.sendMessage(ChatColor.GREEN + "头衔名称: " + ChatColor.AQUA + titleName);
        player.sendMessage(ChatColor.GREEN + "头衔前缀: " + ChatColor.AQUA + prefix);

        // 使用主线程打开GUI，避免异步线程调用同步方法
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            guiManager.openAdminTitleManager(player, 1);
        });
    }

    public void setPlayerInputState(Player player, InputState state) {
        playerInputStates.put(player, state);
    }

    private void handleCurrencyNameInput(Player player, String currencyName) {
        // 检查是否取消操作
        if (currencyName.equalsIgnoreCase("cancel")) {
            player.sendMessage(ChatColor.YELLOW + "更改货币名称操作已取消！");
            return;
        }

        // 验证货币名称长度
        if (currencyName.length() < 1) {
            player.sendMessage(ChatColor.RED + "货币名称不能为空！");
            return;
        }

        if (currencyName.length() > 20) {
            player.sendMessage(ChatColor.RED + "货币名称不能超过20个字符！");
            return;
        }

        // 更新配置文件中的货币名称
        plugin.getConfig().set("economy.currency-name", currencyName);
        plugin.getConfig().set("economy.currency-name-plural", currencyName);
        plugin.saveConfig();
        
        // 更新EconomyManager中的货币名称
        plugin.getEconomyManager().reloadCurrencyName();

        // 发送成功消息
        player.sendMessage(ChatColor.GREEN + "货币名称更改成功！");
        player.sendMessage(ChatColor.GREEN + "新的货币名称：" + ChatColor.AQUA + currencyName);

        // 使用主线程打开GUI，避免异步线程调用同步方法
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            guiManager.openEconomyMenu(player);
        });
    }

    public void clearPlayerInputState(Player player) {
        playerInputStates.remove(player);
    }

    public boolean hasPlayerInputState(Player player) {
        return playerInputStates.containsKey(player);
    }
}