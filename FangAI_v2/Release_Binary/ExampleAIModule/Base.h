#pragma once
#include <BWAPI.h>

using namespace BWAPI;
class Base
{
public:
	Base(BWAPI::Unit unit);
	~Base();
	void add(BWAPI::Unit unit);
	BWAPI::Unit getCC();
private:
	BWAPI::Unit CommandCenter;
	std::vector<BWAPI::Unit> workers;
	

};

