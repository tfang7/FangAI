import bwapi.TechType;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import bwapi.Player;
import bwapi.Game;
import bwapi.UpgradeType;
public class BuildingManager {
	public BuildingUtil buildUtil;
	public BuildingManager(BuildingUtil b){
		buildUtil = b;
	}

	public boolean buildSupplyDepot(Unit u, Game game, int productionRate){
		Player self = game.self();
	//	int supplyDiff = self.supplyTotal() - self.supplyUsed();
		//(productionRate + 5) ;
	//	boolean supplyCheck = supplyDiff <= productionRate + 5;
		//boolean resourceCheck = self.minerals() >  UnitType.Terran_Supply_Depot.mineralPrice();
		if (checkSupply( productionRate, self ) && checkResources(UnitType.Terran_Supply_Depot, self)){
	    	return true;
		}
		return false;
	}
	public boolean buildBarracks(Unit u, Game game, int productionRate){
		Player self = game.self();
		return	self.hasUnitTypeRequirement(UnitType.Terran_Supply_Depot) 
	    		&& checkResources(UnitType.Terran_Barracks, self)
	    		&& (supplyDiff(self)> 2);
	    	      
	}
    public boolean checkResources(UnitType type, Player self){
    	return (self.minerals() >= type.mineralPrice() && self.gas() >= type.gasPrice());
    }
    public boolean checkSupply(int productionRate,Player self){
    	int supplyDiff = self.supplyTotal() - self.supplyUsed();
		//(productionRate + 5) ;
		return supplyDiff <= productionRate + 5;
    }
    public int supplyDiff(Player self){
    	return self.supplyTotal() - self.supplyUsed();
    }
}
