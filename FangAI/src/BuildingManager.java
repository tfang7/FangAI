import java.util.ArrayList;

import bwapi.TechType;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import bwapi.Player;
import bwapi.Game;
import bwapi.UpgradeType;
public class BuildingManager {
	public BuildingUtil buildUtil;
	public ArrayList<Base> bases;
	public BuildingManager(BuildingUtil b){
		buildUtil = b;
		bases = new ArrayList<Base>();
	}
	public void addBase(Unit unit, Game game){
		boolean copy = false;
		for (Base base : bases){
			if (base.CC.getID() == unit.getID()){
				copy = true;
			}
		}
		if (!copy){
			Base b = new Base(unit, game);
			bases.add(b);
			
		}
	}
	public Base getBase(Unit u){
		for (Base b : bases){
			if (u.getID() == b.CC.getID()){
				return b;
			}
		}
		return null;
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
	public boolean build(Unit u, UnitType type, Game game, int productionRate){
		Player self = game.self();
		if (type == UnitType.Terran_Barracks){
			return	self.hasUnitTypeRequirement(UnitType.Terran_Supply_Depot) 
    		&& checkResources(UnitType.Terran_Barracks, self)
    		&& (supplyDiff(self, productionRate)> 2);
		}
		else if (type == UnitType.Terran_Supply_Depot){
			return checkResources(UnitType.Terran_Supply_Depot, game.self()) && (supplyDiff(self, productionRate)<= 5);
		}
		else if (type == UnitType.Terran_Refinery){
			return checkResources(UnitType.Terran_Refinery, game.self()) && game.self().hasUnitTypeRequirement(UnitType.Terran_Barracks);
		}
		else if (type == UnitType.Terran_Academy){
		    return !self.hasUnitTypeRequirement(UnitType.Terran_Academy)
		    		&& self.hasUnitTypeRequirement(UnitType.Terran_Refinery)
			    	&& checkResources(UnitType.Terran_Academy, self) 
			    	&& self.hasUnitTypeRequirement(UnitType.Terran_Barracks);
		}
		return false;

	    	      
	}
	public int supplyDiff(Player self, int productionRate){
		int supply = self.supplyTotal() - self.supplyUsed() - productionRate;
		return supply;
	}
    public boolean checkResources(UnitType type, Player self){
    	return (self.minerals() >= type.mineralPrice() && self.gas() >= type.gasPrice());
    }
    public boolean checkSupply(int productionRate,Player self){
    	int supplyDiff = self.supplyTotal() - self.supplyUsed();
		//(productionRate + 5) ;
		return supplyDiff <= productionRate + 5;
    }

}
