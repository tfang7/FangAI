import bwapi.*;
public class FangUnit {
	private Unit unit;
	private UnitType uType;
	private FangSM.Role role;
	
	public FangUnit(Unit u){
		unit = u;
	}
	public Unit getUnit(){
		return unit;
	}
}
