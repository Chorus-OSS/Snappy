package org.chorus_oss.snappy

import kotlinx.io.Buffer
import org.chorus_oss.varlen.types.writeUIntVar

class SnappyCompressor {
    fun compress(data: ByteArray): ByteArray {
        val buf = Buffer()

        buf.writeUIntVar(data.size.toUInt())

        return byteArrayOf()
    }
}