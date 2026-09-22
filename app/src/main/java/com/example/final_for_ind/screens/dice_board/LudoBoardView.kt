package com.example.final_for_ind.screens.dice_board

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import org.json.JSONArray
import kotlin.math.min
import com.example.final_for_ind.R

class LudoBoardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // --- GAME CONTROL CONFIGURATIONS ---
    var isTwoPlayerMode = false

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.BLACK
    }

    private val colorBlue = Color.parseColor(PlayerColor.BLUE.hexColor)
    private val colorRed = Color.parseColor(PlayerColor.RED.hexColor)
    private val colorGreen = Color.parseColor(PlayerColor.GREEN.hexColor)
    private val colorYellow = Color.parseColor(PlayerColor.YELLOW.hexColor)
    private val colorWhite = Color.WHITE
    private val colorLightGray = Color.parseColor("#F0F4F8")
    private val colorStarGray = Color.parseColor("#A0AAB5")

    // Master Tracks Matrix Maps
    private val baseGlobalTrack = ArrayList<Pair<Int, Int>>()
    val playerPaths = HashMap<PlayerColor, List<Pair<Int, Int>>>()

    // Authoritative tracking array synchronized completely from backend server
    val tokensList = ArrayList<LudoToken>()
    var onTokenClickListener: ((LudoToken) -> Unit)? = null

    // Bitmaps Storage Map
    private val tokenBitmaps = HashMap<PlayerColor, Bitmap?>()
    private var dynamicCellSize = 0f

    init {
        buildMasterTracks()
        loadTokenAssets()
    }

    // --- SERVER STATE SYNCHRONIZER ENGINE ---
    // This is called by MainActivity2 every time the backend returns a fresh Game Snapshot
    fun updateBoardStateFromServer(tokensJsonArray: JSONArray) {
        tokensList.clear()

        for (i in 0 until tokensJsonArray.length()) {
            val jsonToken = tokensJsonArray.getJSONObject(i)
            val id = jsonToken.getInt("id")
            val colorStr = jsonToken.getString("color")
            val position = jsonToken.getInt("position")

            // Map string color safely to your local PlayerColor enum class
            val parsedColor = PlayerColor.valueOf(colorStr)

            val token = LudoToken(id, parsedColor).apply {
                this.position = position
            }
            tokensList.add(token)
        }

        // Redraw entire board canvas layout using new spatial token coordinates
        invalidate()
    }

    private fun loadTokenAssets() {
        tokenBitmaps[PlayerColor.BLUE] = BitmapFactory.decodeResource(resources,
            R.drawable.blue_token)
        tokenBitmaps[PlayerColor.RED] = BitmapFactory.decodeResource(resources,
            R.drawable.red_token)

        tokenBitmaps[PlayerColor.GREEN] = BitmapFactory.decodeResource(resources, R.drawable.green_token)
        tokenBitmaps[PlayerColor.YELLOW] = BitmapFactory.decodeResource(resources, R.drawable.token_yellow)
    }

    private fun buildMasterTracks() {
        val segments = listOf(
            listOf(6 to 1, 6 to 2, 6 to 3, 6 to 4, 6 to 5),
            listOf(5 to 6, 4 to 6, 3 to 6, 2 to 6, 1 to 6, 0 to 6, 0 to 7, 0 to 8),
            listOf(1 to 8, 2 to 8, 3 to 8, 4 to 8, 5 to 8),
            listOf(6 to 9, 6 to 10, 6 to 11, 6 to 12, 6 to 13, 6 to 14, 7 to 14, 8 to 14),
            listOf(8 to 13, 8 to 12, 8 to 11, 8 to 10, 8 to 9),
            listOf(9 to 8, 10 to 8, 11 to 8, 12 to 8, 13 to 8, 14 to 8, 14 to 7, 14 to 6),
            listOf(13 to 6, 12 to 6, 11 to 6, 10 to 6, 9 to 6),
            listOf(8 to 5, 8 to 4, 8 to 3, 8 to 2, 8 to 1, 8 to 0, 7 to 0, 6 to 0)
        )
        for (seg in segments) baseGlobalTrack.addAll(seg)

        playerPaths[PlayerColor.BLUE] = generatePathOffsets(startIndex = 0, runwayRow = 7, runwayColRange = 1..5, goalCoord = 7 to 6)
        playerPaths[PlayerColor.RED] = generatePathOffsets(startIndex = 13, runwayRowRange = 1..5, runwayCol = 7, goalCoord = 6 to 7)
        playerPaths[PlayerColor.GREEN] = generatePathOffsets(startIndex = 26, runwayRow = 7, runwayColRange = 13 downTo 9, goalCoord = 7 to 8)
        playerPaths[PlayerColor.YELLOW] = generatePathOffsets(startIndex = 39, runwayRowRange = 13 downTo 9, runwayCol = 7, goalCoord = 8 to 7)
    }

    private fun generatePathOffsets(
        startIndex: Int,
        runwayRow: Int? = null,
        runwayColRange: IntProgression? = null,
        runwayRowRange: IntProgression? = null,
        runwayCol: Int? = null,
        goalCoord: Pair<Int, Int>
    ): List<Pair<Int, Int>> {
        val path = ArrayList<Pair<Int, Int>>()
        for (i in 0..50) {
            path.add(baseGlobalTrack[(startIndex + i) % baseGlobalTrack.size])
        }
        if (runwayRow != null && runwayColRange != null) {
            for (c in runwayColRange) path.add(runwayRow to c)
        } else if (runwayCol != null && runwayRowRange != null) {
            for (r in runwayRowRange) path.add(r to runwayCol)
        }
        path.add(goalCoord)
        return path
    }

    private fun getAbsoluteTokenCenter(token: LudoToken, cellSize: Float): Pair<Float, Float> {
        if (token.position == -1) {
            val yardLeft = when (token.color) { PlayerColor.BLUE, PlayerColor.YELLOW -> 0f; else -> cellSize * 9 }
            val yardTop = when (token.color) { PlayerColor.BLUE, PlayerColor.RED -> 0f; else -> cellSize * 9 }
            val size = cellSize * 6
            val pSize = size * 0.2f
            val pMarg = size * 0.22f
            val pockets = listOf(
                Pair(yardLeft + pMarg, yardTop + pMarg),
                Pair(yardLeft + size - pMarg - pSize, yardTop + pMarg),
                Pair(yardLeft + pMarg, yardTop + size - pMarg - pSize),
                Pair(yardLeft + size - pMarg - pSize, yardTop + size - pMarg - pSize)
            )
            val pos = pockets[token.id]
            return Pair(pos.first + pSize / 2f, pos.second + pSize / 2f)
        }

        val coord = playerPaths[token.color]?.get(token.position.coerceAtMost(56)) ?: (7 to 7)
        return Pair((coord.second * cellSize) + (cellSize / 2f), (coord.first * cellSize) + (cellSize / 2f))
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val size = min(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec))
        setMeasuredDimension(size, size)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val boardSize = width.toFloat()
        dynamicCellSize = boardSize / 15f
        strokePaint.strokeWidth = resources.displayMetrics.density * 1.5f

        // Draw Board Background and Tracks
        paint.color = colorLightGray
        canvas.drawRect(0f, 0f, boardSize, boardSize, paint)
        drawHomeYard(canvas, 0f, 0f, dynamicCellSize * 6, colorBlue)
        drawHomeYard(canvas, dynamicCellSize * 9, 0f, boardSize, colorRed)
        drawHomeYard(canvas, 0f, dynamicCellSize * 9, dynamicCellSize * 6, colorYellow)
        drawHomeYard(canvas, dynamicCellSize * 9, dynamicCellSize * 9, boardSize, colorGreen)
        drawGridTracks(canvas, dynamicCellSize)
        colorSpecialCells(canvas, dynamicCellSize)
        drawCenterTriangles(canvas, boardSize, dynamicCellSize)

        // Draw Server Synchronized Active Tokens
        for (token in tokensList) {
            val center = getAbsoluteTokenCenter(token, dynamicCellSize)
            val radius = dynamicCellSize * 0.42f
            val destRect = RectF(center.first - radius, center.second - radius, center.first + radius, center.second + radius)

            val bmp = tokenBitmaps[token.color]
            if (bmp != null) {
                val overlayPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE; style = Paint.Style.FILL }
                canvas.drawCircle(center.first, center.second, radius * 1.05f, overlayPaint)
                canvas.drawBitmap(bmp, null, destRect, null)
            } else {
                paint.color = when(token.color) {
                    PlayerColor.BLUE -> Color.BLUE; PlayerColor.RED -> Color.RED
                    PlayerColor.GREEN -> Color.GREEN; else -> Color.YELLOW
                }
                canvas.drawCircle(center.first, center.second, radius, paint)
                val bp = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; color = Color.WHITE; strokeWidth = 5f }
                canvas.drawCircle(center.first, center.second, radius, bp)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val x = event.x
            val y = event.y

            for (token in tokensList) {
                val center = getAbsoluteTokenCenter(token, dynamicCellSize)
                val clickRadius = dynamicCellSize * 0.6f
                val dx = x - center.first
                val dy = y - center.second

                // If a token circle click is detected, notify MainActivity2 to call the server
                if ((dx * dx + dy * dy) <= (clickRadius * clickRadius)) {
                    onTokenClickListener?.invoke(token)
                    return true
                }
            }
        }
        return super.onTouchEvent(event)
    }

    private fun drawGridTracks(canvas: Canvas, cellSize: Float) {
        for (row in 0..14) {
            for (col in 0..14) {
                if ((row in 6..8) || (col in 6..8)) {
                    if (row in 6..8 && col in 6..8) continue
                    canvas.drawRect(col * cellSize, row * cellSize, (col + 1) * cellSize, (row + 1) * cellSize, strokePaint)
                }
            }
        }
    }

    private fun colorSpecialCells(canvas: Canvas, cellSize: Float) {
        fun fillCell(r: Int, c: Int, color: Int) {
            paint.color = color
            canvas.drawRect(c * cellSize, r * cellSize, (c + 1) * cellSize, (r + 1) * cellSize, paint)
            canvas.drawRect(c * cellSize, r * cellSize, (c + 1) * cellSize, (r + 1) * cellSize, strokePaint)
        }
        val safeCells = listOf(6 to 1, 2 to 6, 1 to 8, 6 to 12, 8 to 13, 12 to 8, 13 to 6, 8 to 2)
        for (cell in safeCells) fillCell(cell.first, cell.second, colorStarGray)
        fillCell(6, 1, colorBlue); for (c in 1..5) fillCell(7, c, colorBlue)
        fillCell(1, 8, colorRed); for (r in 1..5) fillCell(r, 7, colorRed)
        fillCell(8, 13, colorGreen); for (c in 9..13) fillCell(7, c, colorGreen)
        fillCell(13, 6, colorYellow); for (r in 9..13) fillCell(r, 7, colorYellow)
    }

    private fun drawHomeYard(canvas: Canvas, left: Float, top: Float, right: Float, color: Int) {
        paint.color = color
        canvas.drawRect(left, top, right, top + (right - left), paint)
        canvas.drawRect(left, top, right, top + (right - left), strokePaint)
        val margin = (right - left) * 0.15f
        paint.color = colorWhite
        canvas.drawRect(left + margin, top + margin, right - margin, top + (right - left) - margin, paint)
        val pSize = (right - left) * 0.2f
        val pMarg = (right - left) * 0.22f
        val pockets = listOf(
            Pair(left + pMarg, top + pMarg), Pair(right - pMarg - pSize, top + pMarg),
            Pair(left + pMarg, top + (right - left) - pMarg - pSize), Pair(right - pMarg - pSize, top + (right - left) - pMarg - pSize)
        )
        paint.color = color
        for (p in pockets) {
            canvas.drawRect(p.first, p.second, p.first + pSize, p.second + pSize, paint)
            canvas.drawRect(p.first, p.second, p.first + pSize, p.second + pSize, strokePaint)
        }
    }

    private fun drawCenterTriangles(canvas: Canvas, boardSize: Float, cellSize: Float) {
        val start = cellSize * 6; val end = cellSize * 9; val mid = boardSize / 2f
        val path = Path()
        val triangles = listOf(
            Triple(colorBlue, Pair(start, start), Pair(start, end)), Triple(colorRed, Pair(start, start), Pair(end, start)),
            Triple(colorGreen, Pair(end, start), Pair(end, end)), Triple(colorYellow, Pair(start, end), Pair(end, end))
        )
        for (t in triangles) {
            path.reset(); path.moveTo(t.second.first, t.second.second); path.lineTo(mid, mid); path.lineTo(t.third.first, t.third.second); path.close()
            paint.color = t.first; canvas.drawPath(path, paint); canvas.drawPath(path, strokePaint)
        }
    }
}