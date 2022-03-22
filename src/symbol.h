#ifndef __SYMBOL_H__
#define __SYMBOL_H__

#include <string>

class Symbol{
  private:
    std::string identifier;

  public:
    Symbol():identifier(""){}
    Symbol(std::string _identifier):identifier(_identifier){}
    explicit Symbol(const Symbol& rhs):identifier(rhs.identifier){}
    bool operator==(const Symbol& rhs) const;
    ~Symbol(){};

    std::string& get_identifier();
};

#endif