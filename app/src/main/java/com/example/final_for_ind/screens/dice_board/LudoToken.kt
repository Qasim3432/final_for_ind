package com.example.final_for_ind.screens.dice_board

enum class PlayerColor(val displayName: String, val hexColor: String) {
    BLUE("Blue", "#1A3A8B"),
    RED("Red", "#E51C23"),
    GREEN("Green", "#0F9D58"),
    YELLOW("Yellow", "#FFDE00")
}

data class LudoToken(
    val id: Int, // 0 to 3 for each player
    val color: PlayerColor,
    var position: Int = -1 // -1 = Yard, 0 = Entry, 51-55 = Runway, 56 = Goal
)