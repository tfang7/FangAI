#include "FangController.h"
#include <map>
#include <vector>
#include <algorithm>

FangController::FangController(BWAPI::Player p)
{
	player = p;
	
}
FangController::FangController()
{
}
FangController::~FangController()
{
}
void FangController::initArray(Unit unit, UnitType uType){
	std::vector<Unit> newUnits = { unit };
	armyUnits.insert(std::make_pair(uType, newUnits));
}
int FangController::availableSupply(){
	return player->supplyTotal()/2 - player->supplyUsed()/2;
}
void FangController::addUnit(Unit unit){
	UnitType uType = unit->getType();
	if (armyUnits.find(uType) == armyUnits.end()){
		initArray(unit, uType);
	}
	else{
		armyUnits[uType].push_back(unit);
	}
}
void FangController::removeUnit(Unit unit)
{
	//std::vector<Unit> currentList = armyUnits[unit->getType()];
	if (armyUnits.find(unit->getType()) != armyUnits.end())
	{
		std::vector<Unit> units = armyUnits[unit->getType()];
	//	std::vector<Unit>::iterator upos = std::find(units.begin(), units.end(), unit->getID());
		for (std::vector<Unit>::iterator itr = units.begin(); itr != units.end(); itr++){
			Unit u = *itr;
			if (u->getID() == unit->getID()){
				units.erase(itr);
				break;
			}
		}
	}

	//for (Unit u : armyUnits.find(unit->getType())){

	//}
}

