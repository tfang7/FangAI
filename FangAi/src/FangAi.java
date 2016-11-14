import java.util.ArrayList;
import java.util.HashMap;

import bwapi.*;
public class FangAi {
	private Player player;
	HashMap<String, ArrayList<Unit>> units;
	ArrayList<UnitType> BuildOrder;
	
	public FangAi(){
		units = new HashMap<String, ArrayList<Unit>>();
	}
	
	
	public void addUnit(String type, Unit u){
		if (units.get(type) == null){
			units.put(type, new ArrayList<Unit>());
			units.get(type).add(u);
		}
		else {
			ArrayList<Unit> list = units.get(type);
			if (!list.contains(u)){
				list.add(u);
			}
		}
	}
	public void removeUnit(String type, Unit u){
		if (units.get(type) != null){
			units.get(type).remove(u);
		}
	}
	public int getUnitsLength(String type){
		if (units.get(type) != null){
			return units.get(type).size();
		}
		else return 0;
	}
	public ArrayList<Unit> getUnitList(String type){
		return units.get(type);
	}
	public int numUnitType( UnitType uType ){
		int counter = 0;
		for (Unit u : player.getUnits())
		{
			if  (u.getType() == uType){
				counter++;
			}
		}
		return counter;
	}

}
