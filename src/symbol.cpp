#include "symbol.h"

bool Symbol::operator==(const Symbol& rhs) const{
  return this->identifier == rhs.identifier;
}

std::string& Symbol::get_identifier(){
  return this->identifier;
}