	import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import bwapi.Color;
import bwapi.DefaultBWListener;
import bwapi.Game;
import bwapi.Mirror;
import bwapi.Player;
import bwapi.Position;
import bwapi.PositionOrUnit;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitCommand;
import bwapi.UnitType;
import bwta.BWTA;
import bwta.BaseLocation;

public class FangBot extends DefaultBWListener {

    private Mirror mirror = new Mirror();
    
    private Game game;
    private Base mainBase;
    private Player self;
    private unitEnum uType = new unitEnum();
    private FangAi fang = new FangAi();
    private FangSM fangState = new FangSM(fang);
    private BuildingUtil builder = new BuildingUtil();
    private ArrayList<Base> allBases = new ArrayList<Base>();
    private ArrayList<UnitType> buildOrder = new ArrayList<UnitType>();
    private BaseLocation target = null;
    private Position enemyLastSeen;
    private ArrayList<BaseLocation> checkedBases = new ArrayList<BaseLocation>();
    private HashSet<Position> enemyBuildingMemory = new HashSet<Position>();
    private Formation fangForm = new Formation();
    
    public void run() {
        mirror.getModule().setEventListener(this);
        mirror.startGame();
    }
    @Override
    public void onUnitDestroy(Unit unit){
    	String ftype = uType.eval(unit.getType()).toString();
		fang.removeUnit(ftype, unit);
    	
    }
    @Override
    public void onUnitCreate(Unit unit) {
    	String ftype = uType.eval(unit.getType()).toString();
    	fang.addUnit(ftype, unit);
    	if (unit.getType() == UnitType.Terran_Refinery){
    		mainBase.refinery = unit;
    	}
    	if (unit.getType() == UnitType.Terran_SCV){

    		
    		if (mainBase.builders != null && mainBase.builders.size() < 2)
    		{
    		
    			mainBase.addBuilder(unit);
    			System.out.println("adding builder" + unit.getID() + " " + mainBase.builders.size());
    		}
    		
/*    		else if (	
    				self.hasUnitTypeRequirement(UnitType.Terran_Refinery) 
    				&& mainBase.gassers != null 
    				&& mainBase.gassers.size() < 3 )
    		{
    			mainBase.addGasWorker(unit);
    			System.out.println("adding gas worker" + unit.getID() + " " + mainBase.gassers.size());
    		}*/
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
       // game.sendText("show me the money");
        //Use BWTA to analyze map
        //This may take a few minutes if the map is processed first time!
      //  System.out.println("Analyzing map...");
        BWTA.readMap();
        BWTA.analyze();
        //System.out.println("Map data ready");
        for (Unit u : self.getUnits()){
        	if (u.getType() == UnitType.Terran_Command_Center){
        		mainBase = new Base(u, game);
        		allBases.add(mainBase);
        	}

        	
        }
        
        
        
        
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
        int supplyDiff = game.self().supplyTotal() - game.self().supplyUsed() - checkProductionRate();
      //  System.out.println("Supply difference: " + supplyDiff);
        boolean buildPylon = supplyDiff <= (checkProductionRate() + 5) && self.minerals() >= 100 && !checkConstructing(UnitType.Terran_Supply_Depot);
        if (enemyLastSeen != null){
        	game.drawLineMap(enemyLastSeen, mainBase.CC.getPosition(), Color.Red);
        }
        for (Unit myUnit : self.getUnits()) 
        {
        	units.append(myUnit.getType()).append(" ").append(myUnit.getTilePosition()).append("\n");
    		UnitType cType = myUnit.getType();
        	//getNextExpansion(mainBase.CC);
        	if (myUnit.getType() == UnitType.Terran_SCV){
        		scoutEnemy(myUnit);
    			if (contains(myUnit, mainBase.builders))
    			{
//            		System.out.println("there are " + mainBase.builders.size() + "workers");
    			    if (	supplyDiff >= 5 
    			    		&& self.hasUnitTypeRequirement(UnitType.Terran_Supply_Depot) 
    			    		&& self.minerals() > 150 
    			    		&& numUnits(UnitType.Terran_Barracks) < mainBase.maxBarracks)
    			    {
    			    	
    					TilePosition toBuild = builder.getBuildTile(myUnit, UnitType.Terran_Barracks, mainBase.supplies.toTilePosition(), game);
    					fangState.Produce(myUnit, UnitType.Terran_Barracks, toBuild);
    				}
    			    if (numUnits(UnitType.Terran_Refinery) <= 0 && self.minerals() > 75 && 
    			    	self.hasUnitTypeRequirement(UnitType.Terran_Barracks) && !checkConstructing(UnitType.Terran_Refinery)){
    			    	List<Unit> geysers = game.getGeysers();
    			    	int dist;
    			    	for (Unit g : geysers)
    			    	{
    			    		dist = g.getDistance(mainBase.CC.getPosition());
    			    		if (dist < 200){
    			    			//System.out.println(dist);
    			    			//System.out.println(g.getType());
    			    			TilePosition toBuild = builder.getBuildTile(myUnit, UnitType.Terran_Refinery, g.getTilePosition(), game);
    			    			fangState.Produce(myUnit, UnitType.Terran_Refinery, toBuild);
    			    			break;
    			    			//mainBase.refineryBuilt = true;
    			    		}
    			    		//System.out.println();
    			    		game.drawLineMap(g.getPosition(), mainBase.CC.getPosition(), Color.Red);
    			    		//if (g.getDistance(mainBase.CC.getPosition())
    			    	}
    			    }

    			    if (buildPylon)
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
    		/*	if (contains(myUnit, mainBase.gassers) && myUnit.isIdle())
			    {
    				//System.out.println("gathering gas..." + mainBase.gassers.size());
			    	fangState.Action(myUnit, game, FangSM.Role.GAS, fang);
			    	
			    }*/
				else 
				{
    				fangState.Action(myUnit, game, FangSM.Role.HARVESTER);
    			}
        	//	
        	//	
        	}
            
    		if (myUnit.getType() == UnitType.Terran_Command_Center && numUnits(UnitType.Terran_SCV) < mainBase.maxWorkers){
    			fangState.Produce(myUnit, UnitType.Terran_SCV); 			
    			mainBase.drawLayout(game);
			
    		}
    		if (myUnit.getType() == UnitType.Terran_Barracks){
    			fangState.Produce(myUnit, UnitType.Terran_Marine);
    			
    		}
    		if (myUnit.getType() == UnitType.Terran_Marine && myUnit.exists()){

    			Unit attackEnemy = getClosestEnemy(myUnit, game.enemy().getUnits());
    			if (attackEnemy.getDistance(myUnit) < 300){
    				PositionOrUnit attackE = new PositionOrUnit(attackEnemy);
    				fangState.Action(myUnit, game, FangSM.Role.RANGED, attackE.getUnit());
    				//break;
    			}
    			else if (numUnits(UnitType.Terran_Marine) > 12) {
    				//Formation.FSM()
				//	fangForm.FSM(Formation.State.ATTACKING, getUnits(UnitType.Terran_Marine));
    				
    				//System.out.println(game.enemy().getUnits().size());
    				if (game.enemy().getUnits().size() >= 0) {
    					for (Position p : enemyBuildingMemory){
    						if (p != null){
    							//System.out.println("attackinging..." + p.getPoint());
    							PositionOrUnit attackPos = new PositionOrUnit(p);
    							game.drawLineMap(attackPos.getPosition(), myUnit.getPosition(), Color.Black);
    							fangState.Action(myUnit, game, FangSM.Role.RANGED, attackPos);
    							//myUnit.issueCommand(UnitCommand.attack(myUnit, pouOrUnit));
    							break;
    						}
    					}
    				}
    				
    			}
    			
    			
        		//	PositionOrUnit attackPos = new PositionOrUnit(mainBase.CC);
        		//	fangState.Action(myUnit, game, FangSM.Role.RANGED, mainBase.CC);

   		/*	if (game.enemy().getUnits().size() > 0){
    				Position enemyCenter = builder.getCenter(game.enemy().getUnits());
    				enemyLastSeen = enemyCenter;
    				PositionOrUnit pouOrUnit = new PositionOrUnit(enemyCenter);
    				myUnit.issueCommand(UnitCommand.attack(myUnit, pouOrUnit));
    				//builder.drawLine(game, myUnit.getPosition(), enemyCenter);
    			}*/
    			//else {
    		/*		if (numUnits(UnitType.Terran_Marine) > 36) {
    					if (game.enemy().getUnits().size() > 0){
    					}
    					for (Position p : enemyBuildingMemory){
    						if (p != null){
    							//System.out.println("attackinging..." + p.getPoint());
    							PositionOrUnit pouOrUnit = new PositionOrUnit(p);
    							myUnit.issueCommand(UnitCommand.attack(myUnit, pouOrUnit));
    							break;
    						}
    					}
    					
    					//Position[] e = (Position[]) enemyBuildingMemory.toArray(); 
    					//PositionOrUnit pouOrUnit = new PositionOrUnit(e[0]);
						//myUnit.issueCommand(UnitCommand.attack(myUnit, pouOrUnit));
						//builder.drawLine(game, myUnit.getPosition(), enemyLastSeen);
						
    				}*/
    			//}
    /*				else if (enemyLastSeen == null && numUnits(UnitType.Terran_Marine) > 24) {
    					
    				
    				if (target == null){
    					target = getDest();
    				}
    				else if (target != null && myUnit.getDistance(target.getPosition()) < 10) {
    					checkedBases.add(target);
        				System.out.println("Dist to Target: " + myUnit.getDistance(target));
        				System.out.println(checkedBases.size());
        				target = null;
    				}
						PositionOrUnit pouOrUnit = new PositionOrUnit(target.getPosition());
						myUnit.issueCommand(UnitCommand.attack(myUnit, pouOrUnit));
						builder.drawLine(game, myUnit.getPosition(), target.getPosition());*/
					//	if (myUnit.getDistance(baseLocation.getPosition()) < 10){
					//		checkedBases.add(baseLocation);
					//	}
    						
    						//target
    					
    				
    				//myUnit.issueCommand(UnitCommand.)
    			//}
    		//	}

    		}

           game.drawTextScreen(50, 25,"" + myUnit.getType());

        }

        //draw my units on screen
        //game.drawTextScreen(10, 25, units.toString());
    }
    public void scoutEnemy(Unit u)
    {
		if (enemyBuildingMemory.size() == 0 && self.hasUnitTypeRequirement(UnitType.Terran_Supply_Depot)){
			
			if (target == null)
			{
				target = getDest();
			}
    		if (mainBase.scout == null){
    			if (!mainBase.builders.contains(u)){
    				mainBase.scout = u;
    			}
    		}
			if (u == mainBase.scout)
			{
				if (target != null && u.getDistance(target.getPosition()) < 10) 
				{
					checkedBases.add(target);
    				target = null;
				}
				u.move(target.getPosition());
			}
		}
    }
    public void buildEngineeringBay(Unit u){
	    if (self.minerals() > 200 && numUnits(UnitType.Terran_Engineering_Bay) <= 0 &&
	        !checkConstructing(UnitType.Terran_Engineering_Bay) && 
	        self.hasUnitTypeRequirement(UnitType.Terran_Barracks))
	    {
	    	System.out.println("Should be constructing an engineering bay");
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
    	ArrayList<Unit> scvs = fang.getUnitList(uType.eval(UnitType.Terran_SCV).toString());
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
    public int numUnits(UnitType unitType){
    	
    	int num = fang.getUnitsLength(uType.eval(unitType).toString());
    //	System.out.println("There are " + num + " units");
    	return num;
    }
    public void getNextExpansion(Unit toCheck){
    	BaseLocation closest = BWTA.getBaseLocations().get(0);
    	for (BaseLocation baseLocation : BWTA.getBaseLocations()){
    		if (baseLocation.isStartLocation()) continue;
    		else if (baseLocation.getDistance(toCheck.getPosition()) > closest.getDistance(toCheck.getPosition())){
    			closest = baseLocation;
    			game.drawLineMap(closest.getPosition(), toCheck.getPosition(),Color.Red);
    		}
    	}
    	game.drawLineMap(closest.getPosition(), toCheck.getPosition(),Color.Blue);
    	
    }
    public BaseLocation getDest(){
    	for (BaseLocation baseLocation : BWTA.getStartLocations()){
			if (!checkedBases.contains(baseLocation) && !game.isVisible(baseLocation.getTilePosition()) )
			{
				return baseLocation;
			}
    	}
    	return null;
    }
    public int checkProductionRate() {
    	int productionRate = 0;
    	for (Unit u : self.getUnits()){
    		if (u.isTraining()){
    			productionRate++;
    		}
    	}
    	return productionRate;
    }
    public Unit getClosestEnemy(Unit u, List<Unit> enemies){
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
    public boolean isMineral(Unit u){
    	return u.getType().isMineralField();
    }
    public void cheese(Unit myUnit){
    	if (mainBase.builders != null){
    		boolean buildPylon = game.self().supplyTotal() - game.self().supplyUsed() < 2 && self.minerals() >= 100;
    		
    		if (myUnit.getType() == UnitType.Terran_SCV){
    			FangSM.Role r = FangSM.Role.HARVESTER;
    	   		if (mainBase.builders.contains(myUnit)){
        			r = FangSM.Role.BUILDER;
    				if (buildPylon){
    					fangState.Produce(myUnit, UnitType.Terran_Supply_Depot, mainBase.CC.getTilePosition());
    				}
        		}
    	   		else{
    	   			fangState.Action(myUnit, game, r );
    	   		}
    	   		
    		}
    	}
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
/*
//if there's enough minerals, train an SCV
boolean isWorker = myUnit.getType().isWorker();
boolean isBuilding = myUnit.getType().isBuilding();
if (isBuilding){
	if (myUnit.getType() == UnitType.Terran_Barracks && myUnit.isCompleted() && myUnit.isIdle()){
    	myUnit.train(UnitType.Terran_Marine);   
    	break;
	}
	else if (myUnit.getType() == UnitType.Terran_Command_Center && self.minerals() >= 50 && myUnit.isIdle()) {
       myUnit.train(UnitType.Terran_SCV);
       break;
    }

}

if (isWorker){

    if (self.hasUnitTypeRequirement(UnitType.Terran_Supply_Depot) && self.minerals() >= 150 && !fang.barracksBuilt){
    	System.out.println("building barracks");
    	TilePosition check = new TilePosition(self.getStartLocation().getX() + (8 * dir), self.getStartLocation().getY() + (3 * dir));
    	TilePosition buildTile = getBuildTile(myUnit, UnitType.Terran_Barracks, check);//new TilePosition(self.getStartLocation().getX() + 5, self.getStartLocation().getY() + 5);
    	if (buildTile == null) dir *= -1;
    	if (buildTile != null){
    		myUnit.build(UnitType.Terran_Barracks, buildTile );
    		game.sendText("built a barracks");

    	}
    	break;
    }
    else if (self.minerals() >= 100 && (self.supplyTotal() - self.supplyUsed() < 3)){
    	TilePosition check = new TilePosition(self.getStartLocation().getX() + (5 * dir), self.getStartLocation().getY());
    	TilePosition buildTile = getBuildTile(myUnit, UnitType.Terran_Supply_Depot, check);
    	if (buildTile == null) dir *= -1;
    	if (buildTile != null){
    		myUnit.build(UnitType.Terran_Supply_Depot, buildTile );
    		break;
    	}
    }
    //if it's a worker and it's idle, send it to the closest mineral patch
    if (myUnit.isIdle()) {
        Unit closestMineral = null;

        //find the closest mineral
        for (Unit neutralUnit : game.neutral().getUnits()) {
            if (neutralUnit.getType().isMineralField()) {
                if (closestMineral == null || myUnit.getDistance(neutralUnit) < myUnit.getDistance(closestMineral)) {
                    closestMineral = neutralUnit;
                }
            }
        }

        //if a mineral patch was found, send the worker to gather it
        if (closestMineral != null) {
            myUnit.gather(closestMineral, false);
        }
    }
}
*/