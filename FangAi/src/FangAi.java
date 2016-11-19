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
			ArrayList<Unit> list = new ArrayList<Unit>();
			list.add(u);
			units.put(type, list );
		}
		else {
			ArrayList<Unit> list = units.get(type);
			if (!list.contains(u)){
				list.add(u);
				units.put(type, list);
			}
		}
	}
	public void removeUnit(String type, Unit u){
		ArrayList<Unit> removed = units.get(type);
		if (removed != null){
			if (removed.contains(u)){
				removed.remove(u);
				units.put(type, removed);
			}
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
