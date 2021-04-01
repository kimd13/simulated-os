package Exceptions

sealed class CriticalUserException : Exception()
class ChildProcessDoesNotExistException : CriticalUserException()
class ProcessDoesNotHaveResourceException : CriticalUserException()
class ProcessZeroRequestDeniedException : CriticalUserException()
class ReleaseDeniedException : CriticalUserException()
class RequestDeniedException : CriticalUserException()
class ResourceDoesNotExistException : CriticalUserException()
class IllegalPriorityException : CriticalUserException()