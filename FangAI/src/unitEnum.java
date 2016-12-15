import bwapi.*;

public class unitEnum {
	public enum Type {
		//T1 Units: 
		SCV, MARINE, MEDIC, FIREBAT,
		CC, SUPPLY, BARRACKS, ACADEMY,
		//T2
		VULTURE, GOLIATH, TANK_TANK, SIEGE_TANK,
		FACTORY, ARMORY, ENGBAY,
		//T3
		DROPSHIP, WRAITH, BATTLECRUISER,
		GHOST, VALKYRIE, SCIENCEVESSEL,
		SCIENCELAB,
		//MISC
		TECHLAB, REFINERY, 
		//DEFENSE
		COMSAT, BUNKER, TURRET
	}
	public unitEnum(){
	}
	public Type eval(UnitType uType){
		if (uType ==  UnitType.Terran_Command_Center){
			return Type.CC;
		}
		else if (uType == UnitType.Terran_Refinery){
			return Type.REFINERY;
		}
		else if (uType == UnitType.Terran_Barracks){
			return Type.BARRACKS;
		}
		else if (uType == UnitType.Terran_Academy){
			return Type.ACADEMY;
		}		
		else if (uType == UnitType.Terran_Supply_Depot){
			return Type.SUPPLY;
		}		
		else if (uType == UnitType.Terran_Factory){
			return Type.FACTORY;
		}
		else if (uType == UnitType.Terran_Armory){
			return Type.ARMORY;
		}
		else if (uType == UnitType.Terran_Engineering_Bay){
			return Type.ENGBAY;
		}
		else if (uType == UnitType.Terran_SCV){
			return Type.SCV;
		}
		else if (uType == UnitType.Terran_Marine){
			return Type.MARINE;
		}
		else if (uType == UnitType.Terran_Medic){
			return Type.MEDIC;
		}
		else if (uType == UnitType.Terran_Firebat){
			return Type.FIREBAT;
		}
		else if (uType == UnitType.Terran_Siege_Tank_Tank_Mode){
			return Type.TANK_TANK;
		}
		return null;

	}
	
}
