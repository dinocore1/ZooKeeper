#ifndef ADD_H
#define ADD_H

#ifdef EXPORT_DLL
#define API __declspec(dllexport)
#else
#define API __declspec(dllimport)
#endif


#ifdef __cplusplus
extern "C" {
#endif

int API add(int a, int b);


#ifdef __cplusplus
}
#endif

int add(int a, int b);

#endif //ADD_H