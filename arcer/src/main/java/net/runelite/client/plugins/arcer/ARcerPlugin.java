/*
 * Copyright (c) 2018, SomeoneWithAnInternetConnection
 * Copyright (c) 2018, oplosthee <https://github.com/oplosthee>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.arcer;

import com.google.inject.Provides;
import java.awt.Rectangle;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.autils.AUtils;
import net.runelite.client.ui.overlay.OverlayManager;
import org.pf4j.Extension;

import static net.runelite.api.MenuAction.ITEM_SECOND_OPTION;
import static net.runelite.client.plugins.arcer.ARcerState.*;
import static net.runelite.client.plugins.arcer.ARcerType.*;


@Extension
@PluginDependency(AUtils.class)
@PluginDescriptor(
		name = "ARunecraft Free",
		enabledByDefault = false,
		description = "Crafts runes.",
		tags = {"rune, craft, runecraft, anarchise"}
)
@Slf4j
public class ARcerPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ARcerConfiguration config;

	@Inject
	private AUtils utils;

	@Inject
	private ConfigManager configManager;

	@Inject
	OverlayManager overlayManager;

	@Inject
	private ARcerOverlay overlay;

	@Inject
	private ItemManager itemManager;



	ARcerState state;
	GameObject targetObject;
	DecorativeObject decorativeObject;
	NPC targetNPC;
	MenuEntry targetMenu;
	WorldPoint skillLocation;
	Instant botTimer;
	LocalPoint beforeLoc;
	Player player;
	Rectangle altRect = new Rectangle(-100,-100, 10, 10);
	Rectangle clickBounds;

	WorldArea FALADOR_EAST_BANK = new WorldArea(new WorldPoint(3009,3353,0),new WorldPoint(3019,3359,0));

	WorldPoint FIRST_CLICK_AIR = new WorldPoint(3006,3315,0);
	WorldArea FIRST_POINT_AIR = new WorldArea(new WorldPoint(3004,3314,0),new WorldPoint(3009,3319,0));

	WorldPoint SECOND_CLICK_AIR = new WorldPoint(3006,3330,0);
	WorldArea SECOND_POINT_AIR = new WorldArea(new WorldPoint(3003,3325,0),new WorldPoint(3010,3333,0));

	WorldArea AIR_ALTAR = new WorldArea(new WorldPoint(2839,4826,0),new WorldPoint(2849,4840,0));
	WorldPoint OUTSIDE_ALTAR_AIR = new WorldPoint(2983,3288,0);


	WorldArea DRAYNOR_BANK = new WorldArea(new WorldPoint(3088,3241,0),new WorldPoint(3097,3247,0));
	WorldPoint FIRST_CLICK_WATER = new WorldPoint(3139,3207,0);
	WorldArea FIRST_POINT_WATER = new WorldArea(new WorldPoint(3131,3202,0),new WorldPoint(3147, 3215,0));

	WorldPoint THIRD_CLICK_WATER = new WorldPoint(3099, 3230, 0);
	WorldArea THIRD_POINT_WATER = new WorldArea(new WorldPoint(3095, 3227, 0), new WorldPoint(3104, 3233, 0));

	WorldPoint SECOND_CLICK_WATER = new WorldPoint(3135,3215,0);
	WorldArea SECOND_POINT_WATER = new WorldArea(new WorldPoint(3130,3211,0),new WorldPoint(3143,3220,0));

	WorldPoint CLICK_NEARER_ALTER = new WorldPoint(3159,3179,0);
	WorldArea POINT_NEARER_ALTER = new WorldArea(new WorldPoint(3155,3176,0),new WorldPoint(3164,3183,0));

	WorldPoint OUTSIDE_ALTAR_WATER = new WorldPoint(3182,3162,0);
	WorldArea WATER_ALTAR = new WorldArea(new WorldPoint(2709,4829,0),new WorldPoint(2731,4842,0));



	WorldArea VARROCK_EAST_BANK = new WorldArea(new WorldPoint(3249, 3416, 0), new WorldPoint(3257, 3424, 0));
	WorldPoint FIRST_CLICK_EARTH = new WorldPoint(3298,3467,0);
	WorldArea FIRST_POINT_EARTH = new WorldArea(new WorldPoint(3295,3464,0),new WorldPoint(3302, 3470,0));
	WorldPoint OUTSIDE_ALTAR_EARTH = new WorldPoint(3302,3477,0);
	WorldArea EARTH_ALTAR = new WorldArea(new WorldPoint(2648,4828,0),new WorldPoint(2666,4847,0));

	WorldPoint SECOND_CLICK_EARTH = new WorldPoint(3281,3428,0);
	WorldArea SECOND_POINT_EARTH = new WorldArea(new WorldPoint(3278,3425,0),new WorldPoint(3285, 3431,0));


	WorldArea FIRE_ALTAR = new WorldArea(new WorldPoint(2568,4830,0),new WorldPoint(2600,4853,0));
	WorldArea CASTLE_WARS_BANK = new WorldArea(new WorldPoint(2435, 3081, 0), new WorldPoint(2445, 3097, 0));

	WorldArea FIRST_POINT_FIRE = new WorldArea(new WorldPoint(3309,3228,0),new WorldPoint(3326, 3244,0));
	WorldArea SECOND_POINT_FIRE =  new WorldArea(new WorldPoint(2435, 3081, 0), new WorldPoint(2445, 3097, 0));




	WorldArea NATURE_ALTAR = new WorldArea(new WorldPoint(2390,4832,0),new WorldPoint(2413,4851,0));
	WorldArea EDGEVILLE_BANK = new WorldArea(new WorldPoint(3082, 3485, 0), new WorldPoint(3100, 3502, 0));


	boolean DidObstacle = false;
	int timeout = 0;
	long sleepLength;
	boolean startTeaks;
	int essenceValue;
	//3055 4846
	@Provides
	ARcerConfiguration provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ARcerConfiguration.class);
	}

	@Override
	protected void startUp()
	{
		resetVals();

	}

	@Override
	protected void shutDown()
	{
		resetVals();

	}

	private void resetVals()
	{
		overlayManager.remove(overlay);

		state = null;
		timeout = 0;
		botTimer = null;
		skillLocation = null;
		if(config.useRuneEssence()){
			essenceValue = 1436;
		} else {
			essenceValue = 7936;
		}
	}
	private void onChatMessage(ChatMessage event) {
		ChatMessageType type = event.getType();
		String msg = event.getMessage();

		if (!type.equals(ChatMessageType.PUBLICCHAT)) {
			if (msg.contains("...and you manage to crawl through.")) {
				DidObstacle = true;
			}
		}
	}
	@Subscribe
	private void onConfigButtonPressed(ConfigButtonClicked configButtonClicked)
	{
		if (!configButtonClicked.getGroup().equalsIgnoreCase("ARcer"))
		{
			return;
		}
		log.info("button {} pressed!", configButtonClicked.getKey());
		if (configButtonClicked.getKey().equals("startButton"))
		{
			if (!startTeaks)
			{
				startTeaks = true;

				state = null;
				targetMenu = null;
				botTimer = Instant.now();
				setLocation();
				overlayManager.add(overlay);
			}
			else
			{
				startTeaks=false;
				resetVals();
			}
		}
	}
	public boolean checkHasBindingNeck(Player localPlayer)
	{
		PlayerComposition playerAppearance = localPlayer.getPlayerComposition();

		if (playerAppearance == null)
		{
			return false;
		}

		Item[] equipmentItems = client.getItemContainer(InventoryID.EQUIPMENT).getItems();

		for (Item equipmentItem : equipmentItems)
		{
			String name = itemManager.getItemComposition(equipmentItem.getId()).getName();
			if (name.contains("Binding necklace"))
				return true;
		}

		return false;
	}
	public boolean checkHasDuelingRing(Player localPlayer)
	{
		PlayerComposition playerAppearance = localPlayer.getPlayerComposition();

		if (playerAppearance == null)
		{
			return false;
		}

		Item[] equipmentItems = client.getItemContainer(InventoryID.EQUIPMENT).getItems();

		for (Item equipmentItem : equipmentItems)
		{
			String name = itemManager.getItemComposition(equipmentItem.getId()).getName();
			if (name.contains("Ring of dueling("))
				return true;
		}

		return false;
	}
	public boolean checkHasGlory(Player localPlayer)
	{
		PlayerComposition playerAppearance = localPlayer.getPlayerComposition();

		if (playerAppearance == null)
		{
			return false;
		}

		Item[] equipmentItems = client.getItemContainer(InventoryID.EQUIPMENT).getItems();

		for (Item equipmentItem : equipmentItems)
		{
			String name = itemManager.getItemComposition(equipmentItem.getId()).getName();
			if (name.contains("Amulet of glory("))
				return true;
		}

		return false;
	}

	@Subscribe
	private void onConfigChanged(ConfigChanged event)
	{
		if (!event.getGroup().equals("ARcer"))
		{
			return;
		}
		startTeaks = false;
	}

	public void setLocation()
	{
		if (client != null && client.getLocalPlayer() != null && client.getGameState().equals(GameState.LOGGED_IN))
		{
			skillLocation = client.getLocalPlayer().getWorldLocation();
			beforeLoc = client.getLocalPlayer().getLocalLocation();
		}
		else
		{
			log.debug("Tried to start bot before being logged in");
			skillLocation = null;
			resetVals();
		}
	}

	private long sleepDelay()
	{
		sleepLength = utils.randomDelay(config.sleepWeightedDistribution(), config.sleepMin(), config.sleepMax(), config.sleepDeviation(), config.sleepTarget());
		return sleepLength;
	}

	private int tickDelay()
	{
		int tickLength = (int) utils.randomDelay(config.tickDelayWeightedDistribution(), config.tickDelayMin(), config.tickDelayMax(), config.tickDelayDeviation(), config.tickDelayTarget());
		log.debug("tick delay for {} ticks", tickLength);
		return tickLength;
	}

	private ARcerState getBankState()
	{
		if (config.runeType() == ARcerRuneType.WATER_RUNE) {
			if (utils.inventoryFull()) {
				return WALK_FIRST_POINT;
			}
			if (utils.inventoryContains(555) || utils.inventoryContains(5531)) {
				return DEPOSIT_ITEMS;
			} else {
				return WITHDRAW_ITEMS;
			}
		}

		if (config.runeType() == ARcerRuneType.EARTH_RUNE) {
			if (utils.inventoryFull()) {
				return WALK_FIRST_POINT;
			}
			if (utils.inventoryContains(557) || utils.inventoryContains(5535)) {
				return DEPOSIT_ITEMS;
			} else {
				return WITHDRAW_ITEMS;
			}
		}
		if (config.runeType() == ARcerRuneType.FIRE_RUNE) {
			if (utils.inventoryFull()) {
				return WALK_FIRST_POINT;
			}
			if (utils.inventoryContains(554) || utils.inventoryContains(4697)) {
				return DEPOSIT_ITEMS;
			} else {
				return WITHDRAW_ITEMS;
			}
		}


		if (config.runeType() == ARcerRuneType.MINE_ESS_VARROCK) {
			if (utils.inventoryFull()) {
				if (utils.inventoryContains(7936) || utils.inventoryContains(1436)) {
					return DEPOSIT_ITEMS;
				} else {
					return WALK_TO_MAGE;
				}
			}
			else return WALK_TO_MAGE;
		}






		else if(config.runeType() == ARcerRuneType.AIR_RUNE) {
			if (utils.inventoryFull()) {
				return WALK_FIRST_POINT;
			}
			if (utils.inventoryContains(556) || utils.inventoryContains(5527)) {
				return DEPOSIT_ITEMS;
			} else {
				return WITHDRAW_ITEMS;
			}
		}
		return UNHANDLED_STATE;
	}

	public ARcerState getState()
	{
		if (timeout > 0)
		{
			return TIMEOUT;
		}
		else if (utils.isMoving(beforeLoc))
		{
			timeout = 2 + tickDelay();
			return MOVING;
		}

		else if(utils.isBankOpen()){
			return getBankState();
		}
		else if(client.getLocalPlayer().getAnimation()!=-1){
			return ANIMATING;
		}
		else {
			return getAirsState();
		}
	}
	private void openBank() {
		GameObject bankTarget = utils.findNearestBankNoDepositBoxes();
		if (bankTarget != null) {
			targetMenu = new MenuEntry("", "", bankTarget.getId(),
					utils.getBankMenuOpcode(bankTarget.getId()), bankTarget.getSceneMinLocation().getX(),
					bankTarget.getSceneMinLocation().getY(), false);
			//utils.doActionMsTime(targetMenu, bankTarget.getConvexHull().getBounds(), sleepDelay());
			utils.setMenuEntry(targetMenu);
			utils.delayMouseClick(bankTarget.getConvexHull().getBounds(), sleepDelay());
		}
	}
	@Subscribe
	private void onGameTick(GameTick tick)
	{
		if (!startTeaks)
		{
			return;
		}
		player = client.getLocalPlayer();
		if (client != null && player != null && skillLocation != null)
		{
			if (!client.isResized())
			{
				utils.sendGameMessage("client must be set to resizable");
				startTeaks = false;
				return;
			}
			state = getState();
			beforeLoc = player.getLocalLocation();
			utils.setMenuEntry(null);
			switch (state)
			{
				case TIMEOUT:
					utils.handleRun(30, 20);
					timeout--;
					break;
				case ANIMATING:
				case MOVING:
					utils.handleRun(30, 20);
					timeout = tickDelay();
					break;
				case FIND_BANK:
						openBank();
					timeout = tickDelay();
					break;
				case DEPOSIT_ITEMS:
					if (config.runeType() == ARcerRuneType.AIR_RUNE) {
						if (config.mode().equals(RUNES)) {
							depositItem(556);
						}
					}
					else if (config.runeType() == ARcerRuneType.WATER_RUNE) {
						if (config.mode().equals(RUNES)) {
							depositItem(555);
						}
					}

					else if (config.runeType() == ARcerRuneType.EARTH_RUNE) {
						if (config.mode().equals(RUNES)) {
							depositItem(557);
						}
					}
					else if (config.runeType() == ARcerRuneType.FIRE_RUNE) {
						if (config.mode().equals(RUNES)) {
							depositItem(554);
						}
					}

					else if (config.runeType() == ARcerRuneType.MINE_ESS_VARROCK) {
						if (utils.inventoryContains(1436))
							utils.depositAllOfItem(1436);
						else if (utils.inventoryContains(7936))
							utils.depositAllOfItem(7936);
					}
					timeout = tickDelay();
					break;
				case WITHDRAW_ITEMS:
					if(config.useStams()) {
						if (client.getVar(Varbits.RUN_SLOWED_DEPLETION_ACTIVE) == 0 && checkRunEnergy() < config.minEnergy()) {
							if (utils.inventoryContains(12631)) {
								targetMenu = new MenuEntry("Drink", "<col=ff9040>Stamina potion(1)</col>", 9, 1007, utils.getInventoryWidgetItem(12631).getIndex(), 983043, false);
								utils.delayMouseClick(utils.getInventoryWidgetItem(12631).getCanvasBounds(), sleepDelay());
								return;
							} else {
								if (utils.inventoryFull()) {
									utils.depositAll();
									return;
								} else {
									targetMenu = new MenuEntry("Withdraw-1", "<col=ff9040>Stamina potion(1)</col>", 1, 57, utils.getBankItemWidget(12631).getIndex(), 786445, false);
									utils.delayMouseClick(utils.getBankItemWidget(12631).getBounds(), sleepDelay());
									return;
								}
							}
						} else if (checkRunEnergy() < config.minEnergyStam()) {
							if (utils.inventoryContains(12631)) {
								targetMenu = new MenuEntry("Drink", "<col=ff9040>Stamina potion(1)</col>", 9, 1007, utils.getInventoryWidgetItem(12631).getIndex(), 983043, false);
								utils.delayMouseClick(utils.getInventoryWidgetItem(12631).getCanvasBounds(), sleepDelay());
								return;
							} else {
								if (utils.inventoryFull()) {
									utils.depositAll();
									return;
								} else {
									targetMenu = new MenuEntry("Withdraw-1", "<col=ff9040>Stamina potion(1)</col>", 1, 57, utils.getBankItemWidget(12631).getIndex(), 786445, false);
									utils.delayMouseClick(utils.getBankItemWidget(12631).getBounds(), sleepDelay());
									return;
								}
							}
						}
					}
					if (config.runeType() == ARcerRuneType.FIRE_RUNE) {


							if (!utils.inventoryContains(2552) && !checkHasDuelingRing(player)) {// && !utils.isItemEquipped(Collections.singleton(2552)) || !utils.isItemEquipped(Collections.singleton(2554)) || !utils.isItemEquipped(Collections.singleton(2556))|| !utils.isItemEquipped(Collections.singleton(2558))|| !utils.isItemEquipped(Collections.singleton(2560))|| !utils.isItemEquipped(Collections.singleton(2562))|| !utils.isItemEquipped(Collections.singleton(2564))|| !utils.isItemEquipped(Collections.singleton(2566))) {
								utils.withdrawItem(2552);
								sleepDelay();
								return;
							}
							if (utils.inventoryContains(2552) && !checkHasDuelingRing(player))//!utils.isItemEquipped(Collections.singleton(2552)) || !utils.isItemEquipped(Collections.singleton(2554))|| !utils.isItemEquipped(Collections.singleton(2556))|| !utils.isItemEquipped(Collections.singleton(2558))|| !utils.isItemEquipped(Collections.singleton(2560))|| !utils.isItemEquipped(Collections.singleton(2562))|| !utils.isItemEquipped(Collections.singleton(2564))|| !utils.isItemEquipped(Collections.singleton(2566)) && utils.inventoryContains(2552))
							{
								//utils.closeBank();
								targetMenu = new MenuEntry("Wear", "Wear", 2552, 34, 0, 9764864, false);
								utils.setMenuEntry(targetMenu);
								utils.delayMouseClick(getRandomNullPoint(), sleepDelay());
								return;
							}

					}
					if (config.runeType() == ARcerRuneType.MINE_ESS_VARROCK) {
						sleepDelay(); // Nothing to withdraw
						return;
					}
					if(utils.inventoryContains(229)){
						utils.depositAll();
						return;
					}
					if(config.mode().equals(RUNES)  && config.runeType() != ARcerRuneType.MINE_ESS_VARROCK){
						utils.withdrawAllItem(essenceValue);
					}

					timeout = tickDelay();
					break;
				case ENTER_ALTAR:
					if (config.runeType() == ARcerRuneType.AIR_RUNE) {
						if (utils.inventoryContains(1438)) {
							useTalismanOnAltar();
						} else {
							useGameObject(34813, 3);
						}
					}
					if (config.runeType() == ARcerRuneType.WATER_RUNE)
					{
						if (utils.inventoryContains(1444)) {
							useTalismanOnAltar();
						} else {
							useGameObject(34815, 3);
						}
					}

					if (config.runeType() == ARcerRuneType.EARTH_RUNE)
					{
						if (utils.inventoryContains(1440)) {
							useTalismanOnAltar();
						} else {
							useGameObject(34816, 3);
						}
					}
					if (config.runeType() == ARcerRuneType.FIRE_RUNE)
					{
						//if (utils.inventoryContains(1440)) {
						//	useTalismanOnAltar();
						//} else {
							useGameObject(34817, 3);
						//}
					}

					if (config.runeType() == ARcerRuneType.MINE_ESS_VARROCK)
					{
						WallObject Obj = utils.findWallObjectWithin(new WorldPoint(3253, 3398, 0),1, 11780);
						if (Obj != null)
						{
							targetMenu = new MenuEntry("", "", 11780, 3, Obj.getLocalLocation().getSceneX(), Obj.getLocalLocation().getSceneY(), false);
							utils.setMenuEntry(targetMenu);
							utils.delayMouseClick(getRandomNullPoint(), sleepDelay());
						}
						 //TODO Check door open/closed
						targetMenu = new MenuEntry("Teleport", "<col=ffff00>Aubury", 8110, 12,  0, 0, false);
						utils.setMenuEntry(targetMenu);
						utils.delayMouseClick(getRandomNullPoint(), sleepDelay());

					}
					timeout = tickDelay();
					break;
				case CRAFT_RUNES:
					if (config.runeType() == ARcerRuneType.AIR_RUNE) {
						useGameObject(34760, 3);
					}
					else if (config.runeType() == ARcerRuneType.WATER_RUNE) {
						useGameObject(34762, 3);
					}

					else if (config.runeType() == ARcerRuneType.EARTH_RUNE) {
						useGameObject(34763, 3);
					}
					else if (config.runeType() == ARcerRuneType.FIRE_RUNE) {
						useGameObject(34764, 3);
					}

					timeout = tickDelay();
					break;
				case USE_PORTAL:
					if (config.runeType() == ARcerRuneType.AIR_RUNE) {
						useGameObject(34748, 3);
					}
					else if (config.runeType() == ARcerRuneType.WATER_RUNE) {
						useGameObject(34750, 3);
					}

					else if (config.runeType() == ARcerRuneType.EARTH_RUNE) {
						useGameObject(34751, 3);
					}
					else if (config.runeType() == ARcerRuneType.FIRE_RUNE) {
						if (utils.isItemEquipped(Collections.singleton(2552)) || utils.isItemEquipped(Collections.singleton(2554))|| utils.isItemEquipped(Collections.singleton(2556))|| utils.isItemEquipped(Collections.singleton(2558))|| utils.isItemEquipped(Collections.singleton(2560))|| utils.isItemEquipped(Collections.singleton(2562))|| utils.isItemEquipped(Collections.singleton(2564))|| utils.isItemEquipped(Collections.singleton(2566)))
						{
							targetMenu = new MenuEntry("Castle Wars", "<col=ff9040>Ring of dueling", 3, 57, -1, 25362456,false);
							utils.setMenuEntry(targetMenu);
							utils.delayMouseClick(getRandomNullPoint(), sleepDelay());
						}
					}

					else if (config.runeType() == ARcerRuneType.MINE_ESS_VARROCK){
						Set<Integer> PORTAL = Set.of(NpcID.PORTAL_3088, NpcID.PORTAL_3086, NullNpcID.NULL_9412);
						targetNPC = utils.findNearestNpcWithin(client.getLocalPlayer().getWorldLocation(), 15, PORTAL);

						TileObject NearestPortal = utils.findNearestObject(34779, 34825);
						if (targetNPC != null) {
							targetMenu = new MenuEntry("", "",
									targetNPC.getIndex(), MenuAction.NPC_FIRST_OPTION.getId(), 0, 0, false);
							utils.setMenuEntry(targetMenu);
							utils.delayMouseClick(targetNPC.getConvexHull().getBounds(), sleepDelay());
							timeout = tickDelay();
							break;
						}
						else if (NearestPortal != null){
							useGameObject(NearestPortal.getId(), 3);
							timeout = tickDelay();
							break;
						}
						//useGameObject(34825, 3);
						//else if (utils.findNearestObject(34779) != null)//
						//useGameObject(34779, 3);
					}
					timeout = tickDelay();
					break;
				case WALK_THIRD_POINT:
					if (config.runeType() == ARcerRuneType.WATER_RUNE) {
						utils.walk(THIRD_CLICK_WATER, 2, sleepDelay());
					}

					timeout = tickDelay();
					break;
				case WALK_NEARER_ALTER:
					if (config.runeType() == ARcerRuneType.WATER_RUNE) {
						utils.walk(CLICK_NEARER_ALTER, 2, sleepDelay());
					}

					timeout = tickDelay();
					break;
				case WALK_TO_MAGE:
					WallObject Obj = utils.findWallObjectWithin(new WorldPoint(3253, 3398, 0),1, 11780);
					if (Obj != null)
					{
						targetMenu = new MenuEntry("", "", 11780, 3, Obj.getLocalLocation().getSceneX(), Obj.getLocalLocation().getSceneY(), false);
						utils.setMenuEntry(targetMenu);
						utils.delayMouseClick(getRandomNullPoint(), sleepDelay());
						timeout = tickDelay();
						break;
					}
					if (config.runeType() == ARcerRuneType.MINE_ESS_VARROCK)
					{
						utils.walk(new WorldPoint(3253, 3401, 0), 1, sleepDelay()); // Aubury Varrock
					}
					timeout = tickDelay();
					break;
				case MINE_ESSENCE:
					if (config.runeType() == ARcerRuneType.MINE_ESS_VARROCK)
					{
						useGameObject(34773, 3);
					}
					timeout = tickDelay();
					break;
				case WALK_SECOND_POINT:
					if (config.runeType() == ARcerRuneType.AIR_RUNE) {
						utils.walk(SECOND_CLICK_AIR, 2, sleepDelay());
						timeout = tickDelay();
						break;
					}
					else if (config.runeType() == ARcerRuneType.WATER_RUNE) {
						utils.walk(SECOND_CLICK_WATER, 2, sleepDelay());
						timeout = tickDelay();
						break;
					}

					else if (config.runeType() == ARcerRuneType.EARTH_RUNE) {
						utils.walk(SECOND_CLICK_EARTH, 2, sleepDelay());
						timeout = tickDelay();
						break;
					}

				case WALK_FIRST_POINT:
					if (config.runeType() == ARcerRuneType.AIR_RUNE) {
						utils.walk(FIRST_CLICK_AIR, 1, sleepDelay());
					}
					else if (config.runeType() == ARcerRuneType.WATER_RUNE) {
						utils.walk(FIRST_CLICK_WATER, 1, sleepDelay());
					}

					else if (config.runeType() == ARcerRuneType.EARTH_RUNE) {
						utils.walk(FIRST_CLICK_EARTH, 1, sleepDelay());
					}
					else if (config.runeType() == ARcerRuneType.FIRE_RUNE) {
						if (utils.isItemEquipped(Collections.singleton(2552)) || utils.isItemEquipped(Collections.singleton(2554))|| utils.isItemEquipped(Collections.singleton(2556))|| utils.isItemEquipped(Collections.singleton(2558))|| utils.isItemEquipped(Collections.singleton(2560))|| utils.isItemEquipped(Collections.singleton(2562))|| utils.isItemEquipped(Collections.singleton(2564))|| utils.isItemEquipped(Collections.singleton(2566)))
						{
							utils.closeBank();
							targetMenu = new MenuEntry("Duel Arena", "<col=ff9040>Ring of dueling", 2, 57, -1, 25362456,false);
							utils.setMenuEntry(targetMenu);
							utils.delayMouseClick(getRandomNullPoint(), sleepDelay());

						}
					}

					timeout = tickDelay();
					break;
			}
		}
	}

	@Subscribe
	private void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGGED_IN && startTeaks)
		{
			state = TIMEOUT;
			timeout = 2;
		}
	}

	private ARcerState getAirsState()
	{
		if (config.runeType() == ARcerRuneType.AIR_RUNE) {
			utils.setMenuEntry(null);
			if (config.mode().equals(RUNES)) {
				if (utils.inventoryContains(essenceValue)) {
					if (player.getWorldArea().intersectsWith(FIRST_POINT_AIR)) {
						return ENTER_ALTAR;
					} else if (player.getWorldArea().intersectsWith(AIR_ALTAR)) {
						return CRAFT_RUNES;
					}
				} else {
					if (player.getWorldArea().intersectsWith(AIR_ALTAR)) {
						return USE_PORTAL;
					} else if (player.getWorldLocation().equals(OUTSIDE_ALTAR_AIR)) {
						return WALK_SECOND_POINT;
					} else if (player.getWorldArea().intersectsWith(SECOND_POINT_AIR)) {
						return FIND_BANK;
					} else if (player.getWorldArea().intersectsWith(FALADOR_EAST_BANK)) {
						return FIND_BANK;
					}
				}
				return UNHANDLED_STATE;
			}
			return UNHANDLED_STATE;
		}
		if (config.runeType() == ARcerRuneType.WATER_RUNE) {
			utils.setMenuEntry(null);
			if (config.mode().equals(RUNES)) {
				if (utils.inventoryContains(essenceValue)) {
					if (player.getWorldArea().intersectsWith(FIRST_POINT_WATER)) {
						return WALK_NEARER_ALTER;
					} else if (player.getWorldArea().intersectsWith(POINT_NEARER_ALTER)) {
						return ENTER_ALTAR;
					} else if (player.getWorldArea().intersectsWith(WATER_ALTAR)) {
						return CRAFT_RUNES;
					}
				} else {
					if (player.getWorldArea().intersectsWith(WATER_ALTAR)) {
						return USE_PORTAL;
					} else if (player.getWorldLocation().equals(OUTSIDE_ALTAR_WATER)) {
						return WALK_SECOND_POINT;
					} else if (player.getWorldArea().intersectsWith(SECOND_POINT_WATER)) {
						return WALK_THIRD_POINT;
					} else if (player.getWorldArea().intersectsWith(THIRD_POINT_WATER)) {
						return FIND_BANK;
					} else if (player.getWorldArea().intersectsWith(DRAYNOR_BANK)) {
						return FIND_BANK;
					}
				}
				return UNHANDLED_STATE;
			}
			return UNHANDLED_STATE;
		}
		if (config.runeType() == ARcerRuneType.EARTH_RUNE) {
			utils.setMenuEntry(null);
			if (config.mode().equals(RUNES)) {
				if (utils.inventoryContains(essenceValue)) {
					if (player.getWorldArea().intersectsWith(FIRST_POINT_EARTH)) {
						return ENTER_ALTAR;
					} else if (player.getWorldArea().intersectsWith(EARTH_ALTAR)) {
						return CRAFT_RUNES;
					}
				} else {
					if (player.getWorldArea().intersectsWith(EARTH_ALTAR)) {
						return USE_PORTAL;
					} else if (player.getWorldLocation().equals(OUTSIDE_ALTAR_EARTH)) {
						return WALK_SECOND_POINT;
					} else if (player.getWorldArea().intersectsWith(SECOND_POINT_EARTH)) {
						return FIND_BANK;
					} else if (player.getWorldArea().intersectsWith(VARROCK_EAST_BANK)) {
						return FIND_BANK;
					}
				}
				return UNHANDLED_STATE;
			}
			return UNHANDLED_STATE;
		}
		if (config.runeType() == ARcerRuneType.FIRE_RUNE) {
			utils.setMenuEntry(null);
			if (config.mode().equals(RUNES)) {
				if (utils.inventoryContains(essenceValue)) {
					if (player.getWorldArea().intersectsWith(CASTLE_WARS_BANK) && utils.inventoryContains(1438) && utils.inventoryContains(556) && checkHasBindingNeck(client.getLocalPlayer()) && checkHasDuelingRing(client.getLocalPlayer())) {
						return WALK_FIRST_POINT;
					} else if (player.getWorldArea().intersectsWith(FIRST_POINT_FIRE)) {
						return ENTER_ALTAR;
					} else if (player.getWorldArea().intersectsWith(FIRE_ALTAR)) {
						return CRAFT_RUNES;
					}
				} else {
					if (player.getWorldArea().intersectsWith(FIRE_ALTAR)) {
						return USE_PORTAL; // teleport away instead
					}  else if (player.getWorldArea().intersectsWith(SECOND_POINT_FIRE)) {
						return FIND_BANK;
					} else if (player.getWorldArea().intersectsWith(CASTLE_WARS_BANK)) {
						return FIND_BANK;
					}
				}
				return UNHANDLED_STATE;
			}
		}


		if (config.runeType() == ARcerRuneType.MINE_ESS_VARROCK) {
			utils.setMenuEntry(null);
			if (!utils.inventoryContains(essenceValue)) {
				if (!utils.inventoryFull() && player.getWorldArea().intersectsWith(new WorldArea(new WorldPoint(3248, 3397, 0), new WorldPoint(3258, 3407, 0)))) {
					return ENTER_ALTAR; // Near Aubury
				}  else {//if (!utils.inventoryFull() && player.getWorldArea().intersectsWith(new WorldArea(new WorldPoint(14535, 1610, 0), new WorldPoint(14606, 1684, 0)))) {
					return MINE_ESSENCE;
				}
			} else {
				if (utils.inventoryFull() && !player.getWorldArea().intersectsWith(new WorldArea(new WorldPoint(3248, 3397, 0), new WorldPoint(3258, 3407, 0)))) {
					return USE_PORTAL;
				}else if (utils.inventoryFull() && player.getWorldArea().intersectsWith(new WorldArea(new WorldPoint(3248, 3397, 0), new WorldPoint(3258, 3407, 0)))) {
					return FIND_BANK;
				}
			}
			return UNHANDLED_STATE;
		}
		return UNHANDLED_STATE;
	}

	@Subscribe
	private void onMenuOptionClicked(MenuOptionClicked event){
		if(targetMenu!=null){
			menuAction(event,targetMenu.getOption(), targetMenu.getTarget(), targetMenu.getIdentifier(), targetMenu.getMenuAction(),
					targetMenu.getParam0(), targetMenu.getParam1());
			targetMenu = null;
		}
	}

	public void menuAction(MenuOptionClicked menuOptionClicked, String option, String target, int identifier, MenuAction menuAction, int param0, int param1)
	{
		menuOptionClicked.setMenuOption(option);
		menuOptionClicked.setMenuTarget(target);
		menuOptionClicked.setId(identifier);
		menuOptionClicked.setMenuAction(menuAction);
		menuOptionClicked.setActionParam(param0);
		menuOptionClicked.setWidgetId(param1);
	}

	private void useTalismanOnAltar()
	{
		if (config.runeType() == ARcerRuneType.AIR_RUNE) {
			targetObject = utils.findNearestGameObject(34813);
			if (targetObject != null) {
				client.setSelectedItemWidget(WidgetInfo.INVENTORY.getId());
				client.setSelectedItemSlot(utils.getInventoryWidgetItem(1438).getIndex());
				client.setSelectedItemID(1438);
				targetMenu = new MenuEntry("", "", targetObject.getId(), 1, targetObject.getSceneMinLocation().getX(), targetObject.getSceneMinLocation().getY(), false);
				//utils.setMenuEntry(targetMenu);
				if (targetObject.getConvexHull() != null) {
					utils.delayMouseClick(targetObject.getConvexHull().getBounds(), sleepDelay());
				} else {
					utils.delayMouseClick(new Point(0, 0), sleepDelay());
				}

			}
		}
		if (config.runeType() == ARcerRuneType.FIRE_RUNE) {
			targetObject = utils.findNearestGameObject(34764);
			if (targetObject != null) {
				client.setSelectedItemWidget(WidgetInfo.INVENTORY.getId());
				client.setSelectedItemSlot(utils.getInventoryWidgetItem(1438).getIndex());
				client.setSelectedItemID(1438);
				targetMenu = new MenuEntry("", "", targetObject.getId(), 1, targetObject.getSceneMinLocation().getX(), targetObject.getSceneMinLocation().getY(), false);
				//utils.setMenuEntry(targetMenu);
				if (targetObject.getConvexHull() != null) {
					utils.delayMouseClick(targetObject.getConvexHull().getBounds(), sleepDelay());
				} else {
					utils.delayMouseClick(new Point(0, 0), sleepDelay());
				}

			}
		}
		if (config.runeType() == ARcerRuneType.WATER_RUNE) {
			targetObject = utils.findNearestGameObject(34815);
			if (targetObject != null) {
				client.setSelectedItemWidget(WidgetInfo.INVENTORY.getId());
				client.setSelectedItemSlot(utils.getInventoryWidgetItem(1444).getIndex());
				client.setSelectedItemID(1444);
				targetMenu = new MenuEntry("", "", targetObject.getId(), 1, targetObject.getSceneMinLocation().getX(), targetObject.getSceneMinLocation().getY(), false);
				//utils.setMenuEntry(targetMenu);
				if (targetObject.getConvexHull() != null) {
					utils.delayMouseClick(targetObject.getConvexHull().getBounds(), sleepDelay());
				} else {
					utils.delayMouseClick(new Point(0, 0), sleepDelay());
				}

			}
		}
		if (config.runeType() == ARcerRuneType.EARTH_RUNE) {
			targetObject = utils.findNearestGameObject(34816);
			if (targetObject != null) {
				client.setSelectedItemWidget(WidgetInfo.INVENTORY.getId());
				client.setSelectedItemSlot(utils.getInventoryWidgetItem(1440).getIndex());
				client.setSelectedItemID(1440);
				targetMenu = new MenuEntry("", "", targetObject.getId(), 1, targetObject.getSceneMinLocation().getX(), targetObject.getSceneMinLocation().getY(), false);
				//utils.setMenuEntry(targetMenu);
				if (targetObject.getConvexHull() != null) {
					utils.delayMouseClick(targetObject.getConvexHull().getBounds(), sleepDelay());
				} else {
					utils.delayMouseClick(new Point(0, 0), sleepDelay());
				}

			}
		}
	}

	private Point getRandomNullPoint()
	{
		if(client.getWidget(161,34)!=null){
			Rectangle nullArea = client.getWidget(161,34).getBounds();
			return new Point ((int)nullArea.getX()+utils.getRandomIntBetweenRange(0,nullArea.width), (int)nullArea.getY()+utils.getRandomIntBetweenRange(0,nullArea.height));
		}

		return new Point(client.getCanvasWidth()-utils.getRandomIntBetweenRange(0,2),client.getCanvasHeight()-utils.getRandomIntBetweenRange(0,2));
	}

	private void useNPCObject(int opcode, int... id)
	{
		NPC targetObject = utils.findNearestNpc(id);
		if(targetObject!=null){
			targetMenu = new MenuEntry("","",targetObject.getId(),opcode,targetObject.getLocalLocation().getX(),targetObject.getLocalLocation().getY(),false);
			utils.setMenuEntry(targetMenu);
			if(targetObject.getConvexHull()!=null){
				utils.delayMouseClick(targetObject.getConvexHull().getBounds(),sleepDelay());
			} else {
				utils.delayMouseClick(new Point(0,0),sleepDelay());
			}
		}
	}

	private void useGameObject(int id, int opcode)
	{
		targetObject = utils.findNearestGameObject(id);
		if(targetObject!=null){
			targetMenu = new MenuEntry("","",targetObject.getId(),opcode,targetObject.getSceneMinLocation().getX(),targetObject.getSceneMinLocation().getY(),false);
			utils.setMenuEntry(targetMenu);
			if(targetObject.getConvexHull()!=null){
				utils.delayMouseClick(targetObject.getConvexHull().getBounds(),sleepDelay());
			} else {
				utils.delayMouseClick(new Point(0,0),sleepDelay());
			}
		}
	}


	private void depositItem(int id)
	{
		targetMenu = new MenuEntry("", "", 8, 57, utils.getInventoryWidgetItem(id).getIndex(),983043,false);
		utils.setMenuEntry(targetMenu);
		utils.delayMouseClick(utils.getInventoryWidgetItem(id).getCanvasBounds(),sleepDelay());
	}

	private void withdrawX(int ID){
		if(client.getVarbitValue(3960)!=14){
			utils.withdrawItemAmount(ID,14);
			timeout+=3;
		} else {
			targetMenu = new MenuEntry("", "", (client.getVarbitValue(6590) == 3) ? 1 : 5, MenuAction.CC_OP.getId(), utils.getBankItemWidget(ID).getIndex(), 786445, false);
			//utils.setMenuEntry(targetMenu);
			clickBounds = utils.getBankItemWidget(ID).getBounds()!=null ? utils.getBankItemWidget(ID).getBounds() : new Rectangle(client.getCenterX() - 50, client.getCenterY() - 50, 100, 100);
			utils.delayMouseClick(clickBounds,sleepDelay());
		}
	}

	private int checkRunEnergy()
	{
		try{
			return client.getEnergy();
		} catch (Exception ignored) {

		}
		return 0;
	}
}
