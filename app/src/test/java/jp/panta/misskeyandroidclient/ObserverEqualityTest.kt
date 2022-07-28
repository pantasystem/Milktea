package jp.panta.misskeyandroidclient

import androidx.lifecycle.Observer
import org.junit.Assert
import org.junit.Test

class ObserverEqualityTest {
    @Test
    fun observerEquality() {
        val observer1 = Observer<String> {

        }
        Assert.assertTrue(observer1 == observer1)

        val observer2 = Observer<String> {

        }
        Assert.assertFalse(observer1 == observer2)

        val objectObserver1 = object : Observer<String> {
            override fun onChanged(t: String?) {

            }
        }
        val objectObserver2 = object : Observer<String> {
            override fun onChanged(t: String?) {

            }
        }
        Assert.assertTrue(objectObserver1 == objectObserver1)
        Assert.assertEquals(objectObserver1, objectObserver1)
        Assert.assertNotEquals(objectObserver1, objectObserver2)



    }

}