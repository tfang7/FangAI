	import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import javax.management.MBeanAttributeInfo;

import bwapi.Color;
import bwapi.DefaultBWListener;
import bwapi.Game;
import bwapi.Mirror;
import bwapi.Order;
import bwapi.Player;
import bwapi.Position;
import bwapi.PositionOrUnit;
import bwapi.TechType;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitCommand;
import bwapi.UnitType;
import bwapi.UpgradeType;
import bwapi.WeaponType;
import bwta.BWTA;
import bwta.BaseLocation;

public class FangBot extends DefaultBWListener {

    private Mirror mirror = new Mirror();
    
    private Game game;
    private Base mainBase;
    private Player self;
    private unitEnum uType = new unitEnum();
    private FangAi fang = new FangAi(uType);
    private FangSM fangState = new FangSM(fang);
    private BuildingUtil builder = new BuildingUtil();
    private BuildingManager buildManager = new BuildingManager(builder);
    private ArrayList<Base> allBases = new ArrayList<Base>();
    private BaseLocation target = null;
    private ArrayList<BaseLocation> checkedBases = new ArrayList<BaseLocation>();
    private HashSet<Position> enemyBuildingMemory = new HashSet<Position>();
    private Formation fangForm = new Formation(16);
    private boolean DEBUG_ENABLED;
    private ArrayList<TilePosition> expansionTiles = new ArrayList<TilePosition>();
    public void run() {
        mirror.getModule().setEventListener(this);
        mirror.startGame();
    }
    @Override
    public void onUnitDestroy(Unit unit){
    	String ftype = uType.eval(unit.getType()).toString();
		fang.removeUnit(ftype, unit);
		 if(fangForm.members.contains(unit)) fangForm.members.remove(unit);
		 if (unit.getID() == mainBase.scout.getID()) mainBase.scout = null;
    	
    }
    @Override
    public void onUnitCreate(Unit unit) {
    	String ftype = uType.eval(unit.getType()).toString();

    	fang.addUnit(ftype, unit);
    	if (unit.getType() == UnitType.Terran_Refinery){
    		mainBase.refinery = unit;
    	}
    	else if (unit.getType() == UnitType.Terran_Command_Center){
    		expansionTiles.add(unit.getTilePosition());
      		//mainBase = new Base(u, game);
    	//	Base b = new Base(unit, game, builder);
    	//	b.tilePos = u.getTilePosition();

    		//allBases.add(new Base(unit, game));
    		Base b = buildManager.addBase(unit, game);
    		if (mainBase == null) mainBase = b;
    		allBases.add(b);
    		if (checkResources(UnitType.Terran_Comsat_Station)){
    			unit.buildAddon(UnitType.Terran_Comsat_Station);
    		}
    	}
    	else if (unit.getType() == UnitType.Terran_SCV){
    		Base closest = buildManager.getClosestCC(allBases, unit);
    		if ( closest.workers != null){
    		//	System.out.println("CC is not null");
    			closest.workers.add(unit);
    			//System.out.println("CC units:" + buildManager.getClosestCC(allBases, unit).workers.size());
    		}
    		
    		if (mainBase.builders != null && mainBase.builders.size() < 3)
    		{
    			mainBase.addBuilder(unit);
    		}
    		else if (	
    				self.hasUnitTypeRequirement(UnitType.Terran_Refinery) 
    				&& mainBase.gassers != null 
    				&& mainBase.gassers.size() < 3 )
    		{
    			mainBase.addGasWorker(unit);
    		//	System.out.println("adding gas worker" + unit.getID() + " " + mainBase.gassers.size());
    		}

    	//	fang.addUnit(ftype, unit);
    		//fang.addWorker(unit);
    	}
        //System.out.println("New unit discovered " + unit.getType());
    }


    @Override
    public void onStart() {
        game = mirror.getGame();
        game.setLocalSpeed(5);
        self = game.self();
        DEBUG_ENABLED = true;
       // game.sendText("show me the money");
        //Use BWTA to analyze map
        //This may take a few minutes if the map is processed first time!
      //  System.out.println("Analyzing map...");
        BWTA.readMap();
        BWTA.analyze();
        game.sendText("gl hf, my elo is higher than yours - FangBot");
       //System.out.println("Map data ready");
      /*  for (Unit u : self.getUnits()){
        	if (u.getType() == UnitType.Terran_Command_Center){
        		//mainBase = new Base(u, game);
        		Base b = new Base(u, game, builder);
        	//	b.tilePos = u.getTilePosition();
        		mainBase = b;
        		allBases.add(b);
        	}
        }*/

  /*      int i = 0;
        for(BaseLocation baseLocation : BWTA.getBaseLocations()){
        	System.out.println("Base location #" + (++i) + ". Printing location's region polygon:");
        	for(Position position : baseLocation.getRegion().getPolygon().getPoints()){
        		System.out.print(position + ", ");
        	}
        }*/

    }

    @Override
    public void onFrame() {
        //game.setTextSize(10);
    	//game.drawTextScreen(10, 10, fang.workers.toString());
        StringBuilder units = new StringBuilder("My units:\n");
        
        checkEnemyMemory();
        //iterate through my units
        for (Unit myUnit : self.getUnits()) 
        {
        	units.append(myUnit.getType()).append(" ").append(myUnit.getTilePosition()).append("\n");
    		UnitType cType = myUnit.getType();
        	//getNextExpansion(mainBase.CC);
           
            if (myUnit.getType() == UnitType.Terran_SCV){
            	 SCV(myUnit, cType);
           //	game.drawTextMap(myUnit.getPosition(),"Supply val:" + supplyDiff);
            }
            else if (myUnit.getType() == UnitType.Terran_Command_Center )
    		{
    			int numSCV = numUnits(UnitType.Terran_SCV);
    			Base b = buildManager.getBase(myUnit);
				if (DEBUG_ENABLED){
    				game.drawTextMap(myUnit.getPosition(), "uid: " + myUnit.getID());
    				Position workerTextPosition = new Position(myUnit.getPosition().getX(), myUnit.getPosition().getY() + 10);
    				game.drawTextMap(workerTextPosition, "scvs: " + b.workers.size());
    			}
    		//	System.out.println("expansions : " + expansionTiles.size() * 27);
    			 //game.drawLineMap(getNextExpansion().getPosition(), mainBase.CC.getPosition(), Color.Blue);
    			//if (self.hasUnitTypeRequirement(UnitType.Terran_Comsat_Station)&& checkResources(UnitType.Terran_Comsat_Station))myUnit.build(UnitType.Terran_Comsat_Station);
    			if (myUnit.isIdle() && numSCV < expansionTiles.size() * 21 && numSCV < 60 && b != null){
    				if (myUnit.getID() == b.CC.getID()){
    					if (b.workers.size() < 21){
    						fangState.Produce(myUnit, UnitType.Terran_SCV);
    					}
    				}
    			}
    			
    		}
    		else if (myUnit.getType() == UnitType.Terran_Academy) Academy(myUnit);
    		else if (myUnit.getType() == UnitType.Terran_Factory) Factory(myUnit);
    		else if (myUnit.getType() == UnitType.Terran_Machine_Shop){
    			if (!self.hasResearched(TechType.Tank_Siege_Mode) && checkResources(TechType.Tank_Siege_Mode)) 
    				myUnit.research(TechType.Tank_Siege_Mode);
    		}
    		else if (myUnit.getType() == UnitType.Terran_Starport){
    			/*if (myUnit.canBuildAddon() && checkResources(UnitType.Terran_Control_Tower)){
    				TilePosition tp = builder.getBuildTile(myUnit,UnitType.Terran_Control_Tower, expansionTiles.get(expansionTiles.size() - 1), game);
    				myUnit.build(UnitType.Terran_Control_Tower, tp);
    			}*/
    			if (numUnits(UnitType.Terran_Science_Vessel)  <= 0 && self.hasUnitTypeRequirement(UnitType.Terran_Control_Tower)){
    				fangState.Produce(myUnit, UnitType.Terran_Science_Vessel);
    			}

    			
    		}
    		else if (myUnit.getType() == UnitType.Terran_Barracks && !myUnit.isTraining())
    		{
    			if (self.hasUnitTypeRequirement(UnitType.Terran_Academy))
    			{
    				int m = numUnits(UnitType.Terran_Marine);
    				int medic = numUnits(UnitType.Terran_Medic);
    				int f = numUnits(UnitType.Terran_Firebat);
    				
    				if (m / 3 >= f){
    					fangState.Produce(myUnit, UnitType.Terran_Firebat);
    				}
    				else if ( m / 5 >= medic ) fangState.Produce(myUnit, UnitType.Terran_Medic);

    				else 
    				{
    					fangState.Produce(myUnit, UnitType.Terran_Marine);
    				}
    			}
    			else fangState.Produce(myUnit, UnitType.Terran_Marine);	
    			}	
    		if (   myUnit.isCompleted() && 
				(
				 myUnit.getType() == UnitType.Terran_Medic 
				|| myUnit.getType() == UnitType.Terran_Firebat
				|| myUnit.getType() == UnitType.Terran_Marine
				|| myUnit.getType() == UnitType.Terran_Siege_Tank_Tank_Mode
				|| myUnit.getType() == UnitType.Terran_Siege_Tank_Siege_Mode
				|| myUnit.getType() == UnitType.Terran_Vulture
				)
			)
			{
				fangForm.add(myUnit);
				fangForm.FSM(myUnit, fangForm, Formation.State.MOVING, fangState, fang, game, enemyBuildingMemory);
			}
    		else if (myUnit.getType().isBuilding() && !myUnit.isCompleted() && !myUnit.isBeingConstructed()){
    			myUnit.cancelConstruction();
    		}
		
        }

        //draw my units on screen
        mainBase.drawLayout(game);
        game.drawTextScreen(10, 25, fangForm.currentState.toString());
        game.drawTextScreen(10, 45, "Marines: " + fang.getUnits(UnitType.Terran_Marine).size());
       // game.drawTextScreen(10, 40, game.getAPM() + " ");
       // game.drawTextScreen(10, 40, "formation: current: " +  formations.get(0).members.size() + "max" + fangForm.memberSize);
    }


    public Position findEnemyBuilding(int numEnemies){
		if (numEnemies > 0) {
			for (Position p : enemyBuildingMemory){
				if (p != null){
					return p;
				}
			}
		}
		return null;
    }

    public void Starport(Unit starport){
    	if (!self.hasUnitTypeRequirement(UnitType.Terran_Control_Tower) && checkResources(UnitType.Terran_Control_Tower) && starport.canBuildAddon()){
    		TilePosition toBuild = builder.getBuildTile(starport, starport.getType(), mainBase.barracks.toTilePosition(), game);
    		starport.build(UnitType.Terran_Control_Tower, toBuild);
    	}
    	else if (starport.canBuild(UnitType.Terran_Science_Vessel) && starport.isIdle() && numUnits(UnitType.Terran_Science_Vessel) < 1){
    		starport.train(UnitType.Terran_Science_Vessel);
    	}
    }
    public void Factory(Unit factory){
    	if (checkResources(UnitType.Terran_Machine_Shop) && factory.canBuildAddon()){
    		//TilePosition toBuild = builder.getBuildTile(factory, factory.getType(), expansionTiles.get(expansionTiles.size() - 1), game);
    	/*	if (!game.canBuildHere(toBuild, UnitType.Terran_Machine_Shop)){
    			if  (buildIndex < expansionTiles.size() - 1){
    				buildIndex++;
    			} else {
    				buildIndex = 0;
    			}
    			// toBuild = builder.getBuildTile(factory, factory.getType(),toBuild, game);
    		}*/
    		if (factory.buildAddon(UnitType.Terran_Machine_Shop)){
    			System.out.println("This is true: building addon");
    		}
    		else{
    			TilePosition toBuild = builder.getBuildTile(factory, factory.getType(), expansionTiles.get(expansionTiles.size() - 1), game);
    			factory.build(UnitType.Terran_Machine_Shop, toBuild);
    		}
    		//	factory.build(UnitType.Terran_Machine_Shop, toBuild);
    	}
    	else if (factory.isIdle()){
    		if (self.hasUnitTypeRequirement(UnitType.Terran_Machine_Shop)) factory.train(UnitType.Terran_Siege_Tank_Tank_Mode);
    		else {
    			factory.train(UnitType.Terran_Vulture);
    		}
    	}
    }
    public void Academy(Unit academy){
    	//System.out.println("Doing academy things...");
    	if (!academy.isResearching() && !academy.isBeingConstructed()){
    		if (!self.hasResearched(TechType.Stim_Packs)){
    			academy.research(TechType.Stim_Packs);
    		}
    		else if (academy.canUpgrade(UpgradeType.U_238_Shells)) {
    			academy.upgrade(UpgradeType.U_238_Shells);
    		/*	List<TechType> techList = academy.getType().researchesWhat();
    			UnitType.Terran_Marine.upgrades();
    			for (TechType t : techList){
    				if (!self.hasResearched(t)){
    					academy.research(t);
    				}
    			}*/
    		}
    	}
    }
    public void SCV(Unit myUnit, UnitType cType)
    {
        int supplyDiff = game.self().supplyTotal() - game.self().supplyUsed() - checkProductionRate();

    	if (cType == UnitType.Terran_SCV){
    		
    		if (mainBase.scout == null){
    			if (!contains(myUnit, mainBase.builders)){
    				mainBase.scout = myUnit;
    			}
    		}
    		
    	    if (contains(myUnit, mainBase.builders))
			{					
				if (expansionTiles.size() > 1 && !checkConstructing(UnitType.Terran_Starport) && self.hasUnitTypeRequirement(UnitType.Terran_Factory) 
						&& !self.hasUnitTypeRequirement(UnitType.Terran_Starport)) {
			   // 	TilePosition toBuild = builder.getBuildTile(myUnit, UnitType.Terran_Starport, expansionTiles.get(expansionTiles.size() - 1), game);
			    	//fangState.Produce(myUnit, UnitType.Terran_Starport, toBuild);
					//buildStarport(myUnit);
				}
				//	buildStarport(myUnit);
				 if (!self.hasUnitTypeRequirement(UnitType.Terran_Science_Facility) 
						&& self.hasUnitTypeRequirement(UnitType.Terran_Starport)
						&& !checkConstructing(UnitType.Terran_Science_Facility)){
			    	TilePosition toBuild = builder.getBuildTile(myUnit, UnitType.Terran_Science_Facility, mainBase.supplies.toTilePosition(), game);
			    	fangState.Produce(myUnit, UnitType.Terran_Science_Facility, toBuild);
				}
			//	buildStarport(myUnit);
				buildFactory(myUnit);
			    if (buildManager.build(myUnit, UnitType.Terran_Academy, game, checkProductionRate())
				    	&& !checkConstructing(UnitType.Terran_Academy))
				    {
				    	TilePosition toBuild = builder.getBuildTile(myUnit, UnitType.Terran_Academy, mainBase.supplies.toTilePosition(), game);
				    	fangState.Produce(myUnit, UnitType.Terran_Academy, toBuild);
				    }
			    
//        		System.out.println("there are " + mainBase.builders.size() + "workers");
			    if (	   !checkConstructing(UnitType.Terran_Barracks)
			    		&& buildManager.build(myUnit, UnitType.Terran_Barracks, game, checkProductionRate())
			    		&& numUnits(UnitType.Terran_Barracks) < (2 * expansionTiles.size()) + 1)
			    {
					TilePosition toBuild = builder.getBuildTile(myUnit, UnitType.Terran_Barracks, expansionTiles.get(expansionTiles.size() - 1), game);
					fangState.Produce(myUnit, UnitType.Terran_Barracks, toBuild);
				}
			  //  buildEngineeringBay(myUnit);
			    if (numUnits(UnitType.Terran_Refinery) <= 0 
			    		&& buildManager.build(myUnit, UnitType.Terran_Refinery, game, checkProductionRate())
			    		&& !checkConstructing(UnitType.Terran_Refinery))
			    {
			    	List<Unit> geysers = game.getGeysers();
			    	int dist;
			    	for (Unit g : geysers)
			    	{
			    		dist = g.getDistance(mainBase.CC.getPosition());
			    		if (dist < 200){
			    			TilePosition toBuild = builder.getBuildTile(myUnit, UnitType.Terran_Refinery, g.getTilePosition(), game);
			    			fangState.Produce(myUnit, UnitType.Terran_Refinery, toBuild);
			    			break;
			    		}
			    	//	game.drawLineMap(g.getPosition(), mainBase.CC.getPosition(), Color.Red);
			    	}
			    }
			    if (supplyDiff <= (checkProductionRate() + 5) && 
			    		buildManager.build(myUnit,UnitType.Terran_Supply_Depot, game, 
			    		checkProductionRate()) 
			    		&& !checkConstructing(UnitType.Terran_Supply_Depot))
			    {
					TilePosition toBuild = builder.getBuildTile(myUnit, UnitType.Terran_Supply_Depot, mainBase.supplies.toTilePosition(), game);
					fangState.Produce(myUnit, UnitType.Terran_Supply_Depot, toBuild);
				}
				else 
				{
    				fangState.Action(myUnit, game, FangSM.Role.HARVESTER);
    			}
    		}
			//System.out.println(contains(myUnit, mainBase.gassers));
			else if (contains(myUnit, mainBase.gassers) && !myUnit.isBeingConstructed())
		    {
				if (!myUnit.isGatheringGas()){
					for (Unit u : self.getUnits()){
						if (u.getType().isRefinery()){
							myUnit.issueCommand(UnitCommand.gather(myUnit, u));
						}
					}
				}
		    }
			else if (mainBase.scout.getID() == myUnit.getID()) scoutEnemy(myUnit);
			else 
			{
				fangState.Action(myUnit, game, FangSM.Role.HARVESTER);
			}
    	}
    }
    public boolean checkResources(UnitType type){
    	return (self.minerals() >= type.mineralPrice() && self.gas() >= type.gasPrice());
    }
    public boolean checkResources(TechType type){
    	return (self.minerals() >= type.mineralPrice() && self.gas() >= type.gasPrice());
    }
    public void scoutEnemy(Unit u)
    {
		if (	
				enemyBuildingMemory.size() == 0 && 
				self.hasUnitTypeRequirement(UnitType.Terran_Supply_Depot)){
			
			if (target == null)
			{
				target = getDest(BWTA.getStartLocations());
			}

			
			else if (target != null && u.getDistance(target.getPosition()) < 100) 
			{
				checkedBases.add(target);
				target = null;
			}
			else if (u.isGatheringMinerals() || u.isIdle()){
				u.move(target.getPosition());
			}
			
			
		}
		else if (self.minerals() > 400 && expansionTiles.size() < 3)
    	{
			TilePosition next = getNextExpansion(self).getTilePosition();
    	//	double dist = next.getDistance(u.getTilePosition());
    		game.drawLineMap(next.toPosition(), u.getPosition(), Color.Orange);
    		if (game.isVisible(next)){
    			if (!checkConstructing(UnitType.Terran_Command_Center)) u.build(UnitType.Terran_Command_Center, next);
    		}
    		else if (u.getID() == mainBase.scout.getID()){
    			u.move(next.toPosition());
    		}
    		//	System.out.println("Dist from expansion: " + expansionTiles.size());

		}
    	else 
    	{
    		fangState.Action(u, game, FangSM.Role.HARVESTER);
    	}
		
    }
    public void buildStarport(Unit u){
	    if (checkResources(UnitType.Terran_Starport) 
	    	&& numUnits(UnitType.Terran_Starport) < 1)
	    {
	    	TilePosition toBuild = builder.getBuildTile(u, UnitType.Terran_Starport,expansionTiles.get(expansionTiles.size()-1), game);
	    	fangState.Produce(u, UnitType.Terran_Starport, toBuild);
	    	//mainBase.ebayBuilt = true;
	    }
    }
    
    public void buildFactory(Unit u){
	    if (checkResources(UnitType.Terran_Factory) && numUnits(UnitType.Terran_Factory) < expansionTiles.size() 
	        && !checkConstructing(UnitType.Terran_Factory)  
	        && self.hasUnitTypeRequirement(UnitType.Terran_Barracks) 
	        && self.hasUnitTypeRequirement(UnitType.Terran_Academy))
	    {
	    	//mainBase.barracks.toTilePosition()
	    	TilePosition toBuild = builder.getBuildTile(u, UnitType.Terran_Factory, expansionTiles.get(expansionTiles.size()-1), game);
	    	
	    		fangState.Produce(u, UnitType.Terran_Factory, toBuild);
	    	
	    	
	    	//mainBase.ebayBuilt = true;
	    }
    }
    public void buildEngineeringBay(Unit u){
	    if (checkResources(UnitType.Terran_Engineering_Bay) && numUnits(UnitType.Terran_Engineering_Bay) <= 0 
	        && !checkConstructing(UnitType.Terran_Engineering_Bay)  
	        && self.hasUnitTypeRequirement(UnitType.Terran_Barracks) 
	        && self.hasUnitTypeRequirement(UnitType.Terran_Academy))
	    {
	    	TilePosition toBuild = builder.getBuildTile(u, UnitType.Terran_Engineering_Bay, mainBase.supplies.toTilePosition(), game);
	    	fangState.Produce(u, UnitType.Terran_Engineering_Bay, toBuild);
	    	//mainBase.ebayBuilt = true;
	    }
    }

    public boolean contains(Unit u, ArrayList<Unit> list){
    	for (Unit unit : list){
    		if (unit.getID() == u.getID()){
    			return true;
    		}
    	}
    	return false;
    }
    ArrayList<Unit> getUnits(UnitType unit){
    	return fang.getUnitList(uType.eval(unit).toString());
    }
    public boolean checkConstructing(UnitType building){
    	ArrayList<Unit> scvs = mainBase.builders;//fang.getUnitList(uType.eval(UnitType.Terran_SCV).toString());
    	Unit b;
    	for (Unit u : scvs){
    		if (u.isConstructing()){
    			b = u.getBuildUnit();
    			if (b.getType() == building){
    				return true;
    			}
    		}
    	}
    	return false;
    }
    public int numUnits(UnitType unitType)
    {
    	int num = fang.getUnitsLength(uType.eval(unitType).toString());
    	return num;
    }
    public BaseLocation getNextExpansion(Player player)
    {
    	TilePosition start = self.getStartLocation();
    	
    	BaseLocation result = null;
    	double dist = 10000;
    	double newDist = 0;
    	//BaseLocation
    	for (BaseLocation b : BWTA.getBaseLocations()){
    		newDist = b.getDistance(start.toPosition());
    		//game.drawLineMap(start.toPosition(), b.getTilePosition().toPosition(), Color.Red);
    		//game.drawLineMap(start.toPosition(), b.getPosition(), Color.Red);
    		//System.out.println(newDist);
    		if (newDist < dist && newDist > 100 && !(expansionTiles.contains(b.getTilePosition()))){
    			dist = newDist;
    			result = b;
    		}

    	}
    	return result;
    	//return result;
    	}
  //  	game.drawLineMap(closest.getPosition(), toCheck.getPosition(),Color.Blue);
    public boolean checkExpansions(TilePosition pos){
    	for (Base b : allBases){
    		if (b.CC.getTilePosition() == pos){
    			return true;
    		}
    	}
    	return false;
    }
    public BaseLocation getDest(List<BaseLocation> loc)
    {
    	for (BaseLocation baseLocation : loc){
			if (!checkedBases.contains(baseLocation) && !game.isVisible(baseLocation.getTilePosition()) )
			{
				return baseLocation;
			}
    	}
    	return null;
    }
    public int checkProductionRate() 
    {
    	int productionRate = 0;
    	for (Unit u : self.getUnits()){
    		if (u.isTraining()){
    			productionRate++;
    		}
    	}
    	return productionRate;
    }
    public TilePosition getClosestUnitTo(ArrayList<TilePosition> tilePos, Unit u){
    	int min = u.getDistance(tilePos.get(0).toPosition());
    	TilePosition closest = tilePos.get(0);
    	for (TilePosition pos : tilePos){
    		if (pos != closest){
    			int dist = u.getDistance(pos.toPosition());
    			if (  dist < min ){
    				closest = pos;
    				min = dist;
    			}
    		}
    	}
    	return closest;
    }

    public Unit getClosestEnemy(Unit u, List<Unit> enemies)
    {
    	Unit closest = enemies.get(0);
    	int min = closest.getDistance(u);
    	for (Unit e : enemies){
    		if (e.getDistance(u) < min){
    			closest = e;
    			min = closest.getDistance(u);
    		}
    	}
    	return closest;
    }
    public void checkEnemyMemory(){
    	for (Unit u : game.enemy().getUnits()){
    		if (u.getType().isBuilding()){
    			if (!enemyBuildingMemory.contains(u.getPosition())) enemyBuildingMemory.add(u.getPosition());
    		}
    	}
    	for (Position p : enemyBuildingMemory){
    		TilePosition tileMemory = new TilePosition(p.getX()/32, p.getY()/32);
    		if (game.isVisible(tileMemory)){
    			boolean buildingStillThere = false;
    			for (Unit u: game.enemy().getUnits()){
    				if (u.getType().isBuilding() && (u.getPosition() == p)){
    					buildingStillThere = true;
    					break;
    				}
    			}
    			if (!buildingStillThere){
    				enemyBuildingMemory.remove(p);
    				break;
    			}
    		}
    	}
    }
    public static void main(String[] args) {
        new FangBot().run();
    }
}