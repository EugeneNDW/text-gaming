package ndw.eugene.textgaming

fun readBytesFromFile(illustration: String): ByteArray {
    val illustrationStream =
        TextGameApplication::class.java.getResourceAsStream("/illustrations/$illustration")
            ?: throw IllegalArgumentException("file with name $illustration not found")

    return illustrationStream.use {
        it.readAllBytes()
    }
}