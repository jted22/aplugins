
package net.runelite.client.plugins.acombiner;

import net.runelite.client.config.*;

//@ConfigGroup("ElFiremaker")
@ConfigGroup("ACombiner")
public interface ACombinerConfig extends Config
{
	@ConfigItem(
			keyName = "firemakerInstructions",
			name = "",
			description = "Instructions.",
			position = 0
	)
	default String firemakerInstructions()
	{
		return "Enter IDs to combine and how many to retrieve from the bank." +
				" ";
	}

	@ConfigItem(
			keyName = "logId",
			name = "Item ID 1",
			description = "Enter the Id of the you want to use.",
			position = 1
	)
	default int logId() { return 0; }

	@ConfigItem(
			keyName = "logId2",
			name = "Item ID 2",
			description = "Enter the Id of the item to use it on.",
			position = 2
	)
	default int logId2() { return 0; }

	@ConfigItem(
			keyName = "log1Amt",
			name = "Amount of Item 1",
			description = "Enter the amount of item 1 to withdraw.",
			position = 3
	)
	default int log1Amt() { return 0; }
	@ConfigItem(
			keyName = "log2Amt",
			name = "Amount of Item 2",
			description = "Enter the amount of item 2 to withdraw.",
			position = 4
	)
	default int log2Amt() { return 0; }

	/*@ConfigItem(
			keyName = "shortbow",
			name = "Shortbows",
			description = "Cut shortbows",
			position = 5
	)*/
	default boolean shortbow() { return true; }


	@ConfigTitle(
			keyName = "delayConfig",
			name = "Delay Configuration",
			description = "Configure how the bot handles sleep delays in milliseconds",
			position = 6
	)
	String delayConfig = "delayConfig";

	@ConfigItem(
			keyName = "tickDelayMin",
			name = "Delay Minimum",
			description = "Tick delay minimum.",
			position = 7

	)
	default int tickDelayMin() { return 10; }

	@ConfigItem(
			keyName = "tickDelayMax",
			name = "Delay Maximum",
			description = "Tick delay maximum.",
			position = 8
	)
	default int tickDelayMax() { return 550; }

	@ConfigItem(
			keyName = "tickDelayDev",
			name = "Delay Deviation",
			description = "Tick delay deviation.",
			position = 9
	)
	default int tickDelayDev() { return 70; }

	@ConfigItem(
			keyName = "tickDelayTarg",
			name = "Delay Target",
			description = "Tick delay target.",
			position = 10
	)
	default int tickDelayTarg() { return 100; }

	@ConfigItem(
			keyName = "enableUI",
			name = "Enable UI",
			description = "Enable to turn on in game UI",
			position = 140
	)
	default boolean enableUI()
	{
		return true;
	}

	@ConfigItem(
			keyName = "startButton",
			name = "Start/Stop",
			description = "Test button that changes variable value",
			position = 150
	)
	default Button startButton()
	{
		return new Button();
	}
}