package com.mrfuzzihead.artifice.command;

import java.util.Arrays;
import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;

// import fathzer.javaluator.DoubleEvaluator;

public class CommandCalc extends CommandBase {

    // private DoubleEvaluator eva = new DoubleEvaluator();

    public String getCommandName() {
        return "calc";
    }

    public List getCommandAliases() {
        return Arrays.asList("calculate");
    }

    public String getCommandUsage(ICommandSender p_71518_1_) {
        return "commands.artifice.calc";
    }

    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }

    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.addChatMessage(new ChatComponentTranslation("commands.artifice.calc.syntax", new Object[0]));
        } else {
            String exp = "";

            for (String s : args) {
                exp = exp + s;
            }

            try {
                sender.addChatMessage(new ChatComponentText(exp + " = "
                // + this.eva.evaluate(exp)
                    + "".toString()));
            } catch (Exception var8) {
                var8.printStackTrace();
                sender.addChatMessage(new ChatComponentTranslation("commands.artifice.calc.error", new Object[0]));
            }
        }
    }
}
