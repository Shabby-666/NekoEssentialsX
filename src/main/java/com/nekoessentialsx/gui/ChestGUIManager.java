package com.nekoessentialsx.gui;

import com.nekoessentialsx.NekoEssentialX;
import com.nekoessentialsx.database.DatabaseManager;
import com.nekoessentialsx.economy.EconomyManager;
import com.nekoessentialsx.kits.KitManager;
import com.nekoessentialsx.titles.TitleManager;
import com.nekoessentialsx.tpa.TPAManager;
import com.nekoessentialsx.warp.WarpManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * 箱子GUI管理器
 * 统一管理所有GUI界面，支持双调用方式（指令+GUI）
 */
public class ChestGUIManager {
    
    private final NekoEssentialX plugin;
    private final DatabaseManager databaseManager;
    private final EconomyManager economyManager;
    private final TitleManager titleManager;
    private final WarpManager warpManager;
    private final KitManager kitManager;
    private final TPAManager tpaManager;
    
    // 存储玩家当前打开的GUI
    private final Map<UUID, ChestGUI> playerGUIs;
    // 存储玩家GUI历史记录（用于返回功能）
    private final Map<UUID, Stack<String>> playerGUIHistory;
    
    // GUI ID常量
    public static final String GUI_MAIN_MENU = "main_menu";
    public static final String GUI_HOME_MENU = "home_menu";
    public static final String GUI_WARP_MENU = "warp_menu";
    public static final String GUI_KIT_MENU = "kit_menu";
    public static final String GUI_TITLE_MENU = "title_menu";
    public static final String GUI_ECONOMY_MENU = "economy_menu";
    public static final String GUI_TP_MENU = "tp_menu";
    public static final String GUI_PLAYER_LIST = "player_list";
    public static final String GUI_CONFIRM = "confirm";
    
    // 价格常量
    public static final int TITLE_COST = 100;
    public static final int CUSTOM_TITLE_COST = 1000;
    
    public ChestGUIManager(NekoEssentialX plugin) {
        this.plugin = plugin;
        this.databaseManager = plugin.getDatabaseManager();
        this.economyManager = plugin.getEconomyManager();
        this.titleManager = plugin.getTitleManager();
        this.warpManager = plugin.getWarpManager();
        this.kitManager = plugin.getKitManager();
        this.tpaManager = plugin.getTPAManager();
        this.playerGUIs = new HashMap<>();
        this.playerGUIHistory = new HashMap<>();
    }
    
    /**
     * 初始化GUI管理器
     */
    public void initialize() {
        // 注册GUI监听器
        plugin.getServer().getPluginManager().registerEvents(new ChestGUIListener(this), plugin);
    }
    
    /**
     * 获取插件实例
     * @return 插件实例
     */
    public NekoEssentialX getPlugin() {
        return plugin;
    }
    
    /**
     * 打开主菜单GUI
     * @param player 玩家
     */
    public void openMainMenu(Player player) {
        ChestGUI gui = new ChestGUI(player, "§6§lNekoEssentialX §7- 主菜单", ChestGUI.DOUBLE_CHEST_SIZE, GUI_MAIN_MENU);
        
        // 填充背景
        gui.fillEmpty(Material.GRAY_STAINED_GLASS_PANE, " ");
        
        // 家系统
        gui.setItem(10, ChestGUI.createCategoryIcon(Material.RED_BED, "家系统", 
            "管理你的家，传送到已设置的家", "§c", true),
            (p, click) -> openHomeMenu(p));
        
        // 传送点系统
        gui.setItem(12, ChestGUI.createCategoryIcon(Material.ENDER_PEARL, "传送点", 
            "查看和传送到公共传送点", "§9", true),
            (p, click) -> openWarpMenu(p, 1));
        
        // 工具包系统
        gui.setItem(14, ChestGUI.createCategoryIcon(Material.CHEST, "工具包", 
            "领取各种工具包奖励", "§6", true),
            (p, click) -> openKitMenu(p, 1));
        
        // 头衔系统
        gui.setItem(16, ChestGUI.createCategoryIcon(Material.NAME_TAG, "头衔系统", 
            "购买、装备和管理你的头衔", "§d", true),
            (p, click) -> openTitleMenu(p));
        
        // 经济系统
        gui.setItem(28, ChestGUI.createCategoryIcon(Material.GOLD_INGOT, "经济系统", 
            "查看余额、转账和管理经济", "§e", true),
            (p, click) -> openEconomyMenu(p));
        
        // 传送系统
        gui.setItem(30, ChestGUI.createCategoryIcon(Material.COMPASS, "传送系统", 
            "发送和接受传送请求", "§b", true),
            (p, click) -> openTPMenu(p));
        
        // 玩家列表
        gui.setItem(32, ChestGUI.createCategoryIcon(Material.PLAYER_HEAD, "在线玩家", 
            "查看当前在线的玩家列表", "§a", true),
            (p, click) -> openPlayerList(p, 1));

        // 新手礼包
        if (plugin.getNewbieGiftManager().canClaimGift(player.getName())) {
            gui.setItem(36, ChestGUI.createCategoryIcon(Material.CHEST, "新手礼包", 
                "领取你的新手大礼包！", "§5", true),
                (p, click) -> {
                    p.closeInventory();
                    plugin.getNewbieGiftManager().handleGiftClaim(p);
                });
        }
        
        // 每日签到
        gui.setItem(38, ChestGUI.createCategoryIcon(Material.CLOCK, "每日签到", 
            "点击领取每日签到奖励", "§3", true),
            (p, click) -> {
                p.closeInventory();
                plugin.getDailyLoginManager().handleDailyCheckIn(p);
            });
        
        // 插件信息
        gui.setItem(40, ChestGUI.createCategoryIcon(Material.BOOK, "插件信息", 
            "查看插件版本和重载配置", "§f", player.hasPermission("nekoessentialsx.admin")),
            (p, click) -> {
                if (p.hasPermission("nekoessentialsx.admin")) {
                    openPluginInfoMenu(p);
                } else {
                    p.sendMessage("§c你没有权限访问此功能！");
                }
            });
        
        // AFK状态
        gui.setItem(42, ChestGUI.createCategoryIcon(Material.LEAD, "AFK状态", 
            "切换你的AFK（离开）状态", "§7", true),
            (p, click) -> {
                p.closeInventory();
                plugin.getAFKManager().setAFK(p, !plugin.getAFKManager().isAFK(p));
            });
        
        // 关闭按钮
        gui.setItem(49, ChestGUI.createItem(Material.BARRIER, "§c§l关闭菜单", 
            List.of("§7点击关闭此菜单"), "§c"),
            (p, click) -> p.closeInventory());
        
        gui.open();
        playerGUIs.put(player.getUniqueId(), gui);
        recordGUIHistory(player, GUI_MAIN_MENU);
    }
    
    /**
     * 打开家菜单GUI
     * @param player 玩家
     */
    public void openHomeMenu(Player player) {
        ChestGUI gui = new ChestGUI(player, "§c§l家系统 §7- 管理你的家", ChestGUI.DOUBLE_CHEST_SIZE, GUI_HOME_MENU);
        
        List<String> homeNames = databaseManager.getPlayerHomeNames(player.getName());
        
        // 填充背景
        gui.fillEmpty(Material.GRAY_STAINED_GLASS_PANE, " ");
        
        // 显示家列表
        int slot = 0;
        for (String homeName : homeNames) {
            if (slot >= 45) break; // 保留底部导航栏
            
            Object[] homeInfo = databaseManager.getPlayerHome(player.getName(), homeName);
            if (homeInfo != null) {
                String worldName = (String) homeInfo[0];
                double x = (double) homeInfo[1];
                double y = (double) homeInfo[2];
                double z = (double) homeInfo[3];
                
                ItemStack homeItem = ChestGUI.createItem(Material.RED_BED, "§c§l" + homeName,
                    List.of(
                        "§7世界: §f" + worldName,
                        "§7坐标: §f" + String.format("%.1f, %.1f, %.1f", x, y, z),
                        "§a左键传送",
                        "§c右键删除"
                    ), "§c");
                
                final String finalHomeName = homeName;
                gui.setItem(slot, homeItem, (p, click) -> {
                    if (click == ChestGUI.ClickType.RIGHT) {
                        // 删除家
                        openConfirmGUI(p, "删除家", "§c确定要删除家 §l" + finalHomeName + " §c吗？",
                            (confirmPlayer) -> {
                                databaseManager.deletePlayerHome(confirmPlayer.getName(), finalHomeName);
                                confirmPlayer.sendMessage("§a家 §l" + finalHomeName + " §a已删除！");
                                openHomeMenu(confirmPlayer);
                            },
                            (cancelPlayer) -> openHomeMenu(cancelPlayer));
                    } else {
                        // 传送
                        p.closeInventory();
                        teleportToHome(p, finalHomeName);
                    }
                });
                slot++;
            }
        }
        
        // 设置新家按钮
        if (slot < 45) {
            gui.setItem(slot, ChestGUI.createItem(Material.GREEN_CONCRETE, "§a§l设置新家",
                List.of("§7在当前位置设置一个新家", "§7左键点击设置"), "§a"),
                (p, click) -> {
                    p.closeInventory();
                    p.sendMessage("§a请使用指令 §e/sethome <家名称> §a来设置新家！");
                });
        }
        
        // 添加导航栏
        gui.addNavigationBar(false, false, 
            (p, click) -> openMainMenu(p), null, null);
        
        gui.open();
        playerGUIs.put(player.getUniqueId(), gui);
        recordGUIHistory(player, GUI_HOME_MENU);
    }
    
    /**
     * 打开传送点菜单GUI
     * @param player 玩家
     * @param page 页码
     */
    public void openWarpMenu(Player player, int page) {
        List<String> warpNames = warpManager.getWarpNames();
        int itemsPerPage = 45;
        int totalPages = (int) Math.ceil((double) warpNames.size() / itemsPerPage);
        page = Math.max(1, Math.min(page, totalPages));
        
        ChestGUI gui = new ChestGUI(player, "§9§l传送点 §7- 第 " + page + "/" + totalPages + " 页", 
            ChestGUI.DOUBLE_CHEST_SIZE, GUI_WARP_MENU);
        
        // 填充背景
        gui.fillEmpty(Material.GRAY_STAINED_GLASS_PANE, " ");
        
        int startIndex = (page - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, warpNames.size());
        
        for (int i = startIndex; i < endIndex; i++) {
            String warpName = warpNames.get(i);
            int slot = i - startIndex;
            
            ItemStack warpItem = ChestGUI.createItem(Material.ENDER_PEARL, "§9§l" + warpName,
                List.of("§7点击传送到此传送点"), "§9");
            
            gui.setItem(slot, warpItem, (p, click) -> {
                p.closeInventory();
                teleportToWarp(p, warpName);
            });
        }
        
        // 添加导航栏
        gui.setCurrentPage(page);
        gui.setMaxPage(totalPages);
        final int currentPage = page;
        gui.addNavigationBar(page > 1, page < totalPages,
            (p, click) -> openMainMenu(p),
            (p, click) -> openWarpMenu(p, currentPage - 1),
            (p, click) -> openWarpMenu(p, currentPage + 1));
        
        gui.open();
        playerGUIs.put(player.getUniqueId(), gui);
        recordGUIHistory(player, GUI_WARP_MENU);
    }
    
    /**
     * 打开工具包菜单GUI
     * @param player 玩家
     * @param page 页码
     */
    public void openKitMenu(Player player, int page) {
        List<String> kitNames = kitManager.getKitNames();
        int itemsPerPage = 45;
        int totalPages = (int) Math.ceil((double) kitNames.size() / itemsPerPage);
        page = Math.max(1, Math.min(page, totalPages));
        
        ChestGUI gui = new ChestGUI(player, "§6§l工具包 §7- 第 " + page + "/" + totalPages + " 页", 
            ChestGUI.DOUBLE_CHEST_SIZE, GUI_KIT_MENU);
        
        // 填充背景
        gui.fillEmpty(Material.GRAY_STAINED_GLASS_PANE, " ");
        
        int startIndex = (page - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, kitNames.size());
        
        for (int i = startIndex; i < endIndex; i++) {
            String kitName = kitNames.get(i);
            int slot = i - startIndex;
            
            String canClaim = kitManager.canClaimKit(player, kitName);
            boolean available = canClaim == null;
            
            Material material = available ? Material.CHEST : Material.BARRIER;
            String color = available ? "§6" : "§c";
            List<String> lore = new ArrayList<>();
            lore.add("§7点击领取此工具包");
            if (!available) {
                lore.add("§c" + canClaim);
            }
            
            ItemStack kitItem = ChestGUI.createItem(material, color + "§l" + kitName, lore, color);
            
            final String finalKitName = kitName;
            if (available) {
                gui.setItem(slot, kitItem, (p, click) -> {
                    p.closeInventory();
                    claimKit(p, finalKitName);
                });
            } else {
                gui.setItem(slot, kitItem, null);
            }
        }
        
        // 添加导航栏
        gui.setCurrentPage(page);
        gui.setMaxPage(totalPages);
        final int currentPage = page;
        gui.addNavigationBar(page > 1, page < totalPages,
            (p, click) -> openMainMenu(p),
            (p, click) -> openKitMenu(p, currentPage - 1),
            (p, click) -> openKitMenu(p, currentPage + 1));
        
        gui.open();
        playerGUIs.put(player.getUniqueId(), gui);
        recordGUIHistory(player, GUI_KIT_MENU);
    }
    
    /**
     * 打开头衔菜单GUI
     * @param player 玩家
     */
    public void openTitleMenu(Player player) {
        ChestGUI gui = new ChestGUI(player, "§d§l头衔系统 §7- 选择功能", ChestGUI.SINGLE_CHEST_SIZE, GUI_TITLE_MENU);
        
        // 填充背景
        gui.fillEmpty(Material.GRAY_STAINED_GLASS_PANE, " ");
        
        // 头衔商店
        gui.setItem(10, ChestGUI.createCategoryIcon(Material.CHEST, "头衔商店", 
            "购买新的头衔", "§a", true),
            (p, click) -> openTitleShop(p, 1));
        
        // 我的头衔
        gui.setItem(12, ChestGUI.createCategoryIcon(Material.BOOK, "我的头衔", 
            "查看和装备已拥有的头衔", "§b", true),
            (p, click) -> openMyTitles(p, 1));
        
        // 创建自定义头衔
        gui.setItem(14, ChestGUI.createCategoryIcon(Material.ANVIL, "创建自定义头衔", 
            "花费 " + CUSTOM_TITLE_COST + " " + economyManager.getCurrencyName() + " 创建专属头衔", "§e", true),
            (p, click) -> {
                p.closeInventory();
                p.sendMessage("§a请使用指令 §e/etitle <名称> §a来创建自定义头衔！");
            });
        
        // 管理员管理
        if (player.hasPermission("nekoessentialsx.title.admin")) {
            gui.setItem(16, ChestGUI.createCategoryIcon(Material.COMMAND_BLOCK, "管理员管理", 
                "管理所有头衔", "§c", true),
                (p, click) -> openTitleAdminMenu(p, 1));
        }
        
        // 添加导航栏
        gui.addNavigationBar(false, false, 
            (p, click) -> openMainMenu(p), null, null);
        
        gui.open();
        playerGUIs.put(player.getUniqueId(), gui);
        recordGUIHistory(player, GUI_TITLE_MENU);
    }
    
    /**
     * 打开头衔商店GUI
     * @param player 玩家
     * @param page 页码
     */
    public void openTitleShop(Player player, int page) {
        Map<String, TitleManager.Title> allTitles = titleManager.getTitles();
        List<String> availableTitles = new ArrayList<>();
        
        for (Map.Entry<String, TitleManager.Title> entry : allTitles.entrySet()) {
            if (entry.getValue().isEnabled() && 
                (player.hasPermission(entry.getValue().getPermission()) || 
                 player.hasPermission("nekoessentialsx.title.admin"))) {
                availableTitles.add(entry.getKey());
            }
        }
        
        int itemsPerPage = 45;
        int totalPages = (int) Math.ceil((double) availableTitles.size() / itemsPerPage);
        page = Math.max(1, Math.min(page, totalPages));
        
        ChestGUI gui = new ChestGUI(player, "§a§l头衔商店 §7- 第 " + page + "/" + totalPages + " 页", 
            ChestGUI.DOUBLE_CHEST_SIZE, GUI_TITLE_MENU);
        
        // 填充背景
        gui.fillEmpty(Material.GRAY_STAINED_GLASS_PANE, " ");
        
        int startIndex = (page - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, availableTitles.size());
        
        for (int i = startIndex; i < endIndex; i++) {
            String titleId = availableTitles.get(i);
            TitleManager.Title title = allTitles.get(titleId);
            int slot = i - startIndex;
            
            boolean owned = databaseManager.hasTitle(player.getName(), titleId);
            boolean equipped = titleId.equals(titleManager.getPlayerTitle(player.getName()));
            
            Material material = owned ? (equipped ? Material.DIAMOND_BLOCK : Material.DIAMOND) : Material.STONE;
            String color = owned ? (equipped ? "§a" : "§b") : "§7";
            
            List<String> lore = new ArrayList<>();
            lore.add("§7前缀: " + title.getPrefix());
            lore.add("§7ID: " + titleId);
            if (!owned) {
                lore.add("§6价格: " + TITLE_COST + " " + economyManager.getCurrencyName());
                lore.add("§a点击购买");
            } else if (equipped) {
                lore.add("§e当前已装备");
                lore.add("§c点击卸下");
            } else {
                lore.add("§a点击装备");
            }
            
            final String finalTitleId = titleId;
            final boolean finalOwned = owned;
            final boolean finalEquipped = equipped;
            final int currentPage = page;
            ItemStack titleItem = ChestGUI.createItem(material, 
                color + "§l" + title.getName() + (equipped ? " §a[已装备]" : ""), lore, color);
            
            gui.setItem(slot, titleItem, (p, click) -> {
                if (!finalOwned) {
                    // 购买头衔
                    buyTitle(p, finalTitleId);
                } else if (finalEquipped) {
                    // 卸下头衔
                    unequipTitle(p);
                } else {
                    // 装备头衔
                    equipTitle(p, finalTitleId);
                }
                openTitleShop(p, currentPage);
            });
        }
        
        // 添加导航栏
        gui.setCurrentPage(page);
        gui.setMaxPage(totalPages);
        final int navPage = page;
        gui.addNavigationBar(page > 1, page < totalPages,
            (p, click) -> openTitleMenu(p),
            (p, click) -> openTitleShop(p, navPage - 1),
            (p, click) -> openTitleShop(p, navPage + 1));
        
        gui.open();
        playerGUIs.put(player.getUniqueId(), gui);
    }
    
    /**
     * 打开我的头衔GUI
     * @param player 玩家
     * @param page 页码
     */
    public void openMyTitles(Player player, int page) {
        List<String> ownedTitles = new ArrayList<>();
        
        // 添加系统头衔
        for (String titleId : titleManager.getTitles().keySet()) {
            if (databaseManager.hasTitle(player.getName(), titleId)) {
                ownedTitles.add(titleId);
            }
        }
        
        // 添加自定义头衔
        List<String> customTitleIds = databaseManager.getPlayerCustomTitles(player.getName());
        for (String customTitleId : customTitleIds) {
            if (databaseManager.hasTitle(player.getName(), customTitleId)) {
                ownedTitles.add(customTitleId);
            }
        }
        
        int itemsPerPage = 45;
        int totalPages = (int) Math.ceil((double) ownedTitles.size() / itemsPerPage);
        page = Math.max(1, Math.min(page, totalPages));
        
        ChestGUI gui = new ChestGUI(player, "§b§l我的头衔 §7- 第 " + page + "/" + totalPages + " 页", 
            ChestGUI.DOUBLE_CHEST_SIZE, GUI_TITLE_MENU);
        
        // 填充背景
        gui.fillEmpty(Material.GRAY_STAINED_GLASS_PANE, " ");
        
        int startIndex = (page - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, ownedTitles.size());
        
        for (int i = startIndex; i < endIndex; i++) {
            String titleId = ownedTitles.get(i);
            int slot = i - startIndex;
            
            TitleManager.Title systemTitle = titleManager.getTitle(titleId);
            Object[] customTitleData = databaseManager.getCustomTitle(titleId);
            
            String titleName;
            String prefix;
            boolean enabled = true;
            
            if (systemTitle != null) {
                titleName = systemTitle.getName();
                prefix = systemTitle.getPrefix();
                enabled = systemTitle.isEnabled();
            } else if (customTitleData != null) {
                titleName = (String) customTitleData[0];
                prefix = (String) customTitleData[1];
                enabled = (boolean) customTitleData[4];
            } else {
                continue;
            }
            
            if (!enabled) continue;
            
            boolean equipped = titleId.equals(titleManager.getPlayerTitle(player.getName()));
            Material material = equipped ? Material.DIAMOND_BLOCK : Material.DIAMOND;
            String color = equipped ? "§a" : "§b";
            
            List<String> lore = new ArrayList<>();
            lore.add("§7前缀: " + prefix);
            lore.add("§7ID: " + titleId);
            if (equipped) {
                lore.add("§e当前已装备");
                lore.add("§c点击卸下");
            } else {
                lore.add("§a点击装备");
            }
            
            final String finalTitleId = titleId;
            final boolean finalEquipped = equipped;
            ItemStack titleItem = ChestGUI.createItem(material, 
                color + "§l" + titleName + (equipped ? " §a[已装备]" : ""), lore, color);
            
            final int currentPage = page;
            gui.setItem(slot, titleItem, (p, click) -> {
                if (finalEquipped) {
                    unequipTitle(p);
                } else {
                    equipTitle(p, finalTitleId);
                }
                openMyTitles(p, currentPage);
            });
        }
        
        // 添加导航栏
        gui.setCurrentPage(page);
        gui.setMaxPage(totalPages);
        final int navPage = page;
        gui.addNavigationBar(page > 1, page < totalPages,
            (p, click) -> openTitleMenu(p),
            (p, click) -> openMyTitles(p, navPage - 1),
            (p, click) -> openMyTitles(p, navPage + 1));
        
        gui.open();
        playerGUIs.put(player.getUniqueId(), gui);
    }
    
    /**
     * 打开头衔管理员菜单
     * @param player 玩家
     * @param page 页码
     */
    public void openTitleAdminMenu(Player player, int page) {
        if (!player.hasPermission("nekoessentialsx.title.admin")) {
            player.sendMessage("§c你没有权限！");
            return;
        }
        
        Map<String, TitleManager.Title> allTitles = titleManager.getTitles();
        List<String> titleIds = new ArrayList<>(allTitles.keySet());
        
        int itemsPerPage = 45;
        int totalPages = (int) Math.ceil((double) titleIds.size() / itemsPerPage);
        page = Math.max(1, Math.min(page, totalPages));
        
        ChestGUI gui = new ChestGUI(player, "§c§l头衔管理 §7- 第 " + page + "/" + totalPages + " 页", 
            ChestGUI.DOUBLE_CHEST_SIZE, GUI_TITLE_MENU);
        
        // 填充背景
        gui.fillEmpty(Material.GRAY_STAINED_GLASS_PANE, " ");
        
        int startIndex = (page - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, titleIds.size());
        
        for (int i = startIndex; i < endIndex; i++) {
            String titleId = titleIds.get(i);
            TitleManager.Title title = allTitles.get(titleId);
            int slot = i - startIndex;
            
            Material material = title.isEnabled() ? Material.DIAMOND_BLOCK : Material.COAL_BLOCK;
            String color = title.isEnabled() ? "§a" : "§c";
            
            List<String> lore = new ArrayList<>();
            lore.add("§7前缀: " + title.getPrefix());
            lore.add("§7ID: " + titleId);
            lore.add("§7状态: " + (title.isEnabled() ? "§a启用" : "§c禁用"));
            lore.add("§a左键编辑");
            lore.add("§c右键删除");
            lore.add("§eShift+左键切换状态");
            
            final String finalTitleId = titleId;
            final boolean finalEnabled = title.isEnabled();
            final int currentPage = page;
            ItemStack titleItem = ChestGUI.createItem(material, 
                color + "§l" + title.getName(), lore, color);
            
            gui.setItem(slot, titleItem, (p, click) -> {
                if (click == ChestGUI.ClickType.RIGHT) {
                    // 删除
                    titleManager.deleteTitle(finalTitleId);
                    p.sendMessage("§a头衔已删除！");
                } else if (click == ChestGUI.ClickType.SHIFT_LEFT) {
                    // 切换状态
                    titleManager.toggleTitleEnabled(finalTitleId, !finalEnabled);
                    p.sendMessage("§a头衔状态已切换！");
                } else {
                    // 编辑
                    p.closeInventory();
                    p.sendMessage("§a请使用指令编辑头衔！");
                }
                openTitleAdminMenu(p, currentPage);
            });
        }
        
        // 添加创建按钮
        if (endIndex < 45) {
            gui.setItem(endIndex, ChestGUI.createItem(Material.GOLD_BLOCK, "§a§l创建新头衔",
                List.of("§7点击创建一个新的系统头衔"), "§a"),
                (p, click) -> {
                    p.closeInventory();
                    p.sendMessage("§a请使用指令 §e/title admin create §a来创建新头衔！");
                });
        }
        
        // 添加导航栏
        gui.setCurrentPage(page);
        gui.setMaxPage(totalPages);
        final int navPage = page;
        gui.addNavigationBar(page > 1, page < totalPages,
            (p, click) -> openTitleMenu(p),
            (p, click) -> openTitleAdminMenu(p, navPage - 1),
            (p, click) -> openTitleAdminMenu(p, navPage + 1));
        
        gui.open();
        playerGUIs.put(player.getUniqueId(), gui);
    }
    
    /**
     * 打开经济菜单GUI
     * @param player 玩家
     */
    public void openEconomyMenu(Player player) {
        ChestGUI gui = new ChestGUI(player, "§e§l经济系统 §7- 管理你的财富", ChestGUI.SINGLE_CHEST_SIZE, GUI_ECONOMY_MENU);
        
        // 填充背景
        gui.fillEmpty(Material.GRAY_STAINED_GLASS_PANE, " ");
        
        double balance = economyManager.getBalance(player);
        
        // 查看余额
        gui.setItem(10, ChestGUI.createItem(Material.GOLD_NUGGET, "§e§l查看余额",
            List.of("§7当前余额: §6" + economyManager.format(balance), "§7点击刷新"), "§e"),
            (p, click) -> {
                double newBalance = economyManager.getBalance(p);
                p.sendMessage("§a你的余额: §6" + economyManager.format(newBalance));
                openEconomyMenu(p);
            });
        
        // 转账
        gui.setItem(12, ChestGUI.createItem(Material.GOLD_INGOT, "§6§l转账",
            List.of("§7转账给其他玩家", "§7点击选择玩家"), "§6"),
            (p, click) -> openPlayerSelector(p, "转账", (target, amount) -> {
                p.closeInventory();
                p.sendMessage("§a请使用指令 §e/money pay " + target.getName() + " <金额> §a来完成转账！");
            }));
        
        // 查看其他玩家余额
        gui.setItem(14, ChestGUI.createItem(Material.PAPER, "§b§l查看他人余额",
            List.of("§7查看其他玩家的余额"), "§b"),
            (p, click) -> openPlayerSelector(p, "查看余额", (target, amount) -> {
                double targetBalance = economyManager.getBalance(target);
                p.sendMessage("§a玩家 §e" + target.getName() + " §a的余额: §6" + economyManager.format(targetBalance));
            }));
        
        // 管理员功能
        if (player.hasPermission("nekoessentialsx.economy.admin")) {
            gui.setItem(16, ChestGUI.createCategoryIcon(Material.COMMAND_BLOCK, "管理员经济", 
                "管理其他玩家的经济", "§c", true),
                (p, click) -> openEconomyAdminMenu(p));
        }
        
        // 添加导航栏
        gui.addNavigationBar(false, false, 
            (p, click) -> openMainMenu(p), null, null);
        
        gui.open();
        playerGUIs.put(player.getUniqueId(), gui);
        recordGUIHistory(player, GUI_ECONOMY_MENU);
    }
    
    /**
     * 打开经济管理员菜单
     * @param player 玩家
     */
    public void openEconomyAdminMenu(Player player) {
        if (!player.hasPermission("nekoessentialsx.economy.admin")) {
            player.sendMessage("§c你没有权限！");
            return;
        }
        
        ChestGUI gui = new ChestGUI(player, "§c§l经济管理 §7- 管理员功能", ChestGUI.SINGLE_CHEST_SIZE, GUI_ECONOMY_MENU);
        
        // 填充背景
        gui.fillEmpty(Material.GRAY_STAINED_GLASS_PANE, " ");
        
        // 充值
        gui.setItem(10, ChestGUI.createItem(Material.GREEN_CONCRETE, "§a§l充值",
            List.of("§7为玩家充值"), "§a"),
            (p, click) -> {
                p.closeInventory();
                p.sendMessage("§a请使用指令 §e/money give <玩家> <金额> §a来充值！");
            });
        
        // 扣款
        gui.setItem(12, ChestGUI.createItem(Material.RED_CONCRETE, "§c§l扣款",
            List.of("§7从玩家账户扣款"), "§c"),
            (p, click) -> {
                p.closeInventory();
                p.sendMessage("§a请使用指令 §e/money take <玩家> <金额> §a来扣款！");
            });
        
        // 设置余额
        gui.setItem(14, ChestGUI.createItem(Material.YELLOW_CONCRETE, "§e§l设置余额",
            List.of("§7直接设置玩家余额"), "§e"),
            (p, click) -> {
                p.closeInventory();
                p.sendMessage("§a请使用指令 §e/money set <玩家> <金额> §a来设置余额！");
            });
        
        // 更改货币名称
        gui.setItem(16, ChestGUI.createItem(Material.NAME_TAG, "§d§l更改货币名称",
            List.of("§7更改游戏中的货币名称", "§7当前: §e" + economyManager.getCurrencyName()), "§d"),
            (p, click) -> {
                p.closeInventory();
                p.sendMessage("§a请使用指令 §e/money name <新名称> §a来更改货币名称！");
            });
        
        // 添加导航栏
        gui.addNavigationBar(false, false, 
            (p, click) -> openEconomyMenu(p), null, null);
        
        gui.open();
        playerGUIs.put(player.getUniqueId(), gui);
    }
    
    /**
     * 打开传送菜单GUI
     * @param player 玩家
     */
    public void openTPMenu(Player player) {
        ChestGUI gui = new ChestGUI(player, "§b§l传送系统 §7- 管理传送请求", ChestGUI.SINGLE_CHEST_SIZE, GUI_TP_MENU);
        
        // 填充背景
        gui.fillEmpty(Material.GRAY_STAINED_GLASS_PANE, " ");
        
        // 发送传送请求
        gui.setItem(10, ChestGUI.createItem(Material.PAPER, "§a§l发送传送请求",
            List.of("§7请求传送到其他玩家身边"), "§a"),
            (p, click) -> openPlayerSelector(p, "发送传送请求", (target, amount) -> {
                p.closeInventory();
                sendTPARequest(p, target);
            }));
        
        // 查看收到的请求
        gui.setItem(12, ChestGUI.createItem(Material.ENCHANTED_BOOK, "§b§l收到的请求",
            List.of("§7查看并处理收到的传送请求"), "§b"),
            (p, click) -> openTPRequestsMenu(p));
        
        // 取消已发送的请求
        gui.setItem(14, ChestGUI.createItem(Material.BARRIER, "§c§l取消请求",
            List.of("§7取消你发送的传送请求"), "§c"),
            (p, click) -> {
                p.closeInventory();
                p.sendMessage("§a请使用指令 §e/tpacancel [玩家] §a来取消请求！");
            });
        
        // 添加导航栏
        gui.addNavigationBar(false, false, 
            (p, click) -> openMainMenu(p), null, null);
        
        gui.open();
        playerGUIs.put(player.getUniqueId(), gui);
        recordGUIHistory(player, GUI_TP_MENU);
    }
    
    /**
     * 打开传送请求菜单
     * @param player 玩家
     */
    public void openTPRequestsMenu(Player player) {
        ChestGUI gui = new ChestGUI(player, "§b§l传送请求 §7- 接受或拒绝", ChestGUI.DOUBLE_CHEST_SIZE, GUI_TP_MENU);
        
        // 填充背景
        gui.fillEmpty(Material.GRAY_STAINED_GLASS_PANE, " ");
        
        List<com.nekoessentialsx.tpa.TPAManager.TPARequest> requests = tpaManager.getReceivedRequests(player);
        
        int slot = 0;
        for (com.nekoessentialsx.tpa.TPAManager.TPARequest request : requests) {
            if (slot >= 45) break;
            
            Player sender = request.getSender();
            ItemStack requestItem = ChestGUI.createItem(Material.PAPER, "§e§l" + sender.getName(),
                List.of("§7请求传送到你身边", "§a左键接受", "§c右键拒绝"), "§e");
            
            gui.setItem(slot, requestItem, (p, click) -> {
                if (click == ChestGUI.ClickType.RIGHT) {
                    denyTPARequest(p, sender.getName());
                } else {
                    acceptTPARequest(p, sender.getName());
                }
                openTPRequestsMenu(p);
            });
            slot++;
        }
        
        // 添加导航栏
        gui.addNavigationBar(false, false, 
            (p, click) -> openTPMenu(p), null, null);
        
        gui.open();
        playerGUIs.put(player.getUniqueId(), gui);
    }
    
    /**
     * 打开玩家选择器GUI
     * @param player 玩家
     * @param actionName 动作名称
     * @param callback 回调函数
     */
    public void openPlayerSelector(Player player, String actionName, PlayerSelectorCallback callback) {
        Collection<Player> onlinePlayers = (Collection<Player>) plugin.getServer().getOnlinePlayers();
        int itemsPerPage = 45;
        int totalPages = (int) Math.ceil((double) onlinePlayers.size() / itemsPerPage);
        
        openPlayerSelector(player, actionName, 1, callback);
    }
    
    /**
     * 打开玩家选择器GUI（带分页）
     * @param player 玩家
     * @param actionName 动作名称
     * @param page 页码
     * @param callback 回调函数
     */
    public void openPlayerSelector(Player player, String actionName, int page, PlayerSelectorCallback callback) {
        Collection<Player> onlinePlayers = new ArrayList<>((Collection<Player>) plugin.getServer().getOnlinePlayers());
        List<Player> playerList = new ArrayList<>(onlinePlayers);
        
        // 移除自己
        playerList.remove(player);
        
        int itemsPerPage = 45;
        int totalPages = Math.max(1, (int) Math.ceil((double) playerList.size() / itemsPerPage));
        page = Math.max(1, Math.min(page, totalPages));
        
        ChestGUI gui = new ChestGUI(player, "§a§l选择玩家 §7- " + actionName + " (第 " + page + "/" + totalPages + " 页)", 
            ChestGUI.DOUBLE_CHEST_SIZE, GUI_PLAYER_LIST);
        
        // 填充背景
        gui.fillEmpty(Material.GRAY_STAINED_GLASS_PANE, " ");
        
        int startIndex = (page - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, playerList.size());
        
        for (int i = startIndex; i < endIndex; i++) {
            Player target = playerList.get(i);
            int slot = i - startIndex;
            final Player finalTarget = target;
            
            ItemStack playerItem = ChestGUI.createItem(Material.PLAYER_HEAD, "§a§l" + target.getName(),
                List.of("§7点击选择此玩家"), "§a");
            
            gui.setItem(slot, playerItem, (p, click) -> callback.onSelect(finalTarget, 0));
        }
        
        // 添加导航栏
        gui.setCurrentPage(page);
        gui.setMaxPage(totalPages);
        final int currentPage = page;
        final String currentActionName = actionName;
        final PlayerSelectorCallback currentCallback = callback;
        gui.addNavigationBar(page > 1, page < totalPages,
            (p, click) -> goBack(p),
            (p, click) -> openPlayerSelector(p, currentActionName, currentPage - 1, currentCallback),
            (p, click) -> openPlayerSelector(p, currentActionName, currentPage + 1, currentCallback));
        
        gui.open();
        playerGUIs.put(player.getUniqueId(), gui);
    }
    
    /**
     * 打开确认GUI
     * @param player 玩家
     * @param title 标题
     * @param message 确认消息
     * @param onConfirm 确认回调
     * @param onCancel 取消回调
     */
    public void openConfirmGUI(Player player, String title, String message, 
                                ConfirmCallback onConfirm, ConfirmCallback onCancel) {
        ChestGUI gui = new ChestGUI(player, "§c§l确认: " + title, ChestGUI.SINGLE_CHEST_SIZE, GUI_CONFIRM);
        
        // 填充背景
        gui.fillEmpty(Material.GRAY_STAINED_GLASS_PANE, " ");
        
        // 确认信息
        gui.setItem(13, ChestGUI.createItem(Material.PAPER, "§e§l" + message,
            List.of("§7请确认你的操作"), "§e"), null);
        
        // 确认按钮
        gui.setItem(11, ChestGUI.createItem(Material.GREEN_CONCRETE, "§a§l确认",
            List.of("§7点击确认操作"), "§a"),
            (p, click) -> onConfirm.onConfirm(p));
        
        // 取消按钮
        gui.setItem(15, ChestGUI.createItem(Material.RED_CONCRETE, "§c§l取消",
            List.of("§7点击取消操作"), "§c"),
            (p, click) -> onCancel.onCancel(p));
        
        gui.open();
        playerGUIs.put(player.getUniqueId(), gui);
    }
    
    /**
     * 打开玩家列表GUI
     * @param player 玩家
     * @param page 页码
     */
    public void openPlayerList(Player player, int page) {
        Collection<Player> onlinePlayers = new ArrayList<>((Collection<Player>) plugin.getServer().getOnlinePlayers());
        List<Player> playerList = new ArrayList<>(onlinePlayers);
        
        int itemsPerPage = 45;
        int totalPages = Math.max(1, (int) Math.ceil((double) playerList.size() / itemsPerPage));
        page = Math.max(1, Math.min(page, totalPages));
        
        ChestGUI gui = new ChestGUI(player, "§a§l在线玩家 §7- 第 " + page + "/" + totalPages + " 页", 
            ChestGUI.DOUBLE_CHEST_SIZE, GUI_PLAYER_LIST);
        
        // 填充背景
        gui.fillEmpty(Material.GRAY_STAINED_GLASS_PANE, " ");
        
        int startIndex = (page - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, playerList.size());
        
        for (int i = startIndex; i < endIndex; i++) {
            Player target = playerList.get(i);
            int slot = i - startIndex;
            final Player finalTarget = target;
            
            String afkStatus = plugin.getAFKManager().isAFK(target) ? " §7[AFK]" : "";
            ItemStack playerItem = ChestGUI.createItem(Material.PLAYER_HEAD, "§a§l" + target.getName() + afkStatus,
                List.of("§7点击查看详情", "§7世界: §f" + target.getWorld().getName()), "§a");
            
            gui.setItem(slot, playerItem, (p, click) -> {
                p.sendMessage("§a玩家: §e" + finalTarget.getName());
                p.sendMessage("§a世界: §e" + finalTarget.getWorld().getName());
                p.sendMessage("§a坐标: §e" + String.format("%.1f, %.1f, %.1f", 
                    finalTarget.getLocation().getX(), finalTarget.getLocation().getY(), finalTarget.getLocation().getZ()));
            });
        }
        
        // 添加导航栏
        gui.setCurrentPage(page);
        gui.setMaxPage(totalPages);
        final int currentPage = page;
        gui.addNavigationBar(page > 1, page < totalPages,
            (p, click) -> openMainMenu(p),
            (p, click) -> openPlayerList(p, currentPage - 1),
            (p, click) -> openPlayerList(p, currentPage + 1));
        
        gui.open();
        playerGUIs.put(player.getUniqueId(), gui);
        recordGUIHistory(player, GUI_PLAYER_LIST);
    }
    
    /**
     * 打开插件信息菜单
     * @param player 玩家
     */
    public void openPluginInfoMenu(Player player) {
        ChestGUI gui = new ChestGUI(player, "§f§l插件信息", ChestGUI.SINGLE_CHEST_SIZE, GUI_MAIN_MENU);
        
        // 填充背景
        gui.fillEmpty(Material.GRAY_STAINED_GLASS_PANE, " ");
        
        // 版本信息
        gui.setItem(10, ChestGUI.createItem(Material.BOOK, "§a§l查看版本",
            List.of("§7当前版本: §e1.0-RELEASE"), "§a"),
            (p, click) -> {
                p.sendMessage("§aNekoEssentialX 当前版本: §e1.0-RELEASE");
            });
        
        // 重载配置
        gui.setItem(12, ChestGUI.createItem(Material.COMMAND_BLOCK, "§6§l重载配置",
            List.of("§7重载插件配置文件"), "§6"),
            (p, click) -> {
                plugin.reloadConfig();
                p.sendMessage("§a插件配置已重载！");
            });
        
        // 添加导航栏
        gui.addNavigationBar(false, false, 
            (p, click) -> openMainMenu(p), null, null);
        
        gui.open();
        playerGUIs.put(player.getUniqueId(), gui);
    }
    
    /**
     * 获取玩家的当前GUI
     * @param player 玩家
     * @return GUI对象，如果没有则返回null
     */
    public ChestGUI getPlayerGUI(Player player) {
        return playerGUIs.get(player.getUniqueId());
    }
    
    /**
     * 移除玩家的GUI
     * @param player 玩家
     */
    public void removePlayerGUI(Player player) {
        playerGUIs.remove(player.getUniqueId());
    }
    
    /**
     * 记录GUI历史
     * @param player 玩家
     * @param guiId GUI ID
     */
    private void recordGUIHistory(Player player, String guiId) {
        playerGUIHistory.computeIfAbsent(player.getUniqueId(), k -> new Stack<>()).push(guiId);
    }
    
    /**
     * 返回上一级菜单
     * @param player 玩家
     */
    public void goBack(Player player) {
        Stack<String> history = playerGUIHistory.get(player.getUniqueId());
        if (history != null && !history.isEmpty()) {
            history.pop(); // 移除当前
            if (!history.isEmpty()) {
                String previousGUI = history.peek();
                switch (previousGUI) {
                    case GUI_MAIN_MENU -> openMainMenu(player);
                    case GUI_HOME_MENU -> openHomeMenu(player);
                    case GUI_WARP_MENU -> openWarpMenu(player, 1);
                    case GUI_KIT_MENU -> openKitMenu(player, 1);
                    case GUI_TITLE_MENU -> openTitleMenu(player);
                    case GUI_ECONOMY_MENU -> openEconomyMenu(player);
                    case GUI_TP_MENU -> openTPMenu(player);
                    case GUI_PLAYER_LIST -> openPlayerList(player, 1);
                    default -> openMainMenu(player);
                }
            } else {
                openMainMenu(player);
            }
        } else {
            openMainMenu(player);
        }
    }
    
    // ==================== 功能方法 ====================
    
    /**
     * 传送到家
     * @param player 玩家
     * @param homeName 家名称
     */
    public void teleportToHome(Player player, String homeName) {
        Object[] homeInfo = databaseManager.getPlayerHome(player.getName(), homeName);
        if (homeInfo == null) {
            player.sendMessage("§c找不到家: " + homeName);
            return;
        }
        
        String worldName = (String) homeInfo[0];
        double x = (double) homeInfo[1];
        double y = (double) homeInfo[2];
        double z = (double) homeInfo[3];
        float yaw = (float) homeInfo[4];
        float pitch = (float) homeInfo[5];
        
        World world = plugin.getServer().getWorld(worldName);
        if (world == null) {
            player.sendMessage("§c找不到世界: " + worldName);
            return;
        }
        
        Location homeLocation = new Location(world, x, y, z, yaw, pitch);
        player.teleport(homeLocation);
        player.sendMessage("§a已传送到家: §e" + homeName);
    }
    
    /**
     * 传送到传送点
     * @param player 玩家
     * @param warpName 传送点名称
     */
    public void teleportToWarp(Player player, String warpName) {
        boolean success = warpManager.teleportToWarp(player, warpName);
        if (success) {
            player.sendMessage("§a已传送到传送点: §e" + warpName);
        } else {
            player.sendMessage("§c传送失败！");
        }
    }
    
    /**
     * 领取工具包
     * @param player 玩家
     * @param kitName 工具包名称
     */
    public void claimKit(Player player, String kitName) {
        kitManager.giveKit(player, kitName);
    }
    
    /**
     * 购买头衔
     * @param player 玩家
     * @param titleId 头衔ID
     */
    public void buyTitle(Player player, String titleId) {
        TitleManager.Title title = titleManager.getTitle(titleId);
        if (title == null || !title.isEnabled()) {
            player.sendMessage("§c该头衔不存在或已禁用！");
            return;
        }
        
        if (databaseManager.hasTitle(player.getName(), titleId)) {
            player.sendMessage("§e你已经拥有这个头衔了！");
            return;
        }
        
        double balance = economyManager.getBalance(player);
        if (balance < TITLE_COST) {
            player.sendMessage("§c余额不足！需要 " + TITLE_COST + " " + economyManager.getCurrencyName());
            return;
        }
        
        if (economyManager.withdrawPlayer(player, TITLE_COST)) {
            databaseManager.addTitleToInventory(player.getName(), titleId, false);
            player.sendMessage("§a购买成功！你已获得头衔: §b" + title.getName());
        } else {
            player.sendMessage("§c购买失败！");
        }
    }
    
    /**
     * 装备头衔
     * @param player 玩家
     * @param titleId 头衔ID
     */
    public void equipTitle(Player player, String titleId) {
        if (!databaseManager.hasTitle(player.getName(), titleId)) {
            player.sendMessage("§c你没有这个头衔！");
            return;
        }
        
        titleManager.updatePlayerTitle(player, titleId);
        
        TitleManager.Title title = titleManager.getTitle(titleId);
        Object[] customTitleData = databaseManager.getCustomTitle(titleId);
        String titleName = title != null ? title.getName() : (customTitleData != null ? (String) customTitleData[0] : titleId);
        
        player.sendMessage("§a头衔装备成功！当前头衔: §b" + titleName);
    }
    
    /**
     * 卸下头衔
     * @param player 玩家
     */
    public void unequipTitle(Player player) {
        titleManager.clearPlayerTitle(player.getName());
        player.sendMessage("§a头衔已卸下！");
    }
    
    /**
     * 发送传送请求
     * @param player 发送者
     * @param target 目标玩家
     */
    public void sendTPARequest(Player player, Player target) {
        tpaManager.sendRequest(player, target);
    }
    
    /**
     * 接受传送请求
     * @param player 接受者
     * @param senderName 发送者名称
     */
    public void acceptTPARequest(Player player, String senderName) {
        tpaManager.acceptRequest(player, senderName);
    }
    
    /**
     * 拒绝传送请求
     * @param player 拒绝者
     * @param senderName 发送者名称
     */
    public void denyTPARequest(Player player, String senderName) {
        tpaManager.denyRequest(player, senderName);
    }

    // ==================== 回调接口 ====================
    
    /**
     * 玩家选择器回调接口
     */
    @FunctionalInterface
    public interface PlayerSelectorCallback {
        void onSelect(Player target, double amount);
    }
    
    /**
     * 确认回调接口
     */
    @FunctionalInterface
    public interface ConfirmCallback {
        void onConfirm(Player player);
        default void onCancel(Player player) {}
    }
}
