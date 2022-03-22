#include "account.h"

bool Account::operator==(const Account& rhs) const{
  return this->accountNumber == rhs.accountNumber &&
    this->balance == rhs.balance &&
    this->positions == rhs.positions;
}