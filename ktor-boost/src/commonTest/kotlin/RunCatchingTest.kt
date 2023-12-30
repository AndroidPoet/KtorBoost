import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

class RunCatchingTest {
    @Test
    fun `test runSafeSuspendCatching extension function CancellationException`() {
        runBlocking {
            try {
                runSafeSuspendCatching {
                    throw CancellationException("Simulated cancellation")
                }.onFailure {
                    fail("Caught an CancellationException")
                }
                // Fail the test if no exception is thrown
                fail("Expected CancellationException, but no exception was thrown.")
            } catch (exception: CancellationException) {
                assertEquals("Simulated cancellation", exception.message)

                // Add any other necessary assertions or handling for the rethrown exception
            } catch (e: Throwable) {
                // Fail the test if an unexpected exception is caught
                fail("Caught an unexpected exception: ${e.message}")
            }
        }
    }

    @Test
    fun `test runCatching extension function CancellationException`() {
        runBlocking {
            runCatching {
                throw CancellationException("Simulated cancellation")
            }.onSuccess {
                // Handle success if needed (not relevant in this scenario)
                // Fail the test explicitly if it reaches here unexpectedly
                fail("Expected onFailure to be called, but onSuccess was invoked.")
            }.onFailure { exception ->
                assertTrue(exception is CancellationException)
                assertEquals("Simulated cancellation", exception.message)
                // Add any other necessary assertions or handling for the failure
            }
        }
    }
}
