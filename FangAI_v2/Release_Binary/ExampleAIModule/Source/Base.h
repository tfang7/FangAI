#pragma once
#include <BWAPI.h>

using namespace BWAPI;
class Base
{
public:
	Base(BWAPI::Unit unit);
	~Base();
private:
	BWAPI::Unit CommandCenter;
};

