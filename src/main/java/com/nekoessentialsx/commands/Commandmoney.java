package com.nekoessentialsx.commands;

import com.nekoessentialsx.NekoEssentialX;
import com.nekoessentialsx.catstyle.CatChatProcessor;
import com.nekoessentialsx.economy.EconomyManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Commandmoney implements CommandExecutor, TabCompleter {
    private final NekoEssentialX plugin;
    private final EconomyManager economyManager;

    public Commandmoney(NekoEssentialX plugin) {
        this.plugin = plugin;
        this.economyManager = plugin.getEconomyManager();
        // 注册Tab补全器到money命令及其所有别名
        plugin.getCommand("money").setTabCompleter(this);
        plugin.getCommand("eco").setTabCompleter(this);
        plugin.getCommand("bal").setTabCompleter(this);
        plugin.getCommand("cash").setTabCompleter(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try {
            if (!economyManager.isEnabled()) {
                String message = "§c经济系统已禁用！喵~";
                if (sender instanceof Player) {
                    // 使用猫娘风格处理消息
                    CatChatProcessor processor = CatChatProcessor.getInstance();
                    if (processor != null) {
                        processor.sendCatStyleMessage((Player) sender, message);
                        return true;
                    }
                }
                sender.sendMessage(message);
                return true;
            }

            if (args.length < 1) {
                // 显示余额
                checkBalance(sender);
                return true;
            }

            final String subCommand = args[0].toLowerCase();

            switch (subCommand) {
                case "balance":
                case "bal":
                case "me":
                    checkBalance(sender);
                    break;
                case "pay":
                    payPlayer(sender, args);
                    break;
                case "deposit":
                    deposit(sender, args);
                    break;
                case "withdraw":
                    withdraw(sender, args);
                    break;
                case "give":
                    giveMoney(sender, args);
                    break;
                case "take":
                    takeMoney(sender, args);
                    break;
                case "name":
                    setCurrencyName(sender, args);
                    break;
                case "help":
                    showHelp(sender, label);
                    break;
                default:
                    sender.sendMessage("§c未知的子命令: §e" + args[0] + "§c喵~");
                    sender.sendMessage("§c请使用 §e/" + label + " help §c查看可用命令喵~");
                    break;
            }
        } catch (Exception e) {
            sender.sendMessage("§c执行命令时发生错误！喵~");
            plugin.getLogger().severe("执行money命令时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        try {
            if (args.length == 1) {
                // 补全子命令
                String[] subCommands = {"balance", "bal", "me", "pay", "deposit", "withdraw", "give", "take", "name", "help"};
                for (String subCommand : subCommands) {
                    if (subCommand.startsWith(args[0].toLowerCase())) {
                        completions.add(subCommand);
                    }
                }
            } else if (args.length >= 2) {
                String subCommand = args[0].toLowerCase();

                switch (subCommand) {
                    case "pay":
                    case "give":
                    case "take":
                        // 补全玩家名
                        if (args.length == 2) {
                            for (Player player : plugin.getServer().getOnlinePlayers()) {
                                if (player.getName().startsWith(args[1])) {
                                    completions.add(player.getName());
                                }
                            }
                        }
                        break;
                    case "deposit":
                    case "withdraw":
                        // 补全数字
                        if (args.length == 2) {
                            completions.add("100");
                            completions.add("1000");
                            completions.add("5000");
                        }
                        break;
                    case "name":
                        // 补全货币名称示例
                        if (args.length == 2) {
                            completions.add("金币");
                            completions.add("点券");
                            completions.add("钻石");
                            completions.add("硬币");
                        }
                        break;
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Tab补全时发生错误: " + e.getMessage());
        }

        Collections.sort(completions);
        return completions;
    }

    /**
     * 显示余额
     */
    private void checkBalance(CommandSender sender) {
        Player target;
        if (sender instanceof Player) {
            target = (Player) sender;
        } else {
            sender.sendMessage("§c控制台不能查看余额！喵~");
            return;
        }

        double balance = economyManager.getBalance(target);
        String message = "§a你的余额: §6" + economyManager.format(balance) + "§a喵~";

        if (sender instanceof Player) {
            CatChatProcessor processor = CatChatProcessor.getInstance();
            if (processor != null) {
                processor.sendCatStyleMessage((Player) sender, message);
                return;
            }
        }

        sender.sendMessage(message);
    }

    /**
     * 支付给其他玩家
     */
    private void payPlayer(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§c只有玩家可以使用这个命令！喵~");
            return;
        }

        if (args.length < 3) {
            String message = "§c用法: /money pay <玩家> <金额>喵~";
            Player player = (Player) sender;
            CatChatProcessor processor = CatChatProcessor.getInstance();
            if (processor != null) {
                processor.sendCatStyleMessage(player, message);
            } else {
                sender.sendMessage(message);
            }
            return;
        }

        Player payer = (Player) sender;
        Player recipient = plugin.getServer().getPlayer(args[1]);

        if (recipient == null) {
            String message = "§c找不到这个玩家！喵~";
            CatChatProcessor processor = CatChatProcessor.getInstance();
            if (processor != null) {
                processor.sendCatStyleMessage(payer, message);
            } else {
                sender.sendMessage(message);
            }
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            String message = "§c请输入有效的金额！喵~";
            CatChatProcessor processor = CatChatProcessor.getInstance();
            if (processor != null) {
                processor.sendCatStyleMessage(payer, message);
            } else {
                sender.sendMessage(message);
            }
            return;
        }

        if (amount <= 0) {
            String message = "§c金额必须大于0！喵~";
            CatChatProcessor processor = CatChatProcessor.getInstance();
            if (processor != null) {
                processor.sendCatStyleMessage(payer, message);
            } else {
                sender.sendMessage(message);
            }
            return;
        }

        if (economyManager.hasBalance(payer, amount)) {
            if (economyManager.transfer(payer, recipient, amount)) {
                String payerMessage = "§a已成功支付 §e" + economyManager.format(amount) + " §a给 §e" + recipient.getName() + "§a喵~";
                String recipientMessage = "§a你收到了来自 §e" + payer.getName() + " §a的 §e" + economyManager.format(amount) + "§a喵~";

                CatChatProcessor processor = CatChatProcessor.getInstance();
                if (processor != null) {
                    processor.sendCatStyleMessage(payer, payerMessage);
                    processor.sendCatStyleMessage(recipient, recipientMessage);
                } else {
                    payer.sendMessage(payerMessage);
                    recipient.sendMessage(recipientMessage);
                }
            } else {
                String message = "§c支付失败！喵~";
                CatChatProcessor processor = CatChatProcessor.getInstance();
                if (processor != null) {
                    processor.sendCatStyleMessage(payer, message);
                } else {
                    sender.sendMessage(message);
                }
            }
        } else {
            String message = "§c你的余额不足！喵~";
            CatChatProcessor processor = CatChatProcessor.getInstance();
            if (processor != null) {
                processor.sendCatStyleMessage(payer, message);
            } else {
                sender.sendMessage(message);
            }
        }
    }

    /**
     * 充值（管理员）
     */
    private void deposit(CommandSender sender, String[] args) {
        if (!sender.hasPermission("catessentials.economy.admin")) {
            String message = "§c你没有权限使用这个命令！";
            if (sender instanceof Player) {
                CatChatProcessor processor = CatChatProcessor.getInstance();
                if (processor != null) {
                    processor.sendCatStyleMessage((Player) sender, message);
                    return;
                }
            }
            sender.sendMessage(message);
            return;
        }

        if (args.length < 3) {
            String message = "§c用法: /money deposit <玩家> <金额>";
            if (sender instanceof Player) {
                CatChatProcessor processor = CatChatProcessor.getInstance();
                if (processor != null) {
                    processor.sendCatStyleMessage((Player) sender, message);
                    return;
                }
            }
            sender.sendMessage(message);
            return;
        }

        Player target = plugin.getServer().getPlayer(args[1]);
        if (target == null) {
            String message = "§c找不到这个玩家！";
            if (sender instanceof Player) {
                CatChatProcessor processor = CatChatProcessor.getInstance();
                if (processor != null) {
                    processor.sendCatStyleMessage((Player) sender, message);
                    return;
                }
            }
            sender.sendMessage(message);
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            String message = "§c请输入有效的金额！";
            if (sender instanceof Player) {
                CatChatProcessor processor = CatChatProcessor.getInstance();
                if (processor != null) {
                    processor.sendCatStyleMessage((Player) sender, message);
                    return;
                }
            }
            sender.sendMessage(message);
            return;
        }

        if (amount <= 0) {
            String message = "§c金额必须大于0！";
            if (sender instanceof Player) {
                CatChatProcessor processor = CatChatProcessor.getInstance();
                if (processor != null) {
                    processor.sendCatStyleMessage((Player) sender, message);
                    return;
                }
            }
            sender.sendMessage(message);
            return;
        }

        if (economyManager.depositPlayer(target, amount)) {
            String senderMessage = "§a已成功充值 §e" + economyManager.format(amount) + " §a给 §e" + target.getName();
            String targetMessage = "§a你收到了 §e" + economyManager.format(amount) + " §a的充值！";

            if (sender instanceof Player) {
                CatChatProcessor processor = CatChatProcessor.getInstance();
                if (processor != null) {
                    processor.sendCatStyleMessage((Player) sender, senderMessage);
                    processor.sendCatStyleMessage(target, targetMessage);
                    return;
                }
            }

            sender.sendMessage(senderMessage);
            target.sendMessage(targetMessage);
        } else {
            String message = "§c充值失败！";
            if (sender instanceof Player) {
                CatChatProcessor processor = CatChatProcessor.getInstance();
                if (processor != null) {
                    processor.sendCatStyleMessage((Player) sender, message);
                    return;
                }
            }
            sender.sendMessage(message);
        }
    }

    /**
     * 提款（管理员）
     */
    private void withdraw(CommandSender sender, String[] args) {
        if (!sender.hasPermission("catessentials.economy.admin")) {
            String message = "§c你没有权限使用这个命令！";
            if (sender instanceof Player) {
                CatChatProcessor processor = CatChatProcessor.getInstance();
                if (processor != null) {
                    processor.sendCatStyleMessage((Player) sender, message);
                    return;
                }
            }
            sender.sendMessage(message);
            return;
        }

        if (args.length < 3) {
            String message = "§c用法: /money withdraw <玩家> <金额>";
            if (sender instanceof Player) {
                CatChatProcessor processor = CatChatProcessor.getInstance();
                if (processor != null) {
                    processor.sendCatStyleMessage((Player) sender, message);
                    return;
                }
            }
            sender.sendMessage(message);
            return;
        }

        Player target = plugin.getServer().getPlayer(args[1]);
        if (target == null) {
            String message = "§c找不到这个玩家！";
            if (sender instanceof Player) {
                CatChatProcessor processor = CatChatProcessor.getInstance();
                if (processor != null) {
                    processor.sendCatStyleMessage((Player) sender, message);
                    return;
                }
            }
            sender.sendMessage(message);
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            String message = "§c请输入有效的金额！";
            if (sender instanceof Player) {
                CatChatProcessor processor = CatChatProcessor.getInstance();
                if (processor != null) {
                    processor.sendCatStyleMessage((Player) sender, message);
                    return;
                }
            }
            sender.sendMessage(message);
            return;
        }

        if (amount <= 0) {
            String message = "§c金额必须大于0！";
            if (sender instanceof Player) {
                CatChatProcessor processor = CatChatProcessor.getInstance();
                if (processor != null) {
                    processor.sendCatStyleMessage((Player) sender, message);
                    return;
                }
            }
            sender.sendMessage(message);
            return;
        }

        if (economyManager.withdrawPlayer(target, amount)) {
            String senderMessage = "§a已成功从 §e" + target.getName() + " §a提取 §e" + economyManager.format(amount);
            String targetMessage = "§a你的账户被提取了 §e" + economyManager.format(amount) + "！";

            if (sender instanceof Player) {
                CatChatProcessor processor = CatChatProcessor.getInstance();
                if (processor != null) {
                    processor.sendCatStyleMessage((Player) sender, senderMessage);
                    processor.sendCatStyleMessage(target, targetMessage);
                    return;
                }
            }

            sender.sendMessage(senderMessage);
            target.sendMessage(targetMessage);
        } else {
            String message = "§c提款失败！";
            if (sender instanceof Player) {
                CatChatProcessor processor = CatChatProcessor.getInstance();
                if (processor != null) {
                    processor.sendCatStyleMessage((Player) sender, message);
                    return;
                }
            }
            sender.sendMessage(message);
        }
    }

    /**
     * 给予玩家金钱（管理员）
     */
    private void giveMoney(CommandSender sender, String[] args) {
        deposit(sender, args);
    }

    /**
     * 从玩家扣除金钱（管理员）
     */
    private void takeMoney(CommandSender sender, String[] args) {
        withdraw(sender, args);
    }

    /**
     * 显示帮助信息
     */
    /**
     * 设置货币名称（管理员）
     */
    private void setCurrencyName(CommandSender sender, String[] args) {
        if (!sender.hasPermission("catessentials.economy.admin")) {
            String message = "§c你没有权限使用这个命令！喵~";
            if (sender instanceof Player) {
                CatChatProcessor processor = CatChatProcessor.getInstance();
                if (processor != null) {
                    processor.sendCatStyleMessage((Player) sender, message);
                    return;
                }
            }
            sender.sendMessage(message);
            return;
        }

        if (args.length < 2) {
            String message = "§c用法: /money name <货币名称>";
            if (sender instanceof Player) {
                CatChatProcessor processor = CatChatProcessor.getInstance();
                if (processor != null) {
                    processor.sendCatStyleMessage((Player) sender, message);
                    return;
                }
            }
            sender.sendMessage(message);
            return;
        }

        // 获取新的货币名称
        String newCurrencyName = args[1];
        
        // 更新配置文件
        plugin.getConfig().set("economy.currency-name", newCurrencyName);
        plugin.getConfig().set("economy.currency-name-plural", newCurrencyName);
        plugin.saveConfig();
        
        // 发送成功消息
        String message = "§a已成功将货币名称更改为: §b" + newCurrencyName + "§a喵~";
        if (sender instanceof Player) {
            CatChatProcessor processor = CatChatProcessor.getInstance();
            if (processor != null) {
                processor.sendCatStyleMessage((Player) sender, message);
                return;
            }
        }
        sender.sendMessage(message);
    }

    /**
     * 显示帮助信息
     */
    private void showHelp(CommandSender sender, String label) {
        String[] messages = {
            "§6===== §e经济系统帮助 §6=====",
            "§e/" + label + " §6- 查看自己的余额",
            "§e/" + label + " balance §6- 查看自己的余额",
            "§e/" + label + " pay <玩家> <金额> §6- 支付给其他玩家",
            "§e/" + label + " deposit <玩家> <金额> §6- 给玩家充值（管理员）",
            "§e/" + label + " withdraw <玩家> <金额> §6- 从玩家账户扣款（管理员）",
            "§e/" + label + " give <玩家> <金额> §6- 给玩家充值（管理员）",
            "§e/" + label + " take <玩家> <金额> §6- 从玩家账户扣款（管理员）",
            "§e/" + label + " name <货币名称> §6- 设置货币名称（管理员）"
        };

        if (sender instanceof Player) {
            Player player = (Player) sender;
            CatChatProcessor processor = CatChatProcessor.getInstance();
            if (processor != null) {
                for (String message : messages) {
                    processor.sendCatStyleMessage(player, message);
                }
                return;
            }
        }

        for (String message : messages) {
            sender.sendMessage(message);
        }
    }
}