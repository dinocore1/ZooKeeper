#include <ctime>
#include <iostream>

using namespace std;

int main(int argc, char* argv[]) {

  time_t t = std::time(0);   // get time now
  tm* now = std::localtime(&t);
  cout << (now->tm_year + 1900) << '-'
         << (now->tm_mon + 1) << '-'
         <<  now->tm_mday
         << std::endl;


    return 0;
}