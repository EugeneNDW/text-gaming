package ndw.eugene.textgaming

import java.net.URI
import java.net.URL
import java.nio.file.*
import java.util.stream.Stream

fun readBytesFromFile(illustration: String): ByteArray {
    val illustrationStream =
        TextGameApplication::class.java.getResourceAsStream("/illustrations/$illustration")
            ?: throw IllegalArgumentException("file with name $illustration not found")

    return illustrationStream.use {
        it.readAllBytes()
    }
}

fun walk(path: String, fileURL: URL): Stream<Path>? {
    val uri = fileURL.toURI()

    val paths = if ("jar" == uri.scheme) {
        safeWalkJar(path, uri)
    } else {
        Files.walk(Paths.get(uri))
    }

    return paths
}

private fun safeWalkJar(path: String, uri: URI): Stream<Path>? {
    val fs = getFileSystem(uri)
    val path1 = fs.getPath(path)
    return Files.walk(path1)
}

private fun getFileSystem(uri: URI): FileSystem{
    return try {
        FileSystems.getFileSystem(uri)
    } catch (e: FileSystemNotFoundException) {
        FileSystems.newFileSystem(uri, emptyMap<String, String>())
    }
}