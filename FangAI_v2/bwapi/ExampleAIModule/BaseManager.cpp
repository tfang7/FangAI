#include "BaseManager.h"


using namespace BWAPI;
BaseManager::BaseManager()
{
	//	Broodwar << "Initializing base manager..." << std::endl;
	//TilePosition start = player->getStartLocation();
//	bases = {};


}
BaseManager::BaseManager(BWAPI::Player player)
{
//	Broodwar << "Initializing base manager..." << std::endl;
	TilePosition start = player->getStartLocation();
	//bases = {};

}

void BaseManager::addBase(BWAPI::Unit unit){
	Base b = Base(unit);
	bases.push_back(b);
}
Base BaseManager::getBase(BWAPI::Unit unit) {
	for (auto &itr : bases){
		if (*itr.getCC->getID() == unit->getID()){
			return itr;
		}
	}
	return NULL;
}
BaseManager::~BaseManager()
{
}
