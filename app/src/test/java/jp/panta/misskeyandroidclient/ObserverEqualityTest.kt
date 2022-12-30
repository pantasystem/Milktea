package jp.panta.misskeyandroidclient

import androidx.lifecycle.Observer
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test


class ObserverEqualityTest {
    @Test
    fun observerEquality() {
        val observer1 = Observer<String> {

        }

        val observer2 = Observer<String> {

        }
        Assertions.assertFalse(observer1 == observer2)

        val objectObserver1 = object : Observer<String> {
            override fun onChanged(t: String?) {

            }
        }
        val objectObserver2 = object : Observer<String> {
            override fun onChanged(t: String?) {

            }
        }
        Assertions.assertEquals(objectObserver1, objectObserver1)
        Assertions.assertNotEquals(objectObserver1, objectObserver2)



    }

}