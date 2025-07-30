import org.chorus_oss.snappy.SnappyCompressor
import org.xerial.snappy.Snappy
import kotlin.test.Test
import kotlin.test.assertContentEquals

class SnappyCompressorTest {
    @Test
    fun roundTrip() {
        val data = "randomStuff".repeat(100).encodeToByteArray()

        val compressor = SnappyCompressor()

        val compressed = compressor.compress(data)

        val decompressed = Snappy.uncompress(compressed)

        assertContentEquals(data, decompressed)
    }
}