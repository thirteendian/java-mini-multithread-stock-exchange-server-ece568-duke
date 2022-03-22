#ifndef __ACCOUNT_H__
#define __ACCOUNT_H__

#include "position.h"
#include "exception.h"

#include <cstdint>
#include <vector>

class Account{
  private:
    std::size_t accountNumber;
    double balance;
    std::vector<Position> positions;

  public:
    Account(std::size_t _accountNumber):accountNumber(_accountNumber), balance(0){}
    Account(std::size_t _accountNumber, double _balance, std::vector<Position>_positions):
      accountNumber(_accountNumber), balance(_balance), positions(_positions){
        if(_balance < 0){
          throw NumberDomainException("error: account balance must be greater than 0");
        }
    }
    Account(const Account& rhs):
      accountNumber(rhs.accountNumber), balance(rhs.balance), positions(rhs.positions){}
    bool operator==(const Account& rhs) const;
    ~Account(){}
};

#endif