#include "Base.h"


Base::Base(Unit unit)
{
	if (unit->getType() == UnitTypes::Terran_Command_Center)
	{
		CommandCenter = unit;
		workers = {};
		//Broodwar << "found base..." << std::endl;
	}
}


Base::~Base()
{
}
void Base::add(Unit unit){
	bool copy = false;
	for (auto&itr : workers){
		if (itr->getID() == unit->getID()){
			Broodwar << "Comparing : " << unit->getID() << " to " << itr->getID() << std::endl;
			copy = true;
		}
	}
	if (!copy) workers.push_back(unit);
}

BWAPI::Unit Base::getCC(){
	if (CommandCenter == NULL) Broodwar << "CC IS NULL: " << CommandCenter->getID() << std::endl;
	return CommandCenter;
}