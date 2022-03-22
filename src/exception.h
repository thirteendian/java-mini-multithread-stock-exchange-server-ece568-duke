#ifndef __EXCEPTION_H__
#define __EXCEPTION_H__

#include <exception>
#include <string>

/**
 * @brief custom exception class related to numbers
 * 
 */
class NumberDomainException: public std::exception{
  private:
    std::string errMsg;

  public:
    NumberDomainException(): errMsg("error: something\'s wrong"){}
    NumberDomainException(std::string _errMsg): errMsg(_errMsg){}
    virtual const char* what() const throw() {
      return this->errMsg.c_str();
    }
};

#endif