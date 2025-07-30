package org.chorus_oss.snappy

import kotlinx.io.*
import org.chorus_oss.varlen.types.readUIntVar

class SnappyDecompressor {
    fun decompress(data: ByteArray): ByteArray {
        val buf = Buffer()
        buf.write(data)

        val length = buf.readUIntVar().toInt()
        val output = ByteArray(length)

        var i = 0
        while (!buf.exhausted()) {
            val tag = buf.readUByte().toInt()
            when (tag and 0x03) {
                0 -> {
                    val len = readLiteralLen(tag, buf)
                    buf.readTo(output, i, i + len)
                    i += len
                }
                1, 2, 3 -> {
                    val (len, offset) = readCopy(tag, buf)
                    repeat(len) {
                        output[i] = output[i - offset]
                        i++
                    }
                }
            }
        }

        return output
    }

    companion object {
        fun readLiteralLen(tag: Int, buf: Buffer): Int {
            val code = tag ushr 2
            return (
                if (code < 60) code
                else when (code) {
                    60 -> buf.readUByte().toInt()
                    61 -> buf.readUShortLe().toInt()
                    62 -> (buf.readUShortLe().toInt() shl 8) or (buf.readUByte().toInt())
                    63 -> buf.readUIntLe().toInt()
                    else -> throw IllegalArgumentException("Invalid tag")
                }
            ) + 1
        }

        fun readCopy(tag: Int, buf: Buffer): Pair<Int, Int> {
            return when (tag and 0x03) {
                1 -> {
                    val len = ((tag ushr 2) and 0x7) + 4
                    val offset = ((tag ushr 5) shl 8) or buf.readUByte().toInt()
                    len to offset
                }
                2 -> {
                    val len = (tag ushr 2) + 1
                    val offset = buf.readUShortLe().toInt()
                    len to offset
                }
                3 -> {
                    val len = (tag ushr 2) + 1
                    val offset = buf.readUIntLe().toInt()
                    len to offset
                }
                else -> throw IllegalArgumentException("Invalid tag")
            }
        }
    }
}