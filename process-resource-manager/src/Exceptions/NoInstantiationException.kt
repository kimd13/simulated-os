package Exceptions

class NoInstantiationException(_struct: Any) : Exception("${_struct.toString()} was not instantiated")