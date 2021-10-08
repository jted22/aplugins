
package net.runelite.client.plugins.acombiner;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.Point;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ConfigButtonClicked;
import net.runelite.api.events.GameTick;
import net.runelite.api.queries.GameObjectQuery;
import net.runelite.api.widgets.WidgetInfo;
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
import java.awt.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
//import static net.runelite.client.plugins.afletcher.AFletcherState.*;
import static net.runelite.client.plugins.acombiner.ACombinerState.*;
@Extension
@PluginDependency(AUtils.class)
@PluginDescriptor(
		name = "ACombiner",
		description = "Combines Items For You..."
)
@Slf4j
public class ACombinerPlugin extends Plugin
{

	@Inject
	private Client client;

	@Inject
	private AUtils utils;

	@Inject
	private ConfigManager configManager;

	@Inject
	OverlayManager overlayManager;

	@Inject
	private ItemManager itemManager;

	@Inject
	private ACombinerConfig config;

	@Inject
	private ACombinerOverlay overlay;

	MenuEntry targetMenu;
	Instant botTimer;
	Player player;
	boolean firstTime;
	ACombinerState state;
	boolean startFireMaker;
	boolean withdrawn1 = false;
	boolean withdrawn2 = false;
	int timeout = 0;
	boolean started = false;
	boolean walkAction;
boolean deposited = false;
	int coordX;
	int coordY;
	GameObject targetObject;
	final Set<GameObject> fireObjects = new HashSet<>();
	final Set<Integer> requiredItems = new HashSet<>();
	boolean[] pathStates;
	WorldPoint currentLoc;
	WorldPoint beforeLoc;

	// Provides our config
	@Provides
	ACombinerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ACombinerConfig.class);
	}

	@Override
	protected void startUp()
	{
		// runs on plugin startup
		log.info("Plugin started");
		botTimer = Instant.now();
		walkAction=false;
		coordX=0;
		coordY=0;
		firstTime=true;
		started=false;
		startFireMaker=false;
		requiredItems.clear();
		requiredItems.add(946);
		pathStates = null;

		// example how to use config items
	}

	@Override
	protected void shutDown()
	{
		// runs on plugin shutdown
		log.info("Plugin stopped");
		overlayManager.remove(overlay);
		startFireMaker=false;
		fireObjects.clear();
		pathStates = null;
		requiredItems.clear();
	}

	private long sleepDelay()
	{
		return utils.randomDelay(false, config.tickDelayMin(),config.tickDelayMax(),config.tickDelayDev(),config.tickDelayTarg());
	}

	private int tickDelay()
	{
		return (int) utils.randomDelay(false,2,3,1,2);
	}

	@Subscribe
	private void onConfigButtonPressed(ConfigButtonClicked configButtonClicked)
	{
		if (!configButtonClicked.getGroup().equalsIgnoreCase("ACombiner"))
		{
			return;
		}
		log.info("button {} pressed!", configButtonClicked.getKey());
		if (configButtonClicked.getKey().equals("startButton"))
		{
			if (!startFireMaker)
			{
				startUp();
				startFireMaker = true;
				targetMenu = null;
				firstTime = true;
				deposited = false;
				withdrawn1 = false;
				withdrawn2 = false;
				started = false;
				botTimer = Instant.now();
				overlayManager.add(overlay);
			} else {
				shutDown();
			}
		}
	}

	@Subscribe
	private void onConfigChanged(ConfigChanged event)
	{
		if (!event.getGroup().equals("ACombiner"))
		{
			return;
		}
		startFireMaker = false;
	}

	@Subscribe
	private void onGameTick(GameTick gameTick)
	{
		if (!startFireMaker)
		{
			return;
		}
		if (!client.isResized())
		{
			utils.sendGameMessage("client must be set to resizable");
			startFireMaker = false;
			return;
		}
		player = client.getLocalPlayer();

		if(player==null){
			state = NULL_PLAYER;
			return;
		}
		//beforeLoc=currentLoc;
		//currentLoc=player.getWorldLocation();
		/*if(player.getAnimation()!=-1){
			state = ANIMATING;
			timeout=tickDelay();
			return;
		}*/
		/*if(currentLoc.getX()!=beforeLoc.getX() ||
			currentLoc.getY()!=beforeLoc.getY()){
			state = MOVING;
			return;
		}*/
		if(timeout>0){
			//utils.handleRun(30, 20);
			timeout--;
			return;
		}

		if(!utils.isBankOpen() && !utils.inventoryContains(config.logId())) {
			started = false;
			openNearestBank();
			timeout = tickDelay();
			state = OPEN_BANK;
			return;
		}

		if(!utils.isBankOpen()){
			if(firstTime && !started){
				//targetMenu=new MenuEntry("", "", config.logId(), 38, utils.getInventoryWidgetItem(config.logId()).getIndex(), WidgetInfo.INVENTORY.getId(),false);
				targetMenu=new MenuEntry("Use","<col=ff9040>"+itemManager.getItemComposition(config.logId()).getName(),config.logId(),38,utils.getInventoryWidgetItem(config.logId()).getIndex(),WidgetInfo.INVENTORY.getId(),false);
				utils.setMenuEntry(targetMenu);
				utils.delayMouseClick(getRandomNullPoint(),sleepDelay());
				firstTime=false;
				state=USE_FIRST;
				return;
			}
			if (config.shortbow() && !started){
				//targetMenu=new MenuEntry("", "", config.logId(), 31, utils.getInventoryWidgetItem(config.logId2()).getIndex(), WidgetInfo.INVENTORY.getId(),false);
				targetMenu = new MenuEntry("Use","<col=ff9040>"+itemManager.getItemComposition(config.logId()).getName()+"<col=ffffff> -> <col=ff9040>"+itemManager.getItemComposition(config.logId2()).getName(),config.logId2(),31,utils.getInventoryWidgetItem(config.logId2()).getIndex(),WidgetInfo.INVENTORY.getId(),false);
				utils.setMenuEntry(targetMenu);
				utils.delayMouseClick(getRandomNullPoint(),sleepDelay());
				//if(client.getWidget(270,5)!=null){
					//if(client.getWidget(270,5).getText().equals("What would you like to make?")){
						timeout=3;
						started = true;
						firstTime = true;
						//targetMenu=new MenuEntry("","",1,57,-1,17694735,false);//Id	17694735
						//utils.setMenuEntry(targetMenu);
						//utils.delayMouseClick(client.getWidget(270,5).getBounds(), sleepDelay());
					//}
				//}
			}
			/*if(!utils.isBankOpen() && started && utils.inventoryContains(946) && utils.inventoryItemContainsAmount(config.logId(),27,false,true)){
				started = false;
				firstTime = true;
				timeout=tickDelay();
				return;
			}*/
			timeout = tickDelay();
			state = USE_ITEMS;
			return;
		}
		if(utils.isBankOpen() && utils.inventoryContains(config.logId2()) && utils.inventoryContains(config.logId())){
			closeBank();
			started = false;
			deposited = false;
			withdrawn1 = false;
			withdrawn2 = false;
			firstTime=true;
			timeout=tickDelay();
			state = CLOSE_BANK;
			return;
		}
		if(utils.isBankOpen() && !deposited && !utils.inventoryContains(config.logId2()) && !utils.inventoryContains(config.logId())){
			utils.depositAll();
			started = false;
			deposited = true;
			firstTime = true;
			timeout=tickDelay();
			state = DEPOSIT_ITEMS;
			return;
		}
		if(utils.isBankOpen() && !withdrawn1 && !utils.inventoryContains(config.logId())){
			utils.withdrawItemAmount(config.logId(), config.log1Amt());
			withdrawn1 = true;
			//utils.withdrawAllItem(946);
			started = false;
			firstTime = true;
			timeout=tickDelay();
			state = WITHDRAW_ITEM1;
			return;
		}
		if(utils.isBankOpen() && !withdrawn2 && !utils.inventoryContains(config.logId2())){
			utils.withdrawItemAmount(config.logId2(), config.log2Amt());
			//utils.withdrawAllItem(config.logId());
			started = false;
			withdrawn2 = true;
			firstTime = true;
			timeout=tickDelay();
			state = WITHDRAW_ITEM2;
			return;
		}
		started = false;
		withdrawn1 = false;
		withdrawn2 = false;
		deposited = false;
		firstTime = true;
		state = NOT_SURE;
	}

	private void openNearestBank()
	{
		GameObject bankTarget = utils.findNearestBank();
		if (bankTarget != null) {
			targetMenu = new MenuEntry("", "", bankTarget.getId(),
					utils.getBankMenuOpcode(bankTarget.getId()), bankTarget.getSceneMinLocation().getX(),
					bankTarget.getSceneMinLocation().getY(), false);
			utils.doActionMsTime(targetMenu, bankTarget.getConvexHull().getBounds(), sleepDelay());
		} else {
			utils.sendGameMessage("Bank not found");
			//startPowerSkiller = false;
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

	private void closeBank()
	{
		targetMenu = new MenuEntry("Close", "", 1, 57, 11, 786434, false);
		utils.setMenuEntry(targetMenu);
		utils.delayMouseClick(getRandomNullPoint(),sleepDelay());
	}
}