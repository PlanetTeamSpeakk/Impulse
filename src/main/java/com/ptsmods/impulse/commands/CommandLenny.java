package com.ptsmods.impulse.commands;

import com.jagrosh.jdautilities.commandclient.Command;
import com.jagrosh.jdautilities.commandclient.CommandEvent;
import com.ptsmods.impulse.utils.Random;

public class CommandLenny extends Command {

	public CommandLenny() {
		name = "lenny";
		help = "Generates a lenny face.";
		arguments = "";
		guildOnly = false;
		ownerCommand = false;
		category = Main.getCategory("Fun");
	}

	@Override
	protected void execute(CommandEvent event) {
		String[] ears = new String[] {"q%sp", "ʢ%sʡ", "⸮%s?", "ʕ%sʔ", "ᖗ%sᖘ", "ᕦ%sᕥ", "ᕦ(%s)ᕥ", "ᕙ(%s)ᕗ", "ᘳ%sᘰ", "ᕮ%sᕭ", "ᕳ%sᕲ", "(%s)", "[%s]", "¯\\_%s_/¯", "୧%s୨", "୨%s୧", "⤜(%s)⤏", "☞%s☞", "ᑫ%sᑷ", "ᑴ%sᑷ", "ヽ(%s)ﾉ", "\\(%s)/", "乁(%s)ㄏ", "└[%s]┘", "(づ%s)づ", "(ง%s)ง", "|%s|"};
		String[] eyes = new String[] {"⌐■%s■", " ͠°%s °", "⇀%s↼", "´• %s •`", "´%s`", "`%s´", "ó%sò", "ò%só", ">%s<", "Ƹ̵̡ %sƷ", "ᗒ%sᗕ", "⪧%s⪦", "⪦%s⪧", "⪩%s⪨", "⪨%s⪩", "⪰%s⪯", "⫑%s⫒", "⨴%s⨵", "⩿%s⪀", "⩾%s⩽", "⩺%s⩹", "⩹%s⩺", "◥▶%s◀◤", "≋%s≋", "૦ઁ%s૦ઁ", "  ͯ%s  ͯ", "  ̿%s  ̿", "  ͌%s  ͌", "ළ%sළ", "◉%s◉", "☉%s☉", "・%s・", "▰%s▰", "ᵔ%sᵔ", "□%s□", "☼%s☼", "*%s*", "⚆%s⚆", "⊜%s⊜", ">%s>", "❍%s❍", "￣%s￣", "─%s─", "✿%s✿", "•%s•", "T%sT", "^%s^", "ⱺ%sⱺ", "@%s@", "ȍ%sȍ", "x%sx", "-%s-", "$%s$", "Ȍ%sȌ", "ʘ%sʘ", "Ꝋ%sꝊ", "๏%s๏", "■%s■", "◕%s◕", "◔%s◔", "✧%s✧", "♥%s♥", " ͡°%s ͡°", "¬%s¬", " º %s º ", "⍜%s⍜", "⍤%s⍤", "ᴗ%sᴗ", "ಠ%sಠ", "σ%sσ"};
		String[] mouth = new String[] {"v", "ᴥ", "ᗝ", "Ѡ", "ᗜ", "Ꮂ", "ヮ", "╭͜ʖ╮", " ͟ل͜", " ͜ʖ", " ͟ʖ", " ʖ̯", "ω", "³", " ε ", "﹏", "ل͜", "╭╮", "‿‿", "▾", "‸", "Д", "∀", "!", "人", ".", "ロ", "_", "෴", "ѽ", "ഌ", "⏏", "ツ", "益"};
		event.reply(String.format(ears[Random.randInt(ears.length)], String.format(eyes[Random.randInt(eyes.length)], mouth[Random.randInt(mouth.length)])));
	}
}
