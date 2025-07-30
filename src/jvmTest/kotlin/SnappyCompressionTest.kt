import org.chorus_oss.snappy.SnappyCompressor
import org.chorus_oss.snappy.SnappyDecompressor
import org.xerial.snappy.Snappy
import kotlin.test.Test
import kotlin.test.assertContentEquals

class SnappyCompressionTest {
    @Test
    fun `round trip - our compressor to xerial decompressor`() {
        val data = "randomStuff".repeat(100).encodeToByteArray()

        val compressed = SnappyCompressor().compress(data)

        val decompressed = Snappy.uncompress(compressed)

        assertContentEquals(data, decompressed)
    }

    @Test
    fun `round trip - xerial compressor to our decompressor`() {
        val data = "randomStuff".repeat(100).encodeToByteArray()

        val compressed = Snappy.compress(data)

        val decompressed = SnappyDecompressor().decompress(compressed)

        assertContentEquals(data, decompressed)
    }

    @Test
    fun `round trip - our compressor to our decompressor`() {
        val data = "randomStuff".repeat(100).encodeToByteArray()

        val compressed = SnappyCompressor().compress(data)

        val decompressed = SnappyDecompressor().decompress(compressed)

        assertContentEquals(data, decompressed)
    }
}