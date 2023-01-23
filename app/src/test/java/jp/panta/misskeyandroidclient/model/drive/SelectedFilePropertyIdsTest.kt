package jp.panta.misskeyandroidclient.model.drive

import net.pantasystem.milktea.model.drive.FileProperty
import net.pantasystem.milktea.model.drive.SelectedFilePropertyIds
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class SelectedFilePropertyIdsTest {

    @Test
    fun testAddAndCopy_WhenNew() {
        val s = SelectedFilePropertyIds(4, emptySet())
        val addId = FileProperty.Id(0, "a01")
        val s2 = s.addAndCopy(addId)
        assertEquals(addId, s2.selectedIds.first())
    }

    @Test
    fun testAddAndCopy_WhenDuplicate() {
        val s = SelectedFilePropertyIds(4, emptySet())
        val addId = FileProperty.Id(0, "a01")
        val s2 = s.addAndCopy(addId).addAndCopy(addId.copy())
        assertEquals(1, s2.selectedIds.size)
    }

    @Test
    fun testAddAndCopy_WhenMany() {
        val s = SelectedFilePropertyIds(5, emptySet())
        val addId = FileProperty.Id(0, "a01")
        val s2 = s.addAndCopy(addId).addAndCopy(addId.copy(fileId = "a02"))
            .addAndCopy(addId.copy(fileId = "a03"))
            .addAndCopy(addId.copy(fileId = "a04"))
            .addAndCopy(addId.copy(fileId = "a05"))
        assertEquals(5, s2.selectedIds.size)
    }


    @Test
    fun testRemoveAndCopy_WhenNormal() {
        val s = SelectedFilePropertyIds(4, emptySet())
        val addId = FileProperty.Id(0, "a01")
        val s2 = s.removeAndCopy(addId)
        assertEquals(0, s2.selectedIds.size)
    }

    @Test
    fun testRemoveAndCopy_WhenMany() {
        val removeId = FileProperty.Id(0, "a01")

        val s = SelectedFilePropertyIds(5, selectedIds = setOf(removeId, removeId.copy(fileId = "02"), removeId.copy(fileId = "03"), removeId.copy(fileId = "04")))

        assertEquals(s.selectedIds.size - 1, s.removeAndCopy(removeId).selectedIds.size)
    }
}