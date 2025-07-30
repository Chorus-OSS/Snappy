package org.chorus_oss.snappy

import kotlinx.io.Buffer
import kotlinx.io.readByteArray
import org.chorus_oss.varlen.types.writeUIntVar
import kotlin.math.min

@OptIn(ExperimentalUnsignedTypes::class)
class SnappyCompressor {
    fun compress(data: ByteArray): ByteArray {
        val buf = Buffer()
        buf.writeUIntVar(data.size.toUInt())
        val table = UShortArray(HASH_SIZE)

        var start = 0
        while (start < data.size) {
            val end = min(start + WINDOW_SIZE, data.size)
            table.fill(0u)

            var next = start
            val limit = end - 15
            var pos = start
            while (pos <= limit) {
                pos += 1

                var candidate = 0
                var skip = 32
                while (pos + (skip shr 5) <= limit) {
                    val value = data.getInt(pos)
                    val hash = hash(value, HASH_ORDER)
                    candidate = start + table[hash].toInt()
                    table[hash] = (pos - start).toUShort()

                    if (value == data.getInt(candidate)) break
                    pos += skip++ shr 5
                }
                if (pos + (skip shr 5) > limit) break

                emitLiteral(buf, data, next, pos - next)
                val match = countMatch(data, pos + 4, candidate + 4, end) + MIN_MATCH_LEN
                emitMatch(buf, pos - candidate, match)

                pos += match
                next = pos
            }

            if (next < end) {
                emitLiteral(buf, data, next, end - next)
            }

            start = end
        }

        return buf.readByteArray()
    }

    companion object {
        private const val WINDOW_SIZE: Int = 1 shl 16
        private const val MIN_MATCH_LEN: Int = 4
        private const val HASH_ORDER: Int = 15
        private const val HASH_SIZE: Int = 1 shl HASH_ORDER

        private fun hash(value: Int, shift: Int): Int {
            return ((value * 0x1E35A7BD) ushr shift) and (HASH_SIZE - 1)
        }

        private fun ByteArray.getInt(pos: Int): Int {
            return (this[pos].toInt() and 0xFF) or
                    (this[pos + 1].toInt() and 0xFF shl 8) or
                    (this[pos + 2].toInt() and 0xFF shl 16) or
                    (this[pos + 3].toInt() and 0xFF shl 24)
        }

        private fun emitLiteral(buf: Buffer, data: ByteArray, pos: Int, length: Int) {
            val n = length - 1
            when {
                n < 60 -> {
                    buf.writeByte((n shl 2).toByte())
                }
                n < (1 shl 8) -> {
                    buf.writeByte((60 shl 2).toByte())
                    buf.writeByte(n.toByte())
                }
                n < (1 shl 16) -> {
                    buf.writeByte((61 shl 2).toByte())
                    buf.writeByte((n and 0xFF).toByte())
                    buf.writeByte((n shr 8).toByte())
                }
                n < (1 shl 24) -> {
                    buf.writeByte((62 shl 2).toByte())
                    repeat(3) { i ->
                        buf.writeByte((n shr (8 * i)).toByte())
                    }
                }
                else -> {
                    buf.writeByte((63 shl 2).toByte())
                    repeat(4) { i ->
                        buf.writeByte((n shr (8 * i)).toByte())
                    }
                }
            }
            buf.write(data, pos, length)
        }

        private fun countMatch(data: ByteArray, p1: Int, p2: Int, limit: Int): Int {
            var matched = 0
            while (p1 + matched < limit &&
                p2 + matched < limit &&
                data[p1 + matched] == data[p2 + matched]) {
                matched++
            }
            return matched
        }

        private fun emitMatch(buf: Buffer, offset: Int, length: Int) {
            var length = length
            while (length >= 68) {
                buf.writeByte(((2) or ((64 - 1) shl 2)).toByte())
                buf.writeByte((offset and 0xFF).toByte())
                buf.writeByte((offset shr 8).toByte())
                length -= 64
            }

            if (length > 64) {
                buf.writeByte(((2) or ((60 - 1) shl 2)).toByte())
                buf.writeByte((offset and 0xFF).toByte())
                buf.writeByte((offset shr 8).toByte())
                length -= 60
            }

            if (length in 4..11 && offset < 2048) {
                val lenMinus4 = length - 4
                val tag = (1 or (lenMinus4 shl 2) or ((offset shr 8) shl 5))
                buf.writeByte(tag.toByte())
                buf.writeByte((offset and 0xFF).toByte())
            } else {
                buf.writeByte(((2) or ((length - 1) shl 2)).toByte())
                buf.writeByte((offset and 0xFF).toByte())
                buf.writeByte((offset shr 8).toByte())
            }
        }
    }
}