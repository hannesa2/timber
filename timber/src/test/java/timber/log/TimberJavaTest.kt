package timber.log

import org.junit.Assert
import org.junit.Test
import timber.log.Timber.Forest.plant
import timber.log.Timber
import java.lang.IllegalArgumentException

class TimberJavaTest {
    @Test
    fun nullTree() {
        try {
            plant((null as Timber.Tree?)!!)
            Assert.fail()
        } catch (ignored: IllegalArgumentException) {
        }
    }

}
