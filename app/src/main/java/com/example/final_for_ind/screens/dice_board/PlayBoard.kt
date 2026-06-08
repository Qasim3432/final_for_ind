package com.example.final_for_ind.screens.dice_board


import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

enum class PlayerColor {
    RED, GREEN, YELLOW, BLUE
}

data class Cell(val row: Int, val col: Int)

data class LudoToken(
    val id: Int,
    val color: PlayerColor,
    val position: Int = -1 // -1=base, 0-56=path, 57=finished
)

object LudoColors {
    val map = mapOf(
        PlayerColor.RED to Color(0xFFE53935),
        PlayerColor.GREEN to Color(0xFF43A047),
        PlayerColor.YELLOW to Color(0xFFFFEB3B),
        PlayerColor.BLUE to Color(0xFF1E88E5)
    )
}

object LudoPath {
    // 52-step main path, starts at red entry Cell(6,1)
    private val mainPath = listOf(
        Cell(6, 1), Cell(6, 2), Cell(6, 3), Cell(6, 4), Cell(6, 5),
        Cell(5, 6), Cell(4, 6), Cell(3, 6), Cell(2, 6), Cell(1, 6), Cell(0, 6),
        Cell(0, 7), Cell(0, 8),
        Cell(1, 8), Cell(2, 8), Cell(3, 8), Cell(4, 8), Cell(5, 8),
        Cell(6, 9), Cell(6, 10), Cell(6, 11), Cell(6, 12), Cell(6, 13), Cell(6, 14),
        Cell(7, 14), Cell(8, 14),
        Cell(8, 13), Cell(8, 12), Cell(8, 11), Cell(8, 10), Cell(8, 9),
        Cell(9, 8), Cell(10, 8), Cell(11, 8), Cell(12, 8), Cell(13, 8), Cell(14, 8),
        Cell(14, 7), Cell(14, 6),
        Cell(13, 6), Cell(12, 6), Cell(11, 6), Cell(10, 6), Cell(9, 6),
        Cell(8, 5), Cell(8, 4), Cell(8, 3), Cell(8, 2), Cell(8, 1), Cell(8, 0),
        Cell(7, 0), Cell(6, 0)
    )

    private val homePath = mapOf(
        PlayerColor.RED to listOf(Cell(7, 1), Cell(7, 2), Cell(7, 3), Cell(7, 4), Cell(7, 5), Cell(7, 6)),
        PlayerColor.GREEN to listOf(Cell(1, 7), Cell(2, 7), Cell(3, 7), Cell(4, 7), Cell(5, 7), Cell(6, 7)),
        PlayerColor.YELLOW to listOf(Cell(7, 13), Cell(7, 12), Cell(7, 11), Cell(7, 10), Cell(7, 9), Cell(7, 8)),
        PlayerColor.BLUE to listOf(Cell(13, 7), Cell(12, 7), Cell(11, 7), Cell(10, 7), Cell(9, 7), Cell(8, 7))
    )

    private val startIndex = mapOf(
        PlayerColor.RED to 0, PlayerColor.GREEN to 13,
        PlayerColor.YELLOW to 26, PlayerColor.BLUE to 39
    )

    fun getFullPath(color: PlayerColor): List<Cell> {
        val start = startIndex[color]!!
        val path = mutableListOf<Cell>()
        var idx = start
        repeat(51) { // 51 steps on main path before home
            path.add(mainPath[idx])
            idx = (idx + 1) % mainPath.size
        }
        path.addAll(homePath[color]!!) // 6 home steps
        return path // Total 57 steps: 0-50 main, 51-56 home
    }

    val basePositions = mapOf(
        PlayerColor.RED to listOf(Cell(2, 2), Cell(2, 4), Cell(4, 2), Cell(4, 4)),
        PlayerColor.GREEN to listOf(Cell(2, 11), Cell(2, 13), Cell(4, 11), Cell(4, 13)),
        PlayerColor.BLUE to listOf(Cell(11, 2), Cell(11, 4), Cell(13, 2), Cell(13, 4)),
        PlayerColor.YELLOW to listOf(Cell(11, 11), Cell(11, 13), Cell(13, 11), Cell(13, 13))
    )
}

@Preview
@Composable
fun LudoGame() {
    var tokens by remember {
        mutableStateOf(
            PlayerColor.values().flatMap { color ->
                List(4) { i -> LudoToken(id = color.ordinal * 4 + i, color = color, position = -1) }
            }
        )
    }

    val paths = remember { PlayerColor.values().associateWith { LudoPath.getFullPath(it) } }

    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFF1A237E)),
        contentAlignment = Alignment.Center
    ) {
        LudoBoard(
            tokens = tokens,
            paths = paths,
            onTokenClick = { clicked ->
                tokens = tokens.map { token ->
                    if (token.id == clicked.id) {
                        val newPos = if (token.position == -1) 0 else (token.position + 1).coerceAtMost(57)
                        token.copy(position = newPos)
                    } else token
                }
            }
        )
    }
}

@Composable
fun LudoBoard(
    tokens: List<LudoToken>,
    paths: Map<PlayerColor, List<Cell>>,
    onTokenClick: (LudoToken) -> Unit
) {
    val cellSize = 22.dp
    val boardSize = 15

    Box(Modifier.size(cellSize * boardSize)) {
        Canvas(Modifier.fillMaxSize()) {
            val px = size.width / boardSize

            fun cell(row: Int, col: Int, color: Color) {
                drawRect(color, Offset(col * px, row * px), Size(px, px))
                drawRect(Color.Black.copy(alpha = 0.3f), Offset(col * px, row * px), Size(px, px), style = Stroke(0.5f))
            }

            fun drawCell(row: Int, col: Int, color: Color) {
                drawRect(color, Offset(col * px, row * px), Size(px, px))
                drawRect(Color.Black.copy(alpha = 0.3f), Offset(col * px, row * px), Size(px, px), style = Stroke(0.5f))
            }

            fun star(row: Int, col: Int, color: Color) {
                val cx = (col + 0.5f) * px
                val cy = (row + 0.5f) * px
                val r = px * 0.25f
                val path = Path()
                for (i in 0 until 10) {
                    val radius = if (i % 2 == 0) r else r * 0.4f
                    val angle = Math.PI * i / 5 - Math.PI / 2
                    val x = cx + radius * cos(angle).toFloat()
                    val y = cy + radius * sin(angle).toFloat()
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                path.close()
                drawPath(path, color)
            }

            // 1. White base
            for (r in 0 until boardSize) for (c in 0 until boardSize) cell(r, c, Color.White)

            // 2. Home areas
            for (r in 0..5) for (c in 0..5) cell(r, c, LudoColors.map[PlayerColor.RED]!!)
            for (r in 0..5) for (c in 9..14) cell(r, c, LudoColors.map[PlayerColor.GREEN]!!)
            for (r in 9..14) for (c in 0..5) cell(r, c, LudoColors.map[PlayerColor.BLUE]!!)
            for (r in 9..14) for (c in 9..14) cell(r, c, LudoColors.map[PlayerColor.YELLOW]!!)

            // 3. White centers + circles
            fun homeArea(sr: Int, sc: Int, color: PlayerColor) {
                for (r in sr + 1..sr + 4) for (c in sc + 1..sc + 4) cell(r, c, Color.White)
                listOf(Offset(2.5f, 2.5f), Offset(3.5f, 2.5f), Offset(2.5f, 3.5f), Offset(3.5f, 3.5f)).forEach { pos ->
                    drawCircle(
                        color = LudoColors.map[color]!!,
                        radius = px * 0.35f,
                        center = Offset((sc + pos.x) * px, (sr + pos.y) * px)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = px * 0.15f,
                        center = Offset((sc + pos.x) * px, (sr + pos.y) * px)
                    )
                }
            }
            homeArea(0, 0, PlayerColor.RED)
            homeArea(0, 9, PlayerColor.GREEN)
            homeArea(9, 0, PlayerColor.BLUE)
            homeArea(9, 9, PlayerColor.YELLOW)

            // 4. Colored paths
            for (c in 1..5) cell(7, c, LudoColors.map[PlayerColor.RED]!!)
            drawCell(6, 1, LudoColors.map[PlayerColor.RED]!!)
            for (r in 1..5) cell(r, 7, LudoColors.map[PlayerColor.GREEN]!!)
            drawCell(1, 8, LudoColors.map[PlayerColor.GREEN]!!)
            for (c in 9..13) cell(7, c, LudoColors.map[PlayerColor.YELLOW]!!)
            drawCell(8, 13, LudoColors.map[PlayerColor.YELLOW]!!)
            for (r in 9..13) cell(r, 7, LudoColors.map[PlayerColor.BLUE]!!)
            drawCell(13, 6, LudoColors.map[PlayerColor.BLUE]!!)

            // 5. Stars
            star(2, 6, LudoColors.map[PlayerColor.GREEN]!!)
            star(6, 2, LudoColors.map[PlayerColor.RED]!!)
            star(8, 12, LudoColors.map[PlayerColor.YELLOW]!!)
            star(12, 8, LudoColors.map[PlayerColor.BLUE]!!)

            // 6. Center triangles
            val c = Offset(7.5f * px, 7.5f * px)
            val p1 = Offset(7 * px, 7 * px); val p2 = Offset(8 * px, 7 * px)
            val p3 = Offset(8 * px, 8 * px); val p4 = Offset(7 * px, 8 * px)
            drawPath(Path().apply { moveTo(c.x, c.y); lineTo(p1.x, p1.y); lineTo(p4.x, p4.y); close() }, LudoColors.map[PlayerColor.RED]!!)
            drawPath(Path().apply { moveTo(c.x, c.y); lineTo(p1.x, p1.y); lineTo(p2.x, p2.y); close() }, LudoColors.map[PlayerColor.GREEN]!!)
            drawPath(Path().apply { moveTo(c.x, c.y); lineTo(p2.x, p2.y); lineTo(p3.x, p3.y); close() }, LudoColors.map[PlayerColor.YELLOW]!!)
            drawPath(Path().apply { moveTo(c.x, c.y); lineTo(p3.x, p3.y); lineTo(p4.x, p4.y); close() }, LudoColors.map[PlayerColor.BLUE]!!)
        }

        // Tokens
        tokens.forEach { token ->
            val cell = when {
                token.position == -1 -> LudoPath.basePositions[token.color]!![token.id % 4]
                token.position < 57 -> paths[token.color]!![token.position]
                else -> null
            }
            cell?.let {
                PinToken(
                    color = LudoColors.map[token.color]!!,
                    modifier = Modifier
                        .size(cellSize * 0.85f)
                        .offset(
                            x = cellSize * it.col + cellSize * 0.075f,
                            y = cellSize * it.row + cellSize * 0.075f
                        )
                        .clickable { onTokenClick(token) }
                )
            }
        }
    }
}

@Composable
fun PinToken(modifier: Modifier = Modifier, color: Color) {
    Canvas(modifier) {
        // Shadow
        drawOval(Color.Black.copy(alpha = 0.3f), Offset(size.width * 0.15f, size.height * 0.8f), Size(size.width * 0.7f, size.height * 0.2f))

        val path = Path().apply {
            val w = size.width; val h = size.height
            addOval(Rect(w * 0.1f, 0f, w * 0.9f, h * 0.7f))
            moveTo(w * 0.5f, h); lineTo(w * 0.3f, h * 0.6f); lineTo(w * 0.7f, h * 0.6f); close()
        }
        drawPath(path, Color.White, style = Stroke(3.dp.toPx()))
        drawPath(path, color)
        drawCircle(color, size.width * 0.18f, Offset(size.width * 0.5f, size.height * 0.35f))
        drawCircle(Color.White, size.width * 0.18f, Offset(size.width * 0.5f, size.height * 0.35f), style = Stroke(2.dp.toPx()))
    }
}