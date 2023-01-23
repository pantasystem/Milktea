package jp.panta.misskeyandroidclient.model.drive


import net.pantasystem.milktea.model.drive.Directory
import net.pantasystem.milktea.model.drive.DirectoryPath
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DirectoryPathTest {
    @Test
    fun testPush() {
        val root = Directory(
            "root",
            "",
            "",
            0,
            0,
            null,
            null
        )
        var directoryPath = DirectoryPath(
            path = listOf(
                root
            )
        )
        directoryPath = directoryPath.push(root.copy(
            parent = root,
            parentId = root.id,
            id = "sub1"
        ))
        assertEquals("sub1", directoryPath.path.last().id)

    }

    @Test
    fun testPop() {
        val root = Directory(
            "root",
            "",
            "",
            0,
            0,
            null,
            null
        )
        var directoryPath = DirectoryPath(
            path = listOf(
                root
            )
        )
        val dirs = listOf("sub1", "sub2", "sub3", "sub4")
        directoryPath = dirs.fold(directoryPath) { acc, s ->
            acc.push(root.copy(id = s, parent = acc.path.last(), parentId = acc.path.last().id))
        }
        directoryPath = directoryPath.pop()
        assertEquals("sub3", directoryPath.path.last().id)

        directoryPath = directoryPath.pop()
        assertEquals("sub2", directoryPath.path.last().id)

        directoryPath = directoryPath.pop()
        assertEquals("sub1", directoryPath.path.last().id)
    }

    @Test
    fun testPopUntil() {
        val root = Directory(
            "root",
            "",
            "",
            0,
            0,
            null,
            null
        )
        var directoryPath = DirectoryPath(
            path = listOf(
                root
            )
        )
        val dirs = listOf("sub1", "sub2", "sub3", "sub4")
        directoryPath = dirs.fold(directoryPath) { acc, s ->
            acc.push(root.copy(id = s, parent = acc.path.last(), parentId = acc.path.last().id))
        }

        val dir = directoryPath.path[1]
        directoryPath = directoryPath.popUntil(dir)
        assertEquals(dir.id, directoryPath.path.last().id)
    }


    @Test
    fun testClear() {
        val root = Directory(
            "root",
            "",
            "",
            0,
            0,
            null,
            null
        )
        var directoryPath = DirectoryPath(
            path = listOf(
                root
            )
        )
        val dirs = listOf("sub1", "sub2", "sub3", "sub4")
        directoryPath = dirs.fold(directoryPath) { acc, s ->
            acc.push(root.copy(id = s, parent = acc.path.last(), parentId = acc.path.last().id))
        }
        directoryPath = directoryPath.clear()
        assertEquals(0, directoryPath.path.size)
    }
}