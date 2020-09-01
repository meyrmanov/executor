Timeout Aware Executor
==========================

Custom implementation of the Executor which executes a given task in a concurrent environment (usually about 500 client threads) interrupting task execution after timeout.

## Notes

- A single supervisor thread is running and managing all the threads started with this executor
- It is possible to optimize the supervisor thread to stop it, when there is no threads and start again when a new thread added
- Thread.stop() is deprecated and not a good way to stop threads, however another way was not found to stop gracefully with interrupt call as there is no control over the client's code
- Thread can work a bit more than executionTimeout (but not less) due to the algorithm and additional overheads

## Tests

To run simple test with 700 client threads run

```mvn test```