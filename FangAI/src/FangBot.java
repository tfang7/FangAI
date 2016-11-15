import java.util.ArrayList;

import bwapi.Color;
import bwapi.DefaultBWListener;
import bwapi.Game;
import bwapi.Mirror;
import bwapi.Player;
import bwapi.Position;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BWTA;
import bwta.BaseLocation;

public class FangBot extends DefaultBWListener {

    private Mirror mirror = new Mirror();
    
    private Game game;
    
    private Player self;
    private unitEnum uType = new unitEnum();
    private FangAi fang = new FangAi();
    private FangSM fangState = new FangSM(fang);
    private BuildingUtil builder = new BuildingUtil();
    private ArrayList<Base> allBases = new ArrayList<Base>();
    public void run() {
        mirror.getModule().setEventListener(this);
        mirror.startGame();
    }

    @Override
    public void onUnitCreate(Unit unit) {
    		
        //System.out.println("New unit discovered " + unit.getType());
    }

    @Override
    public void onStart() {
        game = mirror.getGame();
        game.setLocalSpeed(15);
        self = game.self();
        //game.sendText("show me the money");
        //Use BWTA to analyze map
        //This may take a few minutes if the map is processed first time!
      //  System.out.println("Analyzing map...");
        BWTA.readMap();
        BWTA.analyze();
        //System.out.println("Map data ready");
        for (Unit u : self.getUnits()){
        	fang.addUnit(uType.eval(u.getType()).toString(), u);
        	if (u.getType() == UnitType.Terran_Command_Center){
        		allBases.add(new Base(u, game));
        	}
        }
        
        
        
     //   int i = 0;
       /* for(BaseLocation baseLocation : BWTA.getBaseLocations()){
        	System.out.println("Base location #" + (++i) + ". Printing location's region polygon:");
        	for(Position position : baseLocation.getRegion().getPolygon().getPoints()){
        		System.out.print(position + ", ");
        	}
        	System.out.println();
        }*/

    }

    @Override
    public void onFrame() {
        //game.setTextSize(10);
    	//game.drawTextScreen(10, 10, fang.workers.toString());
        StringBuilder units = new StringBuilder("My units:\n");
        //iterate through my units
        for (Unit myUnit : self.getUnits()) 
        {
            units.append(myUnit.getType()).append(" ").append(myUnit.getTilePosition()).append("\n");
    		if (myUnit.getType() == UnitType.Terran_Command_Center){
    			fangState.Produce(myUnit, UnitType.Terran_SCV, game);
				allBases.get(0).drawLayout(game);
    			
    		}
    		else {
        		cheese(myUnit);
    		}
        }

        //draw my units on screen
      //  game.drawTextScreen(10, 25, units.toString());
        //game.drawTextScreen(50, 25,"" + myUnit.getType());
    }
    public boolean isMineral(Unit u){
    	return u.getType().isMineralField();
    }
    public void cheese(Unit myUnit){

		if (fangState.Action(myUnit, game));

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