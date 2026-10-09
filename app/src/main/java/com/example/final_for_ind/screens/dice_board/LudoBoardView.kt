package com.example.final_for_ind.screens.dice_board

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import org.json.JSONArray
import kotlin.math.min
import com.example.final_for_ind.R

class LudoBoardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // ==========================================================
    // GAME CONFIG
    // ==========================================================

    var isTwoPlayerMode = false

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.BLACK
    }

    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.parseColor("#FFD700")
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

    // Authoritative tracking array
    val tokensList = ArrayList<LudoToken>()
    var onTokenClickListener: ((LudoToken) -> Unit)? = null

    // Bitmaps Storage Map
    private val tokenBitmaps = HashMap<PlayerColor, Bitmap?>()
    private var dynamicCellSize = 0f

    // ==========================================================
    // PUBLIC ANIMATION STATE
    // ==========================================================

    var isBoardAnimating: Boolean = false
        private set

    // Keys of tokens that are valid to click (set from outside)
    var validTokenKeys: Set<String> = emptySet()
        set(value) {
            field = value
            invalidate()
        }

    // Callbacks
    var onTokenHop: (() -> Unit)? = null
    var onAnimationComplete: (() -> Unit)? = null
    var onTokenKicked: (() -> Unit)? = null

    // ==========================================================
    // ANIMATION STATE
    // ==========================================================

    // Current animated visual position for each token
    private val animatedPositions = HashMap<String, PointF>()

    // Scale bounce during hop
    private val animatedScales = HashMap<String, Float>()

    // Alpha for kick fade
    private val animatedAlphas = HashMap<String, Float>()

    // Running animators
    private val runningAnimators = HashMap<String, ValueAnimator>()

    // Pending state received while animating
    private var pendingState: JSONArray? = null

    init {
        buildMasterTracks()
        loadTokenAssets()
    }

    private fun tokenKey(token: LudoToken): String =
        "${token.color.name}_${token.id}"

    // ==========================================================
    // SERVER STATE SYNCHRONIZER
    // ==========================================================

    fun updateBoardStateFromServer(tokensJsonArray: JSONArray) {

        // If animating, queue the update
        if (isBoardAnimating) {
            pendingState = tokensJsonArray
            return
        }

        applyStateFromServer(tokensJsonArray)
    }

    private fun applyStateFromServer(tokensJsonArray: JSONArray) {

        val incomingTokens = ArrayList<LudoToken>()

        for (i in 0 until tokensJsonArray.length()) {
            val jsonToken = tokensJsonArray.getJSONObject(i)
            val id = jsonToken.getInt("id")
            val colorStr = jsonToken.getString("color")
            val position = jsonToken.getInt("position")

            val parsedColor = PlayerColor.valueOf(colorStr)

            val token = LudoToken(id, parsedColor).apply {
                this.position = position
            }
            incomingTokens.add(token)
        }

        // Snapshot old positions
        val oldTokenMap = HashMap<String, Int>()
        for (t in tokensList) {
            oldTokenMap[tokenKey(t)] = t.position
        }

        // Replace list
        tokensList.clear()
        tokensList.addAll(incomingTokens)

        // Build list of animations to run
        val animationsToStart = ArrayList<Pair<LudoToken, String>>()

        for (token in tokensList) {

            val key = tokenKey(token)
            val oldPos = oldTokenMap[key] ?: token.position

            // First time seeing this token — snap
            if (!animatedPositions.containsKey(key)) {
                animatedPositions[key] =
                    centerForPosition(token, token.position, dynamicCellSize)
                animatedScales[key] = 1f
                animatedAlphas[key] = 1f
                continue
            }

            // No change — skip
            if (oldPos == token.position) continue

            // Kick
            if (token.position == -1 && oldPos >= 0) {
                animationsToStart.add(Pair(token, "KICK"))
                continue
            }

            // Forward move
            if (token.position > oldPos && oldPos >= 0) {
                animationsToStart.add(Pair(token, "MOVE"))
                continue
            }

            // Backward / snap
            animatedPositions[key] =
                centerForPosition(token, token.position, dynamicCellSize)
        }

        if (animationsToStart.isEmpty()) {
            invalidate()
            onAnimationComplete?.invoke()
            return
        }

        // Mark animating
        isBoardAnimating = true

        var completed = 0
        val total = animationsToStart.size

        // Wrap each animation with a completion listener
        for ((token, type) in animationsToStart) {

            val key = tokenKey(token)

            // Chain: after this animation finishes, check if all done
            val originalOnEnd = Runnable {
                completed++
                if (completed >= total) {
                    onAllAnimationsComplete()
                }
            }

            when (type) {
                "MOVE" -> animateForward(
                    token,
                    oldTokenMap[key] ?: token.position,
                    token.position,
                    onComplete = originalOnEnd
                )
                "KICK" -> animateKick(
                    token,
                    oldTokenMap[key] ?: token.position,
                    onComplete = originalOnEnd
                )
            }
        }
    }

    private fun onAllAnimationsComplete() {

        isBoardAnimating = false

        // Process any pending state that arrived while we were animating
        val pending = pendingState
        pendingState = null

        if (pending != null) {
            // Small delay so animations visually settle
            postDelayed({
                applyStateFromServer(pending)
            }, 80)
        } else {
            onAnimationComplete?.invoke()
        }
    }

    // ==========================================================
    // FORWARD MOVE ANIMATION (cell-by-cell hop)
    // ==========================================================

    private fun animateForward(
        token: LudoToken,
        fromPos: Int,
        toPos: Int,
        onComplete: Runnable? = null
    ) {

        val key = tokenKey(token)

        runningAnimators[key]?.cancel()

        val cellSize = dynamicCellSize

        // Waypoints
        val waypoints = ArrayList<PointF>()
        for (p in fromPos..toPos) {
            waypoints.add(centerForPosition(token, p, cellSize))
        }

        if (waypoints.size < 2) {
            animatedPositions[key] =
                centerForPosition(token, toPos, cellSize)
            onComplete?.run()
            return
        }

        val totalDuration = 220L * (waypoints.size - 1) + 120L

        // Track hop indices for sound
        var lastSegPlayed = -1

        val animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = totalDuration
            interpolator = AccelerateDecelerateInterpolator()
        }

        animator.addUpdateListener { anim ->

            val fraction = anim.animatedFraction

            val totalSegments = waypoints.size - 1
            val seg = (fraction * totalSegments).toInt()
                .coerceAtMost(totalSegments - 1)

            // Play hop sound whenever segment changes
            if (seg != lastSegPlayed) {
                lastSegPlayed = seg
                onTokenHop?.invoke()
            }

            val segFraction = (fraction * totalSegments) - seg
            val segFractionClamped = segFraction.coerceIn(0f, 1f)

            val start = waypoints[seg]
            val end = waypoints[(seg + 1).coerceAtMost(waypoints.size - 1)]

            val x = start.x + (end.x - start.x) * segFractionClamped
            val y = start.y + (end.y - start.y) * segFractionClamped

            // Vertical hop
            val hopHeight = cellSize * 0.4f
            val hop = kotlin.math.sin(
                (segFractionClamped * Math.PI).toFloat()
            ) * hopHeight

            animatedPositions[key] = PointF(x, y - hop)

            // Bounce scale
            val scale = 1f + 0.18f * kotlin.math.sin(
                (segFractionClamped * Math.PI).toFloat()
            ).toFloat()
            animatedScales[key] = scale

            animatedAlphas[key] = 1f

            invalidate()
        }

        animator.addListener(object : AnimatorListenerAdapter() {

            private var cancelled = false

            override fun onAnimationCancel(animation: Animator) {
                cancelled = true
            }

            override fun onAnimationEnd(animation: Animator) {
                animatedPositions[key] =
                    centerForPosition(token, toPos, dynamicCellSize)
                animatedScales[key] = 1f
                invalidate()

                if (!cancelled) {
                    onComplete?.run()
                }
            }
        })

        animator.start()
        runningAnimators[key] = animator
    }

    // ==========================================================
    // KICK ANIMATION (fly back to base with arc)
    // ==========================================================

    private fun animateKick(
        token: LudoToken,
        fromPos: Int,
        onComplete: Runnable? = null
    ) {

        val key = tokenKey(token)

        runningAnimators[key]?.cancel()

        val cellSize = dynamicCellSize

        val start = centerForPosition(token, fromPos, cellSize)
        val end = baseSlotCenter(token, cellSize)

        val distance = kotlin.math.hypot(
            (end.x - start.x).toDouble(),
            (end.y - start.y).toDouble()
        ).toFloat()

        val peakHeight = (distance * 0.5f).coerceAtLeast(cellSize * 2.5f)

        // Notify kick
        onTokenKicked?.invoke()

        val animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 800L
            interpolator = DecelerateInterpolator()
        }

        animator.addUpdateListener { anim ->

            val f = anim.animatedFraction

            val x = start.x + (end.x - start.x) * f
            val y = start.y + (end.y - start.y) * f

            // Arc
            val hop = kotlin.math.sin((f * Math.PI).toFloat()) * peakHeight

            animatedPositions[key] = PointF(x, y - hop)

            // Shrink then grow
            val scale = if (f < 0.5f) {
                1f - f * 0.7f
            } else {
                0.65f + (f - 0.5f) * 0.7f
            }
            animatedScales[key] = scale

            // Fade during flight
            animatedAlphas[key] = 0.4f + 0.6f * kotlin.math.abs(1f - 2f * f)

            invalidate()
        }

        animator.addListener(object : AnimatorListenerAdapter() {

            private var cancelled = false

            override fun onAnimationCancel(animation: Animator) {
                cancelled = true
            }

            override fun onAnimationEnd(animation: Animator) {
                animatedPositions[key] = end
                animatedScales[key] = 1f
                animatedAlphas[key] = 1f
                invalidate()

                if (!cancelled) {
                    onComplete?.run()
                }
            }
        })

        animator.start()
        runningAnimators[key] = animator
    }

    // ==========================================================
    // HELPERS
    // ==========================================================

    private fun centerForPosition(
        token: LudoToken,
        position: Int,
        cellSize: Float
    ): PointF {

        if (position == -1) {
            return baseSlotCenter(token, cellSize)
        }

        val coord = playerPaths[token.color]
            ?.get(position.coerceAtMost(56))
            ?: (7 to 7)

        return PointF(
            (coord.second * cellSize) + (cellSize / 2f),
            (coord.first * cellSize) + (cellSize / 2f)
        )
    }

    private fun baseSlotCenter(token: LudoToken, cellSize: Float): PointF {

        val yardLeft = when (token.color) {
            PlayerColor.BLUE, PlayerColor.YELLOW -> 0f
            else -> cellSize * 9
        }

        val yardTop = when (token.color) {
            PlayerColor.BLUE, PlayerColor.RED -> 0f
            else -> cellSize * 9
        }

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
        return PointF(
            pos.first + pSize / 2f,
            pos.second + pSize / 2f
        )
    }

    private fun loadTokenAssets() {
        tokenBitmaps[PlayerColor.BLUE] = BitmapFactory.decodeResource(
            resources, R.drawable.blue_token
        )
        tokenBitmaps[PlayerColor.RED] = BitmapFactory.decodeResource(
            resources, R.drawable.red_token
        )
        tokenBitmaps[PlayerColor.GREEN] = BitmapFactory.decodeResource(
            resources, R.drawable.green_token
        )
        tokenBitmaps[PlayerColor.YELLOW] = BitmapFactory.decodeResource(
            resources, R.drawable.token_yellow
        )
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

        playerPaths[PlayerColor.BLUE] = generatePathOffsets(
            startIndex = 0, runwayRow = 7, runwayColRange = 1..5, goalCoord = 7 to 6
        )
        playerPaths[PlayerColor.RED] = generatePathOffsets(
            startIndex = 13, runwayRowRange = 1..5, runwayCol = 7, goalCoord = 6 to 7
        )
        playerPaths[PlayerColor.GREEN] = generatePathOffsets(
            startIndex = 26, runwayRow = 7, runwayColRange = 13 downTo 9, goalCoord = 7 to 8
        )
        playerPaths[PlayerColor.YELLOW] = generatePathOffsets(
            startIndex = 39, runwayRowRange = 13 downTo 9, runwayCol = 7, goalCoord = 8 to 7
        )
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

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val size = min(
            MeasureSpec.getSize(widthMeasureSpec),
            MeasureSpec.getSize(heightMeasureSpec)
        )
        setMeasuredDimension(size, size)
    }

    // ==========================================================
    // ON DRAW
    // ==========================================================

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val boardSize = width.toFloat()
        dynamicCellSize = boardSize / 15f
        strokePaint.strokeWidth = resources.displayMetrics.density * 1.5f
        glowPaint.strokeWidth = resources.displayMetrics.density * 3f

        // Board
        paint.color = colorLightGray
        canvas.drawRect(0f, 0f, boardSize, boardSize, paint)

        drawHomeYard(canvas, 0f, 0f, dynamicCellSize * 6, colorBlue)
        drawHomeYard(canvas, dynamicCellSize * 9, 0f, boardSize, colorRed)
        drawHomeYard(canvas, 0f, dynamicCellSize * 9, dynamicCellSize * 6, colorYellow)
        drawHomeYard(canvas, dynamicCellSize * 9, dynamicCellSize * 9, boardSize, colorGreen)

        drawGridTracks(canvas, dynamicCellSize)
        colorSpecialCells(canvas, dynamicCellSize)
        drawCenterTriangles(canvas, boardSize, dynamicCellSize)

        // ==================================================
        // BUILD CELL OCCUPANCY MAP (fix overlap)
        // ==================================================

        val cellOccupants = HashMap<String, MutableList<LudoToken>>()

        for (token in tokensList) {

            if (token.position < 0) continue

            val coord = playerPaths[token.color]
                ?.get(token.position.coerceAtMost(56))
                ?: continue

            val cellKey = "${coord.first}_${coord.second}"

            cellOccupants.getOrPut(cellKey) { mutableListOf() }.add(token)
        }

        // ==================================================
        // DRAW TOKENS
        // ==================================================

        // Draw order: non-selected first, then selected (so selected on top)
        val sortedTokens = tokensList.sortedBy { token ->
            if (validTokenKeys.contains(tokenKey(token))) 1 else 0
        }

        for (token in sortedTokens) {

            val key = tokenKey(token)

            val baseCenter = animatedPositions[key]
                ?: centerForPosition(token, token.position, dynamicCellSize)

            // Apply stacking offset if multiple tokens share this cell
            val offset = computeStackOffset(
                token,
                cellOccupants
            )

            val center = PointF(
                baseCenter.x + offset.x,
                baseCenter.y + offset.y
            )

            val stackScale = computeStackScale(
                token,
                cellOccupants
            )

            val scale = (animatedScales[key] ?: 1f) * stackScale
            val alpha = animatedAlphas[key] ?: 1f

            val baseRadius = dynamicCellSize * 0.42f
            val radius = baseRadius * scale

            val destRect = RectF(
                center.x - radius,
                center.y - radius,
                center.x + radius,
                center.y + radius
            )

            // Glow ring if valid token
            if (validTokenKeys.contains(key) &&
                token.position >= -1 &&
                !isBoardAnimating
            ) {

                // Pulsing glow
                val pulse = 1f + 0.15f * kotlin.math.sin(
                    (System.currentTimeMillis() % 1000) / 1000f * 2f * Math.PI.toFloat()
                )

                glowPaint.alpha = (200 * pulse).toInt().coerceIn(0, 255)

                canvas.drawCircle(
                    center.x,
                    center.y,
                    radius * 1.25f * pulse,
                    glowPaint
                )

                // Invalidate for animation
                postInvalidateOnAnimation()
            }

            // Draw token
            val bmp = tokenBitmaps[token.color]

            if (bmp != null) {

                val overlayPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.WHITE
                    this.alpha = (255 * alpha).toInt()
                    style = Paint.Style.FILL
                }

                canvas.drawCircle(
                    center.x,
                    center.y,
                    radius * 1.05f,
                    overlayPaint
                )

                val bmpPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    this.alpha = (255 * alpha).toInt()
                }

                canvas.drawBitmap(bmp, null, destRect, bmpPaint)

            } else {

                paint.color = when (token.color) {
                    PlayerColor.BLUE -> Color.BLUE
                    PlayerColor.RED -> Color.RED
                    PlayerColor.GREEN -> Color.GREEN
                    else -> Color.YELLOW
                }
                paint.alpha = (255 * alpha).toInt()

                canvas.drawCircle(center.x, center.y, radius, paint)

                val bp = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.STROKE
                    color = Color.WHITE
                    strokeWidth = 5f
                    this.alpha = (255 * alpha).toInt()
                }
                canvas.drawCircle(center.x, center.y, radius, bp)

                paint.alpha = 255
            }
        }
    }

    // ==========================================================
    // STACKING HELPERS
    // ==========================================================

    private fun computeStackOffset(
        token: LudoToken,
        cellOccupants: HashMap<String, MutableList<LudoToken>>
    ): PointF {

        if (token.position < 0) return PointF(0f, 0f)

        val coord = playerPaths[token.color]
            ?.get(token.position.coerceAtMost(56))
            ?: return PointF(0f, 0f)

        val cellKey = "${coord.first}_${coord.second}"

        val occupants = cellOccupants[cellKey] ?: return PointF(0f, 0f)

        if (occupants.size <= 1) return PointF(0f, 0f)

        val index = occupants.indexOf(token)
            .coerceAtLeast(0)

        val cellSize = dynamicCellSize

        return when (occupants.size) {
            2 -> {
                // Side by side (horizontal)
                val dx = if (index == 0) -cellSize * 0.18f else cellSize * 0.18f
                PointF(dx, 0f)
            }
            3 -> {
                // Triangle
                when (index) {
                    0 -> PointF(-cellSize * 0.18f, -cellSize * 0.10f)
                    1 -> PointF(cellSize * 0.18f, -cellSize * 0.10f)
                    else -> PointF(0f, cellSize * 0.18f)
                }
            }
            else -> {
                // 4 corners
                when (index % 4) {
                    0 -> PointF(-cellSize * 0.18f, -cellSize * 0.18f)
                    1 -> PointF(cellSize * 0.18f, -cellSize * 0.18f)
                    2 -> PointF(-cellSize * 0.18f, cellSize * 0.18f)
                    else -> PointF(cellSize * 0.18f, cellSize * 0.18f)
                }
            }
        }
    }

    private fun computeStackScale(
        token: LudoToken,
        cellOccupants: HashMap<String, MutableList<LudoToken>>
    ): Float {

        if (token.position < 0) return 1f

        val coord = playerPaths[token.color]
            ?.get(token.position.coerceAtMost(56))
            ?: return 1f

        val cellKey = "${coord.first}_${coord.second}"

        val occupants = cellOccupants[cellKey] ?: return 1f

        return when {
            occupants.size <= 1 -> 1f
            occupants.size == 2 -> 0.78f
            occupants.size == 3 -> 0.68f
            else -> 0.6f
        }
    }

    // ==========================================================
    // TOUCH HANDLING
    // ==========================================================

    override fun onTouchEvent(event: MotionEvent): Boolean {

        if (event.action != MotionEvent.ACTION_DOWN) {
            return super.onTouchEvent(event)
        }

        // Block clicks during animation
        if (isBoardAnimating) {
            return true
        }

        val x = event.x
        val y = event.y

        // Build occupancy map (same as in onDraw)
        val cellOccupants = HashMap<String, MutableList<LudoToken>>()
        for (token in tokensList) {
            if (token.position < 0) continue
            val coord = playerPaths[token.color]
                ?.get(token.position.coerceAtMost(56))
                ?: continue
            val cellKey = "${coord.first}_${coord.second}"
            cellOccupants.getOrPut(cellKey) { mutableListOf() }.add(token)
        }

        // Check from top-most to bottom (highest token first = matches visual order)
        val sortedTokens = tokensList.sortedByDescending { token ->
            if (validTokenKeys.contains(tokenKey(token))) 1 else 0
        }

        for (token in sortedTokens) {

            val key = tokenKey(token)

            val baseCenter = animatedPositions[key]
                ?: centerForPosition(token, token.position, dynamicCellSize)

            val offset = computeStackOffset(token, cellOccupants)
            val stackScale = computeStackScale(token, cellOccupants)

            val center = PointF(
                baseCenter.x + offset.x,
                baseCenter.y + offset.y
            )

            val clickRadius = dynamicCellSize * 0.55f * stackScale

            val dx = x - center.x
            val dy = y - center.y

            if ((dx * dx + dy * dy) <= (clickRadius * clickRadius)) {

                // Only fire if it's a valid token
                if (validTokenKeys.contains(key)) {
                    onTokenClickListener?.invoke(token)
                    return true
                }
            }
        }

        return super.onTouchEvent(event)
    }

    // ==========================================================
    // BOARD DRAWING
    // ==========================================================

    private fun drawGridTracks(canvas: Canvas, cellSize: Float) {
        for (row in 0..14) {
            for (col in 0..14) {
                if ((row in 6..8) || (col in 6..8)) {
                    if (row in 6..8 && col in 6..8) continue
                    canvas.drawRect(
                        col * cellSize,
                        row * cellSize,
                        (col + 1) * cellSize,
                        (row + 1) * cellSize,
                        strokePaint
                    )
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
            Pair(left + pMarg, top + pMarg),
            Pair(right - pMarg - pSize, top + pMarg),
            Pair(left + pMarg, top + (right - left) - pMarg - pSize),
            Pair(right - pMarg - pSize, top + (right - left) - pMarg - pSize)
        )
        paint.color = color
        for (p in pockets) {
            canvas.drawRect(p.first, p.second, p.first + pSize, p.second + pSize, paint)
            canvas.drawRect(p.first, p.second, p.first + pSize, p.second + pSize, strokePaint)
        }
    }

    private fun drawCenterTriangles(canvas: Canvas, boardSize: Float, cellSize: Float) {
        val start = cellSize * 6
        val end = cellSize * 9
        val mid = boardSize / 2f
        val path = Path()
        val triangles = listOf(
            Triple(colorBlue, Pair(start, start), Pair(start, end)),
            Triple(colorRed, Pair(start, start), Pair(end, start)),
            Triple(colorGreen, Pair(end, start), Pair(end, end)),
            Triple(colorYellow, Pair(start, end), Pair(end, end))
        )
        for (t in triangles) {
            path.reset()
            path.moveTo(t.second.first, t.second.second)
            path.lineTo(mid, mid)
            path.lineTo(t.third.first, t.third.second)
            path.close()
            paint.color = t.first
            canvas.drawPath(path, paint)
            canvas.drawPath(path, strokePaint)
        }
    }
}