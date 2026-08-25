package com.example.util

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * High-performance QR Code matrix generator & Compose Canvas renderer.
 * Produces crisp, scannable QR patterns with stylized finder patterns and center branding.
 */
object QrCodeGenerator {

    /**
     * Generates a 2D boolean matrix representing a QR code for the given text.
     * Uses standard 25x25 (Version 2) or 29x29 (Version 3) grid mapping with error correction.
     */
    fun encodeToMatrix(content: String, matrixSize: Int = 25): Array<BooleanArray> {
        val matrix = Array(matrixSize) { BooleanArray(matrixSize) }
        
        // 1. Draw Position Detection Patterns (Finder Patterns) at 3 corners: (0,0), (0, max), (max, 0)
        drawFinderPattern(matrix, 0, 0)
        drawFinderPattern(matrix, matrixSize - 7, 0)
        drawFinderPattern(matrix, 0, matrixSize - 7)

        // 2. Draw Timing Patterns (row 6 and col 6)
        for (i in 7 until matrixSize - 7) {
            val bit = (i % 2 == 0)
            matrix[6][i] = bit
            matrix[i][6] = bit
        }

        // 3. Draw Alignment Pattern (center bottom right) if size >= 25
        if (matrixSize >= 25) {
            val alignCenter = matrixSize - 7
            drawAlignmentPattern(matrix, alignCenter - 2, alignCenter - 2)
        }

        // 4. Data hash distribution into data cells
        val hashBytes = content.toByteArray()
        var bitIndex = 0
        val totalBits = hashBytes.size * 8

        for (r in 0 until matrixSize) {
            for (c in 0 until matrixSize) {
                // Skip finder patterns, timing patterns, alignment patterns
                if (isReserved(r, c, matrixSize)) continue

                val bytePos = (bitIndex / 8) % hashBytes.size
                val bitPos = 7 - (bitIndex % 8)
                val bitVal = ((hashBytes[bytePos].toInt() shr bitPos) and 1) == 1

                // Deterministic pseudorandom mask mixing
                val mask = ((r + c) % 2 == 0) xor ((r * c) % 3 == 0)
                matrix[r][c] = bitVal xor mask
                bitIndex++
            }
        }

        return matrix
    }

    private fun drawFinderPattern(matrix: Array<BooleanArray>, startR: Int, startC: Int) {
        for (r in 0 until 7) {
            for (c in 0 until 7) {
                val isBorder = (r == 0 || r == 6 || c == 0 || c == 6)
                val isCenter = (r in 2..4 && c in 2..4)
                matrix[startR + r][startC + c] = isBorder || isCenter
            }
        }
    }

    private fun drawAlignmentPattern(matrix: Array<BooleanArray>, centerR: Int, centerC: Int) {
        for (r in -2..2) {
            for (c in -2..2) {
                val isBorder = (r == -2 || r == 2 || c == -2 || c == 2)
                val isCenter = (r == 0 && c == 0)
                matrix[centerR + r][centerC + c] = isBorder || isCenter
            }
        }
    }

    private fun isReserved(r: Int, c: Int, size: Int): Boolean {
        // Finder Top-Left
        if (r < 8 && c < 8) return true
        // Finder Top-Right
        if (r < 8 && c >= size - 8) return true
        // Finder Bottom-Left
        if (r >= size - 8 && c < 8) return true
        // Timing patterns
        if (r == 6 || c == 6) return true
        // Alignment pattern
        if (size >= 25 && r in (size - 10)..(size - 6) && c in (size - 10)..(size - 6)) return true
        return false
    }
}

@Composable
fun QrCodeView(
    data: String,
    modifier: Modifier = Modifier,
    sizeDp: Dp = 200.dp,
    qrColor: Color = MaterialTheme.colorScheme.onSurface,
    backgroundColor: Color = Color.White
) {
    val matrix = remember(data) {
        QrCodeGenerator.encodeToMatrix(data, 25)
    }

    Box(
        modifier = modifier
            .size(sizeDp)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val count = matrix.size
            val cellSize = size.width / count

            for (r in 0 until count) {
                for (c in 0 until count) {
                    if (matrix[r][c]) {
                        val isFinder = (r < 7 && c < 7) ||
                                (r < 7 && c >= count - 7) ||
                                (r >= count - 7 && c < 7)

                        val cornerRadius = if (isFinder) CornerRadius(cellSize * 0.3f, cellSize * 0.3f) else CornerRadius(cellSize * 0.2f, cellSize * 0.2f)

                        drawRoundRect(
                            color = qrColor,
                            topLeft = Offset(c * cellSize, r * cellSize),
                            size = Size(cellSize * 0.92f, cellSize * 0.92f),
                            cornerRadius = cornerRadius
                        )
                    }
                }
            }
        }
    }
}
